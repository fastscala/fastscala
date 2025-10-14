package com.fastscala.db.keyed.uuid

import com.fastscala.db.RowWithIdBase
import scalikejdbc.*

import java.nio.ByteBuffer
import java.util.UUID

trait RowWithUuidIdBase extends RowWithIdBase {

  def table: TableWithUUIDBase[?]

  var uuid: Option[UUID] = None

  def id: UUID = {
    if(!isPersisted_?) throw new Exception(s"Trying to get the UUID of a row of table ${table.tableName} that is not persistent")
    uuid.get
  }

  def isPersisted_? : Boolean = uuid.isDefined

  def saveSQL(): SQL[Nothing, NoExtractor]
}
