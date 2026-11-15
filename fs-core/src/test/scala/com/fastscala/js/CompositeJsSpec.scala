package com.fastscala.js

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class CompositeJsSpec extends AnyFlatSpec {

  private def streamed(js: Js): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    JsWriter.Cmd.writeJs(js, new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
        val arr = new Array[Byte](bb.remaining()); bb.get(arr); out.write(arr); cb.succeeded()
      }
    }, Callback.NOOP, endContent = false)
    out.toByteArray
  }

  private def check(js: Js, expectedCmd: String): Unit = {
    assert(js.cmd == expectedCmd)
    val bytes = streamed(js)
    assert(new String(bytes, StandardCharsets.UTF_8) == expectedCmd)
  }

  "CompositeJs" should "concatenate children without a separator" in {
    check(new CompositeJs(Js("a()"), Js("b()")), "a()b()")
  }

  "CompositeJs.joined" should "put the separator only between children" in {
    check(CompositeJs.joined(Seq(Js("a()"), Js("b()"), Js("c()")), Js(";")), "a();b();c()")
  }

  it should "add no separator for a single child" in {
    check(CompositeJs.joined(Seq(Js("a()")), Js(";")), "a()")
  }

  it should "be empty with no children" in {
    check(CompositeJs.joined(Seq(), Js(";")), "")
  }

  it should "count multi-byte separators correctly" in {
    check(CompositeJs.joined(Seq(Js("é"), Js("€")), Js("/*😀*/")), "é/*😀*/€")
  }

  // ---------- children ----------

  /** The leaves of `js` (a CompositeJs's children, flattened), as their cmds. */
  private def parts(js: Js): Seq[String] = js match {
    case c: CompositeJs => c.children.flatMap(parts)
    case other => Seq(other.cmd)
  }

  "A Js tree" should "have leaves that, concatenated, are exactly cmd" in {
    val cases: Seq[Js] = Seq(
      Js("a()"),
      Js.Void,
      Js(""),
      new CompositeJs(Js("a()"), Js("b()")),
      CompositeJs.joined(Seq(Js("a()"), Js("b()"), Js("c()")), Js(";")),
      CompositeJs.joined(Seq(Js("a()")), Js(";")),
      CompositeJs.joined(Seq(), Js(";")),
      CompositeJs.joined(Seq(Js("x"), Js.Void, Js("y")), Js(", ")),
      CompositeJs.joined(Seq(CompositeJs.joined(Seq(Js("a"), Js("b")), Js("|")), Js("c")), Js(";")),
      Js("a()") & Js("b()") & Js("c()") & Js.Void & Js("d()"),
      new JsFunc0(Js("x()")),
      new JsFunc1(arg => Js(s"f(${arg.cmd})")),
      new JsFunc3((a, b, c) => Js(a, Js(","), b, Js(","), c)),
      CompositeJs.joined(Seq(Js("é"), Js("€")), Js("/*😀*/")),
    )
    cases.foreach(js => assert(parts(js).mkString == js.cmd, js.cmd))
  }

  it should "keep a CompositeJs's children as they are (joined only with the separators given)" in {
    assert(parts(CompositeJs.joined(Seq(Js("a"), Js("b"), Js("c")), Js(";"))) == Seq("a", ";", "b", ";", "c"))
  }

  // ---------- JsFunc ----------

  "JsFunc0..3" should "be functions of the body, as JavaScript" in {
    assert(new JsFunc0(Js("x()")).cmd == "function(){x()}")
    assert(new JsFunc1(a => Js(s"f(${a.cmd})")).cmd == "function(arg1){f(arg1)}")
    assert(new JsFunc2((a, b) => Js(s"f(${a.cmd}, ${b.cmd})")).cmd == "function(arg1, arg2){f(arg1, arg2)}")
    assert(new JsFunc3((a, b, c) => Js(s"f(${a.cmd}, ${b.cmd}, ${c.cmd})")).cmd == "function(arg1, arg2, arg3){f(arg1, arg2, arg3)}")
    assert(JsFunc1(a => Js(s"g(${a.cmd})")).cmd == "function(arg1){g(arg1)}") // (without `new`)
  }

  it should "call the body once (it can have side effects, e.g. registering a callback)" in {
    var calls = 0
    new JsFunc1(a => { calls += 1; a })
    assert(calls == 1)
    calls = 0
    new JsFunc2((a, _) => { calls += 1; a })
    assert(calls == 1)
    calls = 0
    new JsFunc3((a, _, _) => { calls += 1; a })
    assert(calls == 1)
  }

  it should "still be subtypes of each other" in {
    val f0: JsFunc1 = new JsFunc0(Js("x()"))
    val f1: JsFunc2 = new JsFunc1(a => a)
    val f2: JsFunc3 = new JsFunc2((a, _) => a)
    val f3: CompositeJs = new JsFunc3((a, _, _) => a)
    assert(Seq(f0, f1, f2, f3).forall(_.cmd.startsWith("function(")))
  }
}
