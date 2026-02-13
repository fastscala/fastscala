package com.fastscala.db

import com.fastscala.db.keyed.uuid.{RowWithUUID, TableWithUUIDBase}
import scalikejdbc.*
import scalikejdbc.interpolation.SQLSyntax

import java.util.UUID


trait TableWithUuidId[R <: RowWithUUID[R]] extends Table[R] with TableWithUUIDBase[R] {

  protected lazy val PlaceholderUUID = TableWithUuidId.PlaceholderUUID

  protected lazy val PlaceholderOptionalUUID = Some(TableWithUuidId.PlaceholderUUID)

  override def createSampleRowInternal(): R = {
    val ins = super.createSampleRowInternal()
    if (ins.uuid.isEmpty) ins.uuid = Some(TableWithUuidId.PlaceholderUUID)
    ins
  }

  def getForIdOpt(key: UUID): Option[R] = getForIds(key).headOption

  def getForId(key: UUID): R = getForIdOpt(key).getOrElse(throw new Exception(s"Element with id ${key} not found in table $tableName"))

  def getForIds(uuid: UUID*): List[R] = select(SQLSyntax.createUnsafely("""uuid = ANY(?::UUID[])""", Seq(uuid.map(_.toString).toArray[String])))
  
  def _getForIds(uuid: UUID*)(implicit session: DBSession): List[R] = _selectWithAdditionalCols(SQLSyntax.createUnsafely("""uuid = ANY(?::UUID[])""", Seq(uuid.map(_.toString).toArray[String]))).map(_._1)

  def deleteAll(rows: Seq[R]): Long = {
    delete(SQLSyntax.createUnsafely(""" WHERE uuid = ANY(?::UUID[])""", Seq(rows.flatMap(_.uuid.map(_.toString)).toArray[String])))
  }

  def deleteAllById(rows: Seq[UUID]): Long = {
    delete(SQLSyntax.createUnsafely(""" WHERE uuid = ANY(?::UUID[])""", Seq(rows.map(_.toString).toArray[String])))
  }
}

object TableWithUuidId {

  val PlaceholderUUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
}
