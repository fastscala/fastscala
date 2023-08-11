package com.fastscala.db

import scalikejdbc._

import java.lang.reflect.Field

trait TableWithId[R <: RowWithId[R]] extends Table[R] {

  override def createSampleRowInternal(): R = {
    val ins = super.createSampleRowInternal()
    if (ins == null) ins.id = 0
    ins
  }

  override def insertFields: List[Field] = fieldsList.filter(_.getName != "id")

  override def fieldTypeToSQLType(
                                   field: java.lang.reflect.Field,
                                   clas: Class[_],
                                   value: => Any,
                                   append: String = " not null"
                                 ): String =
    if (field.getName == "id") "bigserial primary key not null"
    else super.fieldTypeToSQLType(field, clas, value, append)

  def forId(id: Long): Option[R] = list(sqls""" where id = $id""").headOption
}


