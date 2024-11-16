package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.FSSessionVar
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.{JS, JsXmlUtils}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.Elem

object RerendererDebugStatusState extends FSSessionVar[RerendererDebugStatus.Value](RerendererDebugStatus.Disabled)

object RerendererDebugStatus extends Enumeration {
  val Enabled: Value = Value
  implicit val Disabled: Value = Value

  def Unsupported = Set("table", "thead", "tbody", "tfooter", "tr")

  implicit class RichValue(v: Value) {
    private def style(bgColor: String = "rgb(147 211 255 / 6%)") = s"width: 100%; height: 100%; position: absolute; top: 0; left: 0; text-align: right; color: #4b4b4b; background-color: $bgColor; font-weight: bold; padding: 2px 4px; border: 2px solid #6290bd;pointer-events: none; z-index: 10;"

    def render(rendered: Elem): Elem = {
      if (v == RerendererDebugStatus.Enabled && !Unsupported.contains(rendered.label)) {
        rendered.attributeTransform("style", _.getOrElse("") + ";position:relative;").withAppendedToContents(
          <span></span>.withAttrs("for" -> rendered.toString, "style" -> style(), "id" -> rendered.getId.map(_ + "-overlay").getOrElse(null))
        )
      } else rendered
    }

    def rerender(aroundId: String, rerenderJs: Js): Js = {
      if (v == RerendererDebugStatus.Enabled) {
        JS.catchAndLogErrors(JS.setAttr(aroundId + "-overlay")("style", style("rgb(255 147 156 / 50%)"))) &
          JS(s"""$$("#$aroundId").fadeOut(1500, function() {${rerenderJs.cmd}});""")
      } else {
        rerenderJs
      }
    }
  }
}
