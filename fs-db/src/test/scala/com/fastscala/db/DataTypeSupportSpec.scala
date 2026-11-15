package com.fastscala.db

import io.circe.Json
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

import java.time.*
import java.util.UUID

class DataTypeSupportSpecRow(
                              var myInt: Int = 1,
                              var optInt: Option[Int] = Some(1),
                              var optString: Option[String] = Some("x"),
                              var optEnum: Option[TestEnum.Value] = Some(TestEnum.Ok),
                              var noneSample: Option[String] = None,
                              var day: DayOfWeek = DayOfWeek.MONDAY,
                              var aVeryLongFieldNameThatGoesOnAndOnAndOnUntilItIsLongerThanSixtyThreeChars: Int = 0,
                              var unsupported: java.util.Date = new java.util.Date(0),
                            )

/** DataTypeSupport: column names, SQL types, and values as SQL (literals checked by PostgreSQL itself). */
class DataTypeSupportSpec extends AnyFlatSpec with DBConn {

  private object Support extends DataTypeSupport

  private def field(name: String): java.lang.reflect.Field = {
    val f = classOf[DataTypeSupportSpecRow].getDeclaredField(name)
    f.setAccessible(true)
    f
  }

  private val sample = new DataTypeSupportSpecRow()

  /** (Trimmed: without constraints, the type is followed by a space.) */
  private def sqlType(name: String): String = Support.fieldTypeToSQLType(field(name), field(name).getType, field(name).get(sample)).trim

  /** What PostgreSQL makes of the literal: `SELECT <literal>`, as text. */
  private def selected(literal: SQLSyntax): String = DB.readOnly { implicit session =>
    sql"select ($literal)::text".map(_.string(1)).single.apply().orNull
  }

  "columnNameForField" should "be the field's name in lower_underscore, with the prefix" in {
    assert(Support.columnNameForField("myInt") == "my_int")
    assert(Support.columnNameForField("MyInt") == "my_int")
    assert(Support.columnNameForField(field("myInt")) == "my_int")
    assert(Support.columnNameForField(field("myInt"), Some("t")) == "t_my_int")
  }

  it should "refuse names longer than PostgreSQL's 63 chars" in {
    assertThrows[Exception](Support.columnNameForField(field("aVeryLongFieldNameThatGoesOnAndOnAndOnUntilItIsLongerThanSixtyThreeChars")))
  }

  "fieldTypeToSQLType" should "map Scala and Java types to PostgreSQL's, not null" in {
    assert(sqlType("myInt") == "integer not null")
    assert(sqlType("day") == "text not null")
    val types = Seq[(Class[?], String)](
      classOf[java.lang.String] -> "text", classOf[Long] -> "bigint", classOf[java.lang.Long] -> "bigint", classOf[Short] -> "integer", classOf[Byte] -> "integer",
      classOf[Float] -> "real", classOf[Double] -> "double precision", classOf[java.math.BigDecimal] -> "numeric", classOf[scala.math.BigDecimal] -> "numeric",
      classOf[Char] -> "char", classOf[Boolean] -> "boolean", classOf[Array[Byte]] -> "bytea", classOf[UUID] -> "UUID", classOf[Json] -> "json",
      classOf[LocalDate] -> "date", classOf[LocalTime] -> "time without time zone", classOf[LocalDateTime] -> "timestamp without time zone",
      classOf[OffsetDateTime] -> "timestamp with time zone", classOf[Instant] -> "timestamptz", classOf[java.sql.Timestamp] -> "timestamptz")
    for ((cls, expected) <- types) assert(Support.fieldTypeToSQLType(field("myInt"), cls, null) == s"$expected not null", cls)
    assert(Support.fieldTypeToSQLType(field("myInt"), classOf[Int], null, Set()).trim == "integer")
  }

  it should "make an Option's column nullable, of the type of the sample's value" in {
    assert(sqlType("optInt") == "integer")
    assert(sqlType("optString") == "text")
    assert(sqlType("optEnum") == "integer")
  }

  it should "refuse an Option without a value in the sample row (its type is unknown), and unknown types" in {
    assertThrows[Exception](sqlType("noneSample"))
    assertThrows[Exception](sqlType("unsupported"))
  }

