package com.fastscala.scala_xml.xml

import com.fastscala.js.{EncJs, Js, js}
import com.fastscala.scala_xml.js.{JS, NodeSeqJs}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import com.fastscala.js.JsEscape
import com.fastscala.scala_xml.testutils.ElemUtilsV1.RichElem
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import scala.xml.*

/** [[XmlJsStream.writeJs]] of [[EncJs]]: escaped as it's written (by an [[Encoder]]), as its cmd. */
class XmlJsStreamEscapingSpec extends AnyFlatSpec {

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

  private def check(js: Js): Unit = {
    val expected = js.cmd
    val out = written(js)
    if (out != expected) {
      val at = out.indices.find(i => i >= expected.length || out(i) != expected(i)).getOrElse(out.length)
      fail(s"differs at $at (of ${out.length} / ${expected.length}):\n  written: ${out.slice(at - 40, at + 60)}\n  cmd:     ${expected.slice(at - 40, at + 60)}")
    }
  }

  /** Every char but surrogates, a few times over (so it's many chunks), and some surrogate pairs. */
  private val allChars: String = {
    val sb = new java.lang.StringBuilder()
    (0 until 0x10000).filterNot(c => Character.isSurrogate(c.toChar)).foreach(c => sb.append(c.toChar))
    sb.append("😀 𝄞 􏿿")
    sb.toString
  }

