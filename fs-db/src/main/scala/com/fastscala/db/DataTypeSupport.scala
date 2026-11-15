package com.fastscala.db

import com.fastscala.db.computed.ComputedCol
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{WrappedResultSet, sqls}

import java.lang.reflect.Field
import java.nio.charset.StandardCharsets
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

import com.fastscala.db.annotations.{ForeignKey, PrimaryKey, Unique}
import com.fastscala.db.computed.ComputedCol
import com.fastscala.db.util.doubleQuoted
import com.google.common.base.CaseFormat
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax

import java.lang.reflect.Field
import java.nio.charset.StandardCharsets
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

// This is just for testing. Consider using cats.effect.IOApp instead of calling
// unsafe methods directly.
import scalikejdbc.*

import java.util.UUID


trait DataTypeSupport {

  private val columnNameCache = collection.mutable.Map[(java.lang.reflect.Field, Option[String]), String]()

  def columnNameForField(field: java.lang.reflect.Field, prefix: Option[String] = None): String = columnNameCache.getOrElseUpdate(field -> prefix, {
    prefix.map(_ + "_" + columnNameForField(field.getName)).getOrElse(columnNameForField(field.getName))
      .tap(name => if (name.length > 63) throw new Exception(s"Cannot create the column name from field '${field.getName}': more than 63 chars."))
  })

