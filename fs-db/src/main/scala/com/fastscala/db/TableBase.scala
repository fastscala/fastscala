package com.fastscala.db

import com.fastscala.db.annotations.{ForeignKey, PrimaryKey}
import com.fastscala.db.computed.ComputedCol
import com.fastscala.db.util.doubleQuoted
import com.google.common.base.CaseFormat
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax

import java.lang.reflect.Field
import java.time.format.DateTimeFormatter
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

// This is just for testing. Consider using cats.effect.IOApp instead of calling
// unsafe methods directly.
import scalikejdbc.*

import java.util.UUID

trait TableBase {

  private val logger = LoggerFactory.getLogger(getClass.getName)

  def createSampleRow(): Any

  def createEmptyRow(): Any

  def createSampleRowInternal(): Any

  def createEmptyRowInternal(): Any

  def sampleRow: Any

  val transientAnnotations = Set("java.beans.Transient", "scala.transient")

  val fieldsList: List[Field] = sampleRow.getClass.getDeclaredFields.iterator.filter({
    case field =>
      !field.getName.contains("$") &&
        !field.getAnnotations.exists(anno => transientAnnotations.contains(anno.annotationType().getName))
  }).toList

  def tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, getClass.getSimpleName.replaceAll("\\$$", ""))

  def tableNameSQLSyntax = SQLSyntax.createUnsafely(tableName)

  def tableNameSQLSyntaxQuoted = tableNameSQLSyntax.doubleQuoted

  def fieldName(field: java.lang.reflect.Field, prefix: Option[String] = None): String = prefix.map(_ + "_" + fieldName(field.getName)).getOrElse(fieldName(field.getName))

  def fieldName(name: String): String = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)

  private case class FK(fkName: SQLSyntax, column: SQLSyntax, referencesTable: SQLSyntax, referencesColumn: SQLSyntax, onUpdate: SQLSyntax, onDelete: SQLSyntax)

  private def foreignKeys: List[FK] = (sampleRow.getClass.getDeclaredConstructors.flatMap(cons => {
    cons.getParameters.flatMap(param => {
      param.getDeclaredAnnotations.collect({
        case fk: ForeignKey => param.getName -> fk
      })
    })
  }) ++ sampleRow.getClass.getDeclaredFields.flatMap(f => {
    f.getDeclaredAnnotations.collect({
      case fk: ForeignKey => f.getName -> fk
    })
  })).toList.map({
    case (name, fk) =>
      val fkName = SQLSyntax.createUnsafely("fk_" + fieldName(name)).doubleQuoted
      val column = SQLSyntax.createUnsafely(fieldName(name)).doubleQuoted
      val referencesTable = SQLSyntax.createUnsafely(fk.refTbl()).doubleQuoted
      val referencesColumn = SQLSyntax.createUnsafely(fk.refCol()).doubleQuoted
      val onUpdate = SQLSyntax.createUnsafely(fk.onUpdate().Sql)
      val onDelete = SQLSyntax.createUnsafely(fk.onDelete().Sql)
      FK(fkName, column, referencesTable, referencesColumn, onUpdate, onDelete)
  })

  private def primaryKeys: List[(String, PrimaryKey)] = (sampleRow.getClass.getDeclaredConstructors.flatMap(cons => {
    cons.getParameters.flatMap(param => {
      param.getDeclaredAnnotations.collect({
        case pk: PrimaryKey => param.getName -> pk
      })
    })
  }) ++ sampleRow.getClass.getDeclaredFields.flatMap(f => {
    f.getDeclaredAnnotations.collect({
      case pk: PrimaryKey => f.getName -> pk
    })
  })).toList

  def __dropForeignKeyConstrainsSQL: List[SQL[Nothing, NoExtractor]] = {
    foreignKeys.map({
      case FK(fkName, column, referencesTable, referencesColumn, onUpdate, onDelete) =>
        sql"""ALTER TABLE $tableNameSQLSyntaxQuoted DROP CONSTRAINT IF EXISTS $fkName;"""
    })
  }

  def __createForeignKeyConstrainsSQL: List[SQL[Nothing, NoExtractor]] = {
    foreignKeys.map({
      case FK(fkName, column, referencesTable, referencesColumn, onUpdate, onDelete) =>
        sql"""ALTER TABLE $tableNameSQLSyntaxQuoted ADD CONSTRAINT $fkName FOREIGN KEY ($column) REFERENCES $referencesTable ($referencesColumn) ON UPDATE $onUpdate ON DELETE $onDelete;"""
    })
  }

  def __createPrimaryKeyConstrainsSQL: List[SQL[Nothing, NoExtractor]] = {
    if (primaryKeys.isEmpty) Nil
    else {
      val pkeyName = SQLSyntax.createUnsafely(tableName + "_pkey").doubleQuoted
      val cols = primaryKeys.map({ case (name, _) => SQLSyntax.createUnsafely(fieldName(name)).doubleQuoted })
      List(sql"""ALTER TABLE $tableNameSQLSyntaxQuoted ADD CONSTRAINT $pkeyName PRIMARY KEY (${SQLSyntax.join(cols, sqls",")});""".tap(println))
    }
  }

  def __createTableSQL: List[SQL[Nothing, NoExtractor]] =
    __createTableOnlySQL :::
      __createPrimaryKeyConstrainsSQL :::
      __createForeignKeyConstrainsSQL

  def __createTableOnlySQL: List[SQL[Nothing, NoExtractor]] = {
    val columns: String = fieldsList.map(field => {
      field.setAccessible(true)
      s""""${fieldName(field)}" ${fieldTypeToSQLType(field, field.getType, field.get(sampleRow))}"""
    }).mkString("(", ",", ")")

    List(SQL(s"""CREATE TABLE IF NOT EXISTS ${s"\"$tableName\""} $columns"""))
  }

  def __dropColumnWithName(colName: String): List[SQL[Nothing, NoExtractor]] = {
    List {
      val statement = sql"""ALTER TABLE ${SQLSyntax.createUnsafely(s"\"$tableName\"")} DROP COLUMN "${SQLSyntax.createUnsafely(colName)}";""".stripMargin
      statement
    }
  }


  def __addMissingColumnsIfNotExists: List[SQL[Nothing, NoExtractor]] = __addMissingColumnsIfNotExistsWithDefaults(PartialFunction.empty[Field, Any])

  def __addMissingColumnsIfNotExistsWithDefaults(default: PartialFunction[Field, Any]): List[SQL[Nothing, NoExtractor]] = {
    fieldsList.map(field => {
      field.setAccessible(true)
      val dfltValue = if (default.isDefinedAt(field)) Some(default(field)) else None
      val defaultSQL: SQLSyntax = dfltValue.map(v => sqls"default " + valueToLiteral(v)).getOrElse(SQLSyntax.empty)
      val statement = sql"""ALTER TABLE ${SQLSyntax.createUnsafely(s"\"$tableName\"")} ADD COLUMN IF NOT EXISTS "${SQLSyntax.createUnsafely(fieldName(field))}" ${SQLSyntax.createUnsafely(fieldTypeToSQLType(field, field.getType, field.get(sampleRow)))} ${defaultSQL}""".stripMargin
      statement
    })
  }

  def __addMissingColumnsIfNotExistsWithDefaultsFromSampleRow(): List[SQL[Nothing, NoExtractor]] = {
    fieldsList.map(field => {
      field.setAccessible(true)
      val dfltValue = field.get(sampleRow)
      val defaultSQL: SQLSyntax = sqls"default " + valueToLiteral(dfltValue)
      val statement = sql"""ALTER TABLE ${SQLSyntax.createUnsafely(s"\"$tableName\"")} ADD COLUMN IF NOT EXISTS "${SQLSyntax.createUnsafely(fieldName(field))}" ${SQLSyntax.createUnsafely(fieldTypeToSQLType(field, field.getType, field.get(sampleRow)))} ${defaultSQL}""".stripMargin
      statement
    })
  }

  def __dropTableSQL: SQL[Nothing, NoExtractor] = SQL(s"""DROP TABLE IF EXISTS ${s"\"$tableName\""}""")

  def __dropAndCreateTableSQL: List[SQL[Nothing, NoExtractor]] = __dropTableSQL :: __createTableSQL

  def __truncateSQL: SQL[Nothing, NoExtractor] = SQL(s"""truncate ${s"\"$tableName\""}""")

  def __renameColumn(from: String, to: String): SQL[Nothing, NoExtractor] = sql"""ALTER TABLE ${SQLSyntax.createUnsafely(s"\"$tableName\"")} RENAME COLUMN ${SQLSyntax.createUnsafely(s"\"$from\"")} TO ${SQLSyntax.createUnsafely(s"\"$to\"")}"""

  def __truncateTableSQL: SQL[Nothing, NoExtractor] = SQL(s"""drop table ${s"\"$tableName\""};""")

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

  def insertFields: List[Field]

  def select(where: SQLSyntax, rest: SQLSyntax): List[Any]

  def delete(where: SQLSyntax, rest: SQLSyntax): Long

  def listFromQuery(query: SQLSyntax): List[Any]

  def selectSQL(prefix: Option[String] = None): SQLSyntax = SQLSyntax.join(fieldsList.map(f => {
    val name = fieldName(f)
    val column: SQLSyntax = sqls"$tableNameSQLSyntaxQuoted.${SQLSyntax.createUnsafely(name).doubleQuoted}"
    val alias: SQLSyntax = SQLSyntax.createUnsafely(prefix.map(prefix => '"' + prefix + '_' + name + '"').getOrElse('"' + name + '"'))
    if (f.getType == classOf[ComputedCol[?]]) {
      f.setAccessible(true)
      sqls"${f.get(sampleRow).asInstanceOf[ComputedCol[?]].sql} AS $alias"
    } else sqls"$column as $alias"
  }), sqls",", true)

  def selectFromSQL(prefix: Option[String] = None): SQLSyntax = sqls"""select ${selectSQL(prefix)} from $tableNameSQLSyntaxQuoted"""

  def deleteFromSQL: SQLSyntax = sqls"""delete from $tableNameSQLSyntaxQuoted"""

  def fromWrappedResultSet(rs: WrappedResultSet, prefix: Option[String]): Any = {
    val instance = createEmptyRowInternal()
    fieldsList.zipWithIndex.foreach({
      case (field, idx) =>
        field.setAccessible(true)
        setValue(rs, field, prefix, field.getType, instance)
    })
    instance
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
        throw new Exception(s"Missing value for field '${fieldName(field)}' in sample row for table '$tableName'")
      }
      fieldTypeToSQLType(field, cast.get.getClass, ???, columnConstrains - "not null")
    case "scala.Enumeration$Value" => "integer" + columnConstrains.mkString(" ", " ", "")

    case "scala.Enumeration$Val" => "integer" + columnConstrains.mkString(" ", " ", "")

    case "java.time.LocalDate" => "date" + columnConstrains.mkString(" ", " ", "")
    case "java.time.LocalTime" => "time without time zone" + columnConstrains.mkString(" ", " ", "")
    case "java.time.LocalDateTime" => "timestamp without time zone" + columnConstrains.mkString(" ", " ", "")
    case "java.time.OffsetDateTime" => "timestamp with time zone" + columnConstrains.mkString(" ", " ", "")

    case "java.time.Instant" => "timestamptz" + columnConstrains.mkString(" ", " ", "")

    case _ => throw new Exception(s"Unexpected field class ${clas.getSimpleName} for field ${field.getName}")
  }

  def setValue(rs: WrappedResultSet, field: java.lang.reflect.Field, prefix: Option[String], valueType: Class[?], instance: Any, nullable: Boolean = false): Unit = try {
    valueType.getName match {

      case "com.fastscala.db.computed.ComputedCol" =>
        val col = field.get(instance).asInstanceOf[ComputedCol[?]]
        col.getClass.getDeclaredFields.find(_.getName == "value").foreach(f => {
          f.setAccessible(true)
          f.set(col, col.read(fieldName(field, prefix))(rs))
        })

      case "java.lang.String" | "string" if nullable => field.set(instance, rs.stringOpt(fieldName(field, prefix)))
      case "java.lang.String" | "string" => field.set(instance, rs.string(fieldName(field, prefix)))

      case "java.lang.Long" | "long" if nullable => field.set(instance, rs.bigIntOpt(fieldName(field, prefix)).map(_.longValue()))
      case "java.lang.Long" | "long" => field.set(instance, rs.bigInt(fieldName(field, prefix)).longValue())

      case "java.lang.Character" | "char" if nullable => field.set(instance, rs.stringOpt(fieldName(field, prefix)).map(_.head))
      case "java.lang.Character" | "char" => field.set(instance, rs.string(fieldName(field, prefix)).head)

      case "java.lang.Integer" | "int" if nullable => field.set(instance, rs.intOpt(fieldName(field, prefix)))
      case "java.lang.Integer" | "int" => field.set(instance, rs.int(fieldName(field, prefix)))

      case "scala.Enumeration$Value" => field.set(instance, enumSampleToValue(field.get(instance), rs.int(fieldName(field, prefix))))

      case "scala.Enumeration$Val" if nullable => field.set(instance, rs.intOpt(fieldName(field, prefix)).map(i => enumSampleToValue(field.get(instance).asInstanceOf[Some[AnyRef]].get, i)))

      case "java.lang.Short" | "short" if nullable => field.set(instance, rs.shortOpt(fieldName(field, prefix)))
      case "java.lang.Short" | "short" => field.set(instance, rs.short(fieldName(field, prefix)))

      case "java.lang.Float" | "float" if nullable => field.set(instance, rs.floatOpt(fieldName(field, prefix)))
      case "java.lang.Float" | "float" => field.set(instance, rs.float(fieldName(field, prefix)))

      case "java.lang.Double" | "double" if nullable => field.set(instance, rs.doubleOpt(fieldName(field, prefix)))
      case "java.lang.Double" | "double" => field.set(instance, rs.double(fieldName(field, prefix)))

      case "java.math.BigDecimal" if nullable => field.set(instance, rs.bigDecimalOpt(fieldName(field, prefix)))
      case "java.math.BigDecimal" => field.set(instance, rs.bigDecimal(fieldName(field, prefix)))

      case "scala.math.BigDecimal" if nullable => field.set(instance, rs.bigDecimalOpt(fieldName(field, prefix)).map(scala.math.BigDecimal(_)))
      case "scala.math.BigDecimal" => field.set(instance, rs.bigDecimal(fieldName(field, prefix)): scala.math.BigDecimal)

      case "java.lang.Boolean" | "boolean" if nullable => field.set(instance, rs.booleanOpt(fieldName(field, prefix)))
      case "java.lang.Boolean" | "boolean" => field.set(instance, rs.boolean(fieldName(field, prefix)))

      case "[B" if nullable => field.set(instance, rs.bytesOpt(fieldName(field, prefix)))
      case "[B" => field.set(instance, rs.bytes(fieldName(field, prefix)))

      case "java.time.LocalDate" if nullable => field.set(instance, rs.localDateOpt(fieldName(field, prefix)))
      case "java.time.LocalDate" => field.set(instance, rs.localDate(fieldName(field, prefix)))

      case "java.time.LocalTime" if nullable => field.set(instance, rs.localTimeOpt(fieldName(field, prefix)))
      case "java.time.LocalTime" => field.set(instance, rs.localTime(fieldName(field, prefix)))

      case "java.time.LocalDateTime" if nullable => field.set(instance, rs.localDateTimeOpt(fieldName(field, prefix)))
      case "java.time.LocalDateTime" => field.set(instance, rs.localDateTime(fieldName(field, prefix)))

      case "java.time.OffsetDateTime" if nullable => field.set(instance, rs.offsetDateTimeOpt(fieldName(field, prefix)))
      case "java.time.OffsetDateTime" => field.set(instance, rs.offsetDateTime(fieldName(field, prefix)))

      case "java.time.Instant" if nullable => field.set(instance, rs.timestampOpt(fieldName(field, prefix)).map(_.toInstant))
      case "java.time.Instant" => field.set(instance, rs.timestamp(fieldName(field, prefix)).toInstant)

      case "java.util.UUID" if nullable => field.set(instance, rs.stringOpt(fieldName(field, prefix)).map(UUID.fromString))
      case "java.util.UUID" => field.set(instance, UUID.fromString(rs.string(fieldName(field, prefix))))

      case "io.circe.Json" if nullable => field.set(instance, rs.stringOpt(fieldName(field, prefix)).map(str => io.circe.parser.parse(str).right.get))
      case "io.circe.Json" => field.set(instance, io.circe.parser.parse(rs.string(fieldName(field, prefix))).right.get)

      case "scala.Option" =>
        if (field.get(instance).isInstanceOf[None.type]) throw new Exception(s"Missing sample value for optional column ${fieldName(field)}")
        setValue(rs, field, prefix, field.get(instance).asInstanceOf[Some[Any]].get.getClass, instance, true)

      case unknown => throw new Exception(s"Unknown value type $unknown of field ${fieldName(field)}")
    }
  } catch {
    case ex: Exception =>
      throw new Exception(s"Exception setting value of field ${fieldName(field)} of type ${valueType.getName}", ex)
  }

  def valueBatchPlaceholder(field: Field, value: Any): String = value match {
    case v: java.util.UUID => "?::UUID"
    case _ => "?"
  }

  def valueToBatchObject(field: Field, value: Any): Object = value match {
    case null => null
    case None => null
    case Some(value) => valueToBatchObject(field, value)
    case v: Int => java.lang.Integer(v)
    case v: Double => java.lang.Double(v)
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
    case v: java.util.UUID => v.toString
  }

  def valueToFragment(field: Field, value: Any): SQLSyntax = value match {
    case null => sqls"null"
    case None => sqls"null"
    case Some(value) => valueToFragment(field, value)
    case Array() => sqls"''::bytea"
    case v: Int => sqls"$v"
    case v: Double => sqls"$v"
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
    case v: java.util.UUID => SQLSyntax.createUnsafely(s"'${v.toString}'::UUID")
    case v: io.circe.Json => sqls"${v.noSpaces}::json"
  }

  def valueToLiteral(value: Any): SQLSyntax = value match {
    case null => SQLSyntax.createUnsafely("null")
    case None => SQLSyntax.createUnsafely("null")
    case Some(value) => valueToLiteral(value)
    case Array() => SQLSyntax.createUnsafely("''::bytea")
    case v: Int => SQLSyntax.createUnsafely(v + "::integer")
    case v: Double => SQLSyntax.createUnsafely(v + "::double precision")
    case v: scala.math.BigDecimal => SQLSyntax.createUnsafely(v.toString() + "::numeric")
    case v: Boolean => SQLSyntax.createUnsafely(v.toString + "::boolean")
    case v: Char => SQLSyntax.createUnsafely(v + "::char")
    case v: Short => SQLSyntax.createUnsafely(v + "::integer")
    case v: Float => SQLSyntax.createUnsafely(v + "::real")
    case v: Long => SQLSyntax.createUnsafely(v + "::bigint")
    case v: String => SQLSyntax.createUnsafely("'" + org.postgresql.core.Utils.escapeLiteral(null, v, true) + "'" + "::text")
    case v: Array[Byte] => ???
    case v: Enumeration#Value => valueToLiteral(v.id)
    case v: java.time.LocalDate => SQLSyntax.createUnsafely("'" + v.format(DateTimeFormatter.ISO_LOCAL_DATE) + "'")
    case v: java.time.LocalTime => SQLSyntax.createUnsafely(v.format(DateTimeFormatter.ISO_LOCAL_TIME))
    case v: java.time.LocalDateTime => SQLSyntax.createUnsafely(v.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    case v: java.time.OffsetDateTime => ???
    case v: java.time.Instant => ???
    case v: io.circe.Json => SQLSyntax.createUnsafely(s"'${v.noSpaces}'::json")
  }

}

