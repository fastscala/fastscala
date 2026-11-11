package com.fastscala.scala_xml

import com.fastscala.components.bootstrap5.helpers.BSHelpers.*
import com.fastscala.scala_xml.utils.XmlStream
import com.fastscala.testutils.AsyncProf
import org.scalatest.flatspec.AnyFlatSpec

import java.io.{StringWriter, Writer}
import scala.xml.*

/**
 * Performance test: a `<table>` with 10 000 `<tr>`, each with 4 classes added via [[BSHelpers]] and 10 `<td>`, each
 * `<td>` also with 4 classes added via [[BSHelpers]], rendered with [[XmlStream]].
 *
 * BSHelpers' class transforms are deferred (see [[ElemWithFastAttrsV2]]) and computed when the attributes are
 * read, i.e. while rendering, so the render time includes computing the final `class` attributes.
 *
 * Lives in fs-components because it uses BSHelpers. Run with:
 * `sbt -J-Xmx8g "fs_components/testOnly com.fastscala.scala_xml.XmlStreamTablePerfSpec"`
 *
 * The second test profiles building the table with [[AsyncProf]] (JFR + HTML flame graph in [[AsyncProf.OutDir]]).
 */
class XmlStreamTablePerfSpec extends AnyFlatSpec {

  private val Rows = 50_000
  private val Cols = 20
  private val WarmupIterations = 10
  private val MeasuredIterations = 20
  private val ProfiledIterations = 10

  private def buildTable(): Elem =
    <table>{
      (1 to Rows).map { r =>
        <tr>{
          (1 to Cols).map { c =>
            <td>{s"r$r c$c"}</td>.px_2.py_1.text_center.align_middle
          }
        }</tr>.table_active.border.border_primary.fw_bold
      }
    }</table>.table.table_striped

  /** Discards output, so only rendering is measured (no growing buffer to copy). */
  private final class CountingWriter extends Writer {
    var count = 0L
    override def write(cbuf: Array[Char], off: Int, len: Int): Unit = count += len
    override def flush(): Unit = ()
    override def close(): Unit = ()
  }

  private def render(table: Elem): Long = {
    val w = new CountingWriter
    XmlStream.writeNode(table, w)
    w.count
  }

  private def ms(nanos: Long): String = f"${nanos / 1e6}%.2f ms"

  private def stats(name: String, times: Seq[Long]): String = {
    val sorted = times.sorted
    s"$name: min ${ms(sorted.head)}, median ${ms(sorted(sorted.size / 2))}, " +
      s"avg ${ms(times.sum / times.size)}, max ${ms(sorted.last)}"
  }

  "XmlStream" should "render a 10000-row x 10-col table with BSHelpers classes" in {
    // Sanity check: output matches toString and the classes are there.
    val small = buildTable()
    val w = new StringWriter
    XmlStream.writeNode(small, w)
    val out = w.toString
    // (Compare as Booleans so a failure doesn't print the multi-MB output.)
    assert(out == small.toString, "XmlStream output differs from toString")
    assert(out.split("<tr ", -1).length - 1 == Rows)
    assert(out.split("<td ", -1).length - 1 == Rows * Cols)
    // addClass may leave a leading space in the class attribute
    val firstTd = """<td class="\s?px-2 py-1 text-center align-middle">r1 c1</td>""".r
    val firstTr = """<tr class="\s?table-active border border-primary fw-bold">""".r
    assert(firstTd.findFirstIn(out).isDefined, out.take(500))
    assert(firstTr.findFirstIn(out).isDefined, out.take(500))

    // Warm up the JIT.
    (1 to WarmupIterations).foreach(_ => render(buildTable()))

    // Each iteration builds a fresh table: the class transforms are computed (and cached) on first render.
    val buildTimes = Seq.newBuilder[Long]
    val renderTimes = Seq.newBuilder[Long]
    var chars = 0L
    (1 to MeasuredIterations).foreach { _ =>
      val t0 = System.nanoTime()
      val table = buildTable()
      val t1 = System.nanoTime()
      chars = render(table)
      val t2 = System.nanoTime()
      buildTimes += t1 - t0
      renderTimes += t2 - t1
    }

    // Rendering an already-rendered table again (attributes already computed).
    val prebuilt = buildTable()
    render(prebuilt)
    val rerenderTimes = (1 to MeasuredIterations).map { _ =>
      val t0 = System.nanoTime()
      render(prebuilt)
      System.nanoTime() - t0
    }

    info(s"Table: $Rows rows x $Cols cols, 4 classes per <tr> and <td>, $chars chars of output")
    info(stats("Build                     ", buildTimes.result()))
    info(stats("Render (first, XmlStream) ", renderTimes.result()))
    info(stats("Re-render (XmlStream)     ", rerenderTimes))
  }

  it should "profile building the table with async-profiler" in {
    (1 to WarmupIterations).foreach(_ => buildTable())
    // Several builds in one recording, for more samples; tables are discarded, so GC shows up too.
    var built = 0
    AsyncProf.profile("XmlStreamTable-build", threshold = 0) {
      (1 to ProfiledIterations).foreach(_ => if (buildTable() ne null) built += 1)
    }
    assert(built == ProfiledIterations)
    info(s"Profiled $ProfiledIterations builds; JFR + flame graph in ${AsyncProf.OutDir}")
  }
}
