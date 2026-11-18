package com.fastscala.db

import com.fastscala.db.keyed.uuid.RowWithUUID
import com.fastscala.db.versioned.{StaleRowException, VersionedRow, VersionedTable}
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

import java.util.UUID
import java.util.concurrent.{Executors, TimeUnit}
import scala.util.chaining.scalaUtilChainingOps

class VersionedRowSpecTable(
                             var name: String = "hello",
                             var counter: Int = 0,
                           ) extends RowWithUUID[VersionedRowSpecTable] with VersionedRow[UUID, VersionedRowSpecTable] {
  override def table: VersionedRowSpecTable.type = VersionedRowSpecTable
}

object VersionedRowSpecTable extends TableWithUuidId[VersionedRowSpecTable] with VersionedTable[UUID, VersionedRowSpecTable] {
  override def createSampleRow(): VersionedRowSpecTable = new VersionedRowSpecTable()
}

class VersionedRowSpec extends AnyFlatSpec with DBConn {

  private def inDB(row: VersionedRowSpecTable): VersionedRowSpecTable = VersionedRowSpecTable.getForId(row.id)

  "Create table" should "succeed, with a version column" in {
    DB.localTx({ implicit session =>
      VersionedRowSpecTable.__dropAndCreateTableSQL.foreach(_.execute())
    })
    assert(VersionedRowSpecTable.fieldsList.map(_.getName).contains("version"))
  }

  "A versioned row" should "have version 0 until it's saved, and 1 once inserted" in {
    val row = new VersionedRowSpecTable()
    assert(row.version == 0)
    row.save()
    assert(row.version == 1)
    assert(inDB(row).version == 1)
    assert(VersionedRowSpecTable.versionOf(row.id).contains(1L))
  }

  it should "increment its version on each save and update" in {
    val row = new VersionedRowSpecTable().save()
    row.name = "a"
    row.save()
    assert(row.version == 2)
    row.name = "b"
    row.update()
    assert(row.version == 3)
    val read = inDB(row)
    assert(read.version == 3 && read.name == "b")
  }

  it should "fail to save a stale copy, writing nothing" in {
    val row = new VersionedRowSpecTable().save()
    val first = inDB(row)
    val second = inDB(row)

    first.name = "first"
    first.save()

    second.name = "second"
    val ex = intercept[StaleRowException](second.save())
    assert(ex.key == row.id && ex.expectedVersion == 1 && ex.actualVersion.contains(2L))
    assert(ex.tableName == VersionedRowSpecTable.tableName)
    assert(second.version == 1, "the stale copy keeps its version")
    assert(intercept[StaleRowException](second.update()).expectedVersion == 1)

    val read = inDB(row)
    assert(read.name == "first" && read.version == 2)
  }

  it should "save again once reloaded" in {
    val row = new VersionedRowSpecTable().save()
    val other = inDB(row)
    other.counter = 7
    other.save()

    row.name = "mine"
    intercept[StaleRowException](row.save())
    row.reload()
    assert(row.version == 2 && row.counter == 7 && row.name == "hello")
    row.name = "mine"
    row.save()
    val read = inDB(row)
    assert(read.version == 3 && read.counter == 7 && read.name == "mine")
  }

  it should "apply update(func) to the current row in the database" in {
    val row = new VersionedRowSpecTable().save()
    inDB(row).tap(_.counter = 5).save()
    row.update(_.name = "updated")
    assert(row.version == 3 && row.counter == 5 && row.name == "updated")
    assert(inDB(row).version == 3)
  }

  it should "roll back the transaction a stale save is in" in {
    val row = new VersionedRowSpecTable().save()
    val stale = inDB(row)
    row.save()
    val other = new VersionedRowSpecTable()
    intercept[StaleRowException] {
      DB.localTx({ session =>
        other.save(session)
        stale.save(session)
      })
    }
    assert(VersionedRowSpecTable.getForIdOpt(other.id).isEmpty)
  }

  it should "fail to delete a stale copy, and delete a current one" in {
    val row = new VersionedRowSpecTable().save()
    val stale = inDB(row)
    row.name = "changed"
    row.save()

    val ex = intercept[StaleRowException](stale.delete())
    assert(ex.expectedVersion == 1 && ex.actualVersion.contains(2L))
    assert(VersionedRowSpecTable.getForIdOpt(row.id).isDefined)

    row.delete()
    assert(VersionedRowSpecTable.getForIdOpt(row.id).isEmpty)
    row.delete() // already deleted: nothing to do
  }

  it should "fail to save a row that was deleted" in {
    val row = new VersionedRowSpecTable().save()
    val copy = inDB(row)
    row.delete()
    val ex = intercept[StaleRowException](copy.save())
    assert(ex.actualVersion.isEmpty)
    assert(VersionedRowSpecTable.getForIdOpt(row.id).isEmpty)
  }

  it should "duplicate as a new row at version 1, that is saved on its own" in {
    val row = new VersionedRowSpecTable().save()
    row.name = "x"
    row.save()
    val dup = row.duplicate()
    assert(dup.id != row.id && dup.name == "x" && dup.version == 1)
    assert(row.version == 2)
    dup.name = "y"
    dup.save()
    row.name = "z"
    row.save()
    assert(inDB(dup).name == "y" && inDB(row).name == "z")
  }

  it should "not support saveSQL()" in {
    intercept[UnsupportedOperationException](new VersionedRowSpecTable().saveSQL())
  }

  it should "lose no update when many threads change the same row (each retries when stale)" in {
    val row = new VersionedRowSpecTable().save()
    val threads = 8
    val incrementsPerThread = 25
    val pool = Executors.newFixedThreadPool(threads)
    val stale = new java.util.concurrent.atomic.AtomicInteger()
    try {
      val tasks = (1 to threads).map(_ => pool.submit(new Runnable {
        override def run(): Unit = for (_ <- 1 to incrementsPerThread) {
          var done = false
          while (!done) {
            val copy = inDB(row)
            copy.counter += 1
            try {
              copy.save()
              done = true
            } catch {
              case _: StaleRowException => stale.incrementAndGet()
            }
          }
        }
      }))
      tasks.foreach(_.get(60, TimeUnit.SECONDS))
    } finally pool.shutdown()
    val read = inDB(row)
    assert(read.counter == threads * incrementsPerThread)
    assert(read.version == 1 + threads * incrementsPerThread)
    info(s"${stale.get()} stale saves retried")
  }

  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      VersionedRowSpecTable.__dropTableSQL.foreach(_.execute())
    })
  }
}
