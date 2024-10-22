package com.fastscala.templates.utils

import com.fastscala.utils.IdGen

trait ElemWithRandomId extends ElemWithId {

  def randomElemId = IdGen.id

  override val elemId: String = "elem_" + randomElemId
}
