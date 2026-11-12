package com.fastscala.db

import com.fastscala.db.computed.ComputedCol
import com.fastscala.utils.Utils
import com.typesafe.scalalogging.LazyLogging
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax

import java.lang.reflect.Field
import scala.util.chaining.scalaUtilChainingOps

// This is just for testing. Consider using cats.effect.IOApp instead of calling
// unsafe methods directly.
import scalikejdbc.*

trait Table[R] extends TableBase with LazyLogging {

  def createSampleRow(): R

  override lazy val sampleRowInstance: R = createSampleRowInternal()

  def createEmptyRow(): R = createSampleRow()

  def createSampleRowInternal(): R = createSampleRow()

  private lazy val unsafe: sun.misc.Unsafe = {
    val theUnsafe = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
    theUnsafe.setAccessible(true)
    theUnsafe.get(null).asInstanceOf[sun.misc.Unsafe]
  }

  // allocateInstance skips the row constructor, so anything it sets that isn't loaded from a column
  // (e.g. the ClassTag used by RowWithUUID.equals) is copied from the sample row. Lazy val state is left unset.
  private lazy val nonColumnFieldValues: Array[(Field, AnyRef)] =
    Iterator.iterate[Class[?]](sampleRowInstance.getClass)(_.getSuperclass)
      .takeWhile(c => c != null && c != classOf[Object])
      .flatMap(_.getDeclaredFields)
      .filterNot(f => java.lang.reflect.Modifier.isStatic(f.getModifiers) || fieldsList.contains(f) || f.getName.contains("$lzy") || f.getName.contains("bitmap$"))
      .map(f => {
        f.setAccessible(true)
        (f, f.get(sampleRowInstance))
      })
      .filter(_._2 != null)
      .toArray

  def createEmptyRowInternal(): R = {
    val instance = unsafe.allocateInstance(sampleRowInstance.getClass)
    nonColumnFieldValues.foreach({ case (field, value) => field.set(instance, value) })
    instance.asInstanceOf[R]
  }

  lazy val sampleRow: R = createSampleRowInternal()

  def copyRow(from: R, to: R): Unit = {
    fieldsList.foreach(field => {
      field.set(to, field.get(from))
    })
  }

  def insertFields: List[Field] = fieldsList.filter(_.getType.getName != "com.fastscala.db.computed.ComputedCol")

  def upsertFields: List[Field] = fieldsList.filter(_.getType.getName != "com.fastscala.db.computed.ComputedCol")

  def updateFields: List[Field] = fieldsList.filter(_.getType.getName != "com.fastscala.db.computed.ComputedCol")

  def insertSQL(row: R, rest: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = {
    val columns: SQLSyntax = SQLSyntax.createUnsafely(insertFields.map(field => {
      field.setAccessible(true)
      columnNameForField(field)
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
      sqls""""${SQLSyntax.createUnsafely(columnNameForField(field))}" = """ + valueToFragment(field, field.get(row))
    }).reduceOption(_ + sqls", " + _).getOrElse(SQLSyntax.empty)

    sql"""update $tableNameSQLSyntaxQuoted set $values $where"""
  }

  def deleteSQL(row: R, where: SQLSyntax = SQLSyntax.empty): SQL[Nothing, NoExtractor] = sql"""delete from $tableNameSQLSyntaxQuoted $where"""

  def selectAll(): List[R] = DB.readOnly({ implicit session => _selectAll() })

  def _selectAll()(implicit session: DBSession): List[R] = _selectWithAdditionalCols(SQLSyntax.empty).map(_._1)

  def select(where: SQLSyntax = SQLSyntax.empty, rest: SQLSyntax = SQLSyntax.empty): List[R] = DB.readOnly({ implicit session => _selectWithAdditionalCols(where, rest, Nil).map(_._1) })

  def selectWithAdditionalCols(where: SQLSyntax, additionalCols: List[(SQLSyntax, WrappedResultSet => Any)], rest: SQLSyntax = SQLSyntax.empty): List[(R, List[Any])] = DB.readOnly({ implicit session => _selectWithAdditionalCols(where, rest, additionalCols) })

  def _selectWithAdditionalCols(where: SQLSyntax = SQLSyntax.empty, rest: SQLSyntax = SQLSyntax.empty, additionalCols: List[(SQLSyntax, WrappedResultSet => Any)] = Nil)(implicit session: DBSession): List[(R, List[Any])] = {
    val colsSql = additionalCols match {
      case Nil => selectSQL(None)
      case additionalCols => SQLSyntax.join(selectSQL(None) :: additionalCols.map(_._1), sqls",")
    }
    val query =
      sqls"""select $colsSql from $tableNameSQLSyntaxQuoted"""
        .where(Some(where).filter(_ != SQLSyntax.empty)).append(rest)
    try {
      val setters = fieldsList.map(field => {
        createValueSetterForField(field, sampleRowInstance, tableName, None)
      })
      sql"${query}".map(rs => (createRowFromWrappedResultSet(rs, setters), additionalCols.map(_._2(rs)))).list()
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
      val fieldsWithColumnNames = fieldsList.map(f => (f, columnNameForField(f, None)))
      sql"${query}".map(_.long(1)).list().head
    } catch {
      case ex: PSQLException =>
        logger.error(s"Error on query $query", ex)
        throw ex
    }
  }

  def delete(where: SQLSyntax = SQLSyntax.empty, rest: SQLSyntax = SQLSyntax.empty): Long = DB.localTx({ implicit session => _delete(where, rest) })

  def _delete(where: SQLSyntax, rest: SQLSyntax = SQLSyntax.empty)(implicit session: DBSession): Long = {
    val query = deleteFromSQL.where(Some(where).filter(_ != SQLSyntax.empty)).append(rest)
    sql"${query}".executeUpdate().longValue()
  }

  def listFromQuery(query: SQLSyntax): List[R] = DB.readOnly({ implicit session => _listFromQuery(query) })

  def _listFromQuery(query: SQLSyntax)(implicit session: DBSession): List[R] = mapRows(sql"${query}").list()

  def mapRows(sql: SQL[Nothing, NoExtractor], prefix: Option[String] = None): SQL[R, HasExtractor] = {
    val setters = fieldsList.map(field => {
      createValueSetterForField(field, sampleRowInstance, tableName, prefix)
    })
    sql.map(rs => createRowFromWrappedResultSet(rs, setters))
  }

  private def createRowFromWrappedResultSet(rs: WrappedResultSet, setters: Seq[(WrappedResultSet, R) => Unit]): R = {
    val instance = createEmptyRowInternal()
    setters.foreach(_(rs, instance))
    instance
  }
}
