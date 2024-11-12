package com.fastscala.utils

import com.fastscala.core.{FSXmlElem, FSXmlNodeSeq, FSContext, FSXmlEnv, FSXmlSupport}

trait Renderable[E <: FSXmlEnv] {

  def render(): FSXmlNodeSeq[E]
}

trait RenderableWithFSContext[E <: FSXmlEnv] {

  def render()(implicit fsc: FSContext): FSXmlNodeSeq[E]
}

object RenderableWithFSContext {
  implicit def toRenderable[E <: FSXmlEnv](using env: FSXmlSupport[E])(renderableWithFuncGen: RenderableWithFSContext[E])(implicit fsc: FSContext): Renderable[E] = new Renderable[E] {
    override def render(): FSXmlNodeSeq[E] = renderableWithFuncGen.render()
  }
}
