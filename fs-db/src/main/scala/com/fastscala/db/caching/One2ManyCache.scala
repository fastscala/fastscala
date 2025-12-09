package com.fastscala.db.caching

import com.fastscala.db.*
import com.fastscala.db.observable.{DBObserver, ObservableRowBase}
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax

import java.util.UUID
import scala.collection.mutable.ListBuffer


class One2ManyCache[
  K,
  O <: Row[O] & RowWithId[K, O] & ObservableRowBase,
  M <: Row[M] & RowWithId[K, M] & ObservableRowBase
](
   val cacheOne: TableCache[K, O],
   val cacheMany: TableCache[K, M],
   val getOneId: M => K,
   val filterOneOnMany: K => SQLSyntax,
   val one2Many: collection.mutable.Map[O, ListBuffer[M]] = collection.mutable.Map[O, ListBuffer[M]](),
   val many2One: collection.mutable.Map[M, O] = collection.mutable.Map[M, O]()
 ) extends DBObserver {

  val OneTable = cacheOne.table

  val ManyTable = cacheMany.table

  override def observingTables: Seq[Table[?]] = Seq[Table[?]](cacheOne.table, cacheMany.table)

  def hydrateFully(): Unit = {
    cacheOne.selectAll()
    cacheMany.selectAll().foreach(many => {
      cacheOne.getForIdOptX(getOneId(many)).foreach(one => {
        one2Many.getOrElseUpdate(one, ListBuffer()) += many
        many2One(many) = one
      })
    })
  }

  val logger = LoggerFactory.getLogger(getClass.getName)

  def getOne(many: M): Option[O] = cacheOne.getForIdOptX(getOneId(many))

  def getMany(one: O): Seq[M] = getMany(one.key)

  def getMany(one: O, where: SQLSyntax): Seq[M] = getMany(one.key, where)

  def getMany(oneId: K, where: SQLSyntax = SQLSyntax.empty): Seq[M] = cacheMany.select(filterOneOnMany(oneId).and(Some(where).filter(_ != SQLSyntax.empty)))

  override def preSave(table: TableBase, row: RowBase): Unit = ()

  override def postSave(table: TableBase, row: RowBase): Unit = (table, row) match {
    case (OneTable, row: O) =>
    case (ManyTable, many: M) =>
      cacheOne.getForIdOptX(getOneId(many)).foreach(one => {
        one2Many.getOrElseUpdate(one, ListBuffer()) += many
        many2One(many) = one
      })
    case _ =>
  }

  override def preDelete(table: TableBase, row: RowBase): Unit = (table, row) match {
    case (OneTable, one: O) =>
      one2Many.get(one).toSeq.flatten.foreach(many => {
        assert(many2One(many) == one)
        many2One -= many
      })
      one2Many -= one
    case (ManyTable, row: M) =>
      many2One.get(row).foreach(one => {
        one2Many.get(one).foreach(many => {
          many -= row
        })
        many2One -= row
      })
    case _ =>
  }

  override def postDelete(table: TableBase, row: RowBase): Unit = ()
}
