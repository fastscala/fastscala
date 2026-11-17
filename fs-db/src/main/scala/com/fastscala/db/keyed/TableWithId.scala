package com.fastscala.db.keyed

import com.fastscala.db.Table
import com.typesafe.scalalogging.LazyLogging
import org.postgresql.util.PSQLException
import scalikejdbc.*
import scalikejdbc.interpolation.SQLSyntax

object TableWithId

/**
 * A table whose rows have a primary key. What depends on the kind of key is left to the tables for UUID and Long keys
 * ([[idSQL]], [[keyEqualsSQL]], [[keyInSQL]], [[keyOf]], [[idFromWrappedResultSetOpt]]); the rest is here.
 */
trait TableWithId[R, K] extends Table[R] with LazyLogging {

  /** The key column. */
  def idSQL: SQLSyntax

  /** The condition that selects the row with this key: `uuid = ...`, `id = ...`. */
  def keyEqualsSQL(key: K): SQLSyntax

  /** The condition that selects the rows with these keys. */
  def keyInSQL(keys: Seq[K]): SQLSyntax

  /** The row's key; None if it was never saved. */
  def keyOf(row: R): Option[K]

  def idFromWrappedResultSetOpt(rs: WrappedResultSet, prefix: Option[String] = None): Option[K]

  def idFromWrappedResultSet(rs: WrappedResultSet, prefix: Option[String] = None): K = idFromWrappedResultSetOpt(rs, prefix).get

  def getForIdOpt(key: K): Option[R] = select(keyEqualsSQL(key)).headOption

  def getForId(key: K): R = getForIdOpt(key).getOrElse(throw new Exception(s"Element with id ${key} not found in table $tableName"))

  def getForIds(keys: K*): List[R] = select(keyInSQL(keys))

  def _getForIds(keys: K*)(implicit session: DBSession): List[R] = _selectWithAdditionalCols(keyInSQL(keys)).map(_._1)

  /** Deletes the row (nothing, if it was never saved): returns how many rows were deleted. */
  def delete(row: R): Long = keyOf(row).map(key => delete(keyEqualsSQL(key))).getOrElse(0L)

  def delete(keys: Seq[K]): Long = delete(keyInSQL(keys))

  def deleteAll(rows: Seq[R]): Long = delete(keyInSQL(rows.flatMap(keyOf)))

  def deleteAllById(keys: Seq[K]): Long = delete(keyInSQL(keys))

  def selectIdFromSQL: SQLSyntax = sqls"""select $idSQL from $tableNameSQLSyntaxQuoted"""

  def selectIds(where: SQLSyntax = SQLSyntax.empty, rest: SQLSyntax = SQLSyntax.empty): List[K] = DB.readOnly({ implicit session => _selectIds(where, rest) })

  def _selectIds(where: SQLSyntax = SQLSyntax.empty, rest: SQLSyntax = SQLSyntax.empty)(implicit session: DBSession): List[K] = {
    val query = selectIdFromSQL.where(Some(where).filter(_ != SQLSyntax.empty)).append(rest)
    try {
      sql"${query}".map(rs => idFromWrappedResultSet(rs)).list()
    } catch {
      case ex: PSQLException =>
        logger.error(s"Error on query $query", ex)
        throw ex
    }
  }

  /** Inserts the row or, if there's one with its key already, updates that one. */
  def upsertSQL(row: R, rest: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = {
    val columns: SQLSyntax = SQLSyntax.createUnsafely(upsertFields.map(field => {
      field.setAccessible(true)
      columnNameForField(field)
    }).map('"' + _ + '"').mkString("(", ",", ")"))

    val values: List[SQLSyntax] = upsertFields.map(field => {
      field.setAccessible(true)
      valueToFragment(field, field.get(row))
    })
    val setters = SQLSyntax.join(insertFields.map(field => {
      val fName = SQLSyntax.createUnsafely(columnNameForField(field))
      sqls"""$fName = EXCLUDED.$fName"""
    }), sqls",")

    sql"""insert into $tableNameSQLSyntaxQuoted $columns VALUES ($values) ON CONFLICT ($idSQL) DO UPDATE SET $setters $rest;"""
  }
}
