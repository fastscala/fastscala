package com.fastscala.db.caching

import com.fastscala.db.*
import com.fastscala.db.observable.{DBObserver, ObservableRowBase}
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax

import scala.collection.mutable.ListBuffer


class One2ManyOptCache[
  K,
  O <: Row[O] with RowWithId[K, O] with ObservableRowBase,
  M <: Row[M] with RowWithId[K, M] with ObservableRowBase
](
   val cacheOne: TableCache[K, O],
   val cacheMany: TableCache[K, M],
   val getOne: M => Option[K],
   val filterOneOnMany: K => SQLSyntax,
   val one2Many: collection.mutable.Map[O, ListBuffer[M]] = collection.mutable.Map[O, ListBuffer[M]](),
   val many2One: collection.mutable.Map[M, O] = collection.mutable.Map[M, O]()
 ) extends DBObserver {

  override def observingTables: Seq[Table[_]] = Seq[Table[_]](cacheOne.table, cacheMany.table)

  val logger = LoggerFactory.getLogger(getClass.getName)

  def getMany(one: O): Seq[M] = getMany(one.key)

  def getMany(oneId: K): Seq[M] = cacheMany.select(filterOneOnMany(oneId))

  override def beforeSaved(table: TableBase, row: RowBase): Unit = ()

  override def saved(table: TableBase, row: RowBase): Unit = (table, row) match {
    case (`cacheOne`, row: O) =>
    case (`cacheMany`, many: M) =>
      getOne(many).flatMap(cacheOne.getForIdOptX(_)).foreach(one => {
        one2Many.getOrElseUpdate(one, ListBuffer()) += many
        many2One(many) = one
      })
    case _ =>
  }

  override def beforeDelete(table: TableBase, row: RowBase): Unit = (table, row) match {
    case (`cacheOne`, row: O) =>
    case (`cacheMany`, row: M) =>
      many2One.get(row).foreach(one => {
        one2Many.get(one).foreach(many => {
          many -= row
        })
        many2One -= row
      })
    case _ =>
  }

  override def deleted(table: TableBase, row: RowBase): Unit = ()
}
