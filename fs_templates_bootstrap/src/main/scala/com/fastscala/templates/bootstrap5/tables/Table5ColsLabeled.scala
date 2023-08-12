package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.FSContext

import scala.xml.Elem

trait Table5ColsLabeled {

  type C

  def colLabel(col: C): String
}
