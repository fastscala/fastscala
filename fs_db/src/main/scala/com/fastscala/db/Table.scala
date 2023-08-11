package com.fastscala.db

import com.google.common.base.CaseFormat
import scalikejdbc.interpolation.SQLSyntax

import java.lang.reflect.Field

// This is just for testing. Consider using cats.effect.IOApp instead of calling
// unsafe methods directly.
import scalikejdbc._

import java.util.UUID

trait Table[R] {

  def createSampleRow(): R

  def createEmptyRow(): R = createSampleRow()

  def createSampleRowInternal(): R = createSampleRow()

  def createEmptyRowInternal(): R = createSampleRowInternal()

  lazy val sampleRow = createSampleRowInternal()

  val fieldsList = sampleRow.getClass.getDeclaredFields.iterator.filter({
    case field => !field.getAnnotations.exists(anno => anno.annotationType().getName == "java.beans.Transient")
  }).toList

  def tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, sampleRow.getClass.getSimpleName)

  def tableNameSQLSyntax = SQLSyntax.createUnsafely(tableName)

  def fieldName(field: java.lang.reflect.Field) = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName)

  def fieldTypeToSQLType(
                          field: java.lang.reflect.Field,
                          clas: Class[_],
                          value: => Any,
                          append: String = " not null"
                        ): String = clas.getName match {
    case "java.lang.Byte" => "integer" + append
    case "byte" => "integer" + append

    case "java.lang.Short" => "integer" + append
    case "short" => "integer" + append

    case "java.lang.Integer" => "integer" + append
    case "int" => "integer" + append

    case "java.lang.Long" => "bigint" + append
    case "long" => "bigint" + append

    case "java.lang.Float" => "real" + append
    case "float" => "real" + append

    case "java.lang.Double" => "double precision" + append
    case "double" => "double precision" + append

    case "java.lang.Character" => "char" + append
    case "char" => "char" + append

    case "java.lang.String" => "text" + append

    case "java.lang.Boolean" => "boolean" + append
    case "boolean" => "boolean" + append

    case "java.util.UUID" => "UUID" + append

    case "[B" => "bytea" + append

    case "scala.Option" =>
      val cast = value.asInstanceOf[Option[Any]]
      if (cast.isEmpty) {
        throw new Exception(s"Missing value for field '${fieldName(field)}' in sample row for table '$tableName'")
      }
      fieldTypeToSQLType(field, cast.get.getClass, ???, append.replaceAll(" not null", ""))
    case "scala.Enumeration$Value" => "integer" + append

    case "scala.Enumeration$Val" => "integer" + append

    case _ => throw new Exception(s"Unexpected field class ${clas.getSimpleName} for field ${field.getName}")
  }

  def __createTableSQL: SQL[Nothing, NoExtractor] = {
    val columns: String = fieldsList.map(field => {
      field.setAccessible(true)
      s"""${fieldName(field)} ${fieldTypeToSQLType(field, field.getType, field.get(sampleRow))}"""
    }).mkString("(", ",", ")")

    SQL(s"""create table ${s"\"$tableName\""} $columns""")
  }

  def __addColumnsIfNotExists: List[SQL[Nothing, NoExtractor]] = {
    fieldsList.map(field => {
      field.setAccessible(true)
      SQL(s"""ALTER TABLE ${s"\"$tableName\""} ADD COLUMN IF NOT EXISTS ${fieldName(field)} ${fieldTypeToSQLType(field, field.getType, field.get(sampleRow))};""")
    })
  }

  def __dropTableSQL: SQL[Nothing, NoExtractor] = SQL(s"""drop table ${s"\"$tableName\""}""")

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
    case v: java.util.UUID => sqls"${v.toString}::UUID"
    case v: Array[Byte] => sqls"$v"
    case v: Enumeration#Value => sqls"${v.id}"
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

      case "java.util.UUID" if nullable => field.set(instance, rs.stringOpt(idx).map(UUID.fromString))
      case "java.util.UUID" => field.set(instance, UUID.fromString(rs.string(idx)))

      case "[B" if nullable => field.set(instance, rs.bytesOpt(idx))
      case "[B" => field.set(instance, rs.bytes(idx))

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
      sql"${query}".map(fromWrappedResultSet).list()()
    })
  }

  def listFromQuery(query: SQLSyntax): List[R] = {
    DB.readOnly({ implicit session =>
      sql"${query}".map(fromWrappedResultSet).list()()
    })
  }

  def select: SQLSyntax = sqls"""select ${SQLSyntax.createUnsafely(fieldsList.map(fieldName).map('"' + _ + '"').mkString(", "))}"""

  def selectFrom: SQLSyntax = sqls"""$select from "$tableNameSQLSyntax""""

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
