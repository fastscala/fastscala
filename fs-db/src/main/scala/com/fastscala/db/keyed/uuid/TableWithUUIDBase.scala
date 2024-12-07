package com.fastscala.db.keyed.uuid

import com.fastscala.db.{Table, TableWithId}

import java.util.UUID

trait TableWithUUIDBase[R <: RowWithUuidIdBase] extends Table[R] with TableWithId[R, UUID] {

  def getForIdOpt(key: UUID): Option[R]

  def getForIds(uuid: UUID*): List[R]
}
