package com.fastscala.db.keyed.numeric

import com.fastscala.db.Row
import com.fastscala.db.annotations.PrimaryKey
import com.fastscala.db.keyed.{RowWithId, RowWithIdBase}
import scalikejdbc.*

import scala.reflect.ClassTag

trait RowWithLongId[R <: RowWithLongId[R] : ClassTag] extends Row[R] with RowWithIdBase with RowWithId[java.lang.Long, R] {
  self: R =>

  @PrimaryKey var id: java.lang.Long = null

  def table: TableWithLongId[R]

  override def key: java.lang.Long = id

  def isPersisted_? : Boolean = id != null

  protected def insertWithNewKey()(implicit session: DBSession): Unit =
    id = table.insertSQL(this).updateAndReturnGeneratedKey("id").apply()

  protected def clearKey(): Unit = id = null

  // (A row never saved has no id: as in equals, it's only itself.)
  override def hashCode(): Int = if (id == null) super.hashCode() else id.hashCode() * 41

  override def equals(obj: Any): Boolean = obj match {
    case obj: R =>
      (obj.keyOpt.isDefined && keyOpt.isDefined && obj.keyOpt == keyOpt) ||
        (obj.keyOpt.isEmpty && keyOpt.isEmpty && super.equals(obj))
    case _ => false
  }
}
