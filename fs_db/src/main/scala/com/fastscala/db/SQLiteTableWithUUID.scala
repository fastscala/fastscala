package com.fastscala.db

import scalikejdbc._

import java.util.UUID

trait SQLiteTableWithUUID[R <: SQLiteRowWithUUID[R]] extends SQLiteTable[R] with TableWithUUIDBase[R] {

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

  def forUUID(uuid: UUID*): List[R] = {
    if (uuid.isEmpty) Nil
    else {
      list(SQLSyntax.createUnsafely(s""" WHERE uuid IN (${(0 until uuid.size).map(_ => "?").mkString(",")})""", uuid.map(_.toString)))
    }
  }
}

object SQLiteTableWithUUID {

  val sampleUUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
}
