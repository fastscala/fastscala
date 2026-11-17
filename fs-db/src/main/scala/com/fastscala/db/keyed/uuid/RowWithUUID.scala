package com.fastscala.db.keyed.uuid

import com.fastscala.db.keyed.RowWithId
import com.fastscala.db.{Row, TableWithUuidId}
import scalikejdbc.*

import java.util.UUID
import scala.reflect.ClassTag

trait RowWithUUID[R <: RowWithUUID[R] : ClassTag] extends Row[R] with RowWithUUIDBase with RowWithId[UUID, R] {
  self: R =>

  def table: TableWithUuidId[R]

  def key: UUID = uuid.getOrElse(null)

  /** The SQL that saves the row (an insert, giving it its uuid, or an update), for the caller to run. */
  def saveSQL(): SQL[Nothing, NoExtractor] = {
    uncheckedWrite("saveSQL()")
    val sql = if (uuid.isEmpty) {
      uuid = Some(UUID.randomUUID())
      table.insertSQL(this)
    } else {
      table.updateSQL(this, sqls" where ${table.keyEqualsSQL(uuid.get)}")
    }
    sql
  }

  protected def insertWithNewKey()(implicit session: DBSession): Unit = {
    uuid = Some(UUID.randomUUID())
    try table.insertSQL(this).update()
    catch {
      case t: Throwable =>
        uuid = None
        throw t
    }
  }

  protected def clearKey(): Unit = uuid = None

  def _reload()(implicit session: DBSession): R = {
    uuid match {
      case Some(uuid) =>
        copyFrom(table._getForIds(uuid).head)
        this
      case None => this
    }
  }

  override def insert(): Unit = {
    if (uuid.isDefined) throw new Exception(s"Row already inserted with uuid ${uuid.get.toString}")
    super.insert()
  }

  override def hashCode(): Int = uuid.hashCode() * 41

  override def equals(obj: Any): Boolean = obj match {
    case obj: R =>
      (obj.uuid.isDefined && uuid.isDefined && obj.uuid == uuid) ||
        (obj.uuid.isEmpty && uuid.isEmpty && super.equals(obj))
    case _ => false
  }
}
