package com.fastscala.db

import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax

import java.lang.reflect.Field

// This is just for testing. Consider using cats.effect.IOApp instead of calling
// unsafe methods directly.
import scalikejdbc.*

trait Table[R] extends TableBase {

  private val logger = LoggerFactory.getLogger(getClass.getName)

  def createSampleRow(): R

  def createEmptyRow(): R = createSampleRow()

  def createSampleRowInternal(): R = createSampleRow()

  def createEmptyRowInternal(): R = createSampleRowInternal()

  lazy val sampleRow = createSampleRowInternal()

  def copyRow(from: R, to: R): Unit = {
    fieldsList.foreach(field => {
      field.setAccessible(true)
      field.set(to, field.get(from))
    })
  }

  def insertFields: List[Field] = fieldsList

  def upsertFields: List[Field] = fieldsList

  def updateFields: List[Field] = fieldsList

  def insertSQL(row: R, rest: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = {
    val columns: SQLSyntax = SQLSyntax.createUnsafely(insertFields.map(field => {
      field.setAccessible(true)
      fieldName(field)
    }).map('"' + _ + '"').mkString("(", ",", ")"))

    val values: List[SQLSyntax] = insertFields.map(field => {
      field.setAccessible(true)
      valueToFragment(field, field.get(row))
    })

    sql"""insert into $tableNameSQLSyntaxQuoted $columns VALUES ($values) $rest"""
  }

  def updateSQL(row: R, where: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = {
    val values: SQLSyntax = updateFields.map(field => {
      field.setAccessible(true)
      sqls""""${SQLSyntax.createUnsafely(fieldName(field))}" = """ + valueToFragment(field, field.get(row))
    }).reduceOption(_ + sqls", " + _).getOrElse(SQLSyntax.empty)

    sql"""update $tableNameSQLSyntaxQuoted set $values $where"""
  }

  def deleteSQL(row: R, where: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = sql"""delete from $tableNameSQLSyntaxQuoted $where"""

  def selectAll(): List[R] = DB.readOnly({ implicit session => _selectAll() })

  def _selectAll()(implicit session: DBSession): List[R] = _select(SQLSyntax.empty)

  def select(where: SQLSyntax = SQLSyntax.empty, rest: SQLSyntax = SQLSyntax.empty): List[R] = DB.readOnly({ implicit session => _select(where, rest) })

  def _select(where: SQLSyntax = SQLSyntax.empty, rest: SQLSyntax = SQLSyntax.empty)(implicit session: DBSession): List[R] = {
    val query = selectFromSQL.where(Some(where).filter(_ != SQLSyntax.empty)).append(rest)
    try {
      sql"${query}".map(fromWrappedResultSet).list()
    } catch {
      case ex: PSQLException =>
        logger.error(s"Error on query $query", ex)
        throw ex
    }
  }

  def count(where: SQLSyntax = SQLSyntax.empty): Long = DB.readOnly({ implicit session => _count(where) })

  def _count(where: SQLSyntax = SQLSyntax.empty)(implicit session: DBSession): Long = {
    val query = sqls"""select count(*) from $tableNameSQLSyntaxQuoted""".where(Some(where).filter(_ != SQLSyntax.empty))
    try {
      sql"${query}".map(fromWrappedResultSet).map(_.long(1)).list().head
    } catch {
      case ex: PSQLException =>
        logger.error(s"Error on query $query", ex)
        throw ex
    }
  }

  def delete(rest: SQLSyntax): Long = DB.localTx({ implicit session => _delete(rest) })

  def _delete(rest: SQLSyntax)(implicit session: DBSession): Long = {
    val query = deleteFrom.append(rest)
    sql"${query}".map(fromWrappedResultSet).executeUpdate().longValue()
  }

  def listFromQuery(query: SQLSyntax): List[R] = DB.readOnly({ implicit session => _listFromQuery(query) })

  def _listFromQuery(query: SQLSyntax)(implicit session: DBSession): List[R] = {
    sql"${query}".map(fromWrappedResultSet).list()
  }

  override def fromWrappedResultSet(rs: WrappedResultSet): R = super.fromWrappedResultSet(rs).asInstanceOf[R]
}
