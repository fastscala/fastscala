package com.fastscala.db.computed

import scalikejdbc.WrappedResultSet
import scalikejdbc.interpolation.SQLSyntax

class ComputedCol[T](
                      val sql: SQLSyntax,
                      val read: String => WrappedResultSet => T,
                      protected var value: T
                    ) {
  def apply(): T = value
}
