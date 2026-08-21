package com.fastscala.db.keyed.numeric

import com.fastscala.db.Row
import com.fastscala.db.annotations.PrimaryKey
import com.fastscala.db.keyed.{RowWithId, RowWithIdBase}
import scalikejdbc.*
import scalikejdbc.interpolation.SQLSyntax

import scala.reflect.ClassTag

trait RowWithLongId[R <: RowWithLongId[R] : ClassTag] extends Row[R] with RowWithIdBase with RowWithId[java.lang.Long, R] {
  self: R =>

  @PrimaryKey var id: java.lang.Long = null

  def table: TableWithLongId[R]

  def keyOpt: Option[java.lang.Long] = Option(id)

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
    val oldId = id
    try {
      id = null
      val newId = DB.localTx({ implicit session => table.insertSQL(this).updateAndReturnGeneratedKey("id").apply() })
      table.getForIdOpt(newId).get
    } finally {
      id = oldId
    }
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

  override def equals(obj: Any): Boolean = obj match {
    case obj: R =>
      (obj.keyOpt.isDefined && keyOpt.isDefined && obj.keyOpt == keyOpt) ||
        (obj.keyOpt.isEmpty && keyOpt.isEmpty && super.equals(obj))
    case _ => false
  }
}
