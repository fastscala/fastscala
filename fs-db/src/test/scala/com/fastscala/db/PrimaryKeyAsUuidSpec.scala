package com.fastscala.db

import com.fastscala.db.keyed.uuid.RowWithUUID
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

class PrimaryKeyBeingUuidSpecTable(
                                    var myColumn: String = "hello",
                                    var myInt: Int = 123,
                                  ) extends RowWithUUID[PrimaryKeyBeingUuidSpecTable] {
  override def table: TableWithUuidId[PrimaryKeyBeingUuidSpecTable] = PrimaryKeyBeingUuidSpecTable
}

object PrimaryKeyBeingUuidSpecTable extends TableWithUuidId[PrimaryKeyBeingUuidSpecTable] {
  override def createSampleRow(): PrimaryKeyBeingUuidSpecTable = new PrimaryKeyBeingUuidSpecTable()
}

class PrimaryKeyBeingUuidSpec extends AnyFlatSpec with PostgresDB {

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      PrimaryKeyBeingUuidSpecTable.__createTableSQL.foreach(_.execute())
    })
  }
  "Save row" should "succeed" in {
    DB.localTx({ implicit session =>
      val saved = new PrimaryKeyBeingUuidSpecTable().save()
      assert(saved.uuid.isDefined)
    })
  }
  "Read row" should "succeed" in {
    DB.localTx({ implicit session =>
      val example = new PrimaryKeyBeingUuidSpecTable()
      val single = PrimaryKeyBeingUuidSpecTable.selectAll().head

      assert(example.myColumn == single.myColumn)
    })
  }
  "Update row" should "succeed" in {
    DB.localTx({ implicit session =>
      val single = PrimaryKeyBeingUuidSpecTable.selectAll().head

      single.myColumn += "!"

      single.update()

      assert(PrimaryKeyBeingUuidSpecTable.selectAll().size == 1)

      val inDB = PrimaryKeyBeingUuidSpecTable.selectAll().head

      assert(single.uuid.isDefined)
      assert(single.uuid == inDB.uuid)
      assert(inDB.myColumn == single.myColumn)
    })
  }
  "Select by UUID" should "succeed" in {
    DB.localTx({ implicit session =>
      new PrimaryKeyBeingUuidSpecTable().save()
      new PrimaryKeyBeingUuidSpecTable().save()
      val uuids = PrimaryKeyBeingUuidSpecTable.selectAll().map(_.uuid.get)

      assert(PrimaryKeyBeingUuidSpecTable.getForIds(uuids *).size == 3)
    })
  }
  "Delete rows" should "succeed" in {
    DB.localTx({ implicit session =>
      PrimaryKeyBeingUuidSpecTable.__truncateSQL.execute()
    })
    DB.localTx({ implicit session =>
      assert(PrimaryKeyBeingUuidSpecTable.selectAll().isEmpty)
    })
  }
  "Reload" should "succeed" in {
    val ins = new PrimaryKeyBeingUuidSpecTable().save()

    assert(ins.myInt == 123)

    DB.localTx({ implicit session =>
      val nRowsUpdated = sql"""update ${PrimaryKeyBeingUuidSpecTable.tableNameSQLSyntaxQuoted} set my_int = 321 where uuid = ${ins.id};""".update()
      assert(nRowsUpdated == 1)
    })

    ins.reload()
    assert(ins.myInt == 321)

    ins.delete()
  }
  //  "Batch save row" should "succeed" in {
  //    DB.localTx({ implicit session =>
  //      TestEntity4.save(Seq(new TestEntity4()))
  //      assert(TestEntity4.selectAll().size == 1)
  //    })
  //  }
  //  "Read row written by batch save" should "succeed" in {
  //    DB.localTx({ implicit session =>
  //      val example = new TestEntity4()
  //      val single = TestEntity4.selectAll().head
  //
  //      assert(example.myInt == single.myInt)
  //      assert(example.myLong == single.myLong)
  //      assert(example.myDouble == single.myDouble)
  //      assert(example.myFloat == single.myFloat)
  //      assert(example.myShort == single.myShort)
  //      assert(example.myString == single.myString)
  //      assert(example.myBoolean == single.myBoolean)
  //      assert(example.myChar == single.myChar)
  //    })
  //  }
  //  "Batch update row" should "succeed" in {
  //    DB.localTx({ implicit session =>
  //      val single = TestEntity4.selectAll().head
  //
  //      single.myInt += 1
  //      single.myLong += 1
  //      single.myDouble += 1
  //      single.myFloat += 1
  //      single.myShort = 778
  //      single.myString += "!"
  //      single.myBoolean = false
  //      single.myChar = '_'
  //
  //      TestEntity4.save(Seq(single))
  //
  //      assert(TestEntity4.selectAll().size == 1)
  //
  //      val inDB = TestEntity4.selectAll().head
  //
  //      assert(single.uuid.isDefined)
  //
  //      assert(single.uuid == inDB.uuid)
  //
  //      assert(inDB.myInt == single.myInt)
  //      assert(inDB.myLong == single.myLong)
  //      assert(inDB.myDouble == single.myDouble)
  //      assert(inDB.myFloat == single.myFloat)
  //      assert(inDB.myString == single.myString)
  //      assert(inDB.myBoolean == single.myBoolean)
  //      assert(inDB.myShort == single.myShort)
  //      assert(inDB.myChar == single.myChar)
  //    })
  //  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      PrimaryKeyBeingUuidSpecTable.__dropTableSQL.foreach(_.execute())
    })
  }
}
