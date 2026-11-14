package com.fastscala.js

import org.owasp.encoder.Encode

import java.util.Date

/**
 * Helpers that build JavaScript. Written with `js"..."` ([[RawJs]]): the Js values in them stay parts of the result
 * (so they're never turned into one String, e.g. when XmlStream writes them into an attribute), and Strings are
 * inserted as JavaScript string literals with [[asJsStr]] (quoted, and escaped with OWASP's `Encode.forJavaScript`).
 */
trait JsUtils {

  import JsHelper.*

  def evalIf(cond: Boolean)(js: => Js): Js = if (cond) js else this.void

  def _if(cond: Js, _then: Js, _else: Js = this.void): Js = js"if($cond) {${_then}} else {${_else}}"

  def _trenaryOp(cond: Js, _then: Js, _else: Js = this.void): Js = js"(($cond) ? (${_then}) : (${_else}))"

  def void: Js = Void

  def log(js: Js): Js = js"console.log(eval(${asJsStr(js.cmd)}));"

  def void[T](code: () => T): Js = {
    code()
    void
  }

  val _null: Js = Js("null")
  val _true: Js = Js("true")
  val _false: Js = Js("false")

  def fromString(s: String): Js = Js(s)

  def escapeStr(s: String) = Encode.forJavaScript(s)

  /** `s` as a JavaScript string literal: quoted, and escaped. */
  def asJsStr(s: String): Js = new EncJs(Js(s), inString = true)

  // ---------- elements, by id ----------

  /** The element with id `id` (an expression). */
  def elementById(id: String): Js = js"document.getElementById(${asJsStr(id)})"

  def elementValueById(id: String): Js = js"${elementById(id)}.value"

  def setElementValue(id: String, value: String): Js = js"${elementById(id)}.value = ${asJsStr(value)}"

  def isCheckedById(id: String): Js = js"${elementById(id)}.checked"

  def checkboxIsChecked(id: String): Js = isCheckedById(id)

  def setIndeterminate(id: String): Js = js"${elementById(id)}.indeterminate = true"

  def setChecked(id: String, checked: Boolean): Js = js"${elementById(id)}.checked = $checked"

  def setCheckboxTo(id: String, checked: Option[Boolean]): Js = checked.map(setChecked(id, _)).getOrElse(setIndeterminate(id))

  def removeId(id: String): Js = js"${elementById(id)}.remove();"

  def show(id: String): Js = js"""${elementById(id)}.style.display = "";"""

  def hide(id: String): Js = js"""${elementById(id)}.style.display = "none";"""

  def focus(id: String): Js = js"${elementById(id)}.focus();"

  def select(id: String): Js = js"${elementById(id)}.select();"

  def blur(id: String): Js = js"${elementById(id)}.blur();"

  def getAttr(id: String, name: String): Js = js"${elementById(id)}.getAttribute(${asJsStr(name)})"

  def setAttr(id: String)(name: String, value: String): Js = js"${elementById(id)}.setAttribute(${asJsStr(name)}, ${asJsStr(value)})"

  def removeAttr(id: String, name: String): Js = js"${elementById(id)}.removeAttribute(${asJsStr(name)})"

  /** The classes in `clas` (separated by spaces), as JavaScript string literals separated by `, `. */
  private def classesAsJsStrs(clas: String): Js = new CompositeJs(clas.split(" +").toSeq.map(asJsStr) *).withSeparator(Js(", "))

  def addClass(id: String, clas: String): Js = js"${elementById(id)}.classList.add(${classesAsJsStrs(clas)})"

  def removeClass(id: String, clas: String): Js = js"${elementById(id)}.classList.remove(${classesAsJsStrs(clas)})"

  // ---------- selectors ----------

  def selectedValues(elem: Js): Js = js"""Array.from($elem.querySelectorAll("option:checked"),e=>e.value)"""

  def forEachQuerySelector(selector: String, func: JsFunc1): Js = js"document.querySelectorAll(${asJsStr(selector)}).forEach($func)"

