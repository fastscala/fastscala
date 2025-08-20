package com.fastscala.db.caching

import com.fastscala.db.*
import com.fastscala.db.observable.{DBObserver, ObservableRowBase}
import com.fastscala.db.util.Utils
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

/**
 * WARN: Only for *immutable* left and right ids!
 */
class JoinTableCache[
  K,
  L <: Row[L] with ObservableRowBase with RowWithId[K, L],
  J <: Row[J] with ObservableRowBase with RowWithId[K, J],
  R <: Row[R] with ObservableRowBase with RowWithId[K, R]
](
   val cacheL: TableCache[K, L],
   table: TableWithId[J, K],
   val cacheR: TableCache[K, R],
   val getLeftId: J => K,
   val getRightId: J => K,
   val leftIdsInSqlClause: Seq[K] => SQLSyntax,
   val rightIdsInSqlClause: Seq[K] => SQLSyntax,
   loadAll: Table[J] => List[J] = (((table: Table[J]) => table.selectAll()): Table[J] => List[J]),
   status: CacheStatus.Value = CacheStatus.NONE_LOADED,
   entries: collection.mutable.Map[K, J] = collection.mutable.Map[K, J](),
   val entriesWithLeft: collection.mutable.Map[K, collection.mutable.Set[J]] = collection.mutable.Map[K, collection.mutable.Set[J]](),
   val entriesWithRight: collection.mutable.Map[K, collection.mutable.Set[J]] = collection.mutable.Map[K, collection.mutable.Set[J]]()
 ) extends TableCache[K, J](table, loadAll, status, entries) {

  def getRightForLeft(left: L*): Seq[R] = getRightForLeftIds(left.map(_.key): _*)

  def getRightForLeftIds(left: K*): Seq[R] = cacheR.getForIdsX(
    (if (isFullyInMemory) left.flatMap(entriesWithLeft.getOrElse(_, Seq())).distinct else processLoadedRows(select(sqls"where ${leftIdsInSqlClause(left)}"))).map(getRightId) *
  )

  def getLeftForRight(right: R*): Seq[L] = getLeftForRightIds(right.map(_.key): _*)

  def getLeftForRightIds(right: K*): Seq[L] = cacheL.getForIdsX(
    (if (isFullyInMemory) right.flatMap(entriesWithRight.getOrElse(_, Seq())).distinct else processLoadedRows(select(sqls"where ${rightIdsInSqlClause(right)}"))).map(getLeftId) *
  )

  def getJoinForLeftIds(left: K*): Seq[J] =
    if (isFullyInMemory) left.flatMap(entriesWithLeft.getOrElse(_, Seq())).distinct
    else processLoadedRows(select(sqls"where ${leftIdsInSqlClause(left)}"))

  def getJoinForLeft(left: L*): Seq[J] = getJoinForLeftIds(left.map(_.key): _*)

  def getJoinForRightIds(right: K*): Seq[J] =
    if (isFullyInMemory) right.flatMap(entriesWithRight.getOrElse(_, Seq())).distinct
    else processLoadedRows(select(sqls"where ${rightIdsInSqlClause(right)}"))

  def getJoinForRight(right: J): Seq[J] = getJoinForRightIds(right.key)

  def deleteX(left: L, right: R)(implicit obs: DBObserver): Unit = deleteX(left.key, right.key)

  def deleteX(left: K, right: K)(implicit obs: DBObserver): Unit = {
    if (isFullyInMemory) getJoinForLeftIds(left).toSet.intersect(getJoinForRightIds(right).toSet).foreach(_.deleteX())
    else processLoadedRows(select(sqls"where ${leftIdsInSqlClause(List(left))} and ${rightIdsInSqlClause(List(right))}")).foreach(_.deleteX())
  }

  override def processLoadedRows(rows: List[J]): List[J] = super.processLoadedRows(rows).map(row => {
    entriesWithLeft.getOrElseUpdate(getLeftId(row), collection.mutable.Set[J]()) += row
    entriesWithRight.getOrElseUpdate(getRightId(row), collection.mutable.Set[J]()) += row
    row
  })

  override def postSave(t: TableBase, row: RowBase): Unit = (table, row) match {
    case (`table`, row: J) =>
      processLoadedRows(List(row))
      if (status == CacheStatus.NONE_LOADED) {
        status = CacheStatus.SOME_LOADED
      }
    case _ =>
  }

  override def preDelete(t: TableBase, row: RowBase): Unit = (table, row) match {
    case (`table`, row: J) =>
      entries -= row.key
      entriesWithLeft.get(getLeftId(row)).foreach(_ -= row)
      entriesWithRight.get(getRightId(row)).foreach(_ -= row)
    case _ =>
  }

  override def postDelete(table: TableBase, row: RowBase): Unit = ()
}
