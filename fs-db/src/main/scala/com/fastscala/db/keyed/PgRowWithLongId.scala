package com.fastscala.db.keyed

import com.fastscala.db.annotations.PrimaryKey
import com.fastscala.db.{Row, RowWithId, RowWithIdBase}
import scalikejdbc.*
import scalikejdbc.interpolation.SQLSyntax

trait PgRowWithLongId[R <: PgRowWithLongId[R]] extends Row[R] with RowWithIdBase with RowWithId[java.lang.Long, R] {
  self: R =>

  @PrimaryKey var id: java.lang.Long = null
  
  def table: PgTableWithLongId[R]

  override def key: java.lang.Long = id

  def isPersisted_? = id != null

  def reloadOpt(): Option[R] = table.getForIdOpt(id)

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
      table.updateSQL(this, sqls" where id = $id").execute()
    })
  }

  def upsert(): R = {
    DB.localTx({ implicit session => table.upsertSQL(this).execute() })
    this
  }

  def update(upd: R => Unit): R = {
    table.getForIdOpt(id).map(row => {
      upd(row)
      row.save()
      upd(this)
    })
    this
  }

  def delete(): Unit = {
    DB.localTx({ implicit session =>
      table.deleteSQL(this, sqls"where id = $id").execute()
    })
  }

  //  override def insert(): Unit = {
  //    if (isPersisted_?) throw new Exception(s"Row already inserted with id $id")
  //    super.insert()
  //  }

  override def hashCode(): Int = id.hashCode() * 41

  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[R]) {
      val obj2 = obj.asInstanceOf[R]
      (obj2.id != null && id != null && obj2.id == id) ||
        (obj2.id == null && id == null && super.equals(obj2))
    } else {
      false
    }
  }
}
