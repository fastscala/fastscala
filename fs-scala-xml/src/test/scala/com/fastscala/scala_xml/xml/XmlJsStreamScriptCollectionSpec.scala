package com.fastscala.scala_xml.xml

import com.fastscala.scala_xml.xml.streaming.*

import com.fastscala.js.{Js, JsEscape}
import com.fastscala.scala_xml.js.{JS, JsWithScriptDetection, NodeSeqJs}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import scala.xml.*

/**
 * How XmlJsStream writes a [[JsWithScriptDetection]]: its XML is written as it is, but for its JS scripts, whose
 * JavaScript is collected while it's written (the XML isn't searched, or copied, beforehand), and written after it:
 * the same code as its `cmd`.
 */
class XmlJsStreamScriptCollectionSpec extends AnyFlatSpec {

  private def script(code: String): Elem = <script type="text/javascript">{code}</script>

  private def written(js: Js): String = {
    val bytes = new ByteArrayOutputStream()
    XmlJsStream.writeJs(js, new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
        while (bb.hasRemaining) bytes.write(bb.get())
        cb.succeeded()
      }
    })
    bytes.toString(UTF_8)
  }

  private def detection(ns: NodeSeq, scripts: Js => Js = identity[Js]): JsWithScriptDetection =
    new JsWithScriptDetection(Js("show("), ns, Js(");"), scripts)

  /** As `d`, but neither its `js` nor its `cmd` can be built: so it can only be written by collecting the scripts. */
  private def streamOnly(d: JsWithScriptDetection): JsWithScriptDetection =
    new JsWithScriptDetection(d.before, d.ns, d.after, d.scripts) {
      override lazy val js: Js = throw new IllegalStateException("JsWithScriptDetection.js was built")

      override def cmd: String = throw new IllegalStateException("JsWithScriptDetection.cmd was built")
    }

  "XmlJsStream" should "write the XML without its scripts, and then the scripts, without building the detection's js" in {
    val d = detection(script("a()") ++ <div class="x">{JS.inScriptTag(Js("b()"))}<b>t &amp; "u"</b>{<script type="module">c()</script>}</div> ++ script("d()"))
    val out = written(streamOnly(d))
    assert(out == d.cmd)
    assert(out == "show(\"" + JsEscape.escape(<div class="x"><b>t &amp; "u"</b></div>.toString) + "\");;a();b();c();d()")
  }

  it should "write nothing after it when there are no scripts" in {
    val d = detection(<p>x</p> ++ <script type="text/template">tpl</script>)
    assert(written(streamOnly(d)) == d.cmd)
    assert(written(streamOnly(d)).endsWith("\");"))
  }

  it should "skip empty scripts, and write the XML of Groups and of nested elements without theirs" in {
    val d = detection(Group(Seq(<i>1</i>, script(""), script("a()"))) ++ <ul><li><span>{script("b()")}</span></li></ul>)
    val out = written(streamOnly(d))
    assert(out == d.cmd)
    assert(out.endsWith(";a();b()") && !out.contains("script"))
  }

  it should "read the XML once (it isn't searched for scripts before it's written)" in {
    var read = 0
    val counting = new NodeSeq {
      override def theSeq: Seq[Node] = { read += 1; Seq(<p>x</p>, script("run()")) }
    }
    assert(written(streamOnly(detection(counting))) == "show(\"" + JsEscape.escape("<p>x</p>") + "\");;run()")
    assert(read == 1)
  }

  it should "call scripts once, with the collected JavaScript, and write what it returns" in {
    var calls = Vector.empty[String]
    val d = detection(<p>{script("a()")}</p> ++ script("b()"), js => { calls :+= js.cmd; Js(Js("later(function(){"), js, Js("})")) })
    assert(written(streamOnly(d)) == "show(\"" + JsEscape.escape("<p></p>") + "\");;later(function(){a();b()})")
    assert(calls == Vector("a();b()"))
  }

  it should "call scripts with Js.Void when there are none, and write nothing if it returns Js.Void" in {
    var got: Js = null
    val out = written(streamOnly(detection(<p>x</p>, js => { got = js; js })))
    assert(got eq Js.Void)
    assert(out.endsWith("\");"))
    assert(written(streamOnly(detection(script("a()"), _ => Js.Void))) == "show(\"\");")
  }

  it should "keep the scripts of XML that's in a Js of the detection's XML (an attribute's value): as its cmd" in {
    val inner = <i>{script("inner()")}</i>
    val d = detection(ElemOptimized("button", "onclick" -> JS.setContents("x", inner))(script("outer()"), Text("Go")))
    val out = written(streamOnly(d))
    assert(out == d.cmd)
    assert(out.contains("inner()") && out.endsWith(";outer()"))
  }

  it should "write a detection in an attribute of another's XML: each with its own scripts" in {
    def nested(make: JsWithScriptDetection => JsWithScriptDetection) = {
      val inner = make(detection(<i>{script("inner()")}</i>))
      make(detection(script("first()") ++ ElemOptimized("button", "onclick" -> inner)(script("outer()"), Text("Go")) ++ script("last()")))
    }
    val out = written(nested(streamOnly))
    assert(out == nested(identity).cmd)
    assert(out.endsWith(";first();outer();last()"))
  }

  it should "write the scripts of XML that isn't a detection's" in {
    val ns = <div>{script("a()")}<b>t</b></div>
    assert(written(new NodeSeqJs(ns)) == ns.toString)
    assert(written(Js(streamOnly(detection(ns)), new NodeSeqJs(ns))) == detection(ns).cmd + ns.toString)
  }

  it should "write a big one (many chunks) as its cmd, to a sink that completes its writes later" in {
    val ns = <div>{(1 to 3_000).map(i => <p class="row">{s"Row $i é \"q\" & <x>"}{if (i % 500 == 0) script(s"row($i)") else NodeSeq.Empty}</p>)}</div>
    val d = detection(ns)
    val bytes = new ByteArrayOutputStream()
    val pending = new java.util.ArrayDeque[Callback]()
    var done = false
    XmlJsStream.writeJs(streamOnly(d), new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
        while (bb.hasRemaining) bytes.write(bb.get())
        pending.add(cb)
      }
    }, new Callback {
      override def succeeded(): Unit = done = true

      override def failed(t: Throwable): Unit = throw t
    })
    assert(!done, "it didn't pause")
    while (!pending.isEmpty) pending.poll().succeeded()
    assert(done)
    val out = bytes.toString(UTF_8)
    assert(out.length == d.cmd.length && out == d.cmd)
    assert(out.endsWith(";row(500);row(1000);row(1500);row(2000);row(2500);row(3000)"))
  }
}