  def columnNameForField(name: String): String = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)

  protected def enumSampleToValue(sample: AnyRef, id: Int): AnyRef = sample match {
    case enumValue: scala.Enumeration#Value =>
      val enumValueClass = enumValue.getClass
      val enumReferenceFieldInEnumValue =
        Try(enumValueClass.getDeclaredField("$outer")).orElse(
          Try(enumValueClass.getField("$outer"))
        ).get
      enumReferenceFieldInEnumValue.setAccessible(true)
      val enumReference = enumReferenceFieldInEnumValue.get(enumValue)
      // Get the value with that id from the enum:
      enumReference.asInstanceOf[scala.Enumeration].apply(id)
  }


  // ================================= SUPPORT FOR DATA TYPES: =================================

  def fieldTypeToSQLType(
                          field: java.lang.reflect.Field,
                          clas: Class[?],
                          value: => Any,
                          columnConstrains: Set[String] = Set("not null")
                        ): String = clas.getName match {

    case "java.util.UUID" => "UUID" + columnConstrains.mkString(" ", " ", "")

    case "io.circe.Json" => "json" + columnConstrains.mkString(" ", " ", "")

    case "java.lang.Byte" => "integer" + columnConstrains.mkString(" ", " ", "")
    case "byte" => "integer" + columnConstrains.mkString(" ", " ", "")

    case "java.lang.Short" => "integer" + columnConstrains.mkString(" ", " ", "")
    case "short" => "integer" + columnConstrains.mkString(" ", " ", "")

    case "java.lang.Integer" => "integer" + columnConstrains.mkString(" ", " ", "")
    case "int" => "integer" + columnConstrains.mkString(" ", " ", "")

    case "java.lang.Long" => "bigint" + columnConstrains.mkString(" ", " ", "")
    case "long" => "bigint" + columnConstrains.mkString(" ", " ", "")

    case "java.lang.Float" => "real" + columnConstrains.mkString(" ", " ", "")
    case "float" => "real" + columnConstrains.mkString(" ", " ", "")

    case "java.lang.Double" => "double precision" + columnConstrains.mkString(" ", " ", "")
    case "double" => "double precision" + columnConstrains.mkString(" ", " ", "")

    case "java.math.BigDecimal" => "numeric" + columnConstrains.mkString(" ", " ", "")
    case "scala.math.BigDecimal" => "numeric" + columnConstrains.mkString(" ", " ", "")

    case "java.lang.Character" => "char" + columnConstrains.mkString(" ", " ", "")
    case "char" => "char" + columnConstrains.mkString(" ", " ", "")

    case "java.lang.String" => "text" + columnConstrains.mkString(" ", " ", "")

    case "java.lang.Boolean" => "boolean" + columnConstrains.mkString(" ", " ", "")
    case "boolean" => "boolean" + columnConstrains.mkString(" ", " ", "")

    case "[B" => "bytea" + columnConstrains.mkString(" ", " ", "")

    case "scala.Option" =>
      val cast = value.asInstanceOf[Option[Any]]
      if (cast.isEmpty) {
        throw new Exception(s"Missing value for field '${columnNameForField(field)}' in sample row for table '$this'")
      }
      fieldTypeToSQLType(field, cast.get.getClass, ???, columnConstrains - "not null")
    case "scala.Enumeration$Value" => "integer" + columnConstrains.mkString(" ", " ", "")

    case "scala.Enumeration$Val" => "integer" + columnConstrains.mkString(" ", " ", "")

    case "java.time.LocalDate" => "date" + columnConstrains.mkString(" ", " ", "")
    case "java.time.LocalTime" => "time without time zone" + columnConstrains.mkString(" ", " ", "")
    case "java.time.LocalDateTime" => "timestamp without time zone" + columnConstrains.mkString(" ", " ", "")
    case "java.time.OffsetDateTime" => "timestamp with time zone" + columnConstrains.mkString(" ", " ", "")

    case "java.time.Instant" => "timestamptz" + columnConstrains.mkString(" ", " ", "")
    case "java.sql.Timestamp" => "timestamptz" + columnConstrains.mkString(" ", " ", "")

    case _ if clas.isEnum => "text" + columnConstrains.mkString(" ", " ", "")

    case _ => throw new Exception(s"Unexpected field class ${clas.getSimpleName} for field ${field.getName}")
  }

  // JDBC primitive getters return 0/false for SQL NULL, so these must be called right after reading the value.
  private def optionUnlessNull[T](rs: WrappedResultSet, value: T): Option[T] = if (rs.underlying.wasNull()) None else Some(value)

  private def requireNotNull[T](rs: WrappedResultSet, columnName: String, value: T): T = {
    if (rs.underlying.wasNull()) throw new Exception(s"Unexpected NULL in non-optional column $columnName")
    value
  }

  def createValueSetterForField[T](field: java.lang.reflect.Field, sampleRowInstance: T, tableName: String, prefix: Option[String]): (WrappedResultSet, T) => Unit =
    createValueSetterForField(field, field.getType, sampleRowInstance, tableName, false, prefix)

  private def createValueSetterForField[T](field: java.lang.reflect.Field, valueType: Class[?], sampleRowInstance: T, tableName: String, nullable: Boolean, prefix: Option[String]): (WrappedResultSet, T) => Unit = {
    val columnName = columnNameForField(field, prefix)
    val sampleValue = field.get(sampleRowInstance)

    def setter(value: (WrappedResultSet, Int) => Any): (WrappedResultSet, T) => Unit = { (rs: WrappedResultSet, instance: T) =>
      var idx = -1
      try {
        if (idx == -1) {
          idx = rs.underlying.findColumn(columnName)
        }
        field.set(instance, value(rs, idx))
      } catch {
        case ex: Exception =>
          val meta = rs.underlying.getMetaData
          val existing = (1 to meta.getColumnCount).map(idx => s"$idx: ${meta.getColumnName(idx)} (${meta.getColumnTypeName(idx)})").mkString("[", ", ", "]")
          throw new Exception(s"Exception setting value of field ${columnNameForField(field)} of type ${valueType.getName} (existing columns: $existing)", ex)
      }
    }

    valueType.getName match {
      case "com.fastscala.db.computed.ComputedCol" =>
        val sample = sampleValue.asInstanceOf[ComputedCol[Any]]
        setter((rs, idx) => new ComputedCol[Any](sample.sql, sample.read, sample.read(columnName)(rs)))

      case "java.lang.String" | "string" if nullable => setter((rs, idx) => Option(rs.underlying.getString(idx)))
      case "java.lang.String" | "string" => setter((rs, idx) => rs.underlying.getString(idx))

      case "java.lang.Long" | "long" if nullable => setter((rs, idx) => optionUnlessNull(rs, rs.underlying.getLong(idx)))
      case "java.lang.Long" | "long" => setter((rs, idx) => requireNotNull(rs, columnName, rs.underlying.getLong(idx)))

      case "java.lang.Character" | "char" if nullable => setter((rs, idx) => Option(rs.underlying.getString(idx)).map(_.head))
      case "java.lang.Character" | "char" => setter((rs, idx) => rs.underlying.getString(idx).head)

      case "java.lang.Integer" | "int" if nullable => setter((rs, idx) => optionUnlessNull(rs, rs.underlying.getInt(idx)))
      case "java.lang.Integer" | "int" => setter((rs, idx) => requireNotNull(rs, columnName, rs.underlying.getInt(idx)))

      case "scala.Enumeration$Value" => setter((rs, idx) => enumSampleToValue(sampleValue.asInstanceOf[AnyRef], rs.underlying.getInt(idx)))

      case "scala.Enumeration$Val" if nullable => setter((rs, idx) => optionUnlessNull(rs, rs.underlying.getInt(idx)).map(i => enumSampleToValue(sampleValue.asInstanceOf[Some[AnyRef]].get, i)))

      case "java.lang.Short" | "short" if nullable => setter((rs, idx) => optionUnlessNull(rs, rs.underlying.getShort(idx)))
      case "java.lang.Short" | "short" => setter((rs, idx) => requireNotNull(rs, columnName, rs.underlying.getShort(idx)))

      case "java.lang.Float" | "float" if nullable => setter((rs, idx) => optionUnlessNull(rs, rs.underlying.getFloat(idx)))
      case "java.lang.Float" | "float" => setter((rs, idx) => requireNotNull(rs, columnName, rs.underlying.getFloat(idx)))

      case "java.lang.Double" | "double" if nullable => setter((rs, idx) => optionUnlessNull(rs, rs.underlying.getDouble(idx)))
      case "java.lang.Double" | "double" => setter((rs, idx) => requireNotNull(rs, columnName, rs.underlying.getDouble(idx)))

      case "java.math.BigDecimal" if nullable => setter((rs, idx) => Option(rs.underlying.getBigDecimal(idx)))
      case "java.math.BigDecimal" => setter((rs, idx) => rs.underlying.getBigDecimal(idx))

      case "scala.math.BigDecimal" if nullable => setter((rs, idx) => Option(rs.underlying.getBigDecimal(idx)).map(scala.math.BigDecimal(_)))
      case "scala.math.BigDecimal" => setter((rs, idx) => rs.underlying.getBigDecimal(idx): scala.math.BigDecimal)

      case "java.lang.Boolean" | "boolean" if nullable => setter((rs, idx) => optionUnlessNull(rs, rs.underlying.getBoolean(idx)))
      case "java.lang.Boolean" | "boolean" => setter((rs, idx) => requireNotNull(rs, columnName, rs.underlying.getBoolean(idx)))

      case "[B" if nullable => setter((rs, idx) => Option(rs.underlying.getBytes(idx)))
      case "[B" => setter((rs, idx) => rs.underlying.getBytes(idx))

      case "java.time.LocalDate" if nullable => setter((rs, idx) => Option(rs.underlying.getObject(columnName, classOf[java.time.LocalDate])))
      case "java.time.LocalDate" => setter((rs, idx) => rs.underlying.getObject(columnName, classOf[java.time.LocalDate]))

      case "java.time.LocalTime" if nullable => setter((rs, idx) => Option(rs.underlying.getObject(columnName, classOf[java.time.LocalTime])))
      case "java.time.LocalTime" => setter((rs, idx) => rs.underlying.getObject(columnName, classOf[java.time.LocalTime]))

      case "java.time.LocalDateTime" if nullable => setter((rs, idx) => Option(rs.underlying.getObject(columnName, classOf[java.time.LocalDateTime])))
      case "java.time.LocalDateTime" => setter((rs, idx) => rs.underlying.getObject(columnName, classOf[java.time.LocalDateTime]))

        // Via Timestamp so values stay in the system offset; pgjdbc's getObject(OffsetDateTime) returns UTC.
      case "java.time.OffsetDateTime" if nullable => setter((rs, idx) => Option(rs.underlying.getTimestamp(idx)).map(ts => java.time.OffsetDateTime.ofInstant(ts.toInstant, java.time.ZoneId.systemDefault())))
      case "java.time.OffsetDateTime" => setter((rs, idx) => Option(rs.underlying.getTimestamp(idx)).map(ts => java.time.OffsetDateTime.ofInstant(ts.toInstant, java.time.ZoneId.systemDefault())).orNull)

      case "java.time.Instant" if nullable => setter((rs, idx) => Option(rs.underlying.getTimestamp(idx)).map(_.toInstant))
      case "java.time.Instant" => setter((rs, idx) => rs.underlying.getTimestamp(idx).toInstant)

      case "java.sql.Timestamp" if nullable => setter((rs, idx) => Option(rs.underlying.getTimestamp(idx)))
      case "java.sql.Timestamp" => setter((rs, idx) => rs.underlying.getTimestamp(idx))

      case "java.util.UUID" if nullable => setter((rs, idx) => {
        val obj = rs.underlying.getObject(idx)
        if (obj == null) None
        else Some(obj.asInstanceOf[UUID])
      })
      case "java.util.UUID" => setter((rs, idx) => rs.underlying.getObject(idx).asInstanceOf[UUID])

      case "io.circe.Json" if nullable => setter((rs, idx) => Option(rs.underlying.getString(idx)).map(str => io.circe.parser.parse(str).right.get))
      case "io.circe.Json" => setter((rs, idx) => io.circe.parser.parse(rs.underlying.getString(idx)).right.get)

      case "scala.Option" =>
        if (sampleValue.isInstanceOf[None.type]) throw new Exception(s"Missing sample value for optional column ${columnNameForField(field)} on table ${tableName}")
        createValueSetterForField(field, sampleValue.asInstanceOf[Some[Any]].get.getClass, sampleRowInstance, tableName, true, prefix)

      case _ if field.getType.isEnum => setter((rs, idx) => trustMe(field.getType, rs.underlying.getString(idx)))

      case unknown => throw new Exception(s"Unknown value type $unknown of field ${columnNameForField(field)}")
    }
  }

  def trustMe[T <: Enum[T]](cls: Class[?], name: String): Enum[T] = Enum.valueOf[T](cls.asInstanceOf[Class[T]], name)

  def valueBatchPlaceholder(field: Field, value: Any): String = value match {
    case v: java.util.UUID => "?::UUID"
    case v: io.circe.Json => "?::json" // (its batch object is its text)
    case _ => "?"
  }

  def valueToBatchObject(field: Field, value: Any): Object = value match {
    case null => null
    case None => null
    case Some(value) => valueToBatchObject(field, value)
    case v: Int => java.lang.Integer(v)
    case v: Double => java.lang.Double(v)
    case v: java.math.BigDecimal => v
    case v: scala.math.BigDecimal => v.bigDecimal
    case v: Boolean => java.lang.Boolean(v)
    case v: Char => java.lang.Character(v)
    case v: Short => java.lang.Short(v)
    case v: Float => java.lang.Float(v)
    case v: Long => java.lang.Long(v)
    case v: String => java.lang.String(v)
    case v: Array[Byte] => v
    case v: Enumeration#Value => java.lang.Integer(v.id)
    case v: java.time.LocalDate => v
    case v: java.time.LocalTime => v
    case v: java.time.LocalDateTime => v
    case v: java.time.OffsetDateTime => v
    case v: java.time.Instant => v
    case v: java.sql.Timestamp => v
    case v: java.util.UUID => v.toString
    case v: io.circe.Json => v.noSpaces
    case v if field.getType.isEnum => v.toString
  }

  def valueToFragment(field: Field, value: Any): SQLSyntax = value match {
    case null => sqls"null"
    case None => sqls"null"
    case Some(value) => valueToFragment(field, value)
    case Array() => sqls"''::bytea"
    case v: Int => sqls"$v"
    case v: Double => sqls"$v"
    case v: java.math.BigDecimal => sqls"$v"
    case v: scala.math.BigDecimal => sqls"$v"
    case v: Boolean => sqls"$v"
    case v: Char => sqls"$v"
    case v: Short => sqls"$v"
    case v: Float => sqls"$v"
    case v: Long => sqls"$v"
    case v: String => sqls"$v"
    case v: Array[Byte] => sqls"$v"
    case v: Enumeration#Value => sqls"${v.id}"
    case v: java.time.LocalDate => sqls"$v"
    case v: java.time.LocalTime => sqls"$v"
    case v: java.time.LocalDateTime => sqls"$v"
    case v: java.time.OffsetDateTime => sqls"$v"
    case v: java.time.Instant => sqls"$v"
    case v: java.sql.Timestamp => sqls"$v"
    case v: java.util.UUID => SQLSyntax.createUnsafely(s"'${v.toString}'::UUID")
    case v: io.circe.Json => sqls"${v.noSpaces}::json"
    case v if field.getType.isEnum => sqls"${v.toString}"
  }

  def valueToLiteral(value: Any): SQLSyntax = value match {
    case null => SQLSyntax.createUnsafely("null")
    case None => SQLSyntax.createUnsafely("null")
    case Some(value) => valueToLiteral(value)
    case Array() => SQLSyntax.createUnsafely("''::bytea")
    case v: Int => SQLSyntax.createUnsafely(v + "::integer")
    // (NaN and the infinities are only literals quoted: 'NaN'::double precision.)
    case v: Double if v.isNaN || v.isInfinite => SQLSyntax.createUnsafely(s"'$v'::double precision")
    case v: Double => SQLSyntax.createUnsafely(v + "::double precision")
    case v: java.math.BigDecimal => SQLSyntax.createUnsafely(v.toString() + "::numeric")
    case v: scala.math.BigDecimal => SQLSyntax.createUnsafely(v.toString() + "::numeric")
    case v: Boolean => SQLSyntax.createUnsafely(v.toString + "::boolean")
    case v: Char => SQLSyntax.createUnsafely("'" + org.postgresql.core.Utils.escapeLiteral(null, v.toString, true) + "'::char")
    case v: Short => SQLSyntax.createUnsafely(s"$v::integer")
    case v: Float if v.isNaN || v.isInfinite => SQLSyntax.createUnsafely(s"'$v'::real")
    case v: Float => SQLSyntax.createUnsafely(s"$v::real")
    case v: Long => SQLSyntax.createUnsafely(s"$v::bigint")
    case v: String => SQLSyntax.createUnsafely("'" + org.postgresql.core.Utils.escapeLiteral(null, v, true) + "'" + "::text")
    case v: Array[Byte] if v.isEmpty => SQLSyntax.createUnsafely("'{}'")
    case v: Array[Byte] => SQLSyntax.createUnsafely(s"'\\x${bytesToHex(v)}'::bytea")
    case v: Enumeration#Value => valueToLiteral(v.id)
    case v: java.time.LocalDate => SQLSyntax.createUnsafely(s"'${v.format(DateTimeFormatter.ISO_LOCAL_DATE)}'")
    case v: java.time.LocalTime => SQLSyntax.createUnsafely(s"'${v.format(DateTimeFormatter.ISO_LOCAL_TIME)}'")
    case v: java.time.LocalDateTime => SQLSyntax.createUnsafely(s"'${v.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}'")
    case v: java.time.OffsetDateTime => SQLSyntax.createUnsafely(s"'${v.toString}'")
    case v: java.time.Instant => SQLSyntax.createUnsafely(s"'${v.atOffset(ZoneOffset.UTC).toString}'")
    case v: java.sql.Timestamp => SQLSyntax.createUnsafely(s"'${v.toString}'")
    case v: io.circe.Json => SQLSyntax.createUnsafely(s"'${v.noSpaces}'::json")
    case v: UUID => SQLSyntax.createUnsafely(s"'$v'::UUID")
    case v if v.getClass.isEnum => valueToLiteral(v.toString)
    case _ => throw new Exception(s"Could not translate value $value (${value.getClass.getSimpleName}) to literal SQL value")
  }

  private val HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII)

  def bytesToHex(bytes: Array[Byte]): String = {
    val hexChars = new Array[Byte](bytes.length * 2)
    for (j <- 0 until bytes.length) {
      val v = bytes(j) & 0xFF
      hexChars(j * 2) = HEX_ARRAY(v >>> 4)
      hexChars(j * 2 + 1) = HEX_ARRAY(v & 0x0F)
    }
    new String(hexChars, StandardCharsets.UTF_8)
  }
}
