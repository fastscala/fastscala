package com.fastscala.scala_xml.js

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.owasp.encoder.Encode
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import scala.xml.*

class NodeSeqInStringEscapedJsSpec extends AnyFlatSpec {

  /** A [[Content.Sink]] that collects everything written to it. */
  private class CapturingSink extends Content.Sink {
    val bytes = new ByteArrayOutputStream()
    var lastSeen = false

    override def write(last: Boolean, byteBuffer: ByteBuffer, callback: Callback): Unit = {
      val arr = new Array[Byte](byteBuffer.remaining())
      byteBuffer.get(arr)
      bytes.write(arr)
      lastSeen ||= last
      callback.succeeded()
    }

    def asString: String = new String(bytes.toByteArray, StandardCharsets.UTF_8)
  }

  /** A [[Callback]] that records how it was completed. */
  private class RecordingCallback extends Callback {
    var successes = 0
    var failure: Option[Throwable] = None

    override def succeeded(): Unit = successes += 1

    override def failed(x: Throwable): Unit = failure = Some(x)
  }

  /** Decodes a JS string literal body, as a JS engine would (`\xHH`, `\uHHHH` and single-char escapes). */
  private def unescapeJsString(s: String): String = {
    val sb = new StringBuilder
    var i = 0
    while (i < s.length) {
      if (s(i) == '\\' && i + 1 < s.length) {
        s(i + 1) match {
          case 'x' => sb.append(Integer.parseInt(s.substring(i + 2, i + 4), 16).toChar); i += 4
          case 'u' => sb.append(Integer.parseInt(s.substring(i + 2, i + 6), 16).toChar); i += 6
          case 'n' => sb.append('\n'); i += 2
          case 'r' => sb.append('\r'); i += 2
          case 't' => sb.append('\t'); i += 2
          case 'b' => sb.append('\b'); i += 2
          case 'f' => sb.append('\f'); i += 2
          case c => sb.append(c); i += 2
        }
      } else {
        sb.append(s(i)); i += 1
      }
    }
    sb.toString
  }

  private def streamed(ns: NodeSeq): (String, Array[Byte], RecordingCallback) = {
    val sink = new CapturingSink
    val cb = new RecordingCallback
    new NodeSeqInStringEscapedJs(ns).writeUtf8Enc(sink, cb)
    (sink.asString, sink.bytes.toByteArray, cb)
  }

  private val samples: Seq[(String, NodeSeq)] = Seq(
    "plain text element" -> <p>hello</p>,
    "nested elements with attributes" -> <div class="a b" id="x"><span data-v="1">t</span></div>,
    "double and single quotes" -> <p title={"he said \"hi\""}>it's</p>,
    "backslashes and newlines" -> <pre>{"a\\b\nc\r\nd\te"}</pre>,
    "script-closing sequence" -> <p>{"</script><script>alert(1)</script>"}</p>,
    "non-ASCII (2, 3 and 4 byte UTF-8)" -> <p>{"çãé € 日本 😀"}</p>,
    "line/paragraph separators" -> <p>{"a b c"}</p>,
    "several top-level nodes" -> (<b>1</b> ++ Text(" & ") ++ <i>2</i>),
    "group" -> Group(Seq(<b>g</b>, Text("t"))),
    "self-closing element" -> <br/>,
    "empty non-void element" -> <div></div>,
    "comment" -> <div><!-- c --></div>,
    "large content (bigger than writer buffers)" -> <div>{(1 to 5000).map(i => <span>{s"item $i 'q' \"dq\" é"}</span>)}</div>,
  )

  // ---------- cmd ----------

  "cmd" should "be the NodeSeq's XML, escaped for a JS string" in {
    val ns = <p title="x">a 'b' "c"</p>
    assert(new NodeSeqInStringEscapedJs(ns).cmd == Encode.forJavaScript(ns.toString()))
  }

  it should "be empty for an empty NodeSeq" in {
    assert(new NodeSeqInStringEscapedJs(NodeSeq.Empty).cmd == "")
  }

  it should "not contain unescaped quotes, backslashes, newlines or '</script'" in {
    val cmd = new NodeSeqInStringEscapedJs(<p a={"\""}>{"' \\ \n </script>"}</p>).cmd
    assert(!cmd.matches("""(?s).*(?<!\\)["'].*"""))
    assert(!cmd.contains("\n"))
    assert(!cmd.toLowerCase.contains("</script"))
  }

  // ---------- writeUtf8Enc / lengthUtf8Enc ----------

  samples.foreach { case (name, ns) =>
    "writeUtf8Enc" should s"stream exactly what cmd returns ($name)" in {
      val (out, _, _) = streamed(ns)
      assert(out == new NodeSeqInStringEscapedJs(ns).cmd)
    }

    "lengthUtf8Enc" should s"equal the number of bytes streamed ($name)" in {
      val (_, bytes, _) = streamed(ns)
      assert(new NodeSeqInStringEscapedJs(ns).lengthUtf8Enc == bytes.length)
    }

    it should s"equal the UTF-8 length of cmd ($name)" in {
      val js = new NodeSeqInStringEscapedJs(ns)
      assert(js.lengthUtf8Enc == js.cmd.getBytes(StandardCharsets.UTF_8).length)
    }
  }

  "writeUtf8Enc" should "complete the callback exactly once, successfully" in {
    val (_, _, cb) = streamed(<p>x</p>)
    assert(cb.successes == 1)
    assert(cb.failure.isEmpty)
  }

  it should "write nothing for an empty NodeSeq" in {
    val (out, _, cb) = streamed(NodeSeq.Empty)
    assert(out == "")
    assert(cb.successes == 1)
  }

  it should "be usable inside a JS string literal (round-trips through JS unescaping)" in {
    val ns = <p a={"\""}>{"' \\ \n </script> é"}</p>
    val (out, _, _) = streamed(ns)
    assert(unescapeJsString(out) == ns.toString())
  }

  "lengthUtf8Enc" should "be 0 for an empty NodeSeq" in {
    assert(new NodeSeqInStringEscapedJs(NodeSeq.Empty).lengthUtf8Enc == 0)
  }

  // ---------- composition ----------

  "JS.setContents" should "embed the escaped NodeSeq consistently in cmd and the stream" in {
    val js = JS.setContents("target", <p>{"a 'b' \"c\""}</p>)
    val sink = new CapturingSink
    js.writeUtf8Enc(sink, Callback.NOOP)
    assert(sink.asString == js.cmd)
    assert(js.lengthUtf8Enc == sink.bytes.size())
  }
}
