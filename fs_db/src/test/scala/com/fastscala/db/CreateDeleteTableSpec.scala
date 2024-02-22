package com.fastscala.db

import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc._

class TestEntity2(
                   var myInt: Int = 123,
                   var myLong: Long = 321,
                   var myDouble: Double = 1.234,
                   var myFloat: Float = 4.321f,
                   var myString: String = "hello",
                   var myBoolean: Boolean = true,
                   var myShort: Short = 777,
                   var myChar: Char = 'X',
                 ) extends Row[TestEntity2] {
  override def table: PgTable[TestEntity2] = TestEntity2
}

object TestEntity2 extends PgTable[TestEntity2] {
  override def createSampleRow(): TestEntity2 = new TestEntity2
}

trait CreateTableSpecBase extends AnyFlatSpec {

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity2.__createTableSQL.execute()
    })
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity2.__dropTableSQL.execute()
    })
  }
}

class SQLiteCreateTableSpec extends CreateTableSpecBase with SQLiteDB

class PostgresCreateTableSpec extends CreateTableSpecBase with PostgresDB