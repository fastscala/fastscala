package com.fastscala.js

import com.fastscala.core.FSContext
import com.fastscala.utils.IdGen
import org.apache.commons.text.StringEscapeUtils
import org.eclipse.jetty.http.{HttpHeader, MimeTypes}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Response
import org.eclipse.jetty.util.{BufferUtil, Callback, IteratingCallback}
import org.owasp.encoder.Encode

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Date



trait JsUtils {

  import JsHelper.*

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

  def escapeStr(s: String) = Encode.forJavaScript(s)

  def asJsStr(s: String): Js = Js(s""""${escapeStr(s)}"""")

  def elementById(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}")""")

  def elementValueById(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").value""")

  def setElementValue(id: String, value: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").value = "${escapeStr(value)}"""")

  def isCheckedById(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").checked""")

  def setIndeterminate(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").indeterminate = true""")

  def setChecked(id: String, checked: Boolean): Js = Js(s"""document.getElementById("${escapeStr(id)}").checked = $checked""")

  def setCheckboxTo(id: String, checked: Option[Boolean]): Js = checked.map(checked => Js(s"""document.getElementById("${escapeStr(id)}").checked = $checked""")).getOrElse(setIndeterminate(id))

  def selectedValues(elem: Js): Js = Js(s"""Array.from(${elem.cmd}.querySelectorAll("option:checked"),e=>e.value)""")

  def withVarStmt(name: String, value: Js)(code: Js => Js): Js = Js(s"""(function ($name) {${code(Js(name)).cmd}})(${value.cmd});""")

  def withVarExpr(name: String, value: Js)(code: Js => Js): Js = Js(s"""(function ($name) {return ${code(Js(name)).cmd};})(${value.cmd});""")

  def valueOrElse(value: Js, default: Js) = withVarExpr("value", value) {
    value => this._trenaryOp(value `_!=` _null, _then = value, _else = default)
  }

  def wrapAsExpr(stmt: Js*)(expr: Js): Js = Js(s"""((function () {${stmt.reduceOption(_ & _).getOrElse(this.void).cmd}; return ${expr.cmd};})())""")

  def varOrElseUpdate(variable: Js, defaultValue: Js) =
    this._trenaryOp(variable `_!=` _null, _then = variable, _else = wrapAsExpr(variable.`_=`(defaultValue))(variable))

  def checkboxIsChecked(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").checked""")

  def alert(text: String): Js = Js(s"""alert("${escapeStr(text)}");""")

  def function0(body: Js): Js = Js(s"""function(){$body}""")

  def function1(body: Js => Js): Js = Js(s"""function(arg1){${body(Js("arg1"))}}""")

  def function2(body: (Js, Js) => Js): Js = Js(s"""function(arg1, arg2){${body(Js("arg1"), Js("arg2"))}}""")

  def function3(body: (Js, Js, Js) => Js): Js = Js(s"""function(arg1, arg2, arg3){${body(Js("arg1"), Js("arg2"), Js("arg3"))}}""")

  def copy2Clipboard(text: String): Js = Js(s"navigator.clipboard.writeText('${escapeStr(text)}');")

  def consoleLog(js: Js): Js = Js(s"""console.log(${js.cmd});""")

  def consoleLog(str: String): Js = this.consoleLog(this.asJsStr(str))

  def confirm(text: String, js: Js): Js = Js(s"""if(confirm("${escapeStr(text)}")) {${js.cmd}};""")

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

  def show(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").style.display = "";""")

  def undefined(js: Js): Js = Js(s"""($js === undefined)""")

  def hide(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").style.display = "none";""")

  def focus(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").focus();""")

  def select(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").select();""")

  def blur(id: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").blur();""")

  def getAttr(id: String, name: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").getAttribute(${this.asJsStr(name)})""")

  def setAttr(id: String)(name: String, value: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").setAttribute(${this.asJsStr(name)}, ${this.asJsStr(value)})""")

  def removeAttr(id: String, name: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").removeAttribute(${this.asJsStr(name)})""")

  def forEachQuerySelector(selector: String, func: JsFunc1): Js = Js(s"""document.querySelectorAll(${this.asJsStr(selector)}).forEach($func)""")

  def addClass(id: String, clas: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").classList.add(${clas.split(" +").map(clas => this.asJsStr(clas)).mkString(", ")})""")

  def addClassToElemsMatchingSelector(selector: String, clas: String): Js = Js(s"""document.querySelectorAll(${this.asJsStr(selector)}).forEach(el=>el.classList.add(${clas.split(" +").map(clas => this.asJsStr(clas)).mkString(", ")}))""")

  def removeClass(id: String, clas: String): Js = Js(s"""document.getElementById("${escapeStr(id)}").classList.remove(${clas.split(" +").map(clas => this.asJsStr(clas)).mkString(", ")})""")

  def removeClassFromElemsMatchingSelector(selector: String, clas: String): Js = Js(s"""document.querySelectorAll(${this.asJsStr(selector)}).forEach(el=>el.classList.remove(${clas.split(" +").map(clas => this.asJsStr(clas)).mkString(", ")}))""")

  def setCookie(name: String, cookie: String, expires: Option[Long] = None, path: Option[String] = None): Js =
    Js(s"""document.cookie='$name=${escapeStr(cookie)};${expires.map(new Date(_)).map(_.toGMTString).map("; expires=" + _).getOrElse("")}${path.map("; path=" + _).getOrElse("")}'""")

  def deleteCookie(name: String, path: String): Js = setCookie(name, "", expires = Some(0), path = Some(path))

  def catchAndLogErrors(js: Js): Js = Js(s"""try {${js.cmd}} catch (error) { console.error(error); }""")
}
