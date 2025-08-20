package com.fastscala.components.bootstrap5.accordion

import com.fastscala.components.utils.{ElemWithId, ElemWithRandomId}
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.scala_xml.js.{JS, printBeforeExec}
import com.fastscala.utils.IdGen

import scala.xml.{Elem, NodeSeq}

class BSAccordionControls(val tabIdx: Int, val deleteTab: () => Js, val changeTitle: NodeSeq => Js)

class BSAccordion(items: (NodeSeq, FSContext => BSAccordionControls => NodeSeq)*) extends ElemWithRandomId {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def elemAccordion: Elem = accordion

  def elemAccordionItem: Elem = accordion_item

  def elemAccordionHeader(tabTitleId: String, title: NodeSeq, onclick: Js): Elem = h2.accordion_header.apply {
    button.collapsed.addOnClick(onclick).accordion_button.withId(tabTitleId).apply(title)
  }

  def elemAccordionCollapse: Elem = accordion_collapse.collapse.apply {
    elemAccordionBody
  }

  def elemAccordionBody: Elem = accordion_body

  val contentsContextKey = new {}
  val openTabIds = collection.mutable.Set[String]()

  def render()(implicit fsc: FSContext): NodeSeq = {
    elemAccordion.withId(elemId).apply {
      items.zipWithIndex.map({ case ((titleNs, func), idx) =>
        fsc.runInNewOrRenewedChildContextFor((elemId, idx), Some("tab" + idx)) { implicit fsc =>
          val tabId = elemId + s"tab-$idx"
          val collapseId = elemId + s"tab-collapse-$idx"
          val tabTitleId = elemId + s"tab-title-$idx"
          val tabHeaderId = elemId + s"tab-header-$idx"
          val tabBodyId = elemId + s"tab-body-$idx"
          val callback = fsc.callback(() => {
            (if (openTabIds.contains(collapseId)) {
               openTabIds -= collapseId
               fsc.page.deleteContextFor((contentsContextKey, collapseId))
               Js(s"$$('#$collapseId').collapse('hide'); $$('#$tabHeaderId > button').addClass('collapsed');") &
                 Js(s"$$('#$tabBodyId').html('')")
             } else {
               openTabIds += collapseId
               JS.setContents(
                 tabBodyId,
                 fsc.runInNewOrRenewedChildContextFor((contentsContextKey, collapseId))(implicit fsc => {
                   func(fsc)(new BSAccordionControls(idx, () => {
                     fsc.delete()
                     JS.removeId(tabId)
                   }, changeTitle = title => JS.setContents(tabTitleId, title)))
                 })
               ) &
                 Js(s"$$('#$collapseId').collapse('show'); $$('#$tabHeaderId > button').removeClass('collapsed');")
             })
          })
          elemAccordionItem.withId(tabId).apply {
            elemAccordionHeader(tabTitleId, titleNs, callback).withId(tabHeaderId) ++
              elemAccordionCollapse.withId(collapseId).apply {
                elemAccordionBody.withId(tabBodyId)
              }
          }
        }
      }).mkNS
    }
  }

}
