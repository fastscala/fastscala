package com.fastscala.js

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import com.fastscala.js.JsEscape
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Date

/** [[JsUtils]] (and the [[JsHelper]] operators): the JavaScript they build. */
class JsUtilsSpec extends AnyFlatSpec {

  private object J extends JsUtils

  import JsHelper.*

  /** `s` as a JavaScript string literal, as asJsStr writes it. */
  private def q(s: String): String = "\"" + JsEscape.escape(s) + "\""

  private def streamed(js: Js): String = {
    val out = new ByteArrayOutputStream()
    js.writeUtf8Enc(new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
        val arr = new Array[Byte](bb.remaining()); bb.get(arr); out.write(arr); cb.succeeded()
      }
    }, Callback.NOOP)
    out.toString(UTF_8)
  }

  /** The leaves of `js` (a CompositeJs's children, flattened), as their cmds. */
  private def parts(js: Js): Seq[String] = js match {
    case c: CompositeJs => c.children.flatMap(parts)
    case other => Seq(other.cmd)
  }

  /** cmd is `expected`, and so are the parts joined and the streamed bytes. */
  private def check(js: Js, expected: String): Unit = {
    assert(js.cmd == expected)
    assert(parts(js).mkString == expected, "parts")
    assert(streamed(js) == expected, "streamed")
  }

  /** Ids, names and values that must stay inside their string literal. */
  private val hostile = Seq("plain", "a\"b", "a'b", "a\\b", "</script><script>alert(1)</script>", "line\nbreak", "é € 😀", "")

  // ---------- strings ----------

  "asJsStr" should "be a quoted, escaped string literal" in {
    hostile.foreach(s => check(J.asJsStr(s), q(s)))
    // Nothing gets out of the literal: no unescaped quote, backslash sequence ending it, or "</script" inside.
    hostile.foreach { s =>
      val inner = J.asJsStr(s).cmd.drop(1).dropRight(1)
      assert(!inner.contains("\"") && !inner.contains("'") && !inner.toLowerCase.contains("</script"), inner)
    }
  }

  "escapeStr" should "escape for JavaScript (JsEscape)" in {
    hostile.foreach(s => assert(J.escapeStr(s) == JsEscape.escape(s)))
  }

  // ---------- elements, by id ----------

  "elementById" should "be the element, as an expression (no ';': it's used inside other expressions)" in {
    hostile.foreach(id => check(J.elementById(id), s"document.getElementById(${q(id)})"))
    check(J.selectedValues(J.elementById("sel")), s"""Array.from(document.getElementById(${q("sel")}).querySelectorAll("option:checked"),e=>e.value)""")
  }

  "The element helpers" should "use elementById, with the id (and values) escaped" in {
    hostile.foreach { id =>
      val el = s"document.getElementById(${q(id)})"
      check(J.elementValueById(id), s"$el.value")
      check(J.setElementValue(id, "v\"1"), s"$el.value = ${q("v\"1")}")
      check(J.isCheckedById(id), s"$el.checked")
      check(J.checkboxIsChecked(id), s"$el.checked")
      check(J.setIndeterminate(id), s"$el.indeterminate = true")
      check(J.setChecked(id, true), s"$el.checked = true")
      check(J.setChecked(id, false), s"$el.checked = false")
      check(J.setCheckboxTo(id, Some(true)), s"$el.checked = true")
      check(J.setCheckboxTo(id, None), s"$el.indeterminate = true")
      check(J.removeId(id), s"$el.remove();")
      check(J.show(id), s"""$el.style.display = "";""")
      check(J.hide(id), s"""$el.style.display = "none";""")
      check(J.focus(id), s"$el.focus();")
      check(J.select(id), s"$el.select();")
      check(J.blur(id), s"$el.blur();")
      check(J.getAttr(id, "data-x"), s"$el.getAttribute(${q("data-x")})")
      check(J.setAttr(id)("title", "a \"b\""), s"$el.setAttribute(${q("title")}, ${q("a \"b\"")})")
      check(J.removeAttr(id, "title"), s"$el.removeAttribute(${q("title")})")
      check(J.addClass(id, "a  b c"), s"$el.classList.add(${q("a")}, ${q("b")}, ${q("c")})")
      check(J.removeClass(id, "a"), s"$el.classList.remove(${q("a")})")
    }
  }

  // ---------- selectors ----------

  "The selector helpers" should "escape the selector and the classes" in {
    val sel = "tr[data-x=\"1\"] > td"
    check(J.forEachQuerySelector(sel, new JsFunc1(e => js"$e.remove()")), s"document.querySelectorAll(${q(sel)}).forEach(function(arg1){arg1.remove()})")
    check(J.addClassToElemsMatchingSelector(sel, "a b"), s"document.querySelectorAll(${q(sel)}).forEach(el=>el.classList.add(${q("a")}, ${q("b")}))")
    check(J.removeClassFromElemsMatchingSelector(sel, "a"), s"document.querySelectorAll(${q(sel)}).forEach(el=>el.classList.remove(${q("a")}))")
    check(J.selectedValues(Js("el")), """Array.from(el.querySelectorAll("option:checked"),e=>e.value)""")
  }

  // ---------- code structure ----------

  "The code structure helpers" should "build the code around the Js given" in {
    val a = Js("a()")
    val b = Js("b()")
    check(J._if(Js("x"), a, b), "if(x) {a()} else {b()}")
    check(J._if(Js("x"), a), "if(x) {a()} else {}")
    check(J._trenaryOp(Js("x"), a, b), "((x) ? (a()) : (b()))")
    check(J.withVarStmt("v", Js("1"))(v => js"f($v)"), "(function (v) {f(v)})(1);")
    check(J.withVarExpr("v", Js("1"))(v => js"$v + 1"), "(function (v) {return v + 1;})(1);")
    check(J.wrapAsExpr(a, b)(Js("x")), "((function () {a();b(); return x;})())")
    check(J.wrapAsExpr()(Js("x")), "((function () {; return x;})())")
    check(J.valueOrElse(Js("x"), Js("0")), "(function (value) {return ((value != null) ? (value) : (0));})(x);")
    check(J.varOrElseUpdate(Js("window.v"), Js("1")), "((window.v != null) ? (window.v) : (((function () {window.v = 1;; return window.v;})())))")
    check(J.function0(a), "function(){a()}")
    check(J.function1(x => js"f($x)"), "function(arg1){f(arg1)}")
    check(J.function2((x, y) => js"f($x, $y)"), "function(arg1, arg2){f(arg1, arg2)}")
    check(J.function3((x, y, z) => js"f($x, $y, $z)"), "function(arg1, arg2, arg3){f(arg1, arg2, arg3)}")
    check(J.onload(a), "$(document).ready(function() { a() });")
    check(J.onkeypress(13)(a), "event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {a()};")
    check(J.onkeypress(13, 27)(a), "event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13 || (event.keyCode ? event.keyCode : event.which) == 27) {a()};")
    check(J.setTimeout(a, 500), "setTimeout(function(){ a() }, 500);")
    check(J.undefined(Js("x")), "(x === undefined)")
    check(J.catchAndLogErrors(a), "try {a()} catch (error) { console.error(error); }")
  }

  it should "keep the Js given as parts (never building their cmd)" in {
    val noCmd = new CompositeJs(Js("a()"), Js("b()")) {
      override def cmd: String = throw new IllegalStateException("cmd was built")
    }
    val built = Seq(
      J._if(noCmd, noCmd, noCmd), J._trenaryOp(noCmd, noCmd), J.withVarStmt("v", noCmd)(_ => noCmd), J.setTimeout(noCmd, 1),
      J.consoleLog(noCmd), J.confirm("ok?", noCmd), J.catchAndLogErrors(noCmd), J.onload(noCmd), J.selectedValues(noCmd),
      noCmd `_!=` noCmd, noCmd `_=` noCmd,
    )
    built.foreach { js =>
      assert(parts(js).exists(_ == "a()"))
      assert(streamed(js).contains("a()b()"))
    }
  }

  "evalIf" should "be the Js, or nothing" in {
    var evaluated = false
    check(J.evalIf(true)(Js("a()")), "a()")
    check(J.evalIf(false)({ evaluated = true; Js("a()") }), "")
    assert(!evaluated)
  }

  "void" should "be nothing (running the code given)" in {
    check(J.void, "")
    var ran = false
    check(J.void(() => ran = true), "")
    assert(ran)
    check(J._null, "null")
    check(J._true, "true")
    check(J._false, "false")
    check(J.fromString("x()"), "x()")
  }

  // ---------- the browser ----------

  "The browser helpers" should "escape the text given" in {
    hostile.foreach { s =>
      check(J.alert(s), s"alert(${q(s)});")
      check(J.confirm(s, Js("go()")), s"if(confirm(${q(s)})) {go()};")
      check(J.copy2Clipboard(s), s"navigator.clipboard.writeText(${q(s)});")
      check(J.consoleLog(s), s"console.log(${q(s)});")
      check(J.redirectTo(s), s"window.location.href = ${q(s)};")
      check(J.reloadPageWithQueryParam(s, s),
        "var searchParams = new URLSearchParams(window.location.search);" +
          s"searchParams.set(${q(s)}, ${q(s)});" +
          "window.location.search = searchParams.toString();")
    }
    check(J.consoleLog(Js("x")), "console.log(x);")
    check(J.log(Js("a(\"x\")")), s"console.log(eval(${q("a(\"x\")")}));")
    check(J.reload(), "location.reload();")
  }

  "goBack" should "go back n pages" in {
    check(J.goBack(), ";window.history.back();")
    check(J.goBack(1), ";window.history.back();")
    check(J.goBack(3), ";window.history.go(-3);")
  }

  "setCookie" should "set the cookie, with its expiry and path, all escaped" in {
    check(J.setCookie("fssid", "abc"), s"document.cookie=${q("fssid=abc")}")
    check(J.setCookie("fssid", "abc", path = Some("/")), s"document.cookie=${q("fssid=abc; path=/")}")
    val expires = new Date(1_700_000_000_000L).toGMTString
    check(J.setCookie("n", "v", expires = Some(1_700_000_000_000L), path = Some("/a")), s"document.cookie=${q(s"n=v; expires=$expires; path=/a")}")
    check(J.setCookie("n", "a'b\"c</script>"), s"document.cookie=${q("n=a'b\"c</script>")}")
    check(J.deleteCookie("n", "/"), s"document.cookie=${q(s"n=; expires=${new Date(0).toGMTString}; path=/")}")
  }

  // ---------- JsHelper ----------

  "The JsHelper operators" should "join the operands" in {
    val (a, b) = (Js("a"), Js("b"))
    check(a `_==` b, "a == b")
    check(a `_!=` b, "a != b")
    check(a `_===` b, "a === b")
    check(a `_!==` b, "a !== b")
    check(a `_>` b, "a > b")
    check(a `_>=` b, "a >= b")
    check(a `_<` b, "a < b")
    check(a `_<=` b, "a <= b")
    check(a `_&&` b, "a && b")
    check(a `_||` b, "a || b")
    check(a `_=` b, "a = b;")
    check(a.`_===null`, "a === null")
    val asVar = Js("f()").asVar(v => js"g($v)")
    assert(asVar.cmd.matches("""var (v_[0-9A-F]+) = \(f\(\)\); g\(\1\)"""), asVar.cmd)
    assert(parts(asVar).mkString == asVar.cmd && streamed(asVar) == asVar.cmd)
  }
}
