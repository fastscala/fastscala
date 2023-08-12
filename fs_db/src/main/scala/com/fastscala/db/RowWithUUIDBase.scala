package com.fastscala.db

import scalikejdbc._

import java.util.UUID

trait RowWithUUIDBase extends RowBase {

  def table: TableWithUUIDBase[_]

  var uuid: Option[UUID] = None

  def isPersisted_?(): Boolean = uuid.isDefined

  def saveSQL(): SQL[Nothing, NoExtractor]

  def save(): this.type

  def update(): Unit

  def delete(): Unit

  override def insert(): Unit
}
