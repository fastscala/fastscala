package com.fastscala.db.keyed.numeric

import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{NoExtractor, SQL}

import java.lang.reflect.Field

trait TableWithLongIdSeqBacked[R <: RowWithLongId[R]] extends TableWithLongId[R] {
  def sequenceIdName = s"s_${tableName}_id"

  override def insertFields: List[Field] = (super.insertFields ::: fieldsList.filter(_.getName == "id")).distinct

  // A row without an id takes the sequence's next one; a row that has one keeps it (so an upsert finds its row).
  override def valueToFragment(field: Field, value: Any): SQLSyntax =
    if (field.getName == "id" && value == null) SQLSyntax.createUnsafely(s"nextval('$sequenceIdName')")
    else super.valueToFragment(field, value)

  override def __createTableSQL: List[SQL[Nothing, NoExtractor]] =
    SQL(s"CREATE SEQUENCE IF NOT EXISTS \"$sequenceIdName\";") ::
      super.__createTableSQL

  override def __dropTableSQL: List[SQL[Nothing, NoExtractor]] =
    SQL(s"""DROP SEQUENCE IF EXISTS $sequenceIdName;""") ::
      super.__dropTableSQL
}
