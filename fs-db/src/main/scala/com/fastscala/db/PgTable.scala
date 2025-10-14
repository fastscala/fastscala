package com.fastscala.db

import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{WrappedResultSet, scalikejdbcSQLInterpolationImplicitDef}

import java.lang.reflect.Field
import java.util.UUID

trait PgTable[R] extends Table[R] with PgTableWithUUIDSupport[R]

trait PgTableWithUUIDSupport[R] extends Table[R] {

  override def fieldTypeToSQLType(
                                   field: java.lang.reflect.Field,
                                   clas: Class[?],
                                   value: => Any,
                                   columnConstrains: Set[String] = Set("not null")
                                 ): String = clas.getName match {

    case "java.util.UUID" => "UUID" + columnConstrains.mkString(" ", " ", "")

    case _ => super.fieldTypeToSQLType(field, clas, value, columnConstrains)
  }

  override def valueToFragment(field: Field, value: Any): SQLSyntax = value match {
    case v: java.util.UUID => sqls"${v.toString}::UUID"
    case _ => super.valueToFragment(field, value)
  }

  override def valueToLiteral(value: Any): SQLSyntax = value match {
    case v: java.util.UUID => SQLSyntax.createUnsafely(s"'${v.toString}'::UUID")
    case _ => super.valueToLiteral(value)
  }

  override def setValue(rs: WrappedResultSet, idx: Int, field: java.lang.reflect.Field, valueType: Class[?], instance: Any, nullable: Boolean = false): Unit = valueType.getName match {
    case "java.util.UUID" if nullable => field.set(instance, rs.stringOpt(idx).map(UUID.fromString))
    case "java.util.UUID" => field.set(instance, UUID.fromString(rs.string(idx)))

    case _ => super.setValue(rs, idx, field, valueType, instance, nullable)
  }
}

trait PgTableWithJsonSupport[R] extends Table[R] {

  override def fieldTypeToSQLType(
                                   field: java.lang.reflect.Field,
                                   clas: Class[?],
                                   value: => Any,
                                   columnConstrains: Set[String] = Set("not null")
                                 ): String = clas.getName match {

    case "io.circe.Json" => "json" + columnConstrains.mkString(" ", " ", "")

    case _ => super.fieldTypeToSQLType(field, clas, value, columnConstrains)
  }

  override def valueToFragment(field: Field, value: Any): SQLSyntax = value match {
    case v: io.circe.Json => sqls"${v.noSpaces}::json"
    case _ => super.valueToFragment(field, value)
  }

  override def valueToLiteral(value: Any): SQLSyntax = value match {
    case v: io.circe.Json => SQLSyntax.createUnsafely(s"'${v.noSpaces}'::json")
    case _ => super.valueToLiteral(value)
  }

  override def setValue(rs: WrappedResultSet, idx: Int, field: java.lang.reflect.Field, valueType: Class[?], instance: Any, nullable: Boolean = false): Unit = valueType.getName match {
    case "io.circe.Json" if nullable => field.set(instance, rs.stringOpt(idx).map(str => io.circe.parser.parse(str).right.get))
    case "io.circe.Json" => field.set(instance, io.circe.parser.parse(rs.string(idx)).right.get)

    case _ => super.setValue(rs, idx, field, valueType, instance, nullable)
  }
}
