package com.fastscala.js

import com.fastscala.core.FSContext
import com.fastscala.js.Js.consoleLog
import com.fastscala.utils.ElemTransformers.RichElem
import com.fastscala.utils.IdGen
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.text.StringEscapeUtils

import java.util.Date
import scala.xml.{Elem, NodeSeq, Unparsed}

trait Js {

  def cmd: String

  def cmdEscaped: String = {
    println("IN: " + cmd)
    println("OUT: " + "\"" + StringEscapeUtils.escapeEcmaScript(cmd.replaceAll("\n", "")) + "\"")
    "'" + StringEscapeUtils.escapeEcmaScript(cmd.replaceAll("\n", "")) + "'"
  }

  def debug(name: String): Js = {
    consoleLog(Js.asJsStr(s"Running $name...")) &
      this &
      consoleLog(Js.asJsStr(s"Finished $name."))
  }

  def printToConsoleBefore(): Js = {
    consoleLog(Js.asJsStr(s"Running:")) &
      consoleLog(Js.asJsStr(cmd)) &
      this
  }

  def &(js: Js) = RawJs(cmd + ";" + js.cmd)

  def onDOMContentLoaded: Js = Js {
    s"""if (/complete|interactive|loaded/.test(document.readyState)) {$cmd}
       |else { document.addEventListener('DOMContentLoaded', function() {$cmd}, false); }""".stripMargin
  }

