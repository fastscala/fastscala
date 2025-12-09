package com.fastscala.db.util

import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

extension (sqls: SQLSyntax) {
  def doubleQuoted: SQLSyntax = sqls""""$sqls""""
}
