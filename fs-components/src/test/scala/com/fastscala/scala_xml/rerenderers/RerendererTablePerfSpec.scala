package com.fastscala.scala_xml.rerenderers

import com.fastscala.components.bootstrap5.helpers.BSHelpers.*
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.core.*
import com.fastscala.scala_xml.XmlStreamTablePerfSpec.{Cols, Rows, TdStyle}
import com.fastscala.scala_xml.utils.XmlStream
import com.fastscala.testutils.{AsyncProf, PerfFS}
import org.scalatest.flatspec.AnyFlatSpec

import java.io.StringWriter
import scala.xml.*

/**
 * Performance test for [[Rerenderer]]: building the same table as [[com.fastscala.scala_xml.XmlStreamTablePerfSpec]]
 * (V2, i.e. [[com.fastscala.components.bootstrap5.helpers.BSHelpers]]), but with each `<tr>` rendered by its own
 * Rerenderer, and one more `<td>` with a [[BSBtn]] whose callback rerenders the row. Measured: building it, i.e. a new
 * page, rendering every row's Rerenderer (a child FSContext and a callback per row) and the table.
 *
 * Lives in fs-components because it uses BSHelpers and BSBtn. Run with:
 * `sbt -J-Xmx8g "fs_components/testOnly com.fastscala.scala_xml.rerenderers.RerendererTablePerfSpec"`
 *
 * The last test profiles the build with [[AsyncProf]] (JFR + HTML flame graph in [[AsyncProf.OutDir]], named
 * `RerendererTable-build-...`).
 */
class RerendererTablePerfSpec extends AnyFlatSpec {

  import RerendererTablePerfSpec.*

  private val WarmupIterations = 10
  private val MeasuredIterations = 20
  private val ProfiledIterations = 10

  private def ms(nanos: Long): String = f"${nanos / 1e6}%.2f ms"

  private def stats(name: String, times: Seq[Long]): String = {
    val sorted = times.sorted
    s"$name: min ${ms(sorted.head)}, median ${ms(sorted(sorted.size / 2))}, avg ${ms(times.sum / times.size)}, max ${ms(sorted.last)}"
  }

  "A table with a Rerenderer per row" should "be built with a context and a button callback per row" in {
    val built = build()
    val page = built.page
    assert(page.callbacks.size == Rows)
    assert(page.key2FSContext.size == Rows)
    assert(page.rootFSContext.children.size == Rows)
    assert(built.rows.map(_.aroundId.get).distinct.size == Rows)

    // Sanity check of the output (not measured).
    val w = new StringWriter
    XmlStream.writeNode(built.table, w)
    val out = w.toString
    // (Compare as Booleans so a failure doesn't print the multi-MB output.)
    assert(out == built.table.toString, "XmlStream output differs from toString")
    assert(out.split("<tr ", -1).length - 1 == Rows)
    assert(out.split("<td ", -1).length - 1 == Rows * (Cols + 1))
    assert(out.split("<button ", -1).length - 1 == Rows)
    assert(out.contains("window._fs.callback("), out.take(1000))
    val ids = """ id="([^"]*)"""".r.findAllMatchIn(out).map(_.group(1)).toVector
    assert(ids.size == 1 + Rows + Rows * (Cols + 1))
    assert(ids.distinct.size == ids.size, "duplicate ids")
  }

  it should "be measured: building it" in {
    // Warm up the JIT.
    (1 to WarmupIterations).foreach(_ => build())

    val buildTimes = (1 to MeasuredIterations).map { _ =>
      val t0 = System.nanoTime()
      val built = build()
      val t1 = System.nanoTime()
      assert(built.table ne null)
      t1 - t0
    }

    info(s"Table: $Rows rows (each in a Rerenderer, with a button) x ${Cols + 1} cols")
    info(stats("Build (page, Rerenderers, callbacks)", buildTimes))
  }

  it should "profile building it with async-profiler" in {
    (1 to WarmupIterations).foreach(_ => build())
    // Several builds in one recording, for more samples; tables are discarded, so GC shows up too.
    var built = 0
    AsyncProf.profile("RerendererTable-build", threshold = 0) {
      (1 to ProfiledIterations).foreach(_ => if (build().table ne null) built += 1)
    }
    assert(built == ProfiledIterations)
    info(s"Profiled $ProfiledIterations builds; JFR + flame graph in ${AsyncProf.OutDir}")
  }
}

object RerendererTablePerfSpec {

  def newPage(): FSPage = PerfFS.newPage()

  final class Built(val page: FSPage, val table: Elem, val rows: IndexedSeq[Rerenderer])

  def build(): Built = {
    val page = newPage()
    implicit val fsc: FSContext = page.rootFSContext

    // A unique id for every element, from a counter (also for rerendered rows).
    var lastId = 0
    def nextId(): String = { lastId += 1; s"e$lastId" }

    def cell(content: NodeSeq): Elem =
      <td class="data-cell">{content}</td>.withId(nextId()).withStyle(TdStyle).px_2.py_1.text_center.align_middle

    val rows = (1 to Rows).map(r => new Rerenderer(rerenderer => implicit fsc =>
      <tr class="data-row">{
        (1 to Cols).map(c => cell(Text(s"r$r c$c"))) :+
          cell(BSBtn().BtnPrimary.sm.lbl("Refresh").callback(implicit fsc => rerenderer.rerender()).btn)
      }</tr>.withId(nextId()).table_active.border.border_primary.fw_bold
    ))

    val table = <table class="data-table">{rows.map(_.render())}</table>.withId(nextId()).table.table_striped
    new Built(page, table, rows)
  }
}
