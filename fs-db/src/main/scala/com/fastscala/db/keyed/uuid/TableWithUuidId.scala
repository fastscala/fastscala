package com.fastscala.db

import com.fastscala.db.keyed.uuid.{RowWithUUID, TableWithUUIDBase}

import java.util.UUID


trait TableWithUuidId[R <: RowWithUUID[R]] extends Table[R] with TableWithUUIDBase[R] {

  lazy val PlaceholderUUID = TableWithUuidId.PlaceholderUUID

  lazy val PlaceholderOptionalUUID = Some(TableWithUuidId.PlaceholderUUID)
  
  lazy val PlaceholderTimestamp = java.sql.Timestamp(0L)
  
  lazy val PlaceholderOptionalTimestamp = Some(java.sql.Timestamp(0L))

  override def createSampleRowInternal(): R = {
    val ins = super.createSampleRowInternal()
    if (ins.uuid.isEmpty) ins.uuid = Some(TableWithUuidId.PlaceholderUUID)
    ins
  }
}

object TableWithUuidId {

  val PlaceholderUUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
}
