package com.fastscala.db.keyed.uuid

import com.fastscala.db.annotations.PrimaryKey
import com.fastscala.db.keyed.RowWithIdBase
import scalikejdbc.*

import java.nio.ByteBuffer
import java.util.UUID

trait RowWithUUIDBase extends RowWithIdBase {

  def table: TableWithUUIDBase[?]

  @PrimaryKey
  var uuid: Option[UUID] = None

  def id: UUID = {
    if(!isPersisted_?) throw new Exception(s"Trying to get the UUID of a row of table ${table.tableName} that is not persistent")
    uuid.get
  }

  def isPersisted_? : Boolean = uuid.isDefined

  def saveSQL(): SQL[Nothing, NoExtractor]
}
