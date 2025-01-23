package com.fastscala.js

import com.fastscala.core.FSContext
import com.fastscala.utils.IdGen
import org.apache.commons.text.StringEscapeUtils
import org.eclipse.jetty.http.{HttpHeader, MimeTypes}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Response
import org.eclipse.jetty.util.BufferUtil

import java.util.Date

import Js as JS

trait Js {

  def cmd: String

  def cmdEscaped: String = "'" + StringEscapeUtils.escapeEcmaScript(cmd.replaceAll("\n", "")) + "'"

  def &(js: Js) = RawJs(cmd + ";" + js.cmd)

  def onDOMContentLoaded: Js = JS {
    s"""(function () {
       |  var load = function() {
       |    $cmd;
       |  };
       |  if (/complete|interactive|loaded/.test(document.readyState)) {load();}
       |  else { document.addEventListener('DOMContentLoaded', function() {load();}, false); }
       |})();""".stripMargin
  }

  def writeTo(
               resp: Response,
               status: Int = 200,
               contentType: String = "application/javascript; charset=utf-8"
             ): Unit = {
    resp.setStatus(status)
    resp.getHeaders.put(HttpHeader.CONTENT_TYPE, contentType)
    val charsetName = Option(MimeTypes.getCharsetFromContentType(contentType)).getOrElse("UTF-8")
    Content.Sink.write(resp, true, BufferUtil.toBuffer(cmd.getBytes(charsetName)))
  }

  override def toString: String = cmd
}

object Js {
  def apply(js: String): Js = RawJs(js)
}

case class RawJs(js: String) extends Js {
  override def cmd: String = js
}

object JsOps {

  implicit class RichJs(val js: Js) extends AnyVal {

    def `_==`(other: Js): Js = JS(s"${js.cmd} == ${other.cmd}")

    def `_!=`(other: Js): Js = JS(s"${js.cmd} != ${other.cmd}")

    def `_===`(other: Js): Js = JS(s"${js.cmd} === ${other.cmd}")

    def `_!==`(other: Js): Js = JS(s"${js.cmd} !== ${other.cmd}")

    def `_>`(other: Js): Js = JS(s"${js.cmd} > ${other.cmd}")

    def `_>=`(other: Js): Js = JS(s"${js.cmd} >= ${other.cmd}")

    def `_<`(other: Js): Js = JS(s"${js.cmd} < ${other.cmd}")

    def `_<=`(other: Js): Js = JS(s"${js.cmd} <= ${other.cmd}")

    def `_&&`(other: Js): Js = JS(s"${js.cmd} && ${other.cmd}")

    def `_||`(other: Js): Js = JS(s"${js.cmd} || ${other.cmd}")

    def `_=`(other: Js): Js = JS(s"${js.cmd} = ${other.cmd};")
  }

}

trait JsUtils {

  import JsOps.*

  def apply(s: String): Js = RawJs(s)

  def evalIf(cond: Boolean)(js: => Js): Js = if (cond) js else this.void

  def _if(cond: Js, _then: Js, _else: Js = this.void): Js = JS(s"if(${cond.cmd}) {${_then}} else {${_else}}")

  def _trenaryOp(cond: Js, _then: Js, _else: Js = this.void): Js = JS(s"((${cond.cmd}) ? (${_then}) : (${_else}))")

  def void: Js = JS("")

  def log(js: Js): Js = JS(s"""console.log(eval("${escapeStr(js.cmd)}"));""")

  def void[T](code: () => T): Js = {
    code()
    void
  }

  val _null: Js = JS("null")
  val _true: Js = JS("true")
  val _false: Js = JS("false")

  def fromString(s: String): Js = JS(s)

  def escapeStr(s: String) = StringEscapeUtils.escapeEcmaScript(s)

