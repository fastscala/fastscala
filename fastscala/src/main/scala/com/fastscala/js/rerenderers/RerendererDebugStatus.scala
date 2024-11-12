package com.fastscala.js.rerenderers

import com.fastscala.core.FSXmlUtils.elem2NodeSeq
import com.fastscala.core.{FSXmlEnv, FSXmlSupport, FSXmlNodeSeq, FSXmlElem}
import com.fastscala.js.Js

object RerendererDebugStatus extends Enumeration {
  val Enabled: Value = Value
  implicit val Disabled: Value = Value

  def Unsupported = Set("table", "thead", "tbody", "tfooter", "tr")

  implicit class RichValue[E <: FSXmlEnv](using val env: FSXmlSupport[E])(v: Value) {
    private def style(bgColor: String = "rgb(147 211 255 / 6%)") = s"width: 100%; height: 100%; position: absolute; top: 0; left: 0; text-align: right; color: #4b4b4b; background-color: $bgColor; font-weight: bold; padding: 2px 4px; border: 2px solid #6290bd;pointer-events: none; z-index: 10;"

    def render(rendered: FSXmlElem[E]): FSXmlElem[E] = {
      if (v == RerendererDebugStatus.Enabled && !Unsupported.contains(env.label(rendered))) {
        env.transformContents(env.transformAttribute(rendered, "style", _.getOrElse("") + ";position:relative;"), existing =>
          env.concat(existing,
            env.buildElem("span", "for" -> rendered.toString, "style" -> style(), "id" -> env.getId(rendered).map(_ + "-overlay").getOrElse(null))()
          ))
      } else rendered
    }

    def rerender(aroundId: String, rerenderJs: Js): Js = {
      if (v == RerendererDebugStatus.Enabled) {
        com.fastscala.js.Js.catchAndLogErrors(com.fastscala.js.Js.setAttr(aroundId + "-overlay")("style", style("rgb(255 147 156 / 50%)"))) &
          com.fastscala.js.Js(s"""$$("#$aroundId").fadeOut(1500, function() {${rerenderJs.cmd}});""")
      } else {
        rerenderJs
      }
    }
  }
}
