package com.fastscala.db.keyed.numeric

import com.fastscala.db.Table
import com.fastscala.db.keyed.TableWithId
import scalikejdbc.*
import scalikejdbc.interpolation.SQLSyntax

import java.lang.reflect.Field

/** What's specific to Long keys (column `id`, given by the database): the rest is in [[TableWithId]]. */
trait TableWithLongId[R <: RowWithLongId[R]] extends Table[R] with TableWithId[R, java.lang.Long] {

  override def createSampleRowInternal(): R = {
    val ins = super.createSampleRowInternal()
    if (ins == null) ins.id = 0
    ins
  }

  def idSQL: SQLSyntax = sqls"""id"""

  def keyEqualsSQL(key: java.lang.Long): SQLSyntax = sqls"id = $key"

  def keyInSQL(keys: Seq[java.lang.Long]): SQLSyntax = SQLSyntax.createUnsafely("""id = ANY(?)""", Seq(keys.toArray[java.lang.Long]))

  def keyOf(row: R): Option[java.lang.Long] = Option(row.id)

  def idFromWrappedResultSetOpt(rs: WrappedResultSet, prefix: Option[String] = None): Option[java.lang.Long] = {
    val value: java.lang.Long = rs.nullableLong(prefix.map(_ + "_").getOrElse("") + "id")
    if (value == null) None else Some[java.lang.Long](value)
  }

  override def insertFields: List[Field] = fieldsList.filter(_.getName != "id")

  override def updateFields: List[Field] = fieldsList.filter(_.getName != "id")
}
