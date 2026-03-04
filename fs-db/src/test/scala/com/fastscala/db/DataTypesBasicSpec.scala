package com.fastscala.db

import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

class DataTypesBasicSpecTable(
                   var myInt: Int = 123,
                   var myLong: Long = 321,
                   var myDouble: Double = 1.234,
                   var myFloat: Float = 4.321f,
                   var myShort: Short = 777,
                   var myString: String = "hello",
                   var myBoolean: Boolean = true,
                   var myChar: Char = 'X',
                   var myBigDecimal: BigDecimal = 1.23,
                 ) extends Row[DataTypesBasicSpecTable] {
  override def table: Table[DataTypesBasicSpecTable] = DataTypesBasicSpecTable
}

object DataTypesBasicSpecTable extends Table[DataTypesBasicSpecTable] {
  override def createSampleRow(): DataTypesBasicSpecTable = new DataTypesBasicSpecTable
}

class TableRowSpecBase extends AnyFlatSpec with PostgresDB {

  def runTests(): Unit = {
    "Create table" should "succeed" in {
      DB.localTx({ implicit session =>
        DataTypesBasicSpecTable.__createTableSQL.foreach(_.execute())
      })
    }
    "Insert row" should "succeed" in {
      DB.localTx({ implicit session =>
        new DataTypesBasicSpecTable().insert()
      })
    }
    "Read row" should "succeed" in {
      DB.localTx({ implicit session =>
        val example = new DataTypesBasicSpecTable()
        val single = DataTypesBasicSpecTable.selectAll().head

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
        DataTypesBasicSpecTable.__dropTableSQL.foreach(_.execute())
      })
    }
  }
}