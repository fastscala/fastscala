package com.fastscala.scala_xml.xml

import com.fastscala.scala_xml.xml.streaming.*

import com.fastscala.scala_xml.testutils.AttributeOrder.normalized
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import scala.xml.*

/** [[XmlJsStream]]: what toString writes, encoded in UTF-8, written to a Content.Sink in chunks (the last one ends it). See also [[XmlStreamSpec]]. */
class XmlStreamSinkSpec extends AnyFlatSpec {

  /** Records what's written: the bytes, and each write's size and `last`. */
  private final class RecordingSink extends Content.Sink {
    val bytes = new ByteArrayOutputStream()
    val writes = collection.mutable.ArrayBuffer[(Int, Boolean)]()

    override def write(last: Boolean, byteBuffer: ByteBuffer, callback: Callback): Unit = {
      assert(!writes.lastOption.exists(_._2), "a write after the last one")
      val n = byteBuffer.remaining()
      val copy = new Array[Byte](n)
      byteBuffer.get(copy)
      bytes.write(copy)
      writes += ((n, last))
      callback.succeeded()
    }
  }

  private def written(write: Content.Sink => Unit): RecordingSink = { val s = new RecordingSink; write(s); s }

  private def node(n: Node): String = new String(written(XmlJsStream.writeNode(n, _)).bytes.toByteArray, UTF_8)

  private def seq(ns: NodeSeq, prefix: String = ""): String = new String(written(XmlJsStream.writeSeq(ns, _, prefix)).bytes.toByteArray, UTF_8)

  /** Writes `ns`, checking the bytes are exactly toString's in UTF-8. */
  private def assertWritesAsToString(ns: NodeSeq): Unit = {
    val out = written(XmlJsStream.writeSeq(ns, _)).bytes.toByteArray
    val expected = ns.toString.getBytes(UTF_8)
    val same = java.util.Arrays.equals(out, expected)
    assert(same, {
      val (a, b) = (new String(out, UTF_8), ns.toString)
      val i = a.zip(b).indexWhere(p => p._1 != p._2)
      s"first difference at char $i (of ${a.length} / ${b.length}): ...${a.slice(i - 60, i + 60)}... vs ...${b.slice(i - 60, i + 60)}..."
    })
  }

  // ---------- as toString ----------

  private val samples: Seq[(String, NodeSeq)] = Seq(
    "text element" -> <p>hello</p>,
    "nested elements with attributes" -> <div class="a b" id="x"><span data-v="1">t</span><b/></div>,
    "escaping in text and attributes" -> <p title={"\"q\" & <x>"}>{"a < b & c"}</p>,
    "self-closing element" -> <br/>,
    "empty non-void element" -> <div></div>,
    "comment, entity ref, PCData, unparsed" -> <div><!-- c -->&amp;{PCData("x<y")}{Unparsed("<raw/>")}</div>,
    "several top-level nodes" -> (<b>1</b> ++ Text(" & ") ++ <i>2</i>),
    "groups, nested" -> <div>{Group(Seq(<b>g</b>, Group(Seq(Text("t"), <i/>))))}</div>,
    "namespaces" -> <a:x xmlns:a="urn:a"><a:y><z xmlns="urn:z"><w/></z></a:y></a:x>,
    "siblings after a closed element" -> <ul><li>1</li><li><b>2</b></li><li>3</li></ul>,
    "embedded values (Atoms)" -> <p>{"a < \"b\" & c"} {42} {3.5} {Some("<x>")}</p>,
    "control chars (dropped)" -> <p t={"a\u0001b\u001fc"}>{"x\u0000y\n\tz"}</p>,
    "only Atoms (not Text) as children: separated by spaces, as toString" -> <p>{"a"}{"b"}{42}{"c < d"}</p>,
    "Atoms, Unparsed and PCData as the only children" -> <p>{"a"}{Unparsed("<u/>")}{PCData("c")}{1.5}</p>,
    "Atoms with Text in between: not spaced" -> <p>{"a"}-{"b"}</p>,
    "a single Atom child: no space" -> <p>{"only"}</p>,
    "Atoms in a Group: not spaced" -> <p>{Group(Seq(new Atom("a"), new Atom("b")))}</p>,
  )

  samples.foreach { case (name, ns) =>
    "XmlJsStream" should s"write what toString writes, in UTF-8 ($name)" in {
      assertWritesAsToString(ns)
    }
  }

  it should "write every kind of child seq (List, Vector, NodeSeq, a Node itself, buffers, lazy)" in {
    val inner = <i>n</i>
    assertWritesAsToString(<div>{
      Seq(
        Elem(null, "a", Null, TopScope, false, List(<b>1</b>, Text("t"), <c/>) *),
        Elem(null, "v", Null, TopScope, false, Vector(<b>2</b>) *),
        new Elem(null, "s", Null, TopScope, false, (<x/> ++ <y/>) *),
        new Elem(null, "n", Null, TopScope, false, inner *),
        new Elem(null, "w", Null, TopScope, false, NodeSeq.fromSeq(List(<p/>, Text("q"))) *),
        new Elem(null, "m", Null, TopScope, false, collection.mutable.ArrayBuffer[Node](<k/>, Text("m")).toSeq *),
        new Elem(null, "l", Null, TopScope, false, LazyList(<z/>, Text("lz")) *),
      )
    }<after/></div>)
  }

