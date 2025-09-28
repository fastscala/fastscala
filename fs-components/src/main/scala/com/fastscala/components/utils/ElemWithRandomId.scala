package com.fastscala.components.utils

import com.fastscala.utils.IdGen

trait ElemWithRandomId extends ElemWithId {

  protected val randomElemId = "elem_" + IdGen.id

  override def elemId: String = randomElemId
}
