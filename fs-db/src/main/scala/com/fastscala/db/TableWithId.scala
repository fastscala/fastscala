package com.fastscala.db

import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{DB, DBSession, SQLBatch, SQLLargeBatch, WrappedResultSet, scalikejdbcSQLInterpolationImplicitDef, sqls}

trait TableWithId[R, K] extends Table[R] {

  val logger = LoggerFactory.getLogger(getClass.getName)

  def getForIdOpt(key: K): Option[R]

  def getForIds(key: K*): List[R]

  def idSQL: SQLSyntax

  def selectIdFromSQL: SQLSyntax = sqls"""select $idSQL from $tableNameSQLSyntaxQuoted"""

  def idFromWrappedResultSet(rs: WrappedResultSet): K

  def selectIds(where: SQLSyntax = SQLSyntax.empty, rest: SQLSyntax = SQLSyntax.empty): List[K] = DB.readOnly({ implicit session => _selectIds(where, rest) })

  def _selectIds(where: SQLSyntax = SQLSyntax.empty, rest: SQLSyntax = SQLSyntax.empty)(implicit session: DBSession): List[K] = {
    val query = selectIdFromSQL.where(Some(where).filter(_ != SQLSyntax.empty)).append(rest)
    try {
      sql"${query}".map(idFromWrappedResultSet).list()
    } catch {
      case ex: PSQLException =>
        logger.error(s"Error on query $query", ex)
        throw ex
    }
  }

//  def save(rows: Seq[R]): Long = DB.localTx({ implicit session => _save(rows) })
//
//  def _save(rows: Seq[R])(implicit session: DBSession): Long = upsert(rows).apply().sum
//
//  def upsert(rows: Seq[R]): SQLBatch = {
//    insertMultipleSQL(rows, sqls"""ON CONFLICT ($idSQL) DO UPDATE""")
//  }
}



