package com.fastscala.db.keyed.uuid

import com.fastscala.db.Table
import com.fastscala.db.keyed.TableWithId
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{WrappedResultSet, scalikejdbcSQLInterpolationImplicitDef, sqls}

import java.util.UUID

trait TableWithUUIDBase[R <: RowWithUUIDBase] extends Table[R] with TableWithId[R, UUID] {

  def getForIdOpt(key: UUID): Option[R]

  def getForIds(uuid: UUID*): List[R]

  def delete(row: R): Long = delete(sqls"uuid = ${row.id}")

  def delete(ids: Seq[UUID]): Long = delete(SQLSyntax.createUnsafely("""uuid = ANY(?::UUID[])""", Seq(ids.map(_.toString).toArray[String])))

  def idSQL: SQLSyntax = sqls"""uuid"""

  def idFromWrappedResultSetOpt(rs: WrappedResultSet, prefix: Option[String] = None): Option[UUID] = rs.stringOpt(prefix.map(prefix => s"${prefix}_uuid").getOrElse("uuid")).map(UUID.fromString(_))

  override def idFromWrappedResultSet(rs: WrappedResultSet, prefix: Option[String] = None): UUID = idFromWrappedResultSetOpt(rs, prefix).get
}
