package com.fastscala.components.bootstrap5.table5

trait Table5ColsLabeled {

  type C

  def colLabel(col: C): String
}
