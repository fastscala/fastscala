package com.fastscala.db.keyed.uuid

import com.fastscala.db.RowWithIdBase
import scalikejdbc.*

import java.nio.ByteBuffer
import java.util.UUID

trait RowWithUuidIdBase extends RowWithIdBase {

  def table: TableWithUUIDBase[_]

  var uuid: Option[UUID] = None

  def id: UUID = uuid.get

  def isPersisted_? : Boolean = uuid.isDefined

  def saveSQL(): SQL[Nothing, NoExtractor]
}
