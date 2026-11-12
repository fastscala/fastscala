package com.fastscala.db.caching

import com.fastscala.db.*
import com.fastscala.db.keyed.RowWithId
import com.fastscala.db.observable.{DBObserver, ObservableRowBase}
import com.typesafe.scalalogging.LazyLogging
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

import java.util.UUID
import scala.collection.mutable.ListBuffer

class Many2ManyDiffKeysCache[KL, KJ, KR, L <: Row[L] & ObservableRowBase & RowWithId[KL, L], J <: Row[J] &
  ObservableRowBase &
  RowWithId[KJ, J], R <: Row[R] &
  ObservableRowBase &
  RowWithId[KR, R]](
                     val cacheL: TableCache[KL, L],
                     val cacheJ: TableCache[KJ, J],
                     val cacheR: TableCache[KR, R],
                     val getLeft: J => KL,
                     val getRight: J => KR,
                     val filterLeftOnJoinTable: Seq[KL] => SQLSyntax,
                     val filterRightOnJoinTable: Seq[KR] => SQLSyntax,
                     // If a key is present in these maps, then all the joins are *guaranteed* to be loaded:
                     val left2Join: collection.mutable.Map[KL, collection.mutable.Set[J]] = collection.mutable.Map[KL, collection.mutable.Set[J]](),
                     val right2Join: collection.mutable.Map[KR, collection.mutable.Set[J]] = collection.mutable.Map[KR, collection.mutable.Set[J]]()
                   ) extends DBObserver with LazyLogging {

  override def observingTables: Seq[Table[?]] = Seq[Table[?]](cacheL.table, cacheJ.table, cacheR.table)

  def hydrateFully(): Unit = cacheJ.selectAll().foreach(joinElem => {
    left2Join.getOrElseUpdate(getLeft(joinElem), scala.collection.mutable.Set()) += joinElem
    right2Join.getOrElseUpdate(getRight(joinElem), scala.collection.mutable.Set()) += joinElem
  })

  private def hidrateCacheForLeftKeys(keys: Set[KL]): Unit = if (keys.nonEmpty) {
    val joinElems: Seq[J] = cacheJ.select(sqls"${filterLeftOnJoinTable(keys.toSeq)}")
    joinElems.foreach(joinElem => {
      left2Join.getOrElseUpdate(getLeft(joinElem), scala.collection.mutable.Set()) += joinElem
    })
    (keys -- left2Join.keySet).foreach(leftKeyWithNoJoinEntry => {
      left2Join.getOrElseUpdate(leftKeyWithNoJoinEntry, scala.collection.mutable.Set())
    })
  }

  private def hidrateCacheForRightKeys(keys: Set[KR]): Unit = if (keys.nonEmpty) {
    val joinElems: Seq[J] = cacheJ.select(sqls"${filterRightOnJoinTable(keys.toSeq)}")
    joinElems.foreach(joinElem => {
      right2Join.getOrElseUpdate(getRight(joinElem), scala.collection.mutable.Set()) += joinElem
    })
    (keys -- right2Join.keySet).foreach(rightKeyWithNoJoinEntry => {
      right2Join.getOrElseUpdate(rightKeyWithNoJoinEntry, scala.collection.mutable.Set())
    })
  }

  def getRightForLeft(left: L*): Seq[R] = getRightForLeftIds(left.map(_.key) *)

  def getRightForLeftIds(leftKeys: KL*): Seq[R] = cacheR.getForIdsX(getJoinForLeftIds(leftKeys *).map(getRight) *)

  def getLeftForRight(right: R*): Seq[L] = getLeftForRightIds(right.map(_.key) *)

  def getLeftForRightIds(rightKeys: KR*): Seq[L] = cacheL.getForIdsX(getJoinForRightIds(rightKeys *).map(getLeft) *)

  def getJoinForLeft(left: L*): Seq[J] = getJoinForLeftIds(left.map(_.key) *)

  def getJoinForLeftIds(left: KL*): Seq[J] = {
    // Hydrate cache for missing keys:
    hidrateCacheForLeftKeys(left.toSet -- left2Join.keySet.toSet)
    left.toSeq.flatMap(key => left2Join.get(key).toSeq.flatten)
  }

  def getJoinForRight(right: R*): Seq[J] = getJoinForRightIds(right.map(_.key) *)

  def getJoinForRightIds(right: KR*): Seq[J] = {
    // Hydrate cache for missing keys:
    hidrateCacheForRightKeys(right.toSet -- right2Join.keySet.toSet)
    right.toSeq.flatMap(key => right2Join.get(key).toSeq.flatten)
  }

  def getJoinForLeftAndRight(left: L, right: R): Option[J] = getJoinForLeftAndRightIds(left.key, right.key)

  /**
   * A hydrated side already holds *all* its joins, so either side alone is enough to answer.
   * If neither is hydrated, hydrates the left side (one query), so that checking the same left against many rights
   * (e.g. one checkbox per tag) costs one query in total instead of one per right.
   */
  def getJoinForLeftAndRightIds(left: KL, right: KR): Option[J] = {
    if (!left2Join.contains(left) && !right2Join.contains(right)) hidrateCacheForLeftKeys(Set(left))
    left2Join.get(left) match {
      case Some(joinsOfLeft) => joinsOfLeft.find(getRight(_) == right)
      case None => right2Join(right).find(getLeft(_) == left)
    }
  }

  override def preSave(table: TableBase, row: RowBase): Unit = ()

  override def postSave(table: TableBase, row: RowBase): Unit = {
    val joinTable = cacheJ.table
    (table, row) match {
      case (`joinTable`, row: J) =>
        if (left2Join.contains(getLeft(row))) {
          left2Join(getLeft(row)) += row
        }
        if (right2Join.contains(getRight(row))) {
          right2Join(getRight(row)) += row
        }
      case _ =>
    }
  }

  override def preDelete(table: TableBase, row: RowBase): Unit = {
    val leftTable = cacheL.table
    val joinTable = cacheJ.table
    val rightTable = cacheR.table
    (table, row) match {
      case (`leftTable`, left: L) =>
        for {
          joins <- left2Join.get(left.key)
          join <- joins
          leftK = left.key
          rightK = getRight(join)
        } {
          left2Join(leftK) -= join
          right2Join.get(rightK).foreach(_ -= join)
        }
      case (`joinTable`, row: J) =>
        if (left2Join.contains(getLeft(row))) {
          left2Join(getLeft(row)) -= row
        }
        if (right2Join.contains(getRight(row))) {
          right2Join(getRight(row)) -= row
        }
      case (`rightTable`, right: R) =>
        for {
          joins <- right2Join.get(right.key)
          join <- joins
          leftK = getLeft(join)
          rightK = right.key
        } {
          right2Join(rightK) -= join
          left2Join.get(leftK).foreach(_ -= join)
        }
      case _ =>
    }
  }

  override def postDelete(table: TableBase, row: RowBase): Unit = ()
}
