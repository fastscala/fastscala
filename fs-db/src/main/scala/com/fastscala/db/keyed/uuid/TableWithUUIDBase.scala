package com.fastscala.db.keyed.uuid

import com.fastscala.db.{Table, TableWithId}
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

import java.util.UUID

trait TableWithUUIDBase[R <: RowWithUuidIdBase] extends Table[R] with TableWithId[R, UUID] {

  def getForIdOpt(key: UUID): Option[R]

  def getForIds(uuid: UUID*): List[R]
  
  def delete(row: R): Long = delete(sqls"where uuid = ${row.id}")
}
