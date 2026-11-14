package com.fastscala.js

import Js as JS

import com.fastscala.core.FSContext
import com.fastscala.utils.IdGen
import org.apache.commons.text.StringEscapeUtils
import org.eclipse.jetty.http.{HttpHeader, MimeTypes}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Response
import org.eclipse.jetty.util.{BufferUtil, Callback, IteratingCallback}

import java.security.SecureRandom
import java.util.Date
import org.owasp.encoder.Encode

import java.nio.ByteBuffer

class CompositeJs private(val children: Js*)(val separator: Js = Js.Void) extends Js {

  def this(children: Js*) = this(children *)(Js.Void)

  def withSeparator(sep: Js) = new CompositeJs(children *)(sep)

  override def cmd: String = children.map(_.cmd).mkString(separator.cmd)

  /** The children's parts, with the separator's between them: `cmd`, without joining it. */
  override def foreachPart(f: String => Unit): Unit = {
    var first = true
    children.foreach { child =>
      if (!first && (separator ne Js.Void)) separator.foreachPart(f)
      first = false
      child.foreachPart(f)
    }
  }

  override def lengthUtf8Enc: Long =
    children.map(_.lengthUtf8Enc).sum + separator.lengthUtf8Enc * math.max(0, children.size - 1)

  /**
   * Writes the children (with separators between them) one at a time: each part's write starts only once the previous
   * one has completed, and `callback` completes exactly once — after the last write, or on the first failure.
   * Every part's `writeUtf8Enc` must complete the callback it is given exactly once.
   */
  override def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit = {
    val parts: Iterator[Js] =
      if (separator == Js.Void) children.iterator
      else children.iterator.zipWithIndex.flatMap((child, idx) => if (idx == 0) Iterator(child) else Iterator(separator, child))

    new IteratingCallback() {
      override def process(): IteratingCallback.Action =
        if (parts.hasNext) {
          parts.next().writeUtf8Enc(sink, this) // calls succeeded()/failed() on `this`, which re-enters process()
          IteratingCallback.Action.SCHEDULED
        } else IteratingCallback.Action.SUCCEEDED

      override def onCompleteSuccess(): Unit = callback.succeeded()

      override def onCompleteFailure(cause: Throwable): Unit = callback.failed(cause)
    }.iterate()
  }
}

class StringJs(str: String) extends Js {
  override def cmd: String = str

  override def foreachPart(f: String => Unit): Unit = f(str)

  lazy val lengthUtf8Enc: Long = com.google.common.base.Utf8.encodedLength(str)

  override def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit = sink.write(false, ByteBuffer.wrap(str.getBytes("UTF-8")), callback)
}

trait Js {

  def lengthUtf8Enc: Long

  def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit

  def cmd: String

  /**
   * Calls `f` with the parts of [[cmd]], in order: `cmd` is them concatenated. So `cmd` can be written (e.g. escaped,
   * by XmlStream, into an attribute) without being built. By default, `cmd` itself, as one part.
   */
  def foreachPart(f: String => Unit): Unit = f(cmd)

  def &(js: Js) = (this, js) match {
    case (Js.Void, Js.Void) => Js.Void
    case (Js.Void, js) => js
    case (js, Js.Void) => js
    case (js1, js2) => Js(js1, Js(";"), js2)
  }

  def isVoid = cmd.trim == ""

  def onDOMContentLoaded: Js = JS(
    Js("(function () {var load = function() {"),
    this,
    Js(";};if (/complete|interactive|loaded/.test(document.readyState)) {load();}else { document.addEventListener('DOMContentLoaded', function() {load();}, false); }})();"),
  )

  override def toString: String = cmd
}

class JsFunc0(body0: Js) extends JsFunc1(_ => body0) {
  override val children: Seq[Js] = Seq(Js(s"""function(){"""), body0, Js("}"))
}

class JsFunc1(body1: Js => Js) extends JsFunc2((arg1, _) => body1(arg1)) {
  override val children: Seq[Js] = Seq(Js(s"""function(arg1){"""), body1(Js("arg1")), Js("}"))
}

class JsFunc2(body2: (Js, Js) => Js) extends JsFunc3((arg1, arg2, _) => body2(arg1, arg2)) {
  override val children: Seq[Js] = Seq(Js(s"""function(arg1, arg2){"""), body2(Js("arg1"), Js("arg2")), Js("}"))
}

class JsFunc3(body3: (Js, Js, Js) => Js) extends CompositeJs(
  Js(s"""function(arg1, arg2, arg3){"""), body3(Js("arg1"), Js("arg2"), Js("arg3")), Js("}")
)

object Js {

  val Void = com.fastscala.js.Void

  def apply(js: String): Js = if (js == "") Void else StringJs(js)

  def apply(js: Js*): Js = if (js.isEmpty) Void else CompositeJs(js *)
}

object Void extends StringJs("")

object JsHelper {

  val secureRandom = new SecureRandom()

  implicit class RichJs(val js: Js) extends AnyVal {

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

    def `_===null`: Js = Js(s"${js.cmd} === null")

    def asVar(code: Js => Js): Js = {
      val name = "v_" + "%04X".formatted(secureRandom.nextInt())
      Js(s"var $name = (${js.cmd}); " + code(Js(name)))
    }
  }

}

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

  def reloadPageWithQueryParam(key: String, value: String): Js = JS {
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
