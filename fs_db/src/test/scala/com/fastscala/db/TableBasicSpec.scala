package com.fastscala.db

import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc._

class TestEntity1 extends Row[TestEntity1] {

  var username: String = ""

  override def table: Table[TestEntity1] = TestEntity1
}

object TestEntity1 extends Table[TestEntity1] {
  override def createSampleRow(): TestEntity1 = new TestEntity1
}

trait TableBasicSpecBase extends AnyFlatSpec {

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity1.__createTableSQL.execute()
    })
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity1.__dropTableSQL.execute()
    })
  }
}

class SQLiteTableBasicSpec extends TableBasicSpecBase with SQLiteDB

class PostgresTableBasicSpec extends TableBasicSpecBase with PostgresDB