package com.fastscala.db

import com.fastscala.db.computed.ComputedCol
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax

import java.lang.reflect.Field
import scala.util.chaining.scalaUtilChainingOps

// This is just for testing. Consider using cats.effect.IOApp instead of calling
// unsafe methods directly.
import scalikejdbc.*

trait Table[R] extends TableBase {

  private val logger = LoggerFactory.getLogger(getClass.getName)

  def createSampleRow(): R

  def createEmptyRow(): R = createSampleRow()

  def createSampleRowInternal(): R = createSampleRow()

  def createEmptyRowInternal(): R = createSampleRowInternal()

  lazy val sampleRow: R = createSampleRowInternal()

  def copyRow(from: R, to: R): Unit = {
    fieldsList.foreach(field => {
      field.setAccessible(true)
      field.set(to, field.get(from))
    })
  }

  def insertFields: List[Field] = fieldsList.filter(_.getType.getName != "com.fastscala.db.computed.ComputedCol")

  def upsertFields: List[Field] = fieldsList.filter(_.getType.getName != "com.fastscala.db.computed.ComputedCol")

  def updateFields: List[Field] = fieldsList.filter(_.getType.getName != "com.fastscala.db.computed.ComputedCol")

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

  //  def insertMultipleSQL(rows: Seq[R], rest: SQLSyntax = SQLSyntax.empty): SQLBatch = {
  //    val columns: SQLSyntax = SQLSyntax.createUnsafely(upsertFields.map(field => {
  //      field.setAccessible(true)
  //      fieldName(field)
  //    }).map('"' + _ + '"').mkString("(", ",", ")"))
  //
  //    val values: Seq[List[Object]] = rows.map(row => upsertFields.map(field => {
  //      field.setAccessible(true)
  //      valueToBatchObject(field, field.get(row))
  //    }))
  //
  //    println(s"columns: $columns")
  //    val placeholders = SQLSyntax.createUnsafely(upsertFields.map(f => valueBatchPlaceholder(f, f.get(sampleRow))).mkString(","))
  //    println(s"placeholders: $placeholders")
  //    sql"""insert into $tableNameSQLSyntaxQuoted $columns VALUES ($placeholders) $rest""".batchAndReturnGeneratedKey(
  //      values *
  //    )
  //  }

  def updateSQL(row: R, where: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = {
    val values: SQLSyntax = updateFields.map(field => {
      field.setAccessible(true)
      sqls""""${SQLSyntax.createUnsafely(fieldName(field))}" = """ + valueToFragment(field, field.get(row))
    }).reduceOption(_ + sqls", " + _).getOrElse(SQLSyntax.empty)

    sql"""update $tableNameSQLSyntaxQuoted set $values $where"""
  }

  def deleteSQL(row: R, where: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = sql"""delete from $tableNameSQLSyntaxQuoted $where"""

  def selectAll(): List[R] = DB.readOnly({ implicit session => _selectAll() })

  def _selectAll()(implicit session: DBSession): List[R] = _selectWithAdditionalCols(SQLSyntax.empty).map(_._1)

  def select(where: SQLSyntax = SQLSyntax.empty, rest: SQLSyntax = SQLSyntax.empty): List[R] = DB.readOnly({ implicit session => _selectWithAdditionalCols(where, rest, Nil).map(_._1) })

  def selectWithAdditionalCols(where: SQLSyntax, additionalCols: List[(SQLSyntax, WrappedResultSet => Any)], rest: SQLSyntax= SQLSyntax.empty): List[(R, List[Any])] = DB.readOnly({ implicit session => _selectWithAdditionalCols(where, rest, additionalCols) })

  def _selectWithAdditionalCols(where: SQLSyntax = SQLSyntax.empty, rest: SQLSyntax = SQLSyntax.empty, additionalCols: List[(SQLSyntax, WrappedResultSet => Any)] = Nil)(implicit session: DBSession): List[(R, List[Any])] = {
    val colsSql = additionalCols match {
      case Nil => selectSQL(None)
      case additionalCols => SQLSyntax.join(selectSQL(None) :: additionalCols.map(_._1), sqls",")
    }
    val query =
      sqls"""select $colsSql from $tableNameSQLSyntaxQuoted"""
        .where(Some(where).filter(_ != SQLSyntax.empty)).append(rest)
    try {
      sql"${query}".map(rs => (fromWrappedResultSet(rs, None), additionalCols.map(_._2(rs)))).list()
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
      sql"${query}".map(rs => fromWrappedResultSet(rs, None)).map(_.long(1)).list().head
    } catch {
      case ex: PSQLException =>
        logger.error(s"Error on query $query", ex)
        throw ex
    }
  }

  def delete(where: SQLSyntax = SQLSyntax.empty, rest: SQLSyntax = SQLSyntax.empty): Long = DB.localTx({ implicit session => _delete(where, rest) })

  def _delete(where: SQLSyntax, rest: SQLSyntax = SQLSyntax.empty)(implicit session: DBSession): Long = {
    val query = deleteFromSQL.where(Some(where).filter(_ != SQLSyntax.empty)).append(rest)
    sql"${query}".map(rs => fromWrappedResultSet(rs, None)).executeUpdate().longValue()
  }

  def listFromQuery(query: SQLSyntax): List[R] = DB.readOnly({ implicit session => _listFromQuery(query) })

  def _listFromQuery(query: SQLSyntax)(implicit session: DBSession): List[R] = {
    sql"${query}".map(rs => fromWrappedResultSet(rs, None)).list()
  }

  override def fromWrappedResultSet(rs: WrappedResultSet, prefix: Option[String] = None): R = super.fromWrappedResultSet(rs, prefix).asInstanceOf[R]
}
