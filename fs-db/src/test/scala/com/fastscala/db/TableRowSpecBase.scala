package com.fastscala.db

import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

class TestEntity3(
                   var myInt: Int = 123,
                   var myLong: Long = 321,
                   var myDouble: Double = 1.234,
                   var myFloat: Float = 4.321f,
                   var myShort: Short = 777,
                   var myString: String = "hello",
                   var myBoolean: Boolean = true,
                   var myChar: Char = 'X',
                   var myBigDecimal: BigDecimal = 1.23,
                 ) extends Row[TestEntity3] {
  override def table: Table[TestEntity3] = TestEntity3
}

object TestEntity3 extends Table[TestEntity3] {
  override def createSampleRow(): TestEntity3 = new TestEntity3
}

class TableRowSpecBase extends AnyFlatSpec with PostgresDB {

  def runTests(): Unit = {
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
        assert(example.myBigDecimal == single.myBigDecimal)
      })
    }
    "Delete table" should "succeed" in {
      DB.localTx({ implicit session =>
        TestEntity3.__dropTableSQL.execute()
      })
    }
  }
}