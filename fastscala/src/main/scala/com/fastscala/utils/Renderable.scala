package com.fastscala.utils

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}

trait Renderable[E <: FSXmlEnv] {

  def render(): E#NodeSeq
}

trait RenderableWithFSContext[E <: FSXmlEnv] {

  def render()(implicit fsc: FSContext): E#NodeSeq
}

object RenderableWithFSContext {
  implicit def toRenderable[E <: FSXmlEnv : FSXmlSupport](renderableWithFuncGen: RenderableWithFSContext[E])(implicit fsc: FSContext): Renderable[E] = new Renderable[E] {
    override def render(): E#NodeSeq = renderableWithFuncGen.render()
  }
}