  "Encoder's XML layer" should "write what XmlEscape does, for every char (controls, surrogates, pairs split by dropped chars)" in {
    val bmp = (0 until 0x10000).map(_.toChar).mkString
    val texts = Seq(bmp, "a\uD83D\u0001\uDE00b", "\uD800", "x\uDC00", "😀 & <é> \"q\"", "")
    for (text <- texts) {
      def collect(write: XmlJsStream.Out => Unit): Array[Byte] = {
        val bytes = new ByteArrayOutputStream()
        val out = new XmlJsStream.Out(new Content.Sink {
          override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
            while (bb.hasRemaining) bytes.write(bb.get())
            cb.succeeded()
          }
        }, Callback.NOOP, true)
        write(out)
        out.finish()
        bytes.toByteArray
      }
      val expected = collect(out => XmlEscape.escape(text, out))
      val actual = collect(out => Encoder(List(EncodingLayer.Xml)).write(text, out))
      assert(java.util.Arrays.equals(expected, actual), s"differs for a text of ${text.length} chars")
    }
  }

  "JavaScript escaping" should "be per char (so Strings can be escaped one at a time, as they're written)" in {
    val sample = "bé😀"
    val chars = (0 until 0x10000).filterNot(c => Character.isSurrogate(c.toChar)).map(c => c.toChar.toString) ++ Seq("😀", "\uD800", "\uDC00")
    def enc(s: String) = EncodingLayer.Js.encode(s)
    for (c <- chars) {
      val esc = enc(c)
      assert(enc("a" + c + sample + c + c) == "a" + esc + enc(sample) + esc + esc, s"not per char: U+${c.codePointAt(0).toHexString}")
      assert(enc("a" + c) == "a" + esc, s"last: U+${c.codePointAt(0).toHexString}")
    }
  }

  "writeJs" should "escape lone surrogates as OWASP does (in text and in XML)" in {
    check(new EncJs(Js("a\uD800b\uDC00")))
    check(new EncJs(Js("\uD83D")))
    check(JS.htmlAsJsStr(<p title={"t\uD800"}>{"x\uDC00y"}</p>))
  }

  it should "escape every char as EncJs.cmd does (once, twice, three times)" in {
    for (depth <- 1 to 3) {
      val js = (1 to depth).foldLeft(Js(allChars))((acc, i) => new EncJs(acc, inString = i % 2 == 0))
      check(js)
    }
  }

  it should "escape XML (a NodeSeqJs) in an EncJs, quoted, as JS.htmlAsJsStr is" in {
    val html = <div id="x" class="a b" title={"t \"q\" & <x> é  "}>{"Hello & <bye> \\ / ' é € 😀  "}<br/><!-- c --></div>
    check(JS.htmlAsJsStr(html))
    check(new EncJs(new NodeSeqJs(html)))
    check(js"el.innerHTML = ${JS.htmlAsJsStr(html)}; f(${new EncJs(Js("a\"b"))})")
  }

  // (Elements with only the onclick: an ElemOptimized's toString may put its attributes in another order.)
  it should "escape attributes with Js in them (escaped for XML, then for JavaScript), in an EncJs" in {
    val onclick = Js(Js("alert(\"a & b\"); "), Js("f('<x>', \"é \")"))
    val elem = <button>Go</button>.addOnClick(onclick)
    check(JS.htmlAsJsStr(elem))
  }

  it should "escape nested EncJs's (a rerender with a rerender in it), with the quotes escaped by the outer ones" in {
    val inner = <b title={"\"i\""}>{"inner & <x>  "}</b>
    val innerJs = JS.replace("inner", inner)
    val outerHtml = <div>{"outer \"o\""}</div>.addOnClick(innerJs)
    check(JS.replace("main", outerHtml))
    check(new EncJs(js"x = ${JS.htmlAsJsStr(<p>{"\"deep\"  "}</p>)};", inString = true))
  }

  it should "escape a big page (many chunks, the escaping changing in the middle of them)" in {
    val page = <div>{(1 to 5_000).map(i => <p class="row" title={s"r$i"}>{s"Row $i é € 😀 \"q\" & <x> \\ '  "}</p>)}</div>
    check(JS.replace("main-content", page))
    // Short escaped parts between unescaped ones.
    check(Js((1 to 3_000).map(i => if (i % 2 == 0) new EncJs(Js(s"<$i é>"), inString = true) else Js(s"/*$i*/")) *))
  }

  it should "escape chars that are 2, 3 or 4 bytes at every offset of a chunk" in {
    for (pad <- 0 until 8; c <- Seq("é", "€", " ", "😀")) {
      val s = ("x" * pad) + (c * (XmlJsStream.ChunkSize / 2))
      check(new EncJs(Js(s)))
      check(Js(Js("y" * pad), new EncJs(Js(s), inString = true)))
    }
  }

  it should "write an empty EncJs as its quotes" in {
    check(new EncJs(Js.Void, inString = true))
    check(new EncJs(Js.Void))
    check(new EncJs(new EncJs(Js.Void, inString = true), inString = true))
  }

  it should "escape the line terminators and C1 controls as JsEscape does" in {
    check(new EncJs(Js(" \u0085 \u009f")))
    assert(written(new EncJs(Js(" \u0085 \u009f"))) == JsEscape.escape(" \u0085 \u009f"))
  }

  // (OWASP hex-escapes the last char of its input if it's a letter of "script": JsEscape doesn't, anywhere.)
  it should "escape the last char of each EncJs as any other (a letter of \"script\" too)" in {
    for (c <- Seq("s", "S", "t", "c", "a", "<", "\t", "\r", " ", "é", "\"", "x s", "<script")) {
      check(new EncJs(Js(c)))
      check(new EncJs(Js(c), inString = true))
      check(new EncJs(new EncJs(Js(c)))) // e.g. "<": "\x3c", whose last char, "c", is escaped by the outer one
      check(new EncJs(new EncJs(new EncJs(Js(c)), inString = true)))
      check(new EncJs(Js(Js("a"), new EncJs(Js(c)))))
      check(new EncJs(Js(new EncJs(Js(c)), Js("b"))))
      check(Js(new EncJs(Js(c)), new EncJs(Js(c)), Js(c)))
      check(new EncJs(Js(new EncJs(Js(c)), new EncJs(Js.Void), Js.Void)))
      check(new EncJs(Js(new EncJs(Js(c)), new EncJs(new EncJs(Js(c))))))
    }
  }

  it should "escape the last char as any other when it's at a chunk's end" in {
    for (pad <- 0 until 6; tail <- Seq("s", "<", "\t", "é", " ")) {
      val s = ("x" * (XmlJsStream.ChunkSize - 3 + pad)) + tail
      check(new EncJs(Js(s)))
      check(new EncJs(new EncJs(Js(s))))
      check(new EncJs(Js(Js(s), new EncJs(Js(s)))))
    }
  }

  it should "escape random nestings of EncJs's as their cmd does" in {
    val rnd = new scala.util.Random(42)
    val alphabet = Seq("s", "t", "C", "a", "1", "<", ">", "/", "\\", "\"", "'", "\t", "\n", "é", "€", "😀", " ", "\u0085", " ", "&", "-", "x")
    def gen(depth: Int): Js = rnd.nextInt(if (depth > 4) 2 else 5) match {
      case 0 => Js(Seq.fill(rnd.nextInt(4))(alphabet(rnd.nextInt(alphabet.size))).mkString)
      case 1 => Js.Void
      case 2 => new EncJs(gen(depth + 1), inString = rnd.nextBoolean())
      case 3 => Js(Seq.fill(rnd.nextInt(4))(gen(depth + 1)) *)
      case _ => new NodeSeqJs(<p title={alphabet(rnd.nextInt(alphabet.size))}>{alphabet(rnd.nextInt(alphabet.size))}</p>)
    }
    for (_ <- 1 to 3_000) check(gen(0))
  }
}
