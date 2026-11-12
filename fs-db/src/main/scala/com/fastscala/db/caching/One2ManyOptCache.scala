package com.fastscala.db.caching

import com.fastscala.db.*
import com.fastscala.db.keyed.RowWithId
import com.fastscala.db.observable.{DBObserver, ObservableRowBase}
import com.typesafe.scalalogging.LazyLogging
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


/**
 * @param filterOneOnMany  SQL condition selecting the "many" rows that belong to the given "one" id.
 * @param filterManyOnOnes SQL condition selecting the "many" rows that belong to any of the given "one" ids (used by [[hydrateFor]]).
 */
class One2ManyOptCache[
  K,
  O <: Row[O] & RowWithId[K, O] & ObservableRowBase,
  M <: Row[M] & RowWithId[K, M] & ObservableRowBase
](
   val cacheOne: TableCache[K, O],
   val cacheMany: TableCache[K, M],
   val getOne: M => Option[K],
   val filterOneOnMany: K => SQLSyntax,
   val filterManyOnOnes: Seq[K] => SQLSyntax,
   val one2Many: collection.mutable.Map[O, ListBuffer[M]] = collection.mutable.Map[O, ListBuffer[M]](),
   val many2One: collection.mutable.Map[M, O] = collection.mutable.Map[M, O]()
 ) extends DBObserver with LazyLogging {

  /**
   * For each "one" id that was hydrated (see [[hydrateFor]]): the keys of all its "many" rows.
   * Only hydrated ids are present, so presence means the membership is completely known.
   */
  private val hydratedOne2ManyKeys = mutable.Map[K, mutable.LinkedHashSet[K]]()
  /** Reverse index of [[hydratedOne2ManyKeys]]: "many" key -> the hydrated "one" id it is listed under. */
  private val hydratedMany2OneKey = mutable.Map[K, K]()

  override def observingTables: Seq[Table[?]] = Seq[Table[?]](cacheOne.table, cacheMany.table)

  def isHydrated(oneId: K): Boolean = hydratedOne2ManyKeys.contains(oneId)

  /**
   * Loads, in a single query, the "many" rows of all the given "one" ids that aren't hydrated yet.
   * Later [[getMany]] calls for those ids are answered from memory, without querying the DB.
   */
  def hydrateFor(oneIds: K*): Unit = {
    val toLoad = oneIds.distinct.filterNot(isHydrated)
    if (toLoad.nonEmpty) {
      toLoad.foreach(id => hydratedOne2ManyKeys(id) = mutable.LinkedHashSet[K]())
      cacheMany.select(filterManyOnOnes(toLoad)).foreach(link)
    }
  }

  def getMany(one: O): Seq[M] = getMany(one.key)

  def getMany(oneId: K): Seq[M] = hydratedOne2ManyKeys.get(oneId) match {
    case Some(manyKeys) =>
      // The getOne check drops rows whose foreign key was changed in memory without a save
      // (e.g. set to None after the "one" was deleted with ON DELETE SET NULL):
      manyKeys.toSeq.flatMap(cacheMany.entries.get).filter(getOne(_).contains(oneId))
    case None => cacheMany.select(filterOneOnMany(oneId))
  }

  /** Lists `many` under its "one" if that "one" is hydrated, removing it from wherever it was listed before. */
  private def link(many: M): Unit = {
    unlink(many.key)
    getOne(many).foreach(oneId => hydratedOne2ManyKeys.get(oneId).foreach(manyKeys => {
      manyKeys += many.key
      hydratedMany2OneKey(many.key) = oneId
    }))
  }

  private def unlink(manyKey: K): Unit =
    hydratedMany2OneKey.remove(manyKey).foreach(oneId => hydratedOne2ManyKeys.get(oneId).foreach(_ -= manyKey))

  override def preSave(table: TableBase, row: RowBase): Unit = ()

  override def postSave(table: TableBase, row: RowBase): Unit = {
    if (table == cacheMany.table) link(row.asInstanceOf[M])

    (table, row) match {
      case (`cacheOne`, row: O) =>
      case (`cacheMany`, many: M) =>
        getOne(many).flatMap(cacheOne.getForIdOptX(_)).foreach(one => {
          one2Many.getOrElseUpdate(one, ListBuffer()) += many
          many2One(many) = one
        })
      case _ =>
    }
  }

  override def preDelete(table: TableBase, row: RowBase): Unit = {
    // Both sides can be the same table (self relation), so check both:
    if (table == cacheMany.table) unlink(row.asInstanceOf[M].key)
    if (table == cacheOne.table) hydratedOne2ManyKeys.remove(row.asInstanceOf[O].key).foreach(_.foreach(hydratedMany2OneKey -= _))

    (table, row) match {
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
  }

  override def postDelete(table: TableBase, row: RowBase): Unit = ()
}
