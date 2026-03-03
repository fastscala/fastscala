package com.fastscala.db.keyed.numeric

import com.fastscala.db.Table
import com.fastscala.db.keyed.TableWithId
import scalikejdbc.*
import scalikejdbc.interpolation.SQLSyntax

import java.lang
import java.lang.reflect.Field

trait TableWithLongId[R <: RowWithLongId[R]] extends Table[R] with TableWithId[R, java.lang.Long] {

  override def createSampleRowInternal(): R = {
    val ins = super.createSampleRowInternal()
    if (ins == null) ins.id = 0
    ins
  }

  def idSQL: SQLSyntax = sqls"""id"""

  def idFromWrappedResultSetOpt(rs: WrappedResultSet, prefix: Option[String] = None): Option[java.lang.Long] = {
    val value: java.lang.Long = rs.nullableLong(prefix.map(_ + "_").getOrElse("") + "id")
    if (value == null) None else Some[java.lang.Long](value)
  }

  override def idFromWrappedResultSet(rs: WrappedResultSet, prefix: Option[String] = None): java.lang.Long = idFromWrappedResultSetOpt(rs, prefix).get

  def upsertSQL(row: R, rest: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = {
    val columns: SQLSyntax = SQLSyntax.createUnsafely(upsertFields.map(field => {
      field.setAccessible(true)
      fieldName(field)
    }).map('"' + _ + '"').mkString("(", ",", ")"))

    val values: List[SQLSyntax] = upsertFields.map(field => {
      field.setAccessible(true)
      valueToFragment(field, field.get(row))
    })
    val setters = SQLSyntax.join(insertFields.map(field => {
      val fName = SQLSyntax.createUnsafely(fieldName(field))
      sqls"""$fName = EXCLUDED.$fName"""
    }), sqls",")

    sql"""insert into $tableNameSQLSyntaxQuoted $columns VALUES ($values) ON CONFLICT (id) DO UPDATE SET $setters;"""
  }

  override def insertFields: List[Field] = fieldsList.filter(_.getName != "id")

  override def updateFields: List[Field] = fieldsList.filter(_.getName != "id")

  override def fieldTypeToSQLType(field: java.lang.reflect.Field, clas: Class[?], value: => Any, columnConstrains: Set[String] = Set("not null")): String =
    if (field.getName == "id") "bigserial primary key not null"
    else super.fieldTypeToSQLType(field, clas, value, columnConstrains)

  def getForIds(ids: java.lang.Long*): List[R] = select(SQLSyntax.createUnsafely("""id = ANY(?)""", Seq(ids.toArray[java.lang.Long])))

  def getForIdOpt(key: java.lang.Long): Option[R] = select(sqls"""id = $key""").headOption
}
