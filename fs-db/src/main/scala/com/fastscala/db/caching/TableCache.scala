package com.fastscala.db.caching

import com.fastscala.db.*
import com.fastscala.db.keyed.{RowWithId, TableWithId}
import com.fastscala.db.observable.{DBObserver, ObservableRowBase}
import com.fastscala.db.util.Utils
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import scalikejdbc.WrappedResultSet
import scalikejdbc.interpolation.SQLSyntax

class TableCache[K, R <: Row[R] & ObservableRowBase & RowWithId[K, R]](val table: TableWithId[R, K], val whereCond: SQLSyntax = SQLSyntax.empty, var status: CacheStatus.Value = CacheStatus.NONE_LOADED, val entries: collection.mutable.Map[K, R] = collection.mutable.Map[K, R]())
  extends DBObserver
    with TableCacheLike[K, R] {

  def loadAllEntriesAfterNFailedLookups: Int = 5

  val logger = LoggerFactory.getLogger(getClass.getName)

  override def observingTables: Seq[TableBase] = Seq(table)

  def subCache(loadSubsetSQL: SQLSyntax, filterSubset: R => Boolean) = new TableSubCache[K, R](this, loadSubsetSQL, filterSubset)

  def valuesLoadedInCache: Seq[R] = entries.values.toSeq

  def isFullyInMemory = status == CacheStatus.ALL_LOADED

  def hydrate(rows: Seq[R]): Unit = {
    processLoadedRows(rows)
    status = CacheStatus.SOME_LOADED
  }

  def hydrateFully(): Unit = {
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

  def count(): Long = {
    if (status != CacheStatus.ALL_LOADED) {
      table.count(whereCond)
    } else {
      entries.values.size
    }
  }

  def selectAll(): List[R] = {
    hydrateFully()
    entries.values.toList
  }

  def select(where: SQLSyntax): Seq[R] = select(where, SQLSyntax.empty)

  def select[C1](where: SQLSyntax, c1Sql: SQLSyntax, c1Parse: WrappedResultSet => C1): Seq[(R, C1)] =
    select(where, SQLSyntax.empty, List((c1Sql, c1Parse))).map({ case (row, List(c1)) => (row, c1.asInstanceOf[C1]) })

  def select(where: SQLSyntax, rest: SQLSyntax): Seq[R] = select(where, rest, Nil).map(_._1)

  def select(where: SQLSyntax, rest: SQLSyntax, additionalCols: List[(SQLSyntax, WrappedResultSet => Any)]): Seq[(R, List[Any])] = {
    val finalWhere = List(where, whereCond).filter(_ != SQLSyntax.empty).reduceOption[SQLSyntax]((l, r) => SQLSyntax.joinWithAnd(l, r)).getOrElse(SQLSyntax.empty)
    if (status == CacheStatus.NONE_LOADED || additionalCols.nonEmpty) {
      val rows = table.selectWithAdditionalCols(finalWhere, additionalCols, rest)
      processLoadedRows(rows.map(_._1)).zip(rows.map(_._2))
    } else {
      // Load only where necessary:
      val relevantIdsOrdered = table.selectIds(finalWhere, rest)
      val relevantIds = relevantIdsOrdered.toSet
      val missingIds = relevantIds -- entries.keySet
      if (missingIds.nonEmpty) {
        getForIdsX(missingIds.toSeq *)
      }
      val relevantIds2Idx = relevantIdsOrdered.zipWithIndex.toMap
      getForIdsX(relevantIds.toSeq *).sortBy(elem => relevantIds2Idx(elem.key)).map(_ -> Nil)
    }
  }

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
        // logger.debug(s"${table.tableName}.getForIdOptX($key): CACHE MISS (all loaded)")
        None
      case None if entries.size + 1 >= loadAllEntriesAfterNFailedLookups =>
        // logger.trace(s"${table.tableName}.getForIdOptX($key): CACHE MISS (getting all from db, after ${entries.size + 1} cache misses)")
        hydrateFully()
        getForIdOptX(key)
      case None =>
        // logger.trace(s"${table.tableName}.getForIdOptX($key): CACHE MISS (getting from db...)")
        Utils.time(table.getForIdOpt(key))(ms => {
          // logger.trace(s"${table.tableName}.getForIdOptX($key): LOADED FROM DB in ${ms}ms")
        }) match {
          case Some(value) =>
            processLoadedRows(List(value))
            if (status == CacheStatus.NONE_LOADED) {
              status = CacheStatus.SOME_LOADED
              //              logger.trace(s"${table.tableName} cache status: $status")
            }
            Some(value)
          case None =>
            //            logger.trace(s"${table.tableName}.getForIdOptX($key): NOT FOUND (in db)")
            None
        }
    }
  }

  def getForIdsX(ids: K*): List[R] = {
    val idsSet = ids.toSet
    entries.keySet.intersect(idsSet).map(entries) ++ (idsSet.diff(entries.keySet).toList match {
      case Nil => Seq()
      case missingIds => processLoadedRows(table.getForIds(missingIds *))
    })
  }.toList

  override def preSave(table: TableBase, row: RowBase): Unit = ()

  def processLoadedRows(rows: Seq[R]): Seq[R] = rows.map(row =>
    entries.get(row.key) match {
      case Some(existing) =>
        // Update existing row:
        existing.copyFrom(row)
        existing
      case None =>
        entries += (row.key -> row)
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
