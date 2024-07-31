package com.fastscala.templates.bootstrap5.tables

trait Table5ColsLabeled {

  type C

  def colLabel(col: C): String
}
