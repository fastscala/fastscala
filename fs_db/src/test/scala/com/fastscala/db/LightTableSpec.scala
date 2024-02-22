package com.fastscala.db

import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc._

class LightTableTestEntity(
                            var myInt: Int = 123,
                            var myLong: Long = 321,
                            var myDouble: Double = 1.234,
                            var myFloat: Float = 4.321f,
                            var myShort: Short = 777,
                            var myString: String = "hello",
                            var myBoolean: Boolean = true,
                            var myChar: Char = 'X',
                          ) extends Row[LightTableTestEntity] {
  override def table: PgTable[LightTableTestEntity] = LightTableTestEntity
}

object LightTableTestEntity extends PgTable[LightTableTestEntity] {
  override def createSampleRow(): LightTableTestEntity = new LightTableTestEntity
}

class LightTableTestLightEntity(
                                 var myInt: Int = 123,
                                 var myChar: Char = 'X'
                               ) extends Row[LightTableTestLightEntity] {
  override def table: PgTable[LightTableTestLightEntity] = LightTableTestLightEntity
}

object LightTableTestLightEntity extends PgTable[LightTableTestLightEntity] {

  override def tableName: String = LightTableTestEntity.tableName

  override def createSampleRow(): LightTableTestLightEntity = new LightTableTestLightEntity
}

trait LightTableSpecBase extends AnyFlatSpec {

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      LightTableTestEntity.__createTableSQL.execute()
    })
  }
  "Insert row" should "succeed" in {
    DB.localTx({ implicit session =>
      new LightTableTestEntity().insert()
    })
  }
  "Read light row" should "succeed" in {
    DB.localTx({ implicit session =>
      val example = new LightTableTestLightEntity()
      val single = LightTableTestLightEntity.selectAll().head

      assert(example.myInt == single.myInt)
      assert(example.myChar == single.myChar)
    })
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      LightTableTestEntity.__dropTableSQL.execute()
    })
  }
}

class SQLiteLightTableSpec extends LightTableSpecBase with SQLiteDB

class PostgresLightTableSpec extends LightTableSpecBase with PostgresDB
