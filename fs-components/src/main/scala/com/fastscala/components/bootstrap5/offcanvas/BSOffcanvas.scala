package com.fastscala.components.bootstrap5.offcanvas

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS

import scala.xml.NodeSeq

object BSOffcanvas {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers._

  def start(
             title: String,
             backdrop: Boolean = true,
             backdropStatic: Boolean = false,
             scroll: Boolean = true,
             keyboard: Boolean = true,
             onHidden: Js = JS.void
           )(
             contents: BSOffcanvasBase => FSContext => NodeSeq
           )(implicit fsc: FSContext): Js = apply(OffcanvasPosition.Start, title, onHidden)(contents)

  def end(
           title: String,
           backdrop: Boolean = true,
           backdropStatic: Boolean = false,
           scroll: Boolean = true,
           keyboard: Boolean = true,
           onHidden: Js = JS.void
         )(
           contents: BSOffcanvasBase => FSContext => NodeSeq
         )(implicit fsc: FSContext): Js = apply(OffcanvasPosition.End, title, onHidden)(contents)

  def top(
           title: String,
           backdrop: Boolean = true,
           backdropStatic: Boolean = false,
           scroll: Boolean = true,
           keyboard: Boolean = true,
           onHidden: Js = JS.void
         )(
           contents: BSOffcanvasBase => FSContext => NodeSeq
         )(implicit fsc: FSContext): Js = apply(OffcanvasPosition.Top, title, onHidden)(contents)

  def bottom(
              title: String,
              backdrop: Boolean = true,
              backdropStatic: Boolean = false,
              scroll: Boolean = true,
              keyboard: Boolean = true,
              onHidden: Js = JS.void
            )(
              contents: BSOffcanvasBase => FSContext => NodeSeq
            )(implicit fsc: FSContext): Js = apply(OffcanvasPosition.Bottom, title, onHidden)(contents)

  private def apply(
                     offcanvasPosition: OffcanvasPosition,
                     title: String,
                     onHidden: Js = JS.void
                   )(
                     contents: BSOffcanvasBase => FSContext => NodeSeq
                   )(implicit fsc: FSContext): Js = {
    val offcanvas = new BSOffcanvasBase {
      override val position: OffcanvasPosition = offcanvasPosition

      override def offcanvasHeaderTitle: String = title

      override def offcanvasBodyContents()(implicit fsc: FSContext): NodeSeq = contents(this)(fsc)
    }
    offcanvas.installAndShow() & offcanvas.onHidden(onHidden)
  }
}
