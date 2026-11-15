package com.fastscala.scala_xml.xml

import com.fastscala.js.Js
import com.fastscala.scala_xml.xml.XmlJsStreamFixtures.*
import com.fastscala.scala_xml.xml.baseline.XmlJsStreamBaseline
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.*

/**
 * Performance test: [[XmlJsStream]] against [[XmlJsStreamBaseline]] (a frozen copy of what it was at the last baseline
 * reset), writing the pages of [[XmlJsStreamFixtures]] (built once, beforehand: only serializing is measured) into a
 * sink that discards the bytes.
 *
 * For each workload: how fast each writes it (MB of output per second; the best of several runs), how many times faster
 * the current one is, and at the end the geometric mean of those ratios. Only the byte count is asserted on
 * ([[XmlJsStreamBaselineSpec]] checks the bytes themselves), not the times: they vary from run to run (see
 * [[EncoderPerfSpec]]), so only a ratio well away from 1, in several runs, means something. Run it alone.
 *
 * Run with: `sbt "fs_scala_xml/testOnly com.fastscala.scala_xml.xml.XmlJsStreamPerfSpec"`
 */
class XmlJsStreamPerfSpec extends AnyFlatSpec {

  import XmlJsStreamPerfSpec.*

  private val Warmups = 12
  private val Runs = 21

  private val ratios = collection.mutable.ArrayBuffer[Double]()

  /** As EncoderPerfSpec's: `base` and `now` in turn (which one first alternates), the best of `Runs` of each. */
  private def compare(name: String)(base: => Long)(now: => Long): Unit = {
    var i = 0
    var (bytesBase, bytesNow) = (0L, 0L)
    while (i < Warmups) { bytesBase = base; bytesNow = now; i += 1 }
    var (bestBase, bestNow) = (Long.MaxValue, Long.MaxValue)
    i = 0
    while (i < Runs) {
      if (i % 2 == 0) {
        val t0 = System.nanoTime()
        bytesBase = base
        val t1 = System.nanoTime()
        bytesNow = now
        val t2 = System.nanoTime()
        bestBase = math.min(bestBase, t1 - t0)
        bestNow = math.min(bestNow, t2 - t1)
      } else {
        val t0 = System.nanoTime()
        bytesNow = now
        val t1 = System.nanoTime()
        bytesBase = base
        val t2 = System.nanoTime()
        bestNow = math.min(bestNow, t1 - t0)
        bestBase = math.min(bestBase, t2 - t1)
      }
      i += 1
    }
    assert(bytesBase == bytesNow, s"$name: XmlJsStreamBaseline wrote $bytesBase bytes, XmlJsStream $bytesNow")
    def mbs(nanos: Long) = bytesNow / 1e6 / (nanos / 1e9)
    ratios += bestBase.toDouble / bestNow
    info(f"$name%-44s ${bytesNow / 1e6}%6.1f MB   baseline ${mbs(bestBase)}%5.0f MB/s   now ${mbs(bestNow)}%5.0f MB/s   x${bestBase.toDouble / bestNow}%.2f")
  }

  private def node(name: String, n: Node, reps: Int): Unit = compare(name)(nodeBase(n, reps))(nodeNow(n, reps))

  private def js(name: String, code: Js, reps: Int): Unit = compare(name)(jsBase(code, reps))(jsNow(code, reps))

  "XmlJsStream" should "be measured against XmlJsStreamBaseline" in {
    val bigPage = page(sections = 400)
    val smallPage = page(sections = 3)
    val bigTable = table(rows = 5_000, cols = 20)
    val plain = plainTable(rows = 5_000, cols = 20)
    val smallTable = table(rows = 10, cols = 8)

    node("page, 400 sections", bigPage, reps = 4)
    node("page, 3 sections (many small writes)", smallPage, reps = 500)
    node("table 5000x20, ElemOptimized", bigTable, reps = 3)
    node("table 5000x20, plain scala-xml", plain, reps = 3)
    js("rerender of the page (in a JS string)", rerender(bigPage), reps = 4)
    js("rerender of the table", rerender(bigTable), reps = 3)
    js("rerender, scripts collected", rerenderWithScripts(bigPage), reps = 4)
    js("rerender of a small table", rerender(smallTable), reps = 2_000)
    js("rerenders in onclicks, in a rerender", nestedRerender(buttons = 3_000), reps = 3)
    info(f"geometric mean of ${ratios.size} workloads: x${math.exp(ratios.map(math.log).sum / ratios.size)}%.2f")
  }
}

object XmlJsStreamPerfSpec {

  // The measured loops: each a method of its own (so it's compiled as one), once for each XmlJsStream.

  def nodeBase(n: Node, reps: Int): Long = {
    val sink = new CountingSink
    var r = 0
    while (r < reps) { XmlJsStreamBaseline.writeNode(n, sink); r += 1 }
    sink.count
  }

  def nodeNow(n: Node, reps: Int): Long = {
    val sink = new CountingSink
    var r = 0
    while (r < reps) { XmlJsStream.writeNode(n, sink); r += 1 }
    sink.count
  }

  def jsBase(js: Js, reps: Int): Long = {
    val sink = new CountingSink
    var r = 0
    while (r < reps) { XmlJsStreamBaseline.writeJs(js, sink); r += 1 }
    sink.count
  }

  def jsNow(js: Js, reps: Int): Long = {
    val sink = new CountingSink
    var r = 0
    while (r < reps) { XmlJsStream.writeJs(js, sink); r += 1 }
    sink.count
  }
}
