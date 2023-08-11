package com.fastscala.db

import scalikejdbc._

import java.util.UUID

trait RowWithUUIDBase extends RowBase {

  def table: TableWithUUID[_]

  var uuid: Option[UUID] = None

  override def isPersisted_?(): Boolean = uuid.isDefined

  def saveSQL(): SQL[Nothing, NoExtractor]

  def save(): this.type

  def update(): Unit

  def delete(): Unit

  override def insert(): Unit
}

trait RowWithUUID[R <: RowWithUUID[R]] extends Row[R] with RowWithUUIDBase {
  self: R =>

  def table: TableWithUUID[R]

  def saveSQL(): SQL[Nothing, NoExtractor] = {
    val sql = if (uuid.isEmpty) {
      uuid = Some(UUID.randomUUID())
      table.insertSQL(this)
    } else {
      table.updateSQL(this, sqls" where uuid = ${uuid.get.toString}::UUID")
    }
    sql
  }

  def save(): this.type = {
    DB.localTx({ implicit session => saveSQL().update()() })
    this
  }

  def update(): Unit = {
    uuid.foreach(uuid => {
      DB.localTx({ implicit session =>
        table.updateSQL(this, sqls" where uuid = ${uuid.toString}::UUID").execute()()
      })
    })
  }

  def delete(): Unit = {
    uuid.foreach(uuid => {
      DB.localTx({ implicit session =>
        table.deleteSQL(this, sqls"where uuid = $uuid::UUID").execute()()
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
      obj2.uuid.isDefined && uuid.isDefined && obj2.uuid == uuid
    } else {
      false
    }
  }
}
