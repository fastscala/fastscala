package com.fastscala.db

import scalikejdbc._

import java.util.UUID

trait PgTableWithUUID[R <: PgRowWithUUID[R]] extends Table[R] with TableWithUUIDBase[R] {

  protected lazy val PlaceholderUUID = UUID.randomUUID()

  override def createSampleRowInternal(): R = {
    val ins = super.createSampleRowInternal()
    if (ins.uuid.isEmpty) ins.uuid = Some(PgTableWithUUID.sampleUUID)
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

  def forUUIDOpt(uuid: UUID*): Option[R] = forUUID(uuid: _*).headOption

  def forUUID(uuid: UUID*): List[R] = list(SQLSyntax.createUnsafely(""" WHERE uuid = ANY(?::UUID[])""", Seq(uuid.map(_.toString).toArray[String])))
}

object PgTableWithUUID {

  val sampleUUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
}