  def asJsStr(s: String): Js = JS(s""""${escapeStr(s)}"""")

  def elementById(id: String): Js = JS(s"""document.getElementById("${escapeStr(id)}")""")

  def elementValueById(id: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").value""")

  def setElementValue(id: String, value: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").value = "${escapeStr(value)}"""")

  def isCheckedById(id: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").checked""")

  def setIndeterminate(id: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").indeterminate = true""")

  def setChecked(id: String, checked: Boolean): Js = JS(s"""document.getElementById("${escapeStr(id)}").checked = $checked""")

  def setCheckboxTo(id: String, checked: Option[Boolean]): Js = checked.map(checked => JS(s"""document.getElementById("${escapeStr(id)}").checked = $checked""")).getOrElse(setIndeterminate(id))

  def selectedValues(elem: Js): Js = JS(s"""Array.from(${elem.cmd}.querySelectorAll("option:checked"),e=>e.value)""")

  def withVarStmt(name: String, value: Js)(code: Js => Js): Js = JS(s"""(function ($name) {${code(JS(name)).cmd}})(${value.cmd});""")

  def withVarExpr(name: String, value: Js)(code: Js => Js): Js = JS(s"""(function ($name) {return ${code(JS(name)).cmd};})(${value.cmd});""")

  def valueOrElse(value: Js, default: Js) = withVarExpr("value", value) {
    value => this._trenaryOp(value `_!=` _null, _then = value, _else = default)
  }

  def wrapAsExpr(stmt: Js*)(expr: Js): Js = JS(s"""((function () {${stmt.reduceOption(_ & _).getOrElse(this.void).cmd}; return ${expr.cmd};})())""")

  def varOrElseUpdate(variable: Js, defaultValue: Js) =
    this._trenaryOp(variable `_!=` _null, _then = variable, _else = wrapAsExpr(variable.`_=`(defaultValue))(variable))

  def checkboxIsChecked(id: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").checked""")

  def alert(text: String): Js = JS(s"""alert("${escapeStr(text)}");""")

  def function()(body: Js): Js = JS(s"""function(){$body}""")

  def copy2Clipboard(text: String): Js = JS(s"navigator.clipboard.writeText('${escapeStr(text)}');")

  def consoleLog(js: Js): Js = JS(s"""console.log(${js.cmd});""")

  def consoleLog(str: String): Js = this.consoleLog(this.asJsStr(str))

  def confirm(text: String, js: Js): Js = JS(s"""if(confirm("${escapeStr(text)}")) {${js.cmd}};""")

  def redirectTo(link: String): Js = JS(s"""window.location.href = "${escapeStr(link)}";""")

  def reloadPageWithQueryParam(key: String, value: String): Js = JS {
    s"""var searchParams = new URLSearchParams(window.location.search);
       |searchParams.set(${this.asJsStr(key)}, ${this.asJsStr(value)});
       |window.location.search = searchParams.toString();""".stripMargin
  }

  def goBack(n: Int = 1): Js = JS(s""";window.history.back();""")

  def onload(js: Js): Js = JS(s"""$$(document).ready(function() { ${js.cmd} });""")

  def onkeypress(codes: Int*)(js: Js): Js = JS(s"event = event || window.event; if (${codes.map(code => s"(event.keyCode ? event.keyCode : event.which) == $code").mkString(" || ")}) {${js.cmd}};")

  def reload(): Js = JS(s"""location.reload();""")

  def setTimeout(js: Js, timeout: Long): Js = JS(s"""setTimeout(function(){ ${js.cmd} }, $timeout);""")

  def removeId(id: String): Js = JS(s"""document.getElementById("$id").remove();""")

  def show(id: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").style.display = "";""")

  def hide(id: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").style.display = "none";""")

  def focus(id: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").focus();""")

  def select(id: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").select();""")

  def blur(id: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").blur();""")

  def setAttr(id: String)(name: String, value: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").setAttribute(${this.asJsStr(name)}, ${this.asJsStr(value)})""")

  def removeAttr(id: String, name: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").removeAttribute(${this.asJsStr(name)})""")

  def addClass(id: String, clas: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").classList.add(${this.asJsStr(clas)})""")

  def addClassToElemsMatchingSelector(selector: String, clas: String): Js = JS(s"""document.querySelectorAll(${this.asJsStr(selector)}).forEach(el=>el.classList.add(${this.asJsStr(clas)}))""")

  def removeClass(id: String, clas: String): Js = JS(s"""document.getElementById("${escapeStr(id)}").classList.remove(${this.asJsStr(clas)})""")

  def removeClassFromElemsMatchingSelector(selector: String, clas: String): Js = JS(s"""document.querySelectorAll(${this.asJsStr(selector)}).forEach(el=>el.classList.remove(${this.asJsStr(clas)}))""")

  def setCookie(name: String, cookie: String, expires: Option[Long] = None, path: Option[String] = None): Js =
    JS(s"""document.cookie='$name=${escapeStr(cookie)};${expires.map(new Date(_)).map(_.toGMTString).map("; expires=" + _).getOrElse("")}${path.map("; path=" + _).getOrElse("")}'""")

  def deleteCookie(name: String, path: String): Js = setCookie(name, "", expires = Some(0), path = Some(path))

  def catchAndLogErrors(js: Js): Js = JS(s"""try {${js.cmd}} catch (error) { console.error(error); }""")
}
