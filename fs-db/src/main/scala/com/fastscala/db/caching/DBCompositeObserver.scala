package com.fastscala.db.caching

import com.fastscala.db.observable.DBObserver
import com.fastscala.db.{RowBase, TableBase}

trait DBCompositeObserver extends DBObserver {

  private lazy val allObservers: Seq[DBObserver] = {
    val fields = Iterator.iterate[Class[?]](this.getClass)(_.getSuperclass)
      .takeWhile(c => c != null && c != AnyRef.getClass)
      .flatMap(_.getDeclaredFields)
      .filter(f => classOf[DBObserver].isAssignableFrom(f.getType))
      .toVector
    // println("CACHES:\n" + fields.map(_.getName).sorted.mkString("\n"))
    fields.map(f => {
      f.setAccessible(true)
      f.get(this).asInstanceOf[DBObserver]
    })
  }

  private lazy val table2Observers: Map[TableBase, Seq[DBObserver]] = allObservers.flatMap(obs => obs.observingTables.map(t => t -> obs)).groupBy(_._1)
    .transform((k, v) => v.map(_._2))

  lazy val observingTables: Seq[TableBase] = allObservers.flatMap(_.observingTables)

  override def preSave(table: TableBase, row: RowBase): Unit =
    table2Observers.getOrElse(table, Nil).foreach(_.preSave(table, row))

  def postSave(table: TableBase, row: RowBase): Unit =
    table2Observers.getOrElse(table, Nil).foreach(_.postSave(table, row))

  override def preDelete(table: TableBase, row: RowBase): Unit =
    table2Observers.getOrElse(table, Nil).foreach(_.preDelete(table, row))

  def postDelete(table: TableBase, row: RowBase): Unit =
    table2Observers.getOrElse(table, Nil).foreach(_.postDelete(table, row))
}
