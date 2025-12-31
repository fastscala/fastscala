package com.fastscala.db.keyed.uuid

import com.fastscala.db.{Table, TableWithId}
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{WrappedResultSet, scalikejdbcSQLInterpolationImplicitDef, sqls}

import java.util.UUID

trait TableWithUUIDBase[R <: RowWithUuidIdBase] extends Table[R] with TableWithId[R, UUID] {

  def getForIdOpt(key: UUID): Option[R]

  def getForIds(uuid: UUID*): List[R]

  def delete(row: R): Long = delete(sqls"uuid = ${row.id}")

  def delete(ids: Seq[UUID]): Long = delete(SQLSyntax.createUnsafely("""uuid = ANY(?::UUID[])""", Seq(ids.map(_.toString).toArray[String])))

  def idSQL: SQLSyntax = sqls"""uuid"""
  
  def idFromWrappedResultSet(rs: WrappedResultSet): UUID = UUID.fromString(rs.string("uuid"))
}
