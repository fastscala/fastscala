package com.fastscala.db

import com.fastscala.db.keyed.uuid.{PgRowWithUUID, TableWithUUIDBase}
import scalikejdbc._
import scalikejdbc.interpolation.SQLSyntax

import java.util.UUID

trait PgTableWithUUID[R <: PgRowWithUUID[R]] extends PgTable[R] with TableWithUUIDBase[R] {

  protected lazy val PlaceholderUUID = PgTableWithUUID.PlaceholderUUID

  protected lazy val PlaceholderOptionalUUID = Some(PgTableWithUUID.PlaceholderUUID)

  override def createSampleRowInternal(): R = {
    val ins = super.createSampleRowInternal()
    if (ins.uuid.isEmpty) ins.uuid = Some(PgTableWithUUID.PlaceholderUUID)
    ins
  }

  override def fieldTypeToSQLType(
                                   field: java.lang.reflect.Field,
                                   clas: Class[_],
                                   value: => Any,
                                   columnConstrains: Set[String] = Set("not null")
                                 ): String =
    if (field.getName == "uuid") super.fieldTypeToSQLType(field, clas, value, columnConstrains + "primary key")
    else super.fieldTypeToSQLType(field, clas, value, columnConstrains)

  def getForIdOpt(key: UUID): Option[R] = getForIds(key).headOption

  def getForIds(uuid: UUID*): List[R] = select(SQLSyntax.createUnsafely(""" WHERE uuid = ANY(?::UUID[])""", Seq(uuid.map(_.toString).toArray[String])))

  def deleteAll(rows: Seq[R]): Long = {
    delete(SQLSyntax.createUnsafely(""" WHERE uuid = ANY(?::UUID[])""", Seq(rows.flatMap(_.uuid.map(_.toString)).toArray[String])))
  }
}

object PgTableWithUUID {

  val PlaceholderUUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
}
