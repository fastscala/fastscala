package com.fastscala.db

import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc._

class TestEntity3(
                   var myInt: Int = 123,
                   var myLong: Long = 321,
                   var myDouble: Double = 1.234,
                   var myFloat: Float = 4.321f,
                   var myShort: Short = 777,
                   var myString: String = "hello",
                   var myBoolean: Boolean = true,
                   var myChar: Char = 'X',
                 ) extends Row[TestEntity3] {
  override def table: PgTable[TestEntity3] = TestEntity3
}

object TestEntity3 extends PgTable[TestEntity3] {
  override def createSampleRow(): TestEntity3 = new TestEntity3
}

trait TableRowSpecBase extends AnyFlatSpec {

  def runTests() {
  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity3.__createTableSQL.foreach(_.execute())
    })
  }
  "Insert row" should "succeed" in {
    DB.localTx({ implicit session =>
      new TestEntity3().insert()
    })
  }
  "Read row" should "succeed" in {
    DB.localTx({ implicit session =>
      val example = new TestEntity3()
      val single = TestEntity3.selectAll().head

      assert(example.myInt == single.myInt)
      assert(example.myLong == single.myLong)
      assert(example.myDouble == single.myDouble)
      assert(example.myFloat == single.myFloat)
      assert(example.myShort == single.myShort)
      assert(example.myString == single.myString)
      assert(example.myBoolean == single.myBoolean)
      assert(example.myChar == single.myChar)
    })
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity3.__dropTableSQL.execute()
    })
  }
}
}

class SQLiteTableRowSpec extends TableRowSpecBase with SQLiteDB

class PostgresTableRowSpec extends TableRowSpecBase with PostgresDB