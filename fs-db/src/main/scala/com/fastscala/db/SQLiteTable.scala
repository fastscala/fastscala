package com.fastscala.db

import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{WrappedResultSet, scalikejdbcSQLInterpolationImplicitDef}

import java.lang.reflect.Field
import java.util.UUID

trait SQLiteTable[R] extends Table[R] {

  override def fieldTypeToSQLType(
                                   field: java.lang.reflect.Field,
                                   clas: Class[_],
                                   value: => Any,
                                   columnConstrains: Set[String] = Set("not null")
                                 ): String = clas.getName match {

    case "java.util.UUID" => "text" + columnConstrains.mkString(" ", " ", "")

    case _ => super.fieldTypeToSQLType(field, clas, value, columnConstrains)
  }

  override def valueToFragment(field: Field, value: Any): SQLSyntax = value match {
    case v: java.util.UUID => sqls"${v.toString}"
    case _ => super.valueToFragment(field, value)
  }

  override def setValue(rs: WrappedResultSet, idx: Int, field: java.lang.reflect.Field, valueType: Class[_], instance: Any, nullable: Boolean = false): Unit = valueType.getName match {
    case "java.util.UUID" if nullable => field.set(instance, rs.stringOpt(idx).map(UUID.fromString))
    case "java.util.UUID" => field.set(instance, UUID.fromString(rs.string(idx)))

    case _ => super.setValue(rs, idx, field, valueType, instance, nullable)
  }
}
