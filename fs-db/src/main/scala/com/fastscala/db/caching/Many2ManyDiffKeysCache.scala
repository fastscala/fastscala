package com.fastscala.db.caching

import com.fastscala.db.*
import com.fastscala.db.observable.{DBObserver, ObservableRowBase}
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

import java.util.UUID
import scala.collection.mutable.ListBuffer

class Many2ManyDiffKeysCache[
  KL,
  KJ,
  KR,
  L <: Row[L] & ObservableRowBase & RowWithId[KL, L],
  J <: Row[J] & ObservableRowBase & RowWithId[KJ, J],
  R <: Row[R] & ObservableRowBase & RowWithId[KR, R]
](
   val cacheL: TableCache[KL, L],
   val cacheJ: TableCache[KJ, J],
   val cacheR: TableCache[KR, R],
   val getLeft: J => KL,
   val getRight: J => KR,
   val filterLeftOnJoinTable: Seq[KL] => SQLSyntax,
   val filterRightOnJoinTable: Seq[KR] => SQLSyntax,
   val left2Right: collection.mutable.Map[L, ListBuffer[R]] = collection.mutable.Map[L, ListBuffer[R]](),
   val right2Left: collection.mutable.Map[R, ListBuffer[L]] = collection.mutable.Map[R, ListBuffer[L]]()
 ) extends DBObserver {

  override def observingTables: Seq[Table[?]] = Seq[Table[?]](cacheL.table, cacheJ.table, cacheR.table)

  val logger = LoggerFactory.getLogger(getClass.getName)

  def deleteX(left: L, right: R)(implicit obs: DBObserver): Unit = getRightForLeft(left).find(_ == right).foreach(_.deleteX()(using obs))

  def deleteX(left: KL, right: KR)(implicit obs: DBObserver): Unit = getRightForLeftIds(left).find(_.key == right).foreach(_.deleteX()(using obs))

  def getRightForLeft(left: L*): Seq[R] = getRightForLeftIds(left.map(_.key)*)

  def getRightForLeftIds(left: KL*): Seq[R] = {
    val right = cacheJ.select(sqls"${filterLeftOnJoinTable(left)}").map(j => cacheR.getForIdX(getRight(j)))
    cacheR.getForIdsX(right.map(_.key)*)
  }

  def getJoinForLeftIds(left: KL*): Seq[J] = cacheJ.select(sqls"${filterLeftOnJoinTable(left)}")

  def getJoinForLeft(left: L*): Seq[J] = getJoinForLeftIds(left.map(_.key)*)

  def getLeftForRight(right: R*): Seq[L] = getLeftForRightIds(right.map(_.key)*)

  def getLeftForRightIds(right: KR*): Seq[L] = {
    val left = cacheJ.select(sqls"${filterRightOnJoinTable(right)}").map(j => cacheL.getForIdX(getLeft(j)))
    cacheL.getForIdsX(left.map(_.key)*)
  }

  def getJoinForRightId(right: KR): Seq[J] = cacheJ.select(sqls"${filterRightOnJoinTable(Seq(right))}")

  def getJoinForRight(right: R): Seq[J] = getJoinForRightId(right.key)

  def getJoinRow(left: L, right: R): Option[J] =
    cacheJ.select(sqls"${filterLeftOnJoinTable(Seq(left.key))} and ${filterRightOnJoinTable(Seq(right.key))}").headOption

  override def preSave(table: TableBase, row: RowBase): Unit = ()

  override def postSave(table: TableBase, row: RowBase): Unit = (table, row) match {
    case (`cacheL`, row: L) =>
    case (`cacheJ`, row: J) =>
    case (`cacheR`, row: R) =>
    case _ =>
  }

  override def preDelete(table: TableBase, row: RowBase): Unit = (table, row) match {
    case (`cacheL`, row: L) =>
      val relevantOnTheRight: ListBuffer[R] = left2Right.getOrElse(row, ListBuffer[R]())
      left2Right -= row
      relevantOnTheRight.foreach(r => right2Left(r) -= row)
    case (`cacheJ`, row: J) =>
      val leftId = getLeft(row)
      val left = cacheL.getForIdX(leftId)
      val relevantOnTheRight: ListBuffer[R] = left2Right.getOrElse(left, ListBuffer[R]())
      left2Right -= left
      relevantOnTheRight.foreach(r => right2Left(r) -= left)
      val rightId = getRight(row)
      val right = cacheR.getForIdX(rightId)
      val relevantOnTheLeft: ListBuffer[L] = right2Left.getOrElse(right, ListBuffer[L]())
      right2Left -= right
      relevantOnTheLeft.foreach(l => left2Right(l) -= right)
    case (`cacheR`, row: R) =>
      val relevantOnTheLeft: ListBuffer[L] = right2Left.getOrElse(row, ListBuffer[L]())
      right2Left -= row
      relevantOnTheLeft.foreach(l => left2Right(l) -= row)
    case _ =>
  }

  override def postDelete(table: TableBase, row: RowBase): Unit = ()
}
