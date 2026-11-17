package com.fastscala.db.keyed.uuid

import com.fastscala.db.Table
import com.fastscala.db.keyed.TableWithId
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{WrappedResultSet, scalikejdbcSQLInterpolationImplicitDef, sqls}

import java.util.UUID

/** What's specific to UUID keys (column `uuid`): the rest is in [[TableWithId]]. */
trait TableWithUUIDBase[R <: RowWithUUIDBase] extends Table[R] with TableWithId[R, UUID] {

  def idSQL: SQLSyntax = sqls"""uuid"""

  def keyEqualsSQL(key: UUID): SQLSyntax = sqls"uuid = ${key.toString}::UUID"

  def keyInSQL(keys: Seq[UUID]): SQLSyntax = SQLSyntax.createUnsafely("""uuid = ANY(?::UUID[])""", Seq(keys.map(_.toString).toArray[String]))

  def keyOf(row: R): Option[UUID] = row.uuid

  def idFromWrappedResultSetOpt(rs: WrappedResultSet, prefix: Option[String] = None): Option[UUID] = rs.stringOpt(prefix.map(prefix => s"${prefix}_uuid").getOrElse("uuid")).map(UUID.fromString(_))
}
