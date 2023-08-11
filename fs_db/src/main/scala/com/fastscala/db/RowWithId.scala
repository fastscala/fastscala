package com.fastscala.db

import scalikejdbc._

trait RowWithId[R <: RowWithId[R]] extends Row[R] {
  self: R =>

  def table: TableWithId[R]

  var id: java.lang.Long = _

  override def isPersisted_? = id != null

  def reloadOpt(): Option[R] = table.forId(id)

  def reload(): R = reloadOpt().get

  def save(): R = {
    if (isPersisted_?) {
      update()
    } else {
      DB.localTx({ implicit session =>
        id = table.insertSQL(this).updateAndReturnGeneratedKey("id").apply()
      })
    }
    this
  }

  def duplicate(): R = {
    DB.localTx({ implicit session =>
      id = null
      id = table.insertSQL(this).updateAndReturnGeneratedKey("id").apply()
    })
    this
  }

  def update(): Unit = {
    DB.localTx({ implicit session =>
      table.updateSQL(this, sqls" where id = $id").execute()()
    })
  }

  def update(upd: R => Unit): Unit = {
    table.forId(id).foreach(row => {
      upd(row)
      row.save()
      upd(this)
    })
  }

  def delete(): Unit = {
    DB.localTx({ implicit session =>
      table.deleteSQL(this, sqls"where id = $id").execute()()
    })
  }

  override def insert(): Unit = {
    if (isPersisted_?) throw new Exception(s"Row already inserted with id $id")
    super.insert()
  }

  override def hashCode(): Int = id.hashCode() * 41

  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[R]) {
      val obj2 = obj.asInstanceOf[R]
      obj2.id != null && id != null && obj2.id == id
    } else {
      false
    }
  }
}
