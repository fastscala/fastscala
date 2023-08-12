package com.fastscala.db

import java.util.UUID

trait TableWithUUIDBase[R <: RowWithUUIDBase] extends TableBase[R] {

  def forUUIDOpt(uuid: UUID*): Option[R]

  def forUUID(uuid: UUID*): List[R]
}
