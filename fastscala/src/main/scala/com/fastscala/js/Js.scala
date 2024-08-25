package com.fastscala.js

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.rerenderers.{ContentRerenderer, ContentRerendererP, Rerenderer, RerendererP}
import com.fastscala.utils.IdGen
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.text.StringEscapeUtils

import java.util.Date

trait Js {

  def cmd: String

  def cmdEscaped: String = {
    println("IN: " + cmd)
    println("OUT: " + "\"" + StringEscapeUtils.escapeEcmaScript(cmd.replaceAll("\n", "")) + "\"")
    "'" + StringEscapeUtils.escapeEcmaScript(cmd.replaceAll("\n", "")) + "'"
  }

  def &(js: Js) = RawJs(cmd + ";" + js.cmd)

  def onDOMContentLoaded: Js = Js {
    s"""if (/complete|interactive|loaded/.test(document.readyState)) {$cmd}
       |else { document.addEventListener('DOMContentLoaded', function() {$cmd}, false); }""".stripMargin
  }

  def writeTo(
               resp: HttpServletResponse,
               status: Int = 200,
               contentType: String = "application/javascript; charset=utf-8"
             ): Unit = {
    resp.setStatus(status)
    resp.setContentType(contentType)
    resp.getWriter.write(cmd)
  }

  override def toString: String = cmd
}

case class RawJs(js: String) extends Js {
  override def cmd: String = js
}

object JsOps {

  implicit class RichJs(js: Js) {

    def `_==`(other: Js): Js = Js(s"${js.cmd} == ${other.cmd}")

    def `_!=`(other: Js): Js = Js(s"${js.cmd} != ${other.cmd}")

    def `_===`(other: Js): Js = Js(s"${js.cmd} === ${other.cmd}")

    def `_!==`(other: Js): Js = Js(s"${js.cmd} !== ${other.cmd}")

    def `_>`(other: Js): Js = Js(s"${js.cmd} > ${other.cmd}")

    def `_>=`(other: Js): Js = Js(s"${js.cmd} >= ${other.cmd}")

    def `_<`(other: Js): Js = Js(s"${js.cmd} < ${other.cmd}")

    def `_<=`(other: Js): Js = Js(s"${js.cmd} <= ${other.cmd}")

    def `_&&`(other: Js): Js = Js(s"${js.cmd} && ${other.cmd}")

    def `_||`(other: Js): Js = Js(s"${js.cmd} || ${other.cmd}")

    def `_=`(other: Js): Js = Js(s"${js.cmd} = ${other.cmd};")
  }

}

trait JsUtils {

  import JsOps._

  def evalIf(cond: Boolean)(js: => Js): Js = if (cond) js else this.void

  def _if(cond: Js, _then: Js, _else: Js = this.void): Js = Js(s"if(${cond.cmd}) {${_then}} else {${_else}}")

  def _trenaryOp(cond: Js, _then: Js, _else: Js = this.void): Js = Js(s"((${cond.cmd}) ? (${_then}) : (${_else}))")

  def void: Js = Js("")

  def log(js: Js): Js = Js(s"""console.log(eval("${escapeStr(js.cmd)}"));""")

  def void[T](code: () => T): Js = {
    code()
    void
  }

  val _null: Js = Js("null")
  val _true: Js = Js("true")
  val _false: Js = Js("false")

  def fromString(s: String): Js = Js(s)

  def escapeStr(s: String) = StringEscapeUtils.escapeEcmaScript(s)