  def addClassToElemsMatchingSelector(selector: String, clas: String): Js =
    js"document.querySelectorAll(${asJsStr(selector)}).forEach(el=>el.classList.add(${classesAsJsStrs(clas)}))"

  def removeClassFromElemsMatchingSelector(selector: String, clas: String): Js =
    js"document.querySelectorAll(${asJsStr(selector)}).forEach(el=>el.classList.remove(${classesAsJsStrs(clas)}))"

  // ---------- code structure ----------

  /** `code`, with `name` set to `value` (a statement). `name` must be a JavaScript identifier. */
  def withVarStmt(name: String, value: Js)(code: Js => Js): Js = js"(function ($name) {${code(Js(name))}})($value);"

  /** `code`'s value, with `name` set to `value` (an expression). `name` must be a JavaScript identifier. */
  def withVarExpr(name: String, value: Js)(code: Js => Js): Js = js"(function ($name) {return ${code(Js(name))};})($value);"

  def valueOrElse(value: Js, default: Js) = withVarExpr("value", value) {
    value => this._trenaryOp(value `_!=` _null, _then = value, _else = default)
  }

  def wrapAsExpr(stmt: Js*)(expr: Js): Js = js"((function () {${stmt.reduceOption(_ & _).getOrElse(this.void)}; return $expr;})())"

  def varOrElseUpdate(variable: Js, defaultValue: Js) =
    this._trenaryOp(variable `_!=` _null, _then = variable, _else = wrapAsExpr(variable.`_=`(defaultValue))(variable))

  def function0(body: Js): Js = new JsFunc0(body)

  def function1(body: Js => Js): Js = new JsFunc1(body)

  def function2(body: (Js, Js) => Js): Js = new JsFunc2(body)

  def function3(body: (Js, Js, Js) => Js): Js = new JsFunc3(body)

  def onload(js: Js): Js = js"$$(document).ready(function() { $js });"

  def onkeypress(codes: Int*)(js: Js): Js =
    js"event = event || window.event; if (${codes.map(code => s"(event.keyCode ? event.keyCode : event.which) == $code").mkString(" || ")}) {$js};"

  def setTimeout(js: Js, timeout: Long): Js = js"setTimeout(function(){ $js }, $timeout);"

  def undefined(js: Js): Js = js"($js === undefined)"

  def catchAndLogErrors(js: Js): Js = js"try {$js} catch (error) { console.error(error); }"

  // ---------- the browser ----------

  def alert(text: String): Js = js"alert(${asJsStr(text)});"

  def confirm(text: String, js: Js): Js = js"if(confirm(${asJsStr(text)})) {$js};"

  def copy2Clipboard(text: String): Js = js"navigator.clipboard.writeText(${asJsStr(text)});"

  def consoleLog(js: Js): Js = js"console.log($js);"

  def consoleLog(str: String): Js = this.consoleLog(this.asJsStr(str))

  def redirectTo(link: String): Js = js"window.location.href = ${asJsStr(link)};"

  def reloadPageWithQueryParam(key: String, value: String): Js = Js(
    js"var searchParams = new URLSearchParams(window.location.search);",
    js"searchParams.set(${asJsStr(key)}, ${asJsStr(value)});",
    js"window.location.search = searchParams.toString();",
  )

  /** Goes `n` pages back in the history. */
  def goBack(n: Int = 1): Js = if (n == 1) js";window.history.back();" else js";window.history.go(-$n);"

  def reload(): Js = js"location.reload();"

  /** Sets the cookie: `name=cookie`, with its expiry date and path, if given (all of it escaped, as a string literal). */
  def setCookie(name: String, cookie: String, expires: Option[Long] = None, path: Option[String] = None): Js = {
    val value = s"$name=$cookie" +
      expires.map(new Date(_)).map(_.toGMTString).map("; expires=" + _).getOrElse("") +
      path.map("; path=" + _).getOrElse("")
    js"document.cookie=${asJsStr(value)}"
  }

  def deleteCookie(name: String, path: String): Js = setCookie(name, "", expires = Some(0), path = Some(path))
}
