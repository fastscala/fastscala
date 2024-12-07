package com.fastscala.db.keyed.uuid

import com.fastscala.db.{PgTableWithUUID, Row, RowWithId}
import scalikejdbc.*

import java.util.UUID

trait PgRowWithUUID[R <: PgRowWithUUID[R]] extends Row[R] with RowWithUuidIdBase with RowWithId[UUID, R] {
  self: R =>

  def table: PgTableWithUUID[R]

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
    saveSQL().update()(session)
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
      case Some(uuid) => table.getForIdOpt(uuid).get
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