  def asJsStr(s: String): Js = Js(s""""${escapeStr(s)}"""")

  def elementById(id: String) = Js(s"""document.getElementById("${escapeStr(id)}")""")

  def elementValueById(id: String) = Js(s"""document.getElementById("${escapeStr(id)}").value""")

  def checkboxIsCheckedById(id: String) = Js(s"""document.getElementById("${escapeStr(id)}").checked""")

  def selectedValues(elem: Js) = Js(s"""Array.from(${elem.cmd}.querySelectorAll("option:checked"),e=>e.value)""")

  def withVarStmt(name: String, value: Js)(code: Js => Js) = Js(s"""(function ($name) {${code(Js(name)).cmd}})(${value.cmd});""")

  def withVarExpr(name: String, value: Js)(code: Js => Js) = Js(s"""(function ($name) {return ${code(Js(name)).cmd};})(${value.cmd});""")

  def valueOrElse(value: Js, default: Js) = withVarExpr("value", value) {
    value => this._trenaryOp(value `_!=` _null, _then = value, _else = default)
  }

  def wrapAsExpr(stmt: Js*)(expr: Js) = Js(s"""((function () {${stmt.reduceOption(_ & _).getOrElse(this.void).cmd}; return ${expr.cmd};})())""")

  def varOrElseUpdate(variable: Js, defaultValue: Js) =
    this._trenaryOp(variable `_!=` _null, _then = variable, _else = wrapAsExpr(variable.`_=`(defaultValue))(variable))

  def checkboxIsChecked(id: String) = Js(s"""document.getElementById("${escapeStr(id)}").checked""")

  def alert(text: String): Js = Js(s"""alert("${escapeStr(text)}");""")

  def toClipboard(text: String): Js = Js(s"navigator.clipboard.writeText('${escapeStr(text)}');")

  def consoleLog(js: Js): Js = Js(s"""console.log(${js.cmd});""")

  def consoleLog(str: String): Js = this.consoleLog(this.asJsStr(str))

  def confirm(text: String, js: Js) = Js(s"""if(confirm("${escapeStr(text)}")) {${js.cmd}};""")

  def redirectTo(link: String): Js = Js(s"""window.location.href = "${escapeStr(link)}";""")

  def reloadPageWithQueryParam(key: String, value: String): Js = Js {
    s"""var searchParams = new URLSearchParams(window.location.search);
       |searchParams.set(${this.asJsStr(key)}, ${this.asJsStr(value)});
       |window.location.search = searchParams.toString();""".stripMargin
  }

  def goBack(n: Int = 1): Js = Js(s""";window.history.back();""")

  def onload(js: Js): Js = Js(s"""$$(document).ready(function() { ${js.cmd} });""")

  def onkeypress(codes: Int*)(js: Js): Js = Js(s"event = event || window.event; if (${codes.map(code => s"(event.keyCode ? event.keyCode : event.which) == $code").mkString(" || ")}) {${js.cmd}};")

  def reload(): Js = Js(s"""location.reload();""")

  def setTimeout(js: Js, timeout: Long): Js = Js(s"""setTimeout(function(){ ${js.cmd} }, $timeout);""")

  def removeId(id: String): Js = Js(s"""document.getElementById("$id").remove();""")

  def show(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").style.display = "block";""")

  def hide(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").style.display = "none";""")

  def focus(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").focus();""")

  def select(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").select();""")

  def blur(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").blur();""")

  def addClass(id: String, clas: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").classList.add(${this.asJsStr(clas)})""")

  def removeClass(id: String, clas: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").classList.remove(${this.asJsStr(clas)})""")

  def setCookie(name: String, cookie: String, expires: Option[Long] = None, path: Option[String] = None): Js =
    Js(s"""document.cookie='$name=${escapeStr(cookie)};${expires.map(new Date(_)).map(_.toGMTString).map("; expires=" + _).getOrElse("")}${path.map("; path=" + _).getOrElse("")}'""")

  def deleteCookie(name: String, path: String): Js = setCookie(name, "", expires = Some(0), path = Some(path))
}

object Js extends JsUtils {
  def apply(s: String): Js = RawJs(s)

}

object JsUtils {

  def generic[E <: FSXmlEnv : FSXmlSupport] = new JsXmlUtils[E]()
}

class JsXmlUtils[E <: FSXmlEnv](implicit fsXmlSupport: FSXmlSupport[E]) extends JsUtils {

  def rerenderable(
                    render: Rerenderer[E] => FSContext => E#Elem,
                    idOpt: Option[String] = None,
                    debugLabel: Option[String] = None,
                    gcOldFSContext: Boolean = true
                  ): Rerenderer[E] =
    new Rerenderer[E](render, idOpt = idOpt, debugLabel = debugLabel, gcOldFSContext = gcOldFSContext)