  "valueToLiteral" should "be SQL literals of the values, that PostgreSQL reads back as them" in {
    assert(selected(Support.valueToLiteral(42)) == "42")
    assert(selected(Support.valueToLiteral(42L)) == "42")
    assert(selected(Support.valueToLiteral(7.toShort)) == "7")
    assert(selected(Support.valueToLiteral(1.5)) == "1.5")
    assert(selected(Support.valueToLiteral(1.5f)) == "1.5")
    assert(selected(Support.valueToLiteral(true)) == "true")
    assert(selected(Support.valueToLiteral(BigDecimal("12.340"))) == "12.340")
    assert(selected(Support.valueToLiteral(new java.math.BigDecimal("0.1"))) == "0.1")
    assert(selected(Support.valueToLiteral('x')) == "x")
    assert(selected(Support.valueToLiteral("it's \"quoted\" \\ é")) == "it's \"quoted\" \\ é")
    assert(selected(Support.valueToLiteral(Array[Byte](1, 2, -1))) == "\\x0102ff")
    assert(selected(Support.valueToLiteral(Array[Byte]())) == "\\x")
    val uuid = UUID.fromString("12345678-1234-1234-1234-123456789abc")
    assert(selected(Support.valueToLiteral(uuid)) == uuid.toString)
    assert(selected(Support.valueToLiteral(LocalDate.of(2026, 11, 15))) == "2026-11-15")
    assert(selected(Support.valueToLiteral(LocalTime.of(13, 5, 7))) == "13:05:07")
    assert(selected(Support.valueToLiteral(Json.obj("a" -> Json.fromInt(1)))) == """{"a":1}""")
    assert(selected(Support.valueToLiteral(TestEnum.Error)) == "1")
    assert(selected(Support.valueToLiteral(DayOfWeek.FRIDAY)) == "FRIDAY")
  }

  it should "be null for null and None, and the value for Some" in {
    assert(selected(Support.valueToLiteral(null)) == null)
    assert(selected(Support.valueToLiteral(None)) == null)
    assert(selected(Support.valueToLiteral(Some(3))) == "3")
  }

  it should "refuse values it can't write" in {
    assertThrows[Exception](Support.valueToLiteral(new Object))
  }

  it should "quote a ' char" in {
    // Regression (fixed): a Char is written as '$v'::char, unescaped: ''' is a syntax error (and an injection, for other chars).
    assert(selected(Support.valueToLiteral('\'')) == "'")
  }

  it should "write the doubles that aren't numbers" in {
    // Regression (fixed): NaN::double precision (unquoted) is read as a column named nan; PostgreSQL wants 'NaN'::double precision.
    assert(selected(Support.valueToLiteral(Double.NaN)) == "NaN")
    assert(selected(Support.valueToLiteral(Double.PositiveInfinity)) == "Infinity")
    assert(selected(Support.valueToLiteral(Double.NegativeInfinity)) == "-Infinity")
    assert(selected(Support.valueToLiteral(Float.NaN)) == "NaN")
  }

  "valueToBatchObject" should "be the JDBC object of the value" in {
    val f = field("myInt")
    assert(Support.valueToBatchObject(f, 1) == java.lang.Integer.valueOf(1))
    assert(Support.valueToBatchObject(f, Some(2L)) == java.lang.Long.valueOf(2))
    assert(Support.valueToBatchObject(f, None) == null)
    assert(Support.valueToBatchObject(f, BigDecimal(1)) == new java.math.BigDecimal(1))
    assert(Support.valueToBatchObject(f, TestEnum.Error) == java.lang.Integer.valueOf(1))
    val uuid = UUID.randomUUID()
    assert(Support.valueToBatchObject(f, uuid) == uuid.toString)
    assert(Support.valueBatchPlaceholder(f, uuid) == "?::UUID")
    assert(Support.valueBatchPlaceholder(f, 1) == "?")
    assert(Support.valueToBatchObject(field("day"), DayOfWeek.MONDAY) == "MONDAY")
  }

  it should "support the types the columns can have (java.math.BigDecimal, Json)" in {
    // Regression (fixed): java.math.BigDecimal and io.circe.Json have columns (numeric, json) but no case here: MatchError.
    val f = field("myInt")
    assert(Support.valueToBatchObject(f, new java.math.BigDecimal("1.5")) == new java.math.BigDecimal("1.5"))
    assert(Support.valueToBatchObject(f, Json.obj("a" -> Json.fromInt(1))) == """{"a":1}""")
    assert(Support.valueBatchPlaceholder(f, Json.True) == "?::json")
  }

  "bytesToHex" should "be the upper case hex digits of the bytes" in {
    assert(Support.bytesToHex(Array[Byte](0, 15, 16, -1, 127, -128)) == "000F10FF7F80")
    assert(Support.bytesToHex(Array[Byte]()) == "")
  }
}
