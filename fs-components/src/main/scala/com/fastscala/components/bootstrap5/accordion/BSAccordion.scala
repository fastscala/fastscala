package com.fastscala.components.bootstrap5.accordion

import com.fastscala.components.utils.{ElemWithId, ElemWithRandomId}
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.scala_xml.js.{JS, printBeforeExec}
import com.fastscala.utils.IdGen

import scala.xml.{Elem, NodeSeq}

class BSAccordion(items: (NodeSeq, FSContext => Int => NodeSeq)*) extends ElemWithRandomId {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def elemAccordion: Elem = accordion

  def elemAccordionItem: Elem = accordion_item

  def elemAccordionHeader(title: NodeSeq, onclick: Js): Elem = h2.accordion_header.apply {
    button.collapsed.addOnClick(onclick).accordion_button.apply(title)
  }

  def elemAccordionCollapse: Elem = accordion_collapse.collapse.apply {
    elemAccordionBody
  }

  def elemAccordionBody: Elem = accordion_body

  val contentsContextKey = new {}
  val openTabIds = collection.mutable.Set[String]()

  def render()(implicit fsc: FSContext): NodeSeq = {
    elemAccordion.withId(elemId).apply {
      items.zipWithIndex.map({
        case ((titleNs, func), idx) => {
          val tabId = elemId + s"tab-$idx"
          val tabHeaderId = elemId + s"tab-header-$idx"
          val tabBodyId = elemId + s"tab-body-$idx"
          val callback = fsc.callback(() => {
            (if (openTabIds.contains(tabId)) {
               openTabIds -= tabId
               fsc.page.deleteContextFor((contentsContextKey, tabId))
               Js(s"$$('#$tabId').collapse('hide'); $$('#$tabHeaderId > button').addClass('collapsed');") &
                 Js(s"$$('#$tabBodyId').html('')")
             } else {
               openTabIds += tabId
               JS.setContents(tabBodyId, fsc.runInNewOrRenewedChildContextFor((contentsContextKey, tabId))(implicit fsc => {
                 println("IN CONTEXT: "+fsc.fullPath)
                 func(fsc)(idx)
               })) &
                 Js(s"$$('#$tabId').collapse('show'); $$('#$tabHeaderId > button').removeClass('collapsed');")
             })
          })
          elemAccordionItem.apply {
            elemAccordionHeader(titleNs, callback).withId(tabHeaderId) ++
              elemAccordionCollapse.withId(tabId).apply {
                elemAccordionBody.withId(tabBodyId)
              }
          }
        }
      }).mkNS
    }
  }

}
