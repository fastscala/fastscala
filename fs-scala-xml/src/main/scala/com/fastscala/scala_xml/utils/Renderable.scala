package com.fastscala.scala_xml.utils

import com.fastscala.core.{FSContext, FSPageImpl}

import scala.xml.NodeSeq

trait Renderable {

  def render(): NodeSeq
}

trait RenderableWithFSContext {

  def render()(implicit fsc: FSContext): NodeSeq
}

trait FSPageImplWithFSContext extends FSPageImpl[NodeSeq] with RenderableWithFSContext 

object RenderableWithFSContext {
  implicit def toRenderable(renderableWithFuncGen: RenderableWithFSContext)(implicit fsc: FSContext): Renderable = new Renderable {
    override def render(): NodeSeq = renderableWithFuncGen.render()
  }
}
