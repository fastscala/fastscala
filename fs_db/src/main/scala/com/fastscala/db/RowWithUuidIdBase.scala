package com.fastscala.db

import scalikejdbc._

import java.util.UUID

trait RowWithUuidIdBase extends RowWithIdBase {

  def table: TableWithUUIDBase[_]

  var uuid: Option[UUID] = None

  def isPersisted_?(): Boolean = uuid.isDefined

  def saveSQL(): SQL[Nothing, NoExtractor]
}
