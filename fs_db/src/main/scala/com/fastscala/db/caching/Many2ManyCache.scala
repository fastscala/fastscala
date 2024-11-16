package com.fastscala.db.caching

import com.fastscala.db.*
import com.fastscala.db.observable.{DBObserver, ObservableRowBase}
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

import java.util.UUID
import scala.collection.mutable.ListBuffer

class Many2ManyCache[
  K,
  L <: Row[L] with ObservableRowBase with RowWithId[K, L],
  J <: Row[J] with ObservableRowBase with RowWithId[K, J],
  R <: Row[R] with ObservableRowBase with RowWithId[K, R]
](
   val cacheL: TableCache[K, L],
   val cacheJ: TableCache[K, J],
   val cacheR: TableCache[K, R],
   val getLeft: J => K,
   val getRight: J => K,
   val filterLeftOnJoin: K => SQLSyntax,
   val filterRightOnJoin: K => SQLSyntax,
   val left2Right: collection.mutable.Map[L, ListBuffer[R]] = collection.mutable.Map[L, ListBuffer[R]](),
   val right2Left: collection.mutable.Map[R, ListBuffer[L]] = collection.mutable.Map[R, ListBuffer[L]]()
 ) extends DBObserver {

  override def observingTables: Seq[Table[_]] = Seq[Table[_]](cacheL.table, cacheJ.table, cacheR.table)

  val logger = LoggerFactory.getLogger(getClass.getName)

  def deleteX(left: L, right: R)(implicit obs: DBObserver): Unit = getRightForLeft(left).find(_ == right).foreach(_.deleteX()(obs))

  def deleteX(left: K, right: K)(implicit obs: DBObserver): Unit = getRightForLeft(left).find(_.key == right).foreach(_.deleteX()(obs))

  def getRightForLeft(left: L): Seq[R] = getRightForLeft(left.key)

  def getRightForLeft(left: K): Seq[R] = {
    val right = cacheJ.select(sqls"where ${filterLeftOnJoin(left)}").map(j => cacheR.getForIdX(getRight(j)))
    cacheR.getForIdsX(right.map(_.key): _*)
  }

  def getJoinForLeft(left: K): Seq[J] = cacheJ.select(sqls"where ${filterLeftOnJoin(left)}")

  def getJoinForLeft(left: L): Seq[J] = getJoinForLeft(left.key)

  def getLeftForRight(right: R): Seq[L] = getLeftForRight(right.key)

  def getLeftForRight(right: K): Seq[L] = {
    val left = cacheJ.select(sqls"where ${filterRightOnJoin(right)}").map(j => cacheL.getForIdX(getLeft(j)))
    cacheL.getForIdsX(left.map(_.key): _*)
  }

  def getJoinForRight(right: K): Seq[J] = cacheJ.select(sqls"where ${filterRightOnJoin(right)}")

  def getJoinForRight(right: R): Seq[J] = getJoinForRight(right.key)

  def getJoinRow(left: L, right: R): Option[J] =
    cacheJ.select(sqls"where ${filterLeftOnJoin(left.key)} and ${filterRightOnJoin(right.key)}").headOption

  override def beforeSaved(table: TableBase, row: RowBase): Unit = ()

  override def saved(table: TableBase, row: RowBase): Unit = (table, row) match {
    case (`cacheL`, row: L) =>
    case (`cacheJ`, row: J) =>
    case (`cacheR`, row: R) =>
    case _ =>
  }

  override def beforeDelete(table: TableBase, row: RowBase): Unit = (table, row) match {
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

  override def deleted(table: TableBase, row: RowBase): Unit = ()
}
