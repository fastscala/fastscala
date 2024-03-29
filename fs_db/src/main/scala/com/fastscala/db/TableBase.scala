package com.fastscala.db

import com.google.common.base.CaseFormat
import scalikejdbc.interpolation.SQLSyntax

import java.lang.reflect.Field

// This is just for testing. Consider using cats.effect.IOApp instead of calling
// unsafe methods directly.
import scalikejdbc._

import java.util.UUID

trait TableBase[R] {

  def createSampleRow(): R

  def createEmptyRow(): R = createSampleRow()

  def createSampleRowInternal(): R = createSampleRow()

  def createEmptyRowInternal(): R = createSampleRowInternal()

  lazy val sampleRow = createSampleRowInternal()

  val fieldsList: List[Field] = sampleRow.getClass.getDeclaredFields.iterator.filter({
    case field => !field.getAnnotations.exists(anno => Set("java.beans.Transient", "scala.transient").contains(anno.annotationType().getName))
  }).toList

  def tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, sampleRow.getClass.getSimpleName)

  def tableNameSQLSyntax = SQLSyntax.createUnsafely(tableName)

  def fieldName(field: java.lang.reflect.Field) = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName)

  def fieldTypeToSQLType(
                          field: java.lang.reflect.Field,
                          clas: Class[_],
                          value: => Any,
                          columnConstrains: Set[String] = Set("not null")
                        ): String = clas.getName match {
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

    case _ => throw new Exception(s"Unexpected field class ${clas.getSimpleName} for field ${field.getName}")
  }

  def __createTableSQL: SQL[Nothing, NoExtractor] = {
    val columns: String = fieldsList.map(field => {
      field.setAccessible(true)
      s""""${fieldName(field)}" ${fieldTypeToSQLType(field, field.getType, field.get(sampleRow))}"""
    }).mkString("(", ",", ")")

    SQL(s"""create table ${s"\"$tableName\""} $columns""")
  }

  def __addMissingColumnsIfNotExists: List[SQL[Nothing, NoExtractor]] = __addMissingColumnsIfNotExistsWithDefaults(PartialFunction.empty[Field, Any])

  def __addMissingColumnsIfNotExistsWithDefaults(default: PartialFunction[Field, Any]): List[SQL[Nothing, NoExtractor]] = {
    fieldsList.map(field => {
      field.setAccessible(true)
      val isNullable = field.getType.getName != "scala.Option" && default.isDefinedAt(field)
      val defaultSQL: SQLSyntax = if (isNullable) {
        val defaultStr = default(field) match {
          case Array() => "''::bytea"
          case other => other.toString
        }
        SQLSyntax.createUnsafely(s" default $defaultStr")
      } else sqls""
      val statement =
        sql"""ALTER TABLE ${SQLSyntax.createUnsafely(s"\"$tableName\"")}
           ADD COLUMN IF NOT EXISTS "${SQLSyntax.createUnsafely(fieldName(field))}"
           ${SQLSyntax.createUnsafely(fieldTypeToSQLType(field, field.getType, field.get(sampleRow)))}
           ${defaultSQL}"""
      statement
    })
  }

  def __dropTableSQL: SQL[Nothing, NoExtractor] = SQL(s"""drop table ${s"\"$tableName\""}""")

  def __truncateSQL: SQL[Nothing, NoExtractor] = SQL(s"""truncate ${s"\"$tableName\""}""")

  def __renameColumn(from: String, to: String): SQL[Nothing, NoExtractor] = sql"""ALTER TABLE ${s"\"$tableName\""} RENAME COLUMN ${s"\"$from\""} TO ${s"\"$to\""}"""

  def __truncateTableSQL: SQL[Nothing, NoExtractor] = SQL(s"""drop table ${s"\"$tableName\""};""")

  def valueToFragment(value: Any): SQLSyntax = value match {
    case null => sqls"null"
    case None => sqls"null"
    case Some(value) => valueToFragment(value)
    case v: Int => sqls"$v"
    case v: Double => sqls"$v"
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
  }

  private def enumSampleToValue(sample: AnyRef, id: Int): AnyRef = sample match {
    case sample: scala.Enumeration#Value =>
      val sampleClass = sample.getClass
      val field = sampleClass.getField("$outer")
      field.setAccessible(true)
      val enum = field.get(sample)
      enum.asInstanceOf[scala.Enumeration](id)
  }

  def setValue(rs: WrappedResultSet, idx: Int, field: java.lang.reflect.Field, valueType: Class[_], instance: R, nullable: Boolean = false): Unit = try {
    valueType.getName match {

      case "java.lang.String" | "string" if nullable => field.set(instance, rs.stringOpt(idx))
      case "java.lang.String" | "string" => field.set(instance, rs.string(idx))

      case "java.lang.Long" | "long" if nullable => field.set(instance, rs.bigIntOpt(idx).map(_.longValue()))
      case "java.lang.Long" | "long" => field.set(instance, rs.bigInt(idx).longValue())

      case "java.lang.Character" | "char" if nullable => field.set(instance, rs.stringOpt(idx).map(_.head))
      case "java.lang.Character" | "char" => field.set(instance, rs.string(idx).head)

      case "java.lang.Integer" | "int" if nullable => field.set(instance, rs.intOpt(idx))
      case "java.lang.Integer" | "int" => field.set(instance, rs.int(idx))

      case "scala.Enumeration$Value" => field.set(instance, enumSampleToValue(field.get(instance), rs.int(idx)))

      case "scala.Enumeration$Val" if nullable => field.set(instance, rs.intOpt(idx).map(i => enumSampleToValue(field.get(instance).asInstanceOf[Some[AnyRef]].get, i)))

      case "java.lang.Short" | "short" if nullable => field.set(instance, rs.shortOpt(idx))
      case "java.lang.Short" | "short" => field.set(instance, rs.short(idx))

      case "java.lang.Float" | "float" if nullable => field.set(instance, rs.floatOpt(idx))
      case "java.lang.Float" | "float" => field.set(instance, rs.float(idx))

      case "java.lang.Double" | "double" if nullable => field.set(instance, rs.doubleOpt(idx))
      case "java.lang.Double" | "double" => field.set(instance, rs.double(idx))

      case "java.lang.Boolean" | "boolean" if nullable => field.set(instance, rs.booleanOpt(idx))
      case "java.lang.Boolean" | "boolean" => field.set(instance, rs.boolean(idx))

      case "[B" if nullable => field.set(instance, rs.bytesOpt(idx))
      case "[B" => field.set(instance, rs.bytes(idx))

      case "java.time.LocalDate" if nullable => field.set(instance, rs.localDateOpt(idx))
      case "java.time.LocalDate" => field.set(instance, rs.localDate(idx))

      case "java.time.LocalTime" if nullable => field.set(instance, rs.localTimeOpt(idx))
      case "java.time.LocalTime" => field.set(instance, rs.localTime(idx))

      case "java.time.LocalDateTime" if nullable => field.set(instance, rs.localDateTimeOpt(idx))
      case "java.time.LocalDateTime" => field.set(instance, rs.localDateTime(idx))

      case "java.time.OffsetDateTime" if nullable => field.set(instance, rs.offsetDateTimeOpt(idx))
      case "java.time.OffsetDateTime" => field.set(instance, rs.offsetDateTime(idx))

      case "scala.Option" =>
        if (field.get(instance).isInstanceOf[None.type]) throw new Exception(s"Missing sample value for optional column ${fieldName(field)}")
        setValue(rs, idx, field, field.get(instance).asInstanceOf[Some[Any]].get.getClass, instance, true)

      case unknown => throw new Exception(s"Unknown value type $unknown of field ${fieldName(field)}")
    }
  } catch {
    case ex: Exception =>
      throw new Exception(s"Exception setting value of field ${fieldName(field)} of type ${valueType.getName}", ex)
  }

  def insertFields: List[Field] = fieldsList

  def insertSQL(row: R): SQL[Nothing, NoExtractor] = {
    val columns: SQLSyntax = SQLSyntax.createUnsafely(insertFields.map(field => {
      field.setAccessible(true)
      fieldName(field)
    }).map('"' + _ + '"').mkString("(", ",", ")"))

    val values: List[SQLSyntax] = insertFields.map(field => {
      field.setAccessible(true)
      valueToFragment(field.get(row))
    })

    sql"""insert into "$tableNameSQLSyntax" $columns VALUES ($values)"""
  }

  def updateSQL(row: R, where: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = {
    val values: SQLSyntax = insertFields.map(field => {
      field.setAccessible(true)
      sqls""""${SQLSyntax.createUnsafely(fieldName(field))}" = """ + valueToFragment(field.get(row))
    }).reduceOption(_ + sqls", " + _).getOrElse(SQLSyntax.empty)

    sql"""update "$tableNameSQLSyntax" set $values $where"""
  }

  def deleteSQL(row: R, where: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = sql"""delete from "$tableNameSQLSyntax" $where"""

  def listAll(): List[R] = list(SQLSyntax.empty)

  def list(rest: SQLSyntax): List[R] = {
    DB.readOnly({ implicit session =>
      val query = selectFrom.append(rest)
      sql"${query}".map(fromWrappedResultSet).list()
    })
  }

  def delete(rest: SQLSyntax): Long = {
    DB.localTx({ implicit session =>
      val query = deleteFrom.append(rest)
      sql"${query}".map(fromWrappedResultSet).executeUpdate().longValue()
    })
  }

  def listFromQuery(query: SQLSyntax): List[R] = {
    DB.readOnly({ implicit session =>
      sql"${query}".map(fromWrappedResultSet).list()
    })
  }

  def select: SQLSyntax = sqls"""select ${SQLSyntax.createUnsafely(fieldsList.map(fieldName).map('"' + _ + '"').mkString(", "))}"""

  def selectFrom: SQLSyntax = sqls"""$select from "$tableNameSQLSyntax""""

  def deleteFrom: SQLSyntax = sqls"""delete from "$tableNameSQLSyntax""""

  def fromWrappedResultSet(rs: WrappedResultSet): R = {
    val instance = createEmptyRowInternal()
    fieldsList.zipWithIndex.foreach({
      case (field, idx) =>
        field.setAccessible(true)
        setValue(rs, idx + 1, field, field.getType, instance)
    })
    instance
  }
}
