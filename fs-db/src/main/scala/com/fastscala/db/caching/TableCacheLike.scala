package com.fastscala.db.caching

import com.fastscala.db.{RowWithId, RowWithIdBase}
import scalikejdbc.interpolation.SQLSyntax

trait TableCacheLike[K, R <: RowWithIdBase & RowWithId[K, R]] {

  def all: Seq[R]

  def select(rest: SQLSyntax): List[R]

  def getForIdX(id: K): R

  def getForIdOptX(id: K): Option[R]

  def getForIdsX(ids: K*): List[R]
}
