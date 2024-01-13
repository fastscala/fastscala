package com.fastscala.db

import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc._

import java.time.LocalDate

class TestEntity6(
                   var myDate: LocalDate = LocalDate.now()
                 ) extends PgRowWithUUID[TestEntity6] {
  override def table: PgTableWithUUID[TestEntity6] = TestEntity6
}

object TestEntity6 extends PgTableWithUUID[TestEntity6] {
  override def createSampleRow(): TestEntity6 = new TestEntity6()
}

class PgTimeSupportSpec extends AnyFlatSpec with PostgresDB {

  val today = LocalDate.now()
  val tomorrow = today.plusDays(1)

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity6.__createTableSQL.execute()
    })
  }
  "Save row" should "succeed" in {
    DB.localTx({ implicit session =>
      val saved = new TestEntity6(today).save()
      assert(saved.uuid.isDefined)
    })
  }
  "Read row" should "succeed" in {
    DB.localTx({ implicit session =>
      val single = TestEntity6.listAll().head

      assert(single.myDate == today)
      assert(single.myDate != tomorrow)
    })
  }
  "Update row" should "succeed" in {
    DB.localTx({ implicit session =>
      val single = TestEntity6.listAll().head
      single.myDate = tomorrow
      single.update()

      assert(TestEntity6.listAll().size == 1)

      val inDB = TestEntity6.listAll().head
      assert(single.uuid.isDefined)
      assert(single.uuid == inDB.uuid)

      assert(inDB.myDate == tomorrow)
    })
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity6.__dropTableSQL.execute()
    })
  }
}
