package com.fastscala.scala_xml.xml

import com.fastscala.js.Js
import com.fastscala.scala_xml.xml.XmlJsStreamFixtures.*
import com.fastscala.scala_xml.xml.baseline.XmlJsStreamBaseline
import com.fastscala.scala_xml.xml.streaming.XmlJsStream
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.*

/**
 * [[XmlJsStream]] writes exactly the bytes [[XmlJsStreamBaseline]] (the frozen copy the perf specs measure against)
 * does: for the perf specs' pages, and for random trees of every kind of node and Js. So an optimization that changes
 * what's written shows here (if a change of output is intended, reset the baseline).
 */
class XmlJsStreamBaselineSpec extends AnyFlatSpec {

  private def bytesOf(write: CollectingSink => Unit): Array[Byte] = {
    val sink = new CollectingSink
    write(sink)
    assert(sink.lastSeen, "the last write wasn't marked last")
    sink.bytes.toByteArray
  }

  private def firstDifference(a: Array[Byte], b: Array[Byte]): String = {
    val i = a.indices.find(i => i >= b.length || a(i) != b(i)).getOrElse(a.length)
    def around(x: Array[Byte]) = new String(x.slice(math.max(0, i - 60), i + 60), java.nio.charset.StandardCharsets.UTF_8)
    s"lengths ${a.length} / ${b.length}, first difference at byte $i:\n  baseline: ${around(a)}\n  now:      ${around(b)}"
  }

  private def checkNode(what: String, n: Node): Unit = {
    val base = bytesOf(XmlJsStreamBaseline.writeNode(n, _))
    val now = bytesOf(XmlJsStream.writeNode(n, _))
    assert(java.util.Arrays.equals(base, now), s"$what: ${firstDifference(base, now)}")
  }

  private def checkSeq(what: String, ns: NodeSeq, prefix: String): Unit = {
    val base = bytesOf(XmlJsStreamBaseline.writeSeq(ns, _, prefix))
    val now = bytesOf(XmlJsStream.writeSeq(ns, _, prefix))
    assert(java.util.Arrays.equals(base, now), s"$what: ${firstDifference(base, now)}")
  }

  private def checkJs(what: String, js: Js): Unit = {
    val base = bytesOf(XmlJsStreamBaseline.writeJs(js, _))
    val now = bytesOf(XmlJsStream.writeJs(js, _))
    assert(java.util.Arrays.equals(base, now), s"$what: ${firstDifference(base, now)}")
  }

  "XmlJsStream" should "write the perf specs' pages as the baseline does" in {
    checkNode("page, 40 sections", page(sections = 40))
    checkNode("page, 1 section", page(sections = 1))
    checkNode("table", table(rows = 300, cols = 12))
    checkNode("plain table", plainTable(rows = 300, cols = 12))
    checkSeq("a seq, with a prefix", NodeSeq.fromSeq((1 to 30).map(section)), "<!DOCTYPE html>\n")
    checkJs("rerender of a page", rerender(page(sections = 20)))
    checkJs("rerender of a table", rerender(table(rows = 200, cols = 8)))
    checkJs("rerender, scripts collected", rerenderWithScripts(page(sections = 20)))
    checkJs("rerenders in a rerender", nestedRerender(buttons = 300))
  }

  it should "write random trees of every kind of node and Js as the baseline does" in {
    val rnd = new scala.util.Random(1511)
    for (i <- 1 to 3_000) {
      val depth = 1 + rnd.nextInt(5)
      val tree = randomTree(rnd, depth)
      checkNode(s"tree $i (depth $depth)", tree)
      checkJs(s"tree $i in a rerender", rerender(tree))
    }
  }

  it should "write big texts and attribute values (many chunks) as the baseline does" in {
    val big = "Lorem <ipsum> & \"dolor\" — é € 😀 \u2028 " * 20_000
    checkNode("big text", <p title={big.take(50_000)}>{big}</p>)
    checkJs("big text in a rerender", rerender(<p title={big.take(50_000)}>{big}</p>))
  }
}
