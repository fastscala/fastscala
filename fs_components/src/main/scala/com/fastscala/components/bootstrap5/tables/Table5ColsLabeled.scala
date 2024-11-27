package com.fastscala.components.bootstrap5.tables

trait Table5ColsLabeled {

  type C

  def colLabel(col: C): String
}
