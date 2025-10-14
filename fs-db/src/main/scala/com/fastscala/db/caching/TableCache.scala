package com.fastscala.db.caching

import com.fastscala.db.*
import com.fastscala.db.observable.{DBObserver, ObservableRowBase}
import com.fastscala.db.util.Utils
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax

class TableCache[K, R <: Row[R] & ObservableRowBase & RowWithId[K, R]](val table: TableWithId[R, K], val whereCond: SQLSyntax = SQLSyntax.empty, var status: CacheStatus.Value = CacheStatus.NONE_LOADED, val entries: collection.mutable.Map[K, R] = collection.mutable.Map[K, R]())
    extends DBObserver
    with TableCacheLike[K, R] {

  def loadAllEntriesAfterNFailedLookups: Int = 1

  val logger = LoggerFactory.getLogger(getClass.getName)

  override def observingTables: Seq[TableBase] = Seq(table)

  def subCache(loadSubsetSQL: SQLSyntax, filterSubset: R => Boolean) = new TableSubCache[K, R](this, loadSubsetSQL, filterSubset)

  def valuesLoadedInCache: Seq[R] = entries.values.toSeq

  def isFullyInMemory = status == CacheStatus.ALL_LOADED

  def loadAllEntries(): Unit = {
    if (status != CacheStatus.ALL_LOADED) {
      Utils.time({
        val allEntries = table.select(whereCond)
        processLoadedRows(allEntries)
        status = CacheStatus.ALL_LOADED
      })(ms => {
        logger.debug(s"LOADED ${entries.size} entries FROM ${table.tableName} in ${ms}ms")
      })
    }
  }

  def all: List[R] = {
    loadAllEntries()
    entries.values.toList
  }

  def count(): Long = {
    if (status != CacheStatus.ALL_LOADED) {
      table.count(whereCond)
    } else {
      entries.values.size
    }
  }

  def selectAll(): List[R] = all

  def select(where: SQLSyntax): List[R] = processLoadedRows(table.select(where))

  def select(where: SQLSyntax, rest: SQLSyntax): List[R] = processLoadedRows(table.select(where, rest))

  def apply(key: K): R = getForIdX(key)

  def getForIdX(key: K): R = try {
    getForIdOptX(key).get
  } catch {
    case ex: java.util.NoSuchElementException =>
      throw new java.util.NoSuchElementException(s"Not found: key ${key} in table ${table.tableName}")
  }

  def getForIdOptX(key: K): Option[R] = {
    entries.get(key) match {
      case Some(value) =>
        // logger.trace(s"${table.tableName}.getForIdOptX($uuid): CACHE HIT")
        Some(value)
      case None if status == CacheStatus.ALL_LOADED =>
        logger.debug(s"${table.tableName}.getForIdOptX($key): CACHE MISS (all loaded)")
        None
      case None if entries.size + 1 >= loadAllEntriesAfterNFailedLookups =>
        logger.trace(s"${table.tableName}.getForIdOptX($key): CACHE MISS (getting all from db, after ${entries.size + 1} cache misses)")
        loadAllEntries()
        getForIdOptX(key)
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

  def getForIdsX(ids: K*): List[R] = {
    entries.keySet.intersect(ids.toSet).map(entries) ++ (ids.toSet.diff(entries.keySet).toList match {
      case Nil        => Seq()
      case missingIds => table.getForIds(missingIds*)
    })
  }.toList

  override def preSave(table: TableBase, row: RowBase): Unit = ()

  def processLoadedRows(rows: List[R]): List[R] = rows.map(row =>
    entries.get(row.key) match {
      case Some(existing) =>
        existing.copyFrom(row)
        existing
      case None =>
        entries += (row.key -> row)
        processLoadedRows(List(row))
        row
    }
  )

  def postSave(t: TableBase, row: RowBase): Unit = (table, row) match {
    case (`table`, row: R) =>
      entries += row.key -> row
      if (status == CacheStatus.NONE_LOADED) {
        status = CacheStatus.SOME_LOADED
        logger.trace(s"${table.tableName} cache status: $status")
      }
    case _ =>
  }

  def preDelete(t: TableBase, row: RowBase): Unit = (table, row) match {
    case (`table`, row: R) =>
      if (status != CacheStatus.NONE_LOADED) {
        entries -= row.key
      }
    case _ =>
  }

  override def postDelete(table: TableBase, row: RowBase): Unit = ()
}
