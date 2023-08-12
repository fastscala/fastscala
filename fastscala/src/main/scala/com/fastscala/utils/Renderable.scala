package com.fastscala.utils

import com.fastscala.core.FSContext

import scala.xml.NodeSeq

trait Renderable {

  def render(): NodeSeq
}

trait RenderableWithFSContext {

  def render()(implicit fsc: FSContext): NodeSeq
}

object RenderableWithFSContext {
  implicit def toRenderable(renderableWithFuncGen: RenderableWithFSContext)(implicit fsc: FSContext) = new Renderable {
    override def render(): NodeSeq = renderableWithFuncGen.render()
  }
}

object Renderable {

  def apply(ns: => NodeSeq) = new Renderable {
    override def render(): NodeSeq = ns
  }
}