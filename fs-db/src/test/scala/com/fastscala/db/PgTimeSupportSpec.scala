package com.fastscala.db

import com.fastscala.db.keyed.uuid.RowWithUUID
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate}

class TestEntity6(
                   var myDate: LocalDate = LocalDate.now(),
                   var myInstant: Instant = Instant.ofEpochSecond(0),
                 ) extends RowWithUUID[TestEntity6] {
  override def table: TableWithUuidId[TestEntity6] = TestEntity6
}

object TestEntity6 extends TableWithUuidId[TestEntity6] {
  override def createSampleRow(): TestEntity6 = new TestEntity6()
}

class PgTimeSupportSpec extends AnyFlatSpec with PostgresDB {

  val today = LocalDate.now()
  val tomorrow = today.plusDays(1)
  val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
  val tomorrowNow = Instant.now().plusSeconds(24 * 60 * 60).truncatedTo(ChronoUnit.MILLIS)

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity6.__createTableSQL.foreach(_.execute())
    })
  }
  "Save row" should "succeed" in {
    DB.localTx({ implicit session =>
      val saved = new TestEntity6(today, now).save()
      assert(saved.uuid.isDefined)
    })
  }
  "Read row" should "succeed" in {
    DB.localTx({ implicit session =>
      val single = TestEntity6.selectAll().head

      assert(single.myDate == today)
      assert(single.myInstant == now)
      assert(single.myDate != tomorrow)
    })
  }
  "Update row" should "succeed" in {
    DB.localTx({ implicit session =>
      val single = TestEntity6.selectAll().head
      single.myDate = tomorrow
      single.myInstant = tomorrowNow
      single.update()

      assert(TestEntity6.selectAll().size == 1)

      val inDB = TestEntity6.selectAll().head
      assert(single.uuid.isDefined)
      assert(single.uuid == inDB.uuid)

      assert(inDB.myDate == tomorrow)
      assert(inDB.myInstant == tomorrowNow)
    })
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity6.__dropTableSQL.execute()
    })
  }
}