  def inScriptTag: Elem = {
    <script type="text/javascript">{Unparsed(
      """
// <![CDATA[
""" + cmd +
        """
// ]]>
""")}</script>
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

class Rerenderer(
                  renderFunc: Rerenderer => FSContext => Elem,
                  idOpt: Option[String] = None,
                  debugLabel: Option[String] = None,
                  gcOldFSContext: Boolean = true
                ) {

  var aroundId = idOpt.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render()(implicit fsc: FSContext) = {
    rootRenderContext = Some(fsc)
    val rendered = renderFunc(this)({
      if (gcOldFSContext) fsc.createNewChildContextAndGCExistingOne(this, debugLabel = debugLabel)
      else fsc
    })
    rendered.getId match {
      case Some(id) =>
        aroundId = id
        rendered
      case None => rendered.withIdIfNotSet(aroundId)
    }
  }

  def rerender() = Js.replace(aroundId, render()(rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")

  def replaceBy(elem: Elem): Js = Js.replace(aroundId, elem.withId(aroundId))

  def replaceContentsBy(elem: Elem): Js = Js.setContents(aroundId, elem)

  def map(f: Elem => Elem) = {
    val out = this
    new Rerenderer(null, None, None) {
      override def render()(implicit fsc: FSContext): Elem = f(out.render())

      override def rerender() = Js.replace(out.aroundId,
        f(out.render()(out.rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?"))))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")

      override def replaceBy(elem: Elem): Js = out.replaceBy(elem)

      override def replaceContentsBy(elem: Elem): Js = out.replaceContentsBy(elem)
    }
  }
}

class RerendererP[P](
                      renderFunc: RerendererP[P] => FSContext => P => Elem,
                      idOpt: Option[String] = None,
                      debugLabel: Option[String] = None,
                      gcOldFSContext: Boolean = true
                    ) {

  var aroundId = idOpt.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render(param: P)(implicit fsc: FSContext) = {
    rootRenderContext = Some(fsc)
    val rendered = renderFunc(this)({
      if (gcOldFSContext) fsc.createNewChildContextAndGCExistingOne(this, debugLabel = debugLabel)
      else fsc
    })(param)
    rendered.getId match {
      case Some(id) =>
        aroundId = id
        rendered
      case None => rendered.withIdIfNotSet(aroundId)
    }
  }

  def rerender(param: P) = Js.replace(aroundId, render(param)(rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")

  def replaceBy(elem: Elem): Js = Js.replace(aroundId, elem.withId(aroundId))

  def replaceContentsBy(elem: Elem): Js = Js.setContents(aroundId, elem)

  def map(f: Elem => Elem) = {
    val out = this
    new RerendererP[P](null, None, None) {
      override def render(param: P)(implicit fsc: FSContext): Elem = f(out.render(param))

      override def rerender(param: P) = Js.replace(out.aroundId,
        f(out.render(param)(out.rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?"))))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")

      override def replaceBy(elem: Elem): Js = out.replaceBy(elem)

      override def replaceContentsBy(elem: Elem): Js = out.replaceContentsBy(elem)
    }
  }
}

class ContentRerenderer(
                         renderFunc: ContentRerenderer => FSContext => NodeSeq
                         , outterElem: Elem = <div></div>,
                         id: Option[String] = None,
                         debugLabel: Option[String] = None,
                         gcOldFSContext: Boolean = true
                       ) {

  val aroundId = id.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render()(implicit fsc: FSContext) = {
    rootRenderContext = Some(fsc)
    outterElem.withIdIfNotSet(aroundId).apply(renderFunc(this)({
      if (gcOldFSContext) fsc.createNewChildContextAndGCExistingOne(this, debugLabel = debugLabel)
      else fsc
    }))
  }

  def rerender() = Js.replace(aroundId, render()(rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")
}

class ContentRerendererP[P](
                             renderFunc: ContentRerendererP[P] => FSContext => P => NodeSeq
                             , outterElem: Elem = <div></div>,
                             id: Option[String] = None,
                             debugLabel: Option[String] = None,
                             gcOldFSContext: Boolean = true
                           ) {

  val aroundId = id.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render(param: P)(implicit fsc: FSContext) = {
    rootRenderContext = Some(fsc)
    outterElem.withIdIfNotSet(aroundId)(renderFunc.apply(this)({
      if (gcOldFSContext) fsc.createNewChildContextAndGCExistingOne(this, debugLabel = debugLabel)
      else fsc
    })(param))
  }

  def rerender(param: P) = Js.replace(aroundId, render(param)(rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")
}

object Js {

  def apply(s: String): Js = RawJs(s)

  implicit class JsUtils(js: Js) {

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

  def rerenderable(
                    render: Rerenderer => FSContext => Elem,
                    idOpt: Option[String] = None,
                    debugLabel: Option[String] = None,
                    gcOldFSContext: Boolean = true
                  ): Rerenderer = new Rerenderer(render, idOpt = idOpt, debugLabel = debugLabel, gcOldFSContext = gcOldFSContext)

  def rerenderableP[P](
                        render: RerendererP[P] => FSContext => P => Elem,
                        idOpt: Option[String] = None,
                        debugLabel: Option[String] = None,
                        gcOldFSContext: Boolean = true
                      ): RerendererP[P] = new RerendererP[P](render, idOpt = idOpt, debugLabel = debugLabel, gcOldFSContext = gcOldFSContext)

  def rerenderableContents(
                            render: ContentRerenderer => FSContext => NodeSeq,
                            outterElem: Elem = <div></div>,
                            id: Option[String] = None,
                            debugLabel: Option[String] = None,
                            gcOldFSContext: Boolean = true
                          ): ContentRerenderer =
    new ContentRerenderer(render, outterElem = outterElem, id = id, debugLabel = debugLabel, gcOldFSContext = gcOldFSContext)


  def rerenderableContentsP[P](
                                render: ContentRerendererP[P] => FSContext => P => NodeSeq,
                                outterElem: Elem = <div></div>,
                                id: Option[String] = None,
                                debugLabel: Option[String] = None,
                                gcOldFSContext: Boolean = true
                              ): ContentRerendererP[P] =
    new ContentRerendererP[P](render, outterElem = outterElem, id = id, debugLabel = debugLabel, gcOldFSContext = gcOldFSContext)

  def evalIf(cond: Boolean)(js: => Js): Js = if (cond) js else Js.void

  def _if(cond: Js, _then: Js, _else: Js = Js.void): Js = Js(s"if(${cond.cmd}) {${_then}} else {${_else}}")

  def _trenaryOp(cond: Js, _then: Js, _else: Js = Js.void): Js = Js(s"((${cond.cmd}) ? (${_then}) : (${_else}))")

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

  def selectedValues(elem: Js) = Js(s"""Array.from(${elem.cmd}.querySelectorAll("option:checked"),e=>e.value)""")

  def withVarStmt(name: String, value: Js)(code: Js => Js) = Js(s"""(function ($name) {${code(Js(name)).cmd}})(${value.cmd});""")

  def withVarExpr(name: String, value: Js)(code: Js => Js) = Js(s"""(function ($name) {return ${code(Js(name)).cmd};})(${value.cmd});""")

  def valueOrElse(value: Js, default: Js) = withVarExpr("value", value) {
    value => Js._trenaryOp(value `_!=` _null, _then = value, _else = default)
  }

  def wrapAsExpr(stmt: Js*)(expr: Js) = Js(s"""((function () {${stmt.reduceOption(_ & _).getOrElse(Js.void).cmd}; return ${expr.cmd};})())""")

  def varOrElseUpdate(variable: Js, defaultValue: Js) =
    Js._trenaryOp(variable `_!=` _null, _then = variable, _else = wrapAsExpr(variable.`_=`(defaultValue))(variable))

  def checkboxIsChecked(id: String) = Js(s"""document.getElementById("${escapeStr(id)}").checked""")

  def append2Body(ns: NodeSeq): Js = {
    val elemId = IdGen.id("template")
    Js(s"document.body.appendChild(${htmlToElement(ns, elemId).cmd})") &
      removeId(elemId)
  }

  def append2(id: String, ns: NodeSeq): Js = {
    val elemId = IdGen.id("template")
    Js(s"""document.getElementById("${escapeStr(id)}").appendChild(${htmlToElement(ns, elemId).cmd})""") &
      removeId(elemId)
  }

  def prepend2(id: String, ns: NodeSeq): Js = {
    val elemId = IdGen.id("template")
    Js(s"""document.getElementById("${escapeStr(id)}").insertBefore(${htmlToElement(ns, elemId).cmd}, document.getElementById("${escapeStr(id)}").firstChild)""") &
      removeId(elemId)
  }

  def alert(text: String): Js = Js(s"""alert("${escapeStr(text)}");""")

  def toClipboard(text: String): Js = Js(s"navigator.clipboard.writeText('${escapeStr(text)}');")

  def consoleLog(js: Js): Js = Js(s"""console.log(${js.cmd});""")

  def consoleLog(str: String): Js = Js.consoleLog(Js.asJsStr(str))

  def confirm(text: String, js: Js) = Js(s"""if(confirm("${escapeStr(text)}")) {${js.cmd}};""")

  def redirectTo(link: String): Js = Js(s"""window.location.href = "${escapeStr(link)}";""")

  def reloadPageWithQueryParam(key: String, value: String): Js = Js {
    s"""var searchParams = new URLSearchParams(window.location.search);
       |searchParams.set(${Js.asJsStr(key)}, ${Js.asJsStr(value)});
       |window.location.search = searchParams.toString();""".stripMargin
  }

  def goBack(n: Int = 1): Js = Js(s""";window.history.back();""")

  def onload(js: Js): Js = Js(s"""$$(document).ready(function() { ${js.cmd} });""")

  def onkeypress(codes: Int*)(js: Js): Js = Js(s"event = event || window.event; if (${codes.map(code => s"(event.keyCode ? event.keyCode : event.which) == $code").mkString(" || ")}) {${js.cmd}};")

  def reload(): Js = Js(s"""location.reload();""")

  def setTimeout(js: Js, timeout: Long): Js = Js(s"""setTimeout(function(){ ${js.cmd} }, $timeout);""")

  def removeId(id: String): Js = Js(s"""document.getElementById("$id").remove();""")

  def replace(id: String, by: NodeSeq): Js = Js(s"""(document.getElementById("${escapeStr(id)}") ? document.getElementById("${escapeStr(id)}").replaceWith(${htmlToElement(by).cmd}) : console.error("Element with id ${escapeStr(id)} not found"));""")

  def setContents(id: String, ns: NodeSeq): Js = Js(s"""document.getElementById("${escapeStr(id)}").innerHTML = "${StringEscapeUtils.escapeEcmaScript(ns.toString())}"; """)

  def show(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").style.display = "block";""")

  def hide(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").style.display = "none";""")

  def focus(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").focus();""")

  def select(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").select();""")

  def blur(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").blur();""")

  def addClass(id: String, clas: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").classList.add(${Js.asJsStr(clas)})""")

  def removeClass(id: String, clas: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").classList.remove(${Js.asJsStr(clas)})""")

  def setCookie(name: String, cookie: String, expires: Option[Long] = None, path: Option[String] = None): Js =
    Js(s"""document.cookie='$name=${escapeStr(cookie)};${expires.map(new Date(_)).map(_.toGMTString).map("; expires=" + _).getOrElse("")}${path.map("; path=" + _).getOrElse("")}'""")

  def deleteCookie(name: String): Js = setCookie(name, "")

  def htmlToElement(html: NodeSeq, templateId: String = IdGen.id): Js = Js {
    s"""(document.body.appendChild((function htmlToElement(html) {var template = document.createElement('template');template.setAttribute("id", "$templateId");template.innerHTML = html.trim(); return template;})("${StringEscapeUtils.escapeEcmaScript(html.toString())}"))).content"""
  }

  def forceHttps: Elem = {
    <script type="text/javascript">
      //<![CDATA[
      if (location.protocol !== 'https:' && location.hostname !== 'localhost') { location.protocol = 'https:'; }
      //]]>
    </script>
  }
}