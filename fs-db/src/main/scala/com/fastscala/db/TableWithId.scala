package com.fastscala.db

import org.postgresql.util.PSQLException
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef, sqls}

trait TableWithId[R, K] extends Table[R] {

  def getForIdOpt(key: K): Option[R]

  def getForIds(key: K*): List[R]
}



