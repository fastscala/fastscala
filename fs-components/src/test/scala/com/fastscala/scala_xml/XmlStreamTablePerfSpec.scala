package com.fastscala.scala_xml

import com.fastscala.components.bootstrap5.helpers.{BSClassesHelper, BSHelpers}
import com.fastscala.scala_xml.utils.XmlStream
import com.fastscala.testutils.AsyncProf
import org.scalatest.flatspec.AnyFlatSpec

import java.io.{StringWriter, Writer}
import scala.xml.*

/**
 * Performance test: a `<table>` with [[XmlStreamTablePerfSpec.Rows]] `<tr>`, each with 4 classes added via Bootstrap
 * class helpers and [[XmlStreamTablePerfSpec.Cols]] `<td>`, each `<td>` also with 4 classes, rendered with
 * [[XmlStream]]. Run for each implementation of the attribute helpers, for comparison:
 *
 *  - V2: [[BSHelpers]], on [[ScalaXmlElemUtils]] ([[com.fastscala.scala_xml.utils.ElemWithFastAttrsV2]])
 *  - V1: the same class helpers on [[ScalaXmlElemUtilsV1]] ([[com.fastscala.scala_xml.utils.ElemWithFastAttrsV1]])
 *
 * The class transforms are computed when the attributes are read, so for implementations XmlStream has no fast path
 * for, the first render includes computing the final `class` attributes.
 *
 * Lives in fs-components because it uses BSHelpers. Run with:
 * `sbt -J-Xmx8g "fs_components/testOnly com.fastscala.scala_xml.XmlStreamTablePerfSpec"`
 *
 * Running several variants in one JVM lets each skew the JIT for the next (the shared class helpers see several
 * addClass implementations), so for comparable numbers run one per JVM with `-Dperf.variants=V1` (or `V2`; comma
 * separated; default: all), e.g. `sbt -J-Xmx8g -Dperf.variants=V1 "fs_components/testOnly ..."`.
 *
 * The second test of each variant profiles building the table with [[AsyncProf]] (JFR + HTML flame graph in
 * [[AsyncProf.OutDir]], named `XmlStreamTable-build-<variant>-...`).
 */
class XmlStreamTablePerfSpec extends AnyFlatSpec {

  import XmlStreamTablePerfSpec.*

  private val WarmupIterations = 10
  private val MeasuredIterations = 20
  private val ProfiledIterations = 10

  private val variants: Seq[(String, () => Elem)] = {
    val all = Seq("V2" -> (() => V2Table.build()), "V1" -> (() => V1Table.build()))
    sys.props.get("perf.variants").map(_.split(",").map(_.trim).toSet) match {
      case Some(selected) => all.filter(v => selected.contains(v._1))
      case None => all
    }
  }

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

  variants.foreach { case (variant, buildTable) =>

    s"XmlStream ($variant)" should s"render a $Rows-row x $Cols-col table with Bootstrap classes" in {
      // Sanity check: output matches toString and the classes are there.
      val small = buildTable()
      val w = new StringWriter
      XmlStream.writeNode(small, w)
      val out = w.toString
      // (Compare as Booleans so a failure doesn't print the multi-MB output.)
      assert(out == small.toString, "XmlStream output differs from toString")
      assert(out.split("<tr ", -1).length - 1 == Rows)
      assert(out.split("<td ", -1).length - 1 == Rows * Cols)
      // Class order isn't fixed (it differs between implementations), and addClass may leave a leading space.
      def classesOf(tag: String): Set[String] =
        s"""<$tag class="([^"]*)"""".r.findFirstMatchIn(out).map(_.group(1).trim.split("\\s+").toSet).getOrElse(Set())
      assert(classesOf("td") == Set("px-2", "py-1", "text-center", "align-middle"), out.take(500))
      assert(classesOf("tr") == Set("table-active", "border", "border-primary", "fw-bold"), out.take(500))
      assert(out.contains(">r1 c1</td>"), out.take(500))

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

      info(s"[$variant] Table: $Rows rows x $Cols cols, 4 classes per <tr> and <td>, $chars chars of output")
      info(stats(s"[$variant] Build                     ", buildTimes.result()))
      info(stats(s"[$variant] Render (first, XmlStream) ", renderTimes.result()))
      info(stats(s"[$variant] Re-render (XmlStream)     ", rerenderTimes))
    }

    it should s"profile building the table with async-profiler ($variant)" in {
      (1 to WarmupIterations).foreach(_ => buildTable())
      // Several builds in one recording, for more samples; tables are discarded, so GC shows up too.
      var built = 0
      AsyncProf.profile(s"XmlStreamTable-build-$variant", threshold = 0) {
        (1 to ProfiledIterations).foreach(_ => if (buildTable() ne null) built += 1)
      }
      assert(built == ProfiledIterations)
      info(s"[$variant] Profiled $ProfiledIterations builds; JFR + flame graph in ${AsyncProf.OutDir}")
    }
  }
}

object XmlStreamTablePerfSpec {

  val Rows = 50_000
  val Cols = 20

  // Each table is built in its own object, importing only its own helpers, so the implicit conversions don't clash.

  /** With [[BSHelpers]], i.e. [[ScalaXmlElemUtils]] / ElemWithFastAttrsV2. */
  object V2Table {
    import BSHelpers.*

    def build(): Elem =
      <table>{
        (1 to Rows).map { r =>
          <tr>{
            (1 to Cols).map { c =>
              <td>{s"r$r c$c"}</td>.px_2.py_1.text_center.align_middle
            }
          }</tr>.table_active.border.border_primary.fw_bold
        }
      }</table>.table.table_striped
  }

  /** The same Bootstrap class helpers as BSHelpers, but on [[ScalaXmlElemUtilsV1]] / ElemWithFastAttrsV1. */
  object V1Helpers {
    implicit class RichElemV1(val elem: Elem) extends AnyVal with BSClassesHelper[Elem] {
      override protected def addClass(clas: String): Elem = new ScalaXmlElemUtilsV1.RichElem(elem).addClass(clas)
    }
  }

  object V1Table {
    import V1Helpers.*

    def build(): Elem =
      <table>{
        (1 to Rows).map { r =>
          <tr>{
            (1 to Cols).map { c =>
              <td>{s"r$r c$c"}</td>.px_2.py_1.text_center.align_middle
            }
          }</tr>.table_active.border.border_primary.fw_bold
        }
      }</table>.table.table_striped
  }
}
