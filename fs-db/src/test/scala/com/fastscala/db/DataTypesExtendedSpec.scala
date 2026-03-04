package com.fastscala.db

import io.circe.Json
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

import java.sql.Timestamp
import java.time.*
import java.util.UUID

object TestEnum extends Enumeration {
  val Ok = Value
  val Error = Value
}

class DataTypesExtendedSpecTable(
                                  var string: java.lang.String = "hello",
                                  var jBoolean: java.lang.Boolean = true,
                                  var jLong: java.lang.Long = 123,
                                  var jCharacter: java.lang.Character = 'x',
                                  var jInteger: java.lang.Integer = 123,
                                  var jShort: java.lang.Short = 123.toShort,
                                  var jFloat: java.lang.Float = 1.23f,
                                  var jDouble: java.lang.Double = 1.23,
                                  var sBoolean: scala.Boolean = true,
                                  var sLong: scala.Long = 123,
                                  var sCharacter: scala.Char = 'x',
                                  var sInteger: scala.Int = 123,
                                  var sShort: scala.Short = 123,
                                  var sFloat: scala.Float = 1.23f,
                                  var sDouble: scala.Double = 1.23,
                                  //
                                  var enumeration: TestEnum.Value = TestEnum.Error,
                                  //
                                  var jBigDecimal: java.math.BigDecimal = java.math.BigDecimal.valueOf(1.23),
                                  var sBigDecimal: scala.math.BigDecimal = 1.23,
                                  //
                                  var bytes: Array[Byte] = Array(1, 2, 3),
                                  var emptyBytes: Array[Byte] = Array(),
                                  //
                                  var uuid: UUID = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                                  //
                                  var localDate: LocalDate = LocalDate.of(2011, 11, 11),
                                  var localTime: LocalTime = LocalTime.of(11, 11, 11),
                                  var localDateTime: LocalDateTime = LocalDateTime.of(2011, 11, 11, 11, 11, 11),
                                  var offsetDateTime: OffsetDateTime = OffsetDateTime.of(2011, 11, 11, 11, 11, 11, 0, ZoneOffset.UTC),
                                  var instant: Instant = Instant.ofEpochSecond(111111L),
                                  var timestamp: Timestamp = Timestamp.valueOf("2011-11-11 11:11:11"),
                                  //
                                  var json: Json = DataTypesExtendedSpecTable.testJson
                                ) extends Row[DataTypesExtendedSpecTable] {
  override def table: Table[DataTypesExtendedSpecTable] = DataTypesExtendedSpecTable
}

object DataTypesExtendedSpecTable extends Table[DataTypesExtendedSpecTable] {

  import io.circe.syntax._

  lazy val testJson: Json = List(1, 2, 3).asJson

  override def createSampleRow(): DataTypesExtendedSpecTable = new DataTypesExtendedSpecTable()
}

