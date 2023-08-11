package com.fastscala.db

import scalikejdbc._

import java.util.UUID

trait TableWithUUID[R <: RowWithUUID[R]] extends Table[R] {

  protected lazy val PlaceholderUUID = UUID.randomUUID()

  override def createSampleRowInternal(): R = {
    val ins = super.createSampleRowInternal()
    if (ins.uuid.isEmpty) ins.uuid = Some(TableWithUUID.sampleUUID)
    ins
  }

  override def fieldTypeToSQLType(
                                   field: java.lang.reflect.Field,
                                   clas: Class[_],
                                   value: => Any,
                                   append: String = " not null"
                                 ): String =
    if (field.getName == "uuid") "UUID primary key not null"
    else super.fieldTypeToSQLType(field, clas, value, append)

  def forUUIDOpt(uuid: UUID*): Option[R] = forUUID(uuid: _*).headOption

  def forUUID(uuid: UUID*): List[R] = list(SQLSyntax.createUnsafely(""" WHERE uuid = ANY(?::UUID[])""", Seq(uuid.map(_.toString).toArray[String])))
}

object TableWithUUID {

  val sampleUUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
}