  def rerenderableP[P](
                        render: RerendererP[E, P] => FSContext => P => E#Elem,
                        idOpt: Option[String] = None,
                        debugLabel: Option[String] = None,
                        gcOldFSContext: Boolean = true
                      ): RerendererP[E, P] = new RerendererP[E, P](render, idOpt = idOpt, debugLabel = debugLabel, gcOldFSContext = gcOldFSContext)

  def rerenderableContents(
                            render: ContentRerenderer[E] => FSContext => E#NodeSeq,
                            id: Option[String] = None,
                            debugLabel: Option[String] = None,
                            gcOldFSContext: Boolean = true
                          ): ContentRerenderer[E] =
    new ContentRerenderer[E](render, id = id, debugLabel = debugLabel, gcOldFSContext = gcOldFSContext)

  def rerenderableContentsP[P](
                                render: ContentRerendererP[E, P] => FSContext => P => E#NodeSeq,
                                id: Option[String] = None,
                                debugLabel: Option[String] = None,
                                gcOldFSContext: Boolean = true
                              ): ContentRerendererP[E, P] =
    new ContentRerendererP[E, P](render, id = id, debugLabel = debugLabel, gcOldFSContext = gcOldFSContext)

  def append2Body(ns: E#NodeSeq): Js = {
    val elemId = IdGen.id("template")
    Js(s"document.body.appendChild(${htmlToElement(ns, elemId).cmd})") &
      removeId(elemId)
  }

  def append2(id: String, ns: E#NodeSeq): Js = {
    val elemId = IdGen.id("template")
    Js(s"""document.getElementById("${escapeStr(id)}").appendChild(${htmlToElement(ns, elemId).cmd})""") &
      removeId(elemId)
  }

  def prepend2(id: String, ns: E#NodeSeq): Js = {
    val elemId = IdGen.id("template")
    Js(s"""document.getElementById("${escapeStr(id)}").insertBefore(${htmlToElement(ns, elemId).cmd}, document.getElementById("${escapeStr(id)}").firstChild)""") &
      removeId(elemId)
  }

  def replace(id: String, by: E#NodeSeq): Js = Js(s"""(document.getElementById("${escapeStr(id)}") ? document.getElementById("${escapeStr(id)}").replaceWith(${htmlToElement(by).cmd}) : console.error("Element with id ${escapeStr(id)} not found"));""")

  def setContents(id: String, ns: E#NodeSeq): Js = Js(s"""document.getElementById("${escapeStr(id)}").innerHTML = "${StringEscapeUtils.escapeEcmaScript(ns.toString())}"; """)

  def htmlToElement(html: E#NodeSeq, templateId: String = IdGen.id): Js = Js {
    s"""(document.body.appendChild((function htmlToElement(html) {var template = document.createElement('template');template.setAttribute("id", "$templateId");template.innerHTML = html.trim(); return template;})("${StringEscapeUtils.escapeEcmaScript(html.toString())}"))).content"""
  }

  def forceHttps: E#Elem = {
    implicitly[FSXmlSupport[E]].buildElem("script", "type" -> "text/javascript")(
      implicitly[FSXmlSupport[E]].buildUnparsed(
        """//<![CDATA[
          |if (location.protocol !== 'https:' && location.hostname !== 'localhost') { location.protocol = 'https:'; }
          |//]]>""".stripMargin
      )
    )
  }

  def inScriptTag(js: Js): E#Elem = {
    implicitly[FSXmlSupport[E]].buildElem("script", "type" -> "text/javascript")(
      implicitly[FSXmlSupport[E]].buildUnparsed(
        """
// <![CDATA[
""" + js +
          """
// ]]>
"""
      )
    )
  }

  def showIf(b: Boolean)(ns: => E#NodeSeq): E#NodeSeq = if (b) ns else fsXmlSupport.Empty
}

class RichJsXmlUtils[E <: FSXmlEnv](js: Js, utils: JsXmlUtils[E])(implicit fsXmlSupport: FSXmlSupport[E]) {

  def inScriptTag: E#Elem = utils.inScriptTag(js)

  def printBeforeExec: Js = utils.consoleLog(js.cmd) & js
}