  it should "write very deep trees" in {
    val deep = (1 to 20_000).foldLeft[Elem](<p>leaf</p>)((acc, _) => <div>{acc}<s/></div>)
    assertWritesAsToString(deep)
  }

  it should "write an ElemOptimized's attributes (in its own order)" in {
    val e = ElemOptimized.transform(ElemOptimized.transform(<td id="i" title="t"/>, "class", (_: String) => "a b"), "data-x", (_: String) => "é \"€\"")
    val out = node(e)
    assert(normalized(out) == normalized(e.toString), out)
  }

  it should "write the prefix first, as it is" in {
    assert(seq(<p>x</p>, prefix = "<!DOCTYPE html>\n") == "<!DOCTYPE html>\n<p>x</p>")
    assert(seq(NodeSeq.Empty, prefix = "pré ") == "pré ")
  }

  // ---------- UTF-8 ----------

  private val utf8Samples: Seq[(String, String)] = Seq(
    "ASCII only" -> "plain ascii, digits 0123 & <tags>",
    "2-byte chars (é, ü, ç)" -> "Inscrição, ünïcödé, café",
    "3-byte chars (€, CJK)" -> "€ 100, 日本語",
    "4-byte chars (emoji: surrogate pairs)" -> "😀👍🏽 𝄞",
    "the boundaries of each length" -> "\u007f\u0080ÿĀ߿ࠀ￿",
    "lone surrogates (written as '?', as String.getBytes(UTF_8) does)" -> "a\ud800b\udc00c\ud83d",
    "a lone surrogate before chars to escape (they're still escaped)" -> "\ud83d&\ud83d<\ud83d\"\udc00>",
    "a surrogate pair split by a dropped control char (a pair once it's dropped, as toString then UTF-8)" -> "a\ud83d\u0001\ude00b\ud83d\u0001\u0002x",
    "mixed, with escapes" -> "é & \"€\" < 😀 > ç",
  )

  utf8Samples.foreach { case (name, text) =>
    it should s"encode text, attribute values, Unparsed and names in UTF-8 ($name)" in {
      assertWritesAsToString(<p title={text}>{text}</p>)
      assertWritesAsToString(<script>{Unparsed(text)}</script>)
      assertWritesAsToString(Elem(null, "x" + text.filter(_.isLetter).take(3), new UnprefixedAttribute("k" + text.filter(_.isLetter).take(2), "v", Null), TopScope, true))
      // Long enough for the streaming path (over ChunkSize / 6 chars), and to span several chunks.
      val long = text * (XmlJsStream.ChunkSize / text.length * 3)
      assertWritesAsToString(<p title={long}>{long}</p>)
    }
  }

  it should "never split a multi-byte char over two chunks (at every offset near the end of the buffer)" in {
    for (fill <- XmlJsStream.ChunkSize - 12 to XmlJsStream.ChunkSize + 2; text <- Seq("é", "€", "😀", "é€😀&\"")) {
      // `fill` ASCII bytes before the text, so it lands at each position around the end of the first chunk.
      val ascii = "a" * (fill - "<p>".length)
      assertWritesAsToString(<p>{ascii}{text}{text}</p>)
      assertWritesAsToString(<p>{ascii}{Unparsed(text * 3)}</p>)
      assertWritesAsToString(<p>{ascii + text * 3}</p>)
    }
  }

  // ---------- the writes ----------

  it should "write a page that fits in the buffer in a single write, with last = true" in {
    val s = written(XmlJsStream.writeSeq(<p>small é</p>, _, prefix = "<!DOCTYPE html>"))
    assert(s.writes.toSeq == Seq(("<!DOCTYPE html><p>small é</p>".getBytes(UTF_8).length, true)))
  }

  it should "end an empty page with an empty last write" in {
    assert(written(XmlJsStream.writeSeq(NodeSeq.Empty, _)).writes.toSeq == Seq((0, true)))
  }

  it should "write a big page in chunks of at most ChunkSize bytes, only the last one with last = true" in {
    val page = <div>{(1 to 5_000).map(i => <p class="c">{s"row $i é €"}</p>)}</div>
    val s = written(XmlJsStream.writeSeq(page, _))
    assert(s.writes.size > 10)
    assert(s.writes.forall(_._1 <= XmlJsStream.ChunkSize))
    assert(s.writes.init.forall(w => !w._2 && w._1 > 0) && s.writes.last._2)
    assert(java.util.Arrays.equals(s.bytes.toByteArray, page.toString.getBytes(UTF_8)))
  }

  it should "reuse its buffers correctly across writes (the thread's cached Out)" in {
    val a = <a>{"é" * 5000}</a>
    val b = <b>x</b>
    assert(node(a) == a.toString)
    assert(node(b) == b.toString) // nothing left over from the previous write
    assert(node(a) == a.toString)
  }

  it should "propagate a failure of the sink's write" in {
    val failing = new Content.Sink {
      override def write(last: Boolean, byteBuffer: ByteBuffer, callback: Callback): Unit = callback.failed(new java.io.IOException("gone"))
    }
    val e = intercept[java.io.IOException](XmlJsStream.writeSeq(<div>{"x" * 20_000}</div>, failing))
    assert(e.getMessage.contains("gone"))
    // and the thread's Out is still usable afterwards
    assert(node(<p>ok</p>) == "<p>ok</p>")
  }
}
