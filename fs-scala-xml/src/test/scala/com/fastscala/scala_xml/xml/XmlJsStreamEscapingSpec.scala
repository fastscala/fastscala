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

  /** What `write` writes into an Out (after `pad` ASCII bytes, so it starts anywhere in a chunk), and then finishes. */
  private def collect(pad: Int)(write: XmlJsStream.Out => Unit): Array[Byte] = {
    val bytes = new ByteArrayOutputStream()
    val out = new XmlJsStream.Out(new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
        while (bb.hasRemaining) bytes.write(bb.get())
        cb.succeeded()
      }
    }, Callback.NOOP, true)
    Encoder.write("p" * pad, out, Encoder.Unescaped)
    write(out)
    out.finish()
    bytes.toByteArray.drop(pad)
  }

  /** The reference: `Utility.escape` `xml` times, then JsEscape `jsLevels` times, in UTF-8 (a lone surrogate: `?`). */
  private def reference(text: String, xml: Int, jsLevels: Int): Array[Byte] = {
    val xmlEscaped = (1 to xml).foldLeft(text)((acc, _) => Utility.escape(acc))
    (1 to jsLevels).foldLeft(xmlEscaped)((acc, _) => JsEscape.escape(acc)).getBytes(UTF_8)
  }

  "Encoder.write" should "write the reference escaping, for every char, with 0 to 2 XML levels, and 0 to 3 JavaScript levels" in {
    val bmp = (0 until 0x10000).map(_.toChar).mkString
    val texts = Seq(bmp, "a\uD83D\u0001\uDE00b", "\uD800", "x\uDC00", "\uD83D\uD83D\uDE00", "😀 & <é> \"q\" \u2028\u0085", "short", "")
    for (text <- texts; xml <- 0 to 2; jsLevels <- 0 to 3) {
      val expected = reference(text, xml, jsLevels)
      val fromString = collect(0)(out => Encoder.write(text, out, Encoder.escaping(xml, jsLevels)))
      val fromBuilder = collect(0)(out => Encoder.write(new java.lang.StringBuilder(text), out, Encoder.escaping(xml, jsLevels)))
      val what = s"a text of ${text.length} chars, xml=$xml, jsLevels=$jsLevels"
      assert(java.util.Arrays.equals(expected, fromString), s"String: $what")
      assert(java.util.Arrays.equals(expected, fromBuilder), s"StringBuilder: $what")
    }
  }

  it should "write the same at every offset near a chunk's end (escapes and UTF-8 split over two chunks)" in {
    val text = "a<\"é€😀\\\u2028\uD800&z" * 3
    for (pad <- XmlJsStream.ChunkSize - 40 to XmlJsStream.ChunkSize + 2; xml <- 0 to 2; jsLevels <- 0 to 2) {
      assert(java.util.Arrays.equals(reference(text, xml, jsLevels), collect(pad)(out => Encoder.write(text, out, Encoder.escaping(xml, jsLevels)))), s"pad=$pad, xml=$xml, jsLevels=$jsLevels")
    }
  }

  it should "write a markup char escaped as a String of it is" in {
    for (c <- "<>/=\" :".toSeq; xml <- 0 to 2; jsLevels <- 0 to 4) {
      assert(java.util.Arrays.equals(reference(c.toString, xml, jsLevels), collect(0)(out => Encoder.writeAscii(c, out, Encoder.escaping(xml, jsLevels)))), s"$c, $xml, $jsLevels")
    }
  }

  it should "write escapes longer than a chunk (many JavaScript levels)" in {
    val text = "\\\"x"
    val jsLevels = 14 // a backslash: 2^14 of them
    assert(java.util.Arrays.equals(reference(text, xml = 1, jsLevels), collect(100)(out => Encoder.write(text, out, Encoder.escaping(1, jsLevels)))))
  }

  "JavaScript escaping" should "be per char (so Strings can be escaped one at a time, as they're written)" in {
    val sample = "bé😀"
    val chars = (0 until 0x10000).filterNot(c => Character.isSurrogate(c.toChar)).map(c => c.toChar.toString) ++ Seq("😀", "\uD800", "\uDC00")
    def enc(s: String) = JsEscape.escape(s)
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

  // ---------- Js attributes (of an ElemOptimized) with EncJs's and NodeSeqJs's in them: written as levels ----------

  /** An EncJs / NodeSeqJs whose cmd can't be built: so it's written as a level, its parts one at a time. */
  private def noCmd(js: Js): Js = new EncJs(js) {
    override def cmd: String = throw new IllegalStateException("EncJs.cmd was built")
  }

  private def noCmd(ns: NodeSeq): Js = new NodeSeqJs(ns) {
    override def cmd: String = throw new IllegalStateException("NodeSeqJs.cmd was built")
  }

  "A Js attribute" should "write its EncJs's and NodeSeqJs's as levels (never building their cmd)" in {
    val onclick = Js(Js("f("), noCmd(Js("a \"b\" & <c>")), Js(", "), noCmd(<p title="t &amp; &quot;u&quot;">x &amp; y é</p>), Js(")"))
    val elem = ElemOptimized("button", "onclick" -> onclick)(Text("Go"))
    // What cmd would be: the same, with an EncJs and a NodeSeqJs that can build their cmd.
    val same = ElemOptimized("button", "onclick" -> Js(Js("f("), new EncJs(Js("a \"b\" & <c>")), Js(", "), new NodeSeqJs(<p title="t &amp; &quot;u&quot;">x &amp; y é</p>), Js(")")))(Text("Go"))
    assert(written(new NodeSeqJs(elem)) == same.toString)
    assert(written(JS.htmlAsJsStr(elem)) == JS.htmlAsJsStr(same).cmd)
  }

  it should "escape a NodeSeqJs in it (without an EncJs) for XML twice: its own, and the attribute's" in {
    val elem = ElemOptimized("a", "data-x" -> new NodeSeqJs(<p class="c" title="&quot;">a &amp; b &lt;</p>))()
    check(new NodeSeqJs(elem))
    assert(written(new NodeSeqJs(elem)).contains("&amp;amp;"))
    check(JS.htmlAsJsStr(elem))
  }

  it should "write a rerender in a rerender (the scratch pad's cases)" in {
    val case1 = JS.alert("hello")
    val case2 = ElemOptimized("div", "onclick" -> case1)()
    val case3 = JS.replace("my-div", case2)
    val case4 = ElemOptimized("button", "onclick" -> case3)()
    val case5 = JS.append2Body(case4)
    check(new NodeSeqJs(case2))
    check(case3)
    check(new NodeSeqJs(case4))
    check(case5)
    check(JS.replace("outer", ElemOptimized("i", "onclick" -> case5)(Text("deep"))))
  }

  /**
   * `written` is what `ref`'s cmd is. (XmlJsStream writes an ElemOptimized's attributes in the order of its list, and
   * toString in the reverse order: so `ref` is the same, with each list reversed.)
   */
  private def checkAgainst(js: Js, ref: Js): Unit = {
    val (out, expected) = (written(js), ref.cmd)
    if (out != expected) {
      val at = out.indices.find(i => i >= expected.length || out(i) != expected(i)).getOrElse(out.length)
      fail(s"differs at $at (of ${out.length} / ${expected.length}):\n  written: ${out.slice(at - 40, at + 60)}\n  cmd:     ${expected.slice(at - 40, at + 60)}")
    }
  }

  it should "write the other attributes, in order, before and after it; and then the children, or `/>`" in {
    val rerender = JS.replace("x", <b title="&quot;">{"t & <u>"}</b>)
    val attrs = List("a" -> "1 & 2", "onclick" -> rerender, "b" -> "\"", "onblur" -> Js("g()"), "c" -> JS.alert("<&>"), "d" -> "end")
    for (minimize <- Seq(false, true); children <- Seq(Nil, Seq(Text("a & b")), Seq(<i>x</i>, Text("y")))) {
      val elem = ElemOptimized("x", attrs, minimize)(children *)
      val ref = ElemOptimized("x", attrs.reverse, minimize)(children *)
      checkAgainst(new NodeSeqJs(elem), new NodeSeqJs(ref))
      checkAgainst(JS.htmlAsJsStr(elem), JS.htmlAsJsStr(ref))
      checkAgainst(new NodeSeqJs(<div>{elem}{elem}</div>), new NodeSeqJs(<div>{ref}{ref}</div>))
    }
  }

  it should "write a Js with a null cmd as `null`, as the cmd of what it's in does (e.g. asJsStr(null))" in {
    val nullJs = Js(null: String)
    check(new EncJs(nullJs, inString = true))
    check(Js(Js("f("), nullJs, Js(")")))
    check(new NodeSeqJs(ElemOptimized("a", "onclick" -> JS.alert(null))()))
    check(new NodeSeqJs(ElemOptimized("a", "onclick" -> Js(Js("f("), nullJs, Js(")")))()))
  }

  it should "write an attribute's EncJs, in a NodeSeqJs, in an attribute" in {
    val inner = ElemOptimized("b", "onclick" -> JS.alert("<&>\"'"))(Text("t"))
    val elem = ElemOptimized("a", "onclick" -> Js(Js("h("), new NodeSeqJs(inner), Js(")")))()
    check(new NodeSeqJs(elem))
    check(JS.htmlAsJsStr(elem))
    check(new EncJs(new NodeSeqJs(elem), inString = true))
  }

  it should "write random nestings of attributes, EncJs's and NodeSeqJs's as their cmd does" in {
    val rnd = new scala.util.Random(7)
    val alphabet = Seq("s", "a", "<", ">", "/", "\\", "\"", "'", "\n", "é", "😀", " ", "&", "-", "\u2028")
    def text(): String = Seq.fill(rnd.nextInt(3))(alphabet(rnd.nextInt(alphabet.size))).mkString
    /** A random Js, and its reference (the same, but each ElemOptimized's attributes reversed: see checkAgainst). */
    def gen(depth: Int): (Js, Js) = rnd.nextInt(if (depth > 5) 2 else 6) match {
      case 0 => val js = Js(text()); (js, js)
      case 1 => (Js.Void, Js.Void)
      case 2 =>
        val (js, ref) = gen(depth + 1)
        val inString = rnd.nextBoolean()
        (new EncJs(js, inString), new EncJs(ref, inString))
      case 3 =>
        val parts = Seq.fill(rnd.nextInt(4))(gen(depth + 1))
        (Js(parts.map(_._1) *), Js(parts.map(_._2) *))
      case 4 => val js = new NodeSeqJs(<p title={text()}>{text()}</p>); (js, js)
      case _ =>
        val attrs = List.fill(1 + rnd.nextInt(3))(if (rnd.nextBoolean()) { val t = text(); ("s", t, t) } else { val (js, ref) = gen(depth + 1); ("j", js, ref) })
          .zipWithIndex.map { case ((k, v, r), i) => (k + i, v, r) }
        val minimize = rnd.nextBoolean()
        val children = if (rnd.nextBoolean()) Seq(Text(text())) else Nil
        (new NodeSeqJs(ElemOptimized("e", attrs.map(a => a._1 -> a._2), minimize)(children *)),
          new NodeSeqJs(ElemOptimized("e", attrs.reverse.map(a => a._1 -> a._3), minimize)(children *)))
    }
    for (_ <- 1 to 3_000) {
      val (js, ref) = gen(0)
      checkAgainst(js, ref)
    }
  }
}
