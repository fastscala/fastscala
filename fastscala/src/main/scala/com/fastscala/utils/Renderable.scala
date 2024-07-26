package com.fastscala.utils

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}

trait Renderable {

  def render[E <: FSXmlEnv : FSXmlSupport](): E#NodeSeq
}

trait RenderableWithFSContext {

  def render[E <: FSXmlEnv : FSXmlSupport]()(implicit fsc: FSContext): E#NodeSeq
}

object RenderableWithFSContext {
  implicit def toRenderable(renderableWithFuncGen: RenderableWithFSContext)(implicit fsc: FSContext): Renderable = new Renderable {
    override def render[E <: FSXmlEnv : FSXmlSupport](): E#NodeSeq = renderableWithFuncGen.render()
  }
}

object Renderable {

  //  def apply[E <: FSXmlEnv : FSXmlSupport](ns: => E#NodeSeq): Renderable = new Renderable {
  //    override def render[Env <: FSXmlEnv : FSXmlSupport](): E#NodeSeq = ns
  //  }
}