package com.fastscala.db

import com.fastscala.db.keyed.uuid.SQLiteRowWithUUID
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc._

class TestEntity5(
                   var myInt: Int = 123,
                   var myLong: Long = 321,
                   var myDouble: Double = 1.234,
                   var myFloat: Float = 4.321f,
                   var myShort: Short = 777,
                   var myString: String = "hello",
                   var myBoolean: Boolean = true,
                   var myChar: Char = 'X',
                 ) extends SQLiteRowWithUUID[TestEntity5] {
  override def table: SQLiteTableWithUUID[TestEntity5] = TestEntity5
}

object TestEntity5 extends SQLiteTableWithUUID[TestEntity5] {
  override def createSampleRow(): TestEntity5 = new TestEntity5()
}

class SQLiteTableUUIDRowSpec extends AnyFlatSpec with SQLiteDB {

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity5.__createTableSQL.map(_.statement).foreach(println)
      TestEntity5.__createTableSQL.foreach(_.execute())
    })
  }
  "Save row" should "succeed" in {
    DB.localTx({ implicit session =>
      val saved = new TestEntity5().save()
      assert(saved.uuid.isDefined)
    })
  }
  "Read row" should "succeed" in {
    DB.localTx({ implicit session =>
      val example = new TestEntity5()
      val single = TestEntity5.selectAll().head

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
  "Update row" should "succeed" in {
    DB.localTx({ implicit session =>
      val single = TestEntity5.selectAll().head

      single.myInt += 1
      single.myLong += 1
      single.myDouble += 1
      single.myFloat += 1
      single.myShort = 778
      single.myString += "!"
      single.myBoolean = false
      single.myChar = '_'

      single.update()

      assert(TestEntity5.selectAll().size == 1)

      val inDB = TestEntity5.selectAll().head

      assert(single.uuid.isDefined)

      assert(single.uuid == inDB.uuid)

      assert(inDB.myInt == single.myInt)
      assert(inDB.myLong == single.myLong)
      assert(inDB.myDouble == single.myDouble)
      assert(inDB.myFloat == single.myFloat)
      assert(inDB.myString == single.myString)
      assert(inDB.myBoolean == single.myBoolean)
      assert(inDB.myShort == single.myShort)
      assert(inDB.myChar == single.myChar)
    })
  }
  "Select by UUID" should "succeed" in {
    DB.localTx({ implicit session =>
      new TestEntity5().save()
      new TestEntity5().save()
      val uuids = TestEntity5.selectAll().map(_.uuid.get)

      assert(TestEntity5.getForIds(uuids: _*).size == 3)
    })
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      TestEntity5.__dropTableSQL.execute()
    })
  }
}
