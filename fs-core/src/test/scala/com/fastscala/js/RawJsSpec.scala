package com.fastscala.js

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

/** `js"..."`: JavaScript as it's written (a [[RawJs]], or with Js values interpolated, a CompositeJs keeping them). */
class RawJsSpec extends AnyFlatSpec {

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

  /** cmd is `expected`, and so are the parts joined, and the streamed bytes. */
  private def check(js: Js, expected: String): Unit = {
    assert(js.cmd == expected)
    assert(parts(js).mkString == expected)
    assert(streamed(js) == expected)
  }

  "js\"...\"" should "be the code as written" in {
    val js1 = js"alert('hi')"
    assert(js1.isInstanceOf[RawJs])
    check(js1, "alert('hi')")
    check(js"", "")
  }

  it should "keep backslashes (raw: \\n stays a backslash and an n, for JavaScript string literals)" in {
    check(js"alert('a\nb\t\"c\"\\d')", """alert('a\nb\t\"c\"\\d')""")
  }

  it should "insert other values as their toString, not escaped or quoted" in {
    val name = "O'Brien & <co>"
    val n = 42
    val f = js"f('$name', $n, ${1.5}, ${true}, ${null})"
    assert(f.isInstanceOf[RawJs]) // (no Js values: one RawJs)
    check(f, "f('O'Brien & <co>', 42, 1.5, true, null)")
  }

  it should "keep an interpolated Js as a part (written part by part, its cmd never built)" in {
    val inner = new CompositeJs(Js("a()"), Js(";"), Js("b()")) {
      override def cmd: String = throw new IllegalStateException("cmd was built")
    }
    val outer = js"if (x) { $inner } else { ${Js("c()")} }"
    assert(parts(outer) == Seq("if (x) { ", "a()", ";", "b()", " } else { ", "c()", " }"))
    assert(streamed(outer) == "if (x) { a();b() } else { c() }")
  }

  it should "handle values next to each other, at the start and at the end, and Js.Void" in {
    val a = Js("a")
    check(js"$a$a${Js.Void}$a", "aaa")
    check(js"${"x"}y${"z"}", "xyz")
    check(js"[${Js("a")}, '\n', ${"b"}]", "[a, '\\n', b]") // (a Js value, and raw text next to it)
    check(js"$a", "a")
  }

  it should "work with & and inside other Js" in {
    check(js"a()" & js"b()", "a();b()")
    check(Js(Js("["), js"1, 2", Js("]")), "[1, 2]")
    check(new JsFunc1(arg => js"f($arg)"), "function(arg1){f(arg1)}")
  }
}
