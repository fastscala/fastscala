package com.fastscala.db

import com.fastscala.db.keyed.{PgTableWithLongId, RowWithLongId}
import io.circe.Json
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc._

class TestEntity7(
                   var config: Json = TestEntity7.testJson
                 ) extends RowWithLongId[TestEntity7] {
  override def table: PgTableWithLongId[TestEntity7] with PgTableWithJsonSupport[TestEntity7] = TestEntity7
}

object TestEntity7 extends PgTableWithLongId[TestEntity7] with PgTableWithJsonSupport[TestEntity7] {

  import io.circe.syntax._

  val testJson: Json = List(1, 2, 3).asJson

  override def createSampleRow(): TestEntity7 = new TestEntity7()
}

class PgJsonSupportSpec extends AnyFlatSpec with PostgresDB {

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity7.__createTableSQL.foreach(_.execute())
    })
  }
  "Save row" should "succeed" in {
    DB.localTx({ implicit session =>
      val saved = new TestEntity7().save()
      saved.config == TestEntity7.testJson
    })
  }
  "Read row" should "succeed" in {
    DB.localTx({ implicit session =>
      val single = TestEntity7.selectAll().head
      assert(single.config == TestEntity7.testJson)
    })
  }
  "Update row" should "succeed" in {
    DB.localTx({ implicit session =>
      val single = TestEntity7.selectAll().head
      single.config = TestEntity7.testJson
      single.update()

      assert(TestEntity7.selectAll().size == 1)

      val inDB = TestEntity7.selectAll().head
      assert(inDB.config == TestEntity7.testJson)
    })
  }
  "Update row config to Json.Null" should "succeed" in {
    DB.localTx({ implicit session =>
      val single = TestEntity7.selectAll().head
      single.config = Json.Null
      single.update()

      assert(TestEntity7.selectAll().size == 1)

      val inDB = TestEntity7.selectAll().head
      assert(inDB.config == Json.Null)
    })
  }
  "Read row with Json.Null" should "succeed" in {
    DB.localTx({ implicit session =>
      val single = TestEntity7.selectAll().head
      assert(single.config == Json.Null)
    })
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity7.__dropTableSQL.execute()
    })
  }
}
