package com.fastscala.db.caching

import com.fastscala.db._
import com.fastscala.db.observable.{DBObserver, ObservableRowBase}
import com.fastscala.db.util.Utils
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax

class TableCache[K, R <: Row[R] with ObservableRowBase with RowWithId[K, R]](
                                                                              val table: TableWithId[R, K],
                                                                              val loadAll: Table[R] => Seq[R] = (_: Table[R]).selectAll().toVector,
                                                                              var status: CacheStatus.Value = CacheStatus.NONE_LOADED,
                                                                              val entries: collection.mutable.Map[K, R] = collection.mutable.Map[K, R]()
                                                                            ) extends DBObserver with TableCacheLike[K, R] {

  val logger = LoggerFactory.getLogger(getClass.getName)

  override def observingTables: Seq[TableBase] = Seq(table)

  def subCache(loadSubsetSQL: SQLSyntax, filterSubset: R => Boolean) = new TableSubCache[K, R](this, loadSubsetSQL, filterSubset)

  def valuesLoadedInCache: Seq[R] = entries.values.toSeq

  def values: Seq[R] = {
    if (status != CacheStatus.ALL_LOADED) {
      Utils.time({
        loadAll(table).foreach(e => {
          if (!entries.contains(e.key)) {
            entries += (e.key -> e)
          }
        })
        status = CacheStatus.ALL_LOADED
      })(ms => {
        logger.trace(s"${table.tableName}.values: LOADED ${entries.size} entries FROM DB in ${ms}ms")
      })
    }
    entries.values.toList
  }

  def select(rest: SQLSyntax): List[R] = {
    val rslt = table.select(rest)
    rslt.map(loaded => entries.get(loaded.key) match {
      case Some(existing) =>
        existing.copyFrom(loaded)
        existing
      case None => loaded
    })
  }

  def getForIdX(key: K): R = getForIdOptX(key).get

  def getForIdOptX(key: K): Option[R] = {

    if (status != CacheStatus.ALL_LOADED) {
      this.values
    }

    entries.get(key) match {
      case Some(value) =>
        // logger.trace(s"${table.tableName}.getForIdOptX($uuid): CACHE HIT")
        Some(value)
      case None if status == CacheStatus.ALL_LOADED =>
        logger.debug(s"${table.tableName}.getForIdOptX($key): CACHE MISS (all loaded)")
        None
      case None =>
        logger.trace(s"${table.tableName}.getForIdOptX($key): CACHE MISS (getting from db...)")
        Utils.time(table.getForIdOpt(key))(ms => {
          logger.trace(s"${table.tableName}.getForIdOptX($key): LOADED FROM DB in ${ms}ms")
        }) match {
          case Some(value) =>
            entries += value.key -> value
            if (status == CacheStatus.NONE_LOADED) {
              status = CacheStatus.SOME_LOADED
              logger.trace(s"${table.tableName} cache status: $status")
            }
            Some(value)
          case None =>
            logger.trace(s"${table.tableName}.getForIdOptX($key): NOT FOUND (in db)")
            None
        }
    }
  }

  def getForIdsX(ids: K*): List[R] =
    ids.toList.flatMap(id => table.getForIdOpt(id))


  def saved(t: TableBase, row: RowBase): Unit = (table, row) match {
    case (`table`, row: R) =>
      entries += row.key -> row
      if (status == CacheStatus.NONE_LOADED) {
        status = CacheStatus.SOME_LOADED
        logger.trace(s"${table.tableName} cache status: $status")
      }
    case _ =>
  }

  def deleted(t: TableBase, row: RowBase): Unit = (table, row) match {
    case (`table`, row: R) =>
      if (status != CacheStatus.NONE_LOADED) {
        entries -= row.key
      }
    case _ =>
  }
}
