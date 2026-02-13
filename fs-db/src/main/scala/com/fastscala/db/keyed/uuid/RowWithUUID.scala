package com.fastscala.db.keyed.uuid

import com.fastscala.db.keyed.RowWithId
import com.fastscala.db.{TableWithUuidId, Row}
import org.postgresql.util.PSQLException
import scalikejdbc.*
import scalikejdbc.interpolation.SQLSyntax

import java.util.UUID

trait RowWithUUID[R <: RowWithUUID[R]] extends Row[R] with RowWithUUIDBase with RowWithId[UUID, R] {
  self: R =>

  def table: TableWithUuidId[R]

  def key: UUID = uuid.getOrElse(null)

  def saveSQL(): SQL[Nothing, NoExtractor] = {
    val sql = if (uuid.isEmpty) {
      uuid = Some(UUID.randomUUID())
      table.insertSQL(this)
    } else {
      table.updateSQL(this, sqls" where uuid = ${uuid.get.toString}::UUID")
    }
    sql
  }

  def save(): R = {
    DB.localTx({ implicit session => saveSQL().update() })
    this
  }

  def save(session: DBSession): R = {
    saveSQL().update()(using session)
    this
  }

  def update(func: R => Unit): R = {
    val inDB = reload()
    func(inDB)
    inDB.save()
    func(this)
    this
  }

  def reload(): R = {
    uuid match {
      case Some(uuid) =>
        copyFrom(table.getForIdOpt(uuid).get)
        this
      case None => this
    }
  }

  def _reload()(implicit session: DBSession): R = {
    uuid match {
      case Some(uuid) =>
        copyFrom(table._getForIds(uuid).head)
        this
      case None => this
    }
  }

  def update(): Unit = {
    uuid.foreach(uuid => {
      DB.localTx({ implicit session =>
        table.updateSQL(this, sqls" where uuid = ${uuid.toString}::UUID").execute()
      })
    })
  }

  def delete(): Unit = {
    uuid.foreach(uuid => {
      DB.localTx({ implicit session =>
        table.deleteSQL(this, sqls"where uuid = $uuid::UUID").execute()
      })
    })
  }

  override def insert(): Unit = {
    if (uuid.isDefined) throw new Exception(s"Row already inserted with uuid ${uuid.get.toString}")
    super.insert()
  }

  override def hashCode(): Int = uuid.hashCode() * 41

  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[R]) {
      val obj2 = obj.asInstanceOf[R]
      (obj2.uuid.isDefined && uuid.isDefined && obj2.uuid == uuid) ||
        (obj2.uuid.isEmpty && uuid.isEmpty && super.equals(obj2))
    } else {
      false
    }
  }
}
