package com.fastscala.templates.utils

import com.fastscala.core.{FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.utils.Renderable

trait Rerenderable extends Renderable with ElemWithRandomId {

  def rerender[E <: FSXmlEnv : FSXmlSupport](): Js = Js.replace(elemId, render())
}