package com.fastscala.db.caching

import com.fastscala.db.keyed.{RowWithId, RowWithIdBase}
import scalikejdbc.interpolation.SQLSyntax

trait TableCacheLike[K, R <: RowWithIdBase & RowWithId[K, R]] {

  def selectAll(): Seq[R]

  def select(rest: SQLSyntax): Seq[R]

  def getForIdX(id: K): R

  def getForIdOptX(id: K): Option[R]

  def getForIdsX(ids: K*): Seq[R]
}
