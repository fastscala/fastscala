package com.fastscala.components.form7.mixins.mainelem

import com.fastscala.components.form7.F7Field
import com.fastscala.utils.IdGen

import scala.xml.Elem


trait F7FieldWithMainElem extends F7Field {

  private val randomMainElemId = "elem_" + IdGen.id

  def mainElemId: String = randomMainElemId

  def processMainElem(mainElem: Elem): Elem = mainElem
}
