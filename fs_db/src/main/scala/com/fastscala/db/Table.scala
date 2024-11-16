package com.fastscala.db

import scalikejdbc.interpolation.SQLSyntax

import java.lang.reflect.Field

// This is just for testing. Consider using cats.effect.IOApp instead of calling
// unsafe methods directly.
import scalikejdbc.*

trait Table[R] extends TableBase {

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

  def updateFields: List[Field] = fieldsList

  def insertSQL(row: R): SQL[Nothing, NoExtractor] = {
    val columns: SQLSyntax = SQLSyntax.createUnsafely(insertFields.map(field => {
      field.setAccessible(true)
      fieldName(field)
    }).map('"' + _ + '"').mkString("(", ",", ")"))

    val values: List[SQLSyntax] = insertFields.map(field => {
      field.setAccessible(true)
      valueToFragment(field, field.get(row))
    })

    sql"""insert into "$tableNameSQLSyntax" $columns VALUES ($values)"""
  }

  def updateSQL(row: R, where: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = {
    val values: SQLSyntax = updateFields.map(field => {
      field.setAccessible(true)
      sqls""""${SQLSyntax.createUnsafely(fieldName(field))}" = """ + valueToFragment(field, field.get(row))
    }).reduceOption(_ + sqls", " + _).getOrElse(SQLSyntax.empty)

    sql"""update "$tableNameSQLSyntax" set $values $where"""
  }

  def deleteSQL(row: R, where: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = sql"""delete from "$tableNameSQLSyntax" $where"""

  def selectAll(): List[R] = select(SQLSyntax.empty)

  def select(rest: SQLSyntax): List[R] = {
    DB.readOnly({ implicit session =>
      val query = selectFromSQL.append(rest)
      sql"${query}".map(fromWrappedResultSet).list()
    })
  }

  def delete(rest: SQLSyntax): Long = {
    DB.localTx({ implicit session =>
      val query = deleteFrom.append(rest)
      sql"${query}".map(fromWrappedResultSet).executeUpdate().longValue()
    })
  }

  def listFromQuery(query: SQLSyntax): List[R] = {
    DB.readOnly({ implicit session =>
      sql"${query}".map(fromWrappedResultSet).list()
    })
  }

  override def fromWrappedResultSet(rs: WrappedResultSet): R = super.fromWrappedResultSet(rs).asInstanceOf[R]
}
