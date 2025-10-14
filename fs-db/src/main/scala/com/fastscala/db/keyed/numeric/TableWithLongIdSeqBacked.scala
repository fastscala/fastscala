package com.fastscala.db.keyed.numeric

import com.fastscala.db.keyed.{PgTableWithLongId, PgRowWithLongId}
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{NoExtractor, SQL}

import java.lang.reflect.Field

trait TableWithLongIdSeqBacked[R <: PgRowWithLongId[R]] extends PgTableWithLongId[R] {
  def sequenceIdName = s"s_${tableName}_id"

  override def insertFields: List[Field] = (super.insertFields ::: fieldsList.filter(_.getName == "id")).distinct

  override def valueToFragment(field: Field, value: Any): SQLSyntax = if (field.getName == "id") {
    SQLSyntax.createUnsafely(s"nextval('$sequenceIdName')")
  } else super.valueToFragment(field, value)

  override def __createTableSQL: List[SQL[Nothing, NoExtractor]] =
    SQL(s"CREATE SEQUENCE IF NOT EXISTS \"$sequenceIdName\";") ::
      super.__createTableSQL
}