class DataTypesExtendedSpec extends AnyFlatSpec with DBConn {

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      sql"""CREATE TABLE IF NOT EXISTS ${DataTypesExtendedSpecTable.tableNameSQLSyntaxQuoted} ("string" text)""".execute()
    })
  }
  "Insert one empty row" should "succeed" in {
    DB.localTx({ implicit session =>
      sql"INSERT INTO ${DataTypesExtendedSpecTable.tableNameSQLSyntaxQuoted} VALUES ('hello');".execute()
    })
  }
  "Add columns with defaults from sample row" should "succeed" in {
    DB.localTx({ implicit session =>
      DataTypesExtendedSpecTable.__addMissingColumnsIfNotExistsWithDefaultsFromSampleRow().foreach(_.execute())
    })
  }
  "Read row with values being the defaults from the sample row" should "succeed" in {
    DB.localTx({ implicit session =>
      val sampleRow = new DataTypesExtendedSpecTable()
      val single = DataTypesExtendedSpecTable.selectAll().head

      assert(sampleRow.string == single.string)
      assert(sampleRow.jBoolean == single.jBoolean)
      assert(sampleRow.jLong == single.jLong)
      assert(sampleRow.jCharacter == single.jCharacter)
      assert(sampleRow.jInteger == single.jInteger)
      assert(sampleRow.jShort == single.jShort)
      assert(sampleRow.jFloat == single.jFloat)
      assert(sampleRow.jDouble == single.jDouble)
      assert(sampleRow.sBoolean == single.sBoolean)
      assert(sampleRow.sLong == single.sLong)
      assert(sampleRow.sCharacter == single.sCharacter)
      assert(sampleRow.sInteger == single.sInteger)
      assert(sampleRow.sShort == single.sShort)
      assert(sampleRow.sFloat == single.sFloat)
      assert(sampleRow.sDouble == single.sDouble)
      assert(sampleRow.enumeration == single.enumeration)
      assert(sampleRow.jBigDecimal == single.jBigDecimal)
      assert(sampleRow.sBigDecimal == single.sBigDecimal)
      assert(sampleRow.bytes.toList == single.bytes.toList)
      assert(sampleRow.emptyBytes.toList == single.emptyBytes.toList)
      assert(sampleRow.uuid == single.uuid)
      assert(sampleRow.localDate == single.localDate)
      assert(sampleRow.localTime == single.localTime)
      assert(sampleRow.localDateTime == single.localDateTime)
      assert(sampleRow.offsetDateTime == single.offsetDateTime)
      assert(sampleRow.instant == single.instant)
      assert(sampleRow.timestamp == single.timestamp)
      assert(sampleRow.json == single.json)
    })
  }
  "Truncate" should "succeed" in {
    DB.localTx({ implicit session =>
      DataTypesExtendedSpecTable.__truncateSQL.execute()
    })
  }
  "Insert row" should "succeed" in {
    DB.localTx({ implicit session =>
      new DataTypesExtendedSpecTable().insert()
    })
  }
  "Row in DB should have the inserted values" should "succeed" in {
    DB.localTx({ implicit session =>
      val sampleRow = new DataTypesExtendedSpecTable()
      val single = DataTypesExtendedSpecTable.selectAll().head

      assert(sampleRow.string == single.string)
      assert(sampleRow.jBoolean == single.jBoolean)
      assert(sampleRow.jLong == single.jLong)
      assert(sampleRow.jCharacter == single.jCharacter)
      assert(sampleRow.jInteger == single.jInteger)
      assert(sampleRow.jShort == single.jShort)
      assert(sampleRow.jFloat == single.jFloat)
      assert(sampleRow.jDouble == single.jDouble)
      assert(sampleRow.sBoolean == single.sBoolean)
      assert(sampleRow.sLong == single.sLong)
      assert(sampleRow.sCharacter == single.sCharacter)
      assert(sampleRow.sInteger == single.sInteger)
      assert(sampleRow.sShort == single.sShort)
      assert(sampleRow.sFloat == single.sFloat)
      assert(sampleRow.sDouble == single.sDouble)
      assert(sampleRow.enumeration == single.enumeration)
      assert(sampleRow.jBigDecimal == single.jBigDecimal)
      assert(sampleRow.sBigDecimal == single.sBigDecimal)
      assert(sampleRow.bytes.toList == single.bytes.toList)
      assert(sampleRow.emptyBytes.toList == single.emptyBytes.toList)
      assert(sampleRow.uuid == single.uuid)
      assert(sampleRow.localDate == single.localDate)
      assert(sampleRow.localTime == single.localTime)
      assert(sampleRow.localDateTime == single.localDateTime)
      assert(sampleRow.offsetDateTime == single.offsetDateTime)
      assert(sampleRow.instant == single.instant)
      assert(sampleRow.timestamp == single.timestamp)
      assert(sampleRow.json == single.json)
    })
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      DataTypesExtendedSpecTable.__dropTableSQL.foreach(_.execute())
    })
  }
}