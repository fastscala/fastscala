package com.fastscala.db

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

trait TableBase extends DataTypeSupport {

  private val logger = LoggerFactory.getLogger(getClass.getName)

  def createSampleRow(): Any

  def createEmptyRow(): Any

  def createSampleRowInternal(): Any

  def createEmptyRowInternal(): Any

  def sampleRow: Any

  val transientAnnotations = Set("java.beans.Transient", "scala.transient", "com.fastscala.db.annotations.NotAColumn")

  val fieldsList: List[Field] = sampleRow.getClass.getDeclaredFields.iterator.filter({
    case field =>
      !field.getName.contains("$") &&
        !field.getAnnotations.exists(anno => transientAnnotations.contains(anno.annotationType().getName))
  }).toList

  def tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, getClass.getSimpleName.replaceAll("\\$$", ""))

  override def toString: String = tableName

  def tableNameSQLSyntax = SQLSyntax.createUnsafely(tableName)

  def tableNameSQLSyntaxQuoted = tableNameSQLSyntax.doubleQuoted

  private case class ForeignKeyConstraint(constraintName: SQLSyntax, column: SQLSyntax, referencesTable: SQLSyntax, referencesColumn: SQLSyntax, onUpdate: SQLSyntax, onDelete: SQLSyntax)

  private case class UniqueConstraint(constraintName: SQLSyntax, columns: Seq[SQLSyntax])

  private def foreignKeyConstraints: List[ForeignKeyConstraint] = (sampleRow.getClass.getDeclaredConstructors.flatMap(cons => {
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
      val fkName = SQLSyntax.createUnsafely("fk_" + columnNameForField(name)).doubleQuoted
      val column = SQLSyntax.createUnsafely(columnNameForField(name)).doubleQuoted
      val referencesTable = SQLSyntax.createUnsafely(fk.refTbl()).doubleQuoted
      val referencesColumn = SQLSyntax.createUnsafely(fk.refCol()).doubleQuoted
      val onUpdate = SQLSyntax.createUnsafely(fk.onUpdate().Sql)
      val onDelete = SQLSyntax.createUnsafely(fk.onDelete().Sql)
      ForeignKeyConstraint(fkName, column, referencesTable, referencesColumn, onUpdate, onDelete)
  })

  private def uniqueConstraints: List[UniqueConstraint] = (sampleRow.getClass.getDeclaredConstructors.flatMap(cons => {
    cons.getParameters.flatMap(param => {
      param.getDeclaredAnnotations.collect({
        case unq: Unique => param.getName -> unq
      })
    })
  }) ++ sampleRow.getClass.getDeclaredFields.flatMap(f => {
    f.getDeclaredAnnotations.collect({
      case unq: Unique => f.getName -> unq
    })
  })).toList match {
    case Nil => Nil
    case uniqueCols =>
      val constraintName = SQLSyntax.createUnsafely("unq_" + uniqueCols.map(col => columnNameForField(col._1)).mkString("_")).doubleQuoted
      List(UniqueConstraint(constraintName, uniqueCols.map(col => SQLSyntax.createUnsafely(columnNameForField(col._1)).doubleQuoted)))
  }

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
    foreignKeyConstraints.map({
      case ForeignKeyConstraint(constraintName, _, _, _, _, _) =>
        sql"""ALTER TABLE $tableNameSQLSyntaxQuoted DROP CONSTRAINT IF EXISTS $constraintName;"""
    })
  }

  def __dropUniqueConstrainsSQL: List[SQL[Nothing, NoExtractor]] = {
    uniqueConstraints.map({
      case UniqueConstraint(constraintName, _) =>
        sql"""ALTER TABLE $tableNameSQLSyntaxQuoted DROP CONSTRAINT IF EXISTS $constraintName;"""
    })
  }

  def __createForeignKeyConstrainsSQL: List[SQL[Nothing, NoExtractor]] = {
    foreignKeyConstraints.map({
      case ForeignKeyConstraint(constraintName, column, referencesTable, referencesColumn, onUpdate, onDelete) =>
        sql"""ALTER TABLE $tableNameSQLSyntaxQuoted ADD CONSTRAINT $constraintName FOREIGN KEY ($column) REFERENCES $referencesTable ($referencesColumn) ON UPDATE $onUpdate ON DELETE $onDelete;"""
    })
  }

  def __createUniqueConstrainsSQL: List[SQL[Nothing, NoExtractor]] = {
    uniqueConstraints.map({
      case UniqueConstraint(constraintName, columns) =>
        sql"""CREATE UNIQUE INDEX $constraintName ON $tableNameSQLSyntaxQuoted (${SQLSyntax.join(columns, sqls",")});"""
    })
  }

  def __createPrimaryKeyConstrainsSQL: List[SQL[Nothing, NoExtractor]] = {
    if (primaryKeys.isEmpty) Nil
    else {
      val pkeyName = SQLSyntax.createUnsafely(tableName + "_pkey").doubleQuoted
      val cols = primaryKeys.map({ case (name, _) => SQLSyntax.createUnsafely(columnNameForField(name)).doubleQuoted })
      List(sql"""ALTER TABLE $tableNameSQLSyntaxQuoted ADD CONSTRAINT $pkeyName PRIMARY KEY (${SQLSyntax.join(cols, sqls",")});""")
    }
  }

  def __createTableSQL: List[SQL[Nothing, NoExtractor]] =
    __createTableOnlySQL :::
      __createPrimaryKeyConstrainsSQL :::
      __createForeignKeyConstrainsSQL :::
      __createUniqueConstrainsSQL

  def __createTableOnlySQL: List[SQL[Nothing, NoExtractor]] = {
    val columns: List[SQLSyntax] = fieldsList.map(field => {
      field.setAccessible(true)
      SQLSyntax.createUnsafely(s""""${columnNameForField(field)}" ${fieldTypeToSQLType(field, field.getType, field.get(sampleRow))}""")
    })

    List(sql"""CREATE TABLE IF NOT EXISTS $tableNameSQLSyntaxQuoted ($columns)""")
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
      val statement = sql"""ALTER TABLE ${SQLSyntax.createUnsafely(s"\"$tableName\"")} ADD COLUMN IF NOT EXISTS "${SQLSyntax.createUnsafely(columnNameForField(field))}" ${SQLSyntax.createUnsafely(fieldTypeToSQLType(field, field.getType, field.get(sampleRow)))} ${defaultSQL}""".stripMargin
      statement
    })
  }

  def __addMissingColumnsIfNotExistsWithDefaultsFromSampleRow(): List[SQL[Nothing, NoExtractor]] = {
    fieldsList.map(field => {
      field.setAccessible(true)
      val dfltValue = field.get(sampleRow)
      val defaultSQL: SQLSyntax = sqls"default " + valueToLiteral(dfltValue)
      val statement = sql"""ALTER TABLE ${SQLSyntax.createUnsafely(s"\"$tableName\"")} ADD COLUMN IF NOT EXISTS "${SQLSyntax.createUnsafely(columnNameForField(field))}" ${SQLSyntax.createUnsafely(fieldTypeToSQLType(field, field.getType, field.get(sampleRow)))} ${defaultSQL}""".stripMargin
      statement
    })
  }

  def __dropTableSQL: List[SQL[Nothing, NoExtractor]] = List(sql"""DROP TABLE IF EXISTS $tableNameSQLSyntaxQuoted;""")

  def __dropAndCreateTableSQL: List[SQL[Nothing, NoExtractor]] = __dropTableSQL ::: __createTableSQL

  def __truncateSQL: SQL[Nothing, NoExtractor] = sql"""truncate $tableNameSQLSyntaxQuoted;"""

  def __renameColumn(from: String, to: String): SQL[Nothing, NoExtractor] = sql"""ALTER TABLE ${SQLSyntax.createUnsafely(s"\"$tableName\"")} RENAME COLUMN ${SQLSyntax.createUnsafely(s"\"$from\"")} TO ${SQLSyntax.createUnsafely(s"\"$to\"")}"""

  def insertFields: List[Field]

  def select(where: SQLSyntax, rest: SQLSyntax): List[Any]

  def delete(where: SQLSyntax, rest: SQLSyntax): Long

  def listFromQuery(query: SQLSyntax): List[Any]

  def selectSQL(prefix: Option[String] = None): SQLSyntax = SQLSyntax.join(fieldsList.map(f => {
    val name = columnNameForField(f)
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
}

