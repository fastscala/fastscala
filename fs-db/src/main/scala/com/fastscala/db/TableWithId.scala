package com.fastscala.db

import scalikejdbc.{scalikejdbcSQLInterpolationImplicitDef, sqls}

trait TableWithId[R, K] extends Table[R] {

  def getForIdOpt(key: K): Option[R]

  def getForIds(key: K*): List[R]
}



