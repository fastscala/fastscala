package com.fastscala.db

import java.util.UUID

trait TableWithUUIDBase[R <: RowWithUUIDBase] extends TableBase[R] {

  def getForIdOpt(uuid: UUID): Option[R]

  def getForIds(uuid: UUID*): List[R]
}
