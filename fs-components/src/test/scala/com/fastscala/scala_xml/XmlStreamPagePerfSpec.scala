package com.fastscala.scala_xml

import com.fastscala.components.bootstrap5.alerts.SimpleAlert
import com.fastscala.components.bootstrap5.helpers.{BSClassesHelper, BSColorsHelper, BSDataHelper}
import com.fastscala.components.bootstrap5.modals.BSModal5Base
import com.fastscala.components.bootstrap5.progress.BSProgress
import com.fastscala.components.bootstrap5.utils.{BSBtn, BsIcn, ImmediateInputFields}
import com.fastscala.components.bootstrap5.utils.BsIcn.RichIcn
import com.fastscala.core.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.utils.XmlStream
import com.fastscala.testutils.{AsyncProf, PerfFS}
import org.scalatest.flatspec.AnyFlatSpec

import java.io.{StringWriter, Writer}
import scala.xml.*

/**
 * Performance test: a big page using many parts of the library, rendered with [[XmlStream]]. The page has
 * [[XmlStreamPagePerfSpec.Sections]] sections (Bootstrap cards), each with:
 *
 *  - literal XML with Bootstrap class helpers on [[ScalaXmlElemUtilsV1]] (see [[XmlStreamPagePerfSpec.V1Helpers]]):
 *    ids from a counter, styles, classes and data attributes, as in `<td>...</td>.withId(nextId()).withStyle(...).px_2`;
 *  - [[BSBtn]]s: as `<button>` (with callbacks, a confirm, icons), `<span>` and `<a>` (`btnLink`), in several styles;
 *  - [[ImmediateInputFields]]: text (with and without a button), password, checkbox, select, range and textarea;
 *  - a [[BSModal5Base]] rendered in the page (its Rerenderers, an ImmediateInputFields field in the body, and footer
 *    buttons), with a literal button that opens it;
 *  - a small table, a list group, a [[BSProgress]] bar and a [[SimpleAlert]].
 *
 * Each page is built on a new FastScala page (see [[PerfFS]]), so building it includes registering every callback and
 * the modals' child contexts. Measured: building the page, writing it the first time (when the class transforms of the
 * helpers are computed) and writing it again.
 *
 * Run with: `sbt -J-Xmx8g "fs_components/testOnly com.fastscala.scala_xml.XmlStreamPagePerfSpec"`
 *
 * The profiling tests write a JFR + HTML flame graph to [[AsyncProf.OutDir]], named `XmlStreamPage-build-...` and
 * `XmlStreamPage-write-...`.
 */
class XmlStreamPagePerfSpec extends AnyFlatSpec {

  import XmlStreamPagePerfSpec.*

  private val WarmupIterations = 10
  private val MeasuredIterations = 20
  private val ProfiledIterations = 10

  /** Discards output, so only writing is measured (no growing buffer to copy). */
  private final class CountingWriter extends Writer {
    var count = 0L

    override def write(cbuf: Array[Char], off: Int, len: Int): Unit = count += len

    override def flush(): Unit = ()

    override def close(): Unit = ()
  }

  private def write(page: Elem): Long = {
    val w = new CountingWriter
    XmlStream.writeNode(page, w)
    w.count
  }

  private def ms(nanos: Long): String = f"${nanos / 1e6}%.2f ms"

  private def stats(name: String, times: Seq[Long]): String = {
    val sorted = times.sorted
    s"$name: min ${ms(sorted.head)}, median ${ms(sorted(sorted.size / 2))}, " +
      s"avg ${ms(times.sum / times.size)}, max ${ms(sorted.last)}"
  }

  private def count(out: String, s: String): Int = out.split(java.util.regex.Pattern.quote(s), -1).length - 1

  "A big page using many parts of the library" should "be written by XmlStream as by toString, with everything in it" in {
    val one = build(1)
    val built = build(Sections)
    // Every section registers the same callbacks, and the page nothing else.
    assert(one.page.callbacks.size >= 15, one.page.callbacks.size)
    assert(built.page.callbacks.size == Sections * one.page.callbacks.size)

    val w = new StringWriter
    XmlStream.writeNode(built.html, w)
    val out = w.toString
    // (Compare as Booleans so a failure doesn't print the multi-MB output.)
    assert(out == built.html.toString, "XmlStream output differs from toString")

    // The start tags, as (label, attributes); the attributes' order isn't fixed, so they're compared as a Map.
    val tags: Vector[(String, Map[String, String])] = """<(\w+)((?: [\w-]+="[^"]*")*)\s*/?>""".r.findAllMatchIn(out).map { m =>
      m.group(1) -> """ ([\w-]+)="([^"]*)"""".r.findAllMatchIn(m.group(2)).map(a => a.group(1) -> a.group(2)).toMap
    }.toVector

    def classesOf(attrs: Map[String, String]): Set[String] = attrs.get("class").map(_.trim.split("\\s+").toSet).getOrElse(Set())

    /** How many `<label ...>` start tags have all of `classes`. */
    def countTags(label: String, classes: String*): Int = tags.count { case (l, attrs) => l == label && classes.toSet.subsetOf(classesOf(attrs)) }

    def countAttr(name: String, value: String): Int = tags.count(_._2.get(name).contains(value))

    assert(countTags("div", "card") == Sections, out.take(2000))
    assert(countTags("div", "modal", "fade") == Sections, out.take(2000))
    assert(countTags("button") == Sections * ButtonsPerSection, out.take(2000))
    assert(countTags("input") == Sections * InputsPerSection, out.take(2000))
    assert(countTags("select") == Sections, out.take(2000))
    assert(countTags("textarea") == Sections, out.take(2000))
    assert(countTags("tr", "data-row", "border", "border-primary") == Sections * TableRows, out.take(2000))
    assert(countTags("td", "data-cell", "px-2", "py-1", "text-center", "align-middle") == Sections * TableRows * TableCols, out.take(2000))
    assert(countTags("i") == Sections * IconsPerSection, out.take(2000))
    assert(countAttr("role", "progressbar") == Sections, out.take(2000))
    assert(countAttr("role", "alert") == Sections, out.take(2000))
    assert(countAttr("data-bs-toggle", "modal") == Sections, out.take(2000))
    assert(countAttr("style", TdStyle) == Sections * TableRows * TableCols, out.take(2000))
    assert(count(out, "window._fs.callback(") >= Sections * one.page.callbacks.size, out.take(2000))
    assert(out.contains(""">s1 r1 c1</td>"""), out.take(2000))
    // No helper calls ended up as text (as `<a/>.b` in XML content would).
    assert("""</\w+>\.\w""".r.findFirstIn(out).isEmpty, """</\w+>\.\w""".r.findFirstMatchIn(out).map(m => out.slice(m.start - 200, m.end + 200)))

    // Every id is different (from the counter, and the ones the components generate).
    val ids = """ id="([^"]*)"""".r.findAllMatchIn(out).map(_.group(1)).toVector
    assert(ids.size > Sections * TableRows * TableCols)
    assert(ids.distinct.size == ids.size, "duplicate ids")
  }

  it should "be measured: building and writing it" in {
    // Warm up the JIT.
    (1 to WarmupIterations).foreach(_ => write(build(Sections).html))

    // Each iteration builds a fresh page: the class transforms are computed (and cached) when first written.
    val buildTimes = Seq.newBuilder[Long]
    val writeTimes = Seq.newBuilder[Long]
    var chars = 0L
    var callbacks = 0
    (1 to MeasuredIterations).foreach { _ =>
      val t0 = System.nanoTime()
      val built = build(Sections)
      val t1 = System.nanoTime()
      chars = write(built.html)
      val t2 = System.nanoTime()
      buildTimes += t1 - t0
      writeTimes += t2 - t1
      callbacks = built.page.callbacks.size
    }

    // Writing an already-written page again (attributes already computed).
    val prebuilt = build(Sections).html
    write(prebuilt)
    val rewriteTimes = (1 to MeasuredIterations).map { _ =>
      val t0 = System.nanoTime()
      write(prebuilt)
      System.nanoTime() - t0
    }

    info(s"Page: $Sections sections; $callbacks callbacks; $chars chars of output")
    info(stats("Build (page, callbacks, modals)", buildTimes.result()))
    info(stats("Write (first, XmlStream)       ", writeTimes.result()))
    info(stats("Write again (XmlStream)        ", rewriteTimes))
  }

  it should "profile building it with async-profiler" in {
    (1 to WarmupIterations).foreach(_ => build(Sections))
    // Several builds in one recording, for more samples; pages are discarded, so GC shows up too.
    var built = 0
    AsyncProf.profile("XmlStreamPage-build", threshold = 0) {
      (1 to ProfiledIterations).foreach(_ => if (build(Sections).html ne null) built += 1)
    }
    assert(built == ProfiledIterations)
    info(s"Profiled $ProfiledIterations builds; JFR + flame graph in ${AsyncProf.OutDir}")
  }

  it should "profile writing it with async-profiler" in {
    // Fresh pages, so each write also computes the class transforms, as the first write of a real page does.
    val pages = (1 to ProfiledIterations).map(_ => build(Sections).html)
    (1 to WarmupIterations).foreach(_ => write(build(Sections).html))
    var chars = 0L
    AsyncProf.profile("XmlStreamPage-write", threshold = 0) {
      pages.foreach(p => chars += write(p))
    }
    assert(chars > 0)
    info(s"Profiled $ProfiledIterations writes; JFR + flame graph in ${AsyncProf.OutDir}")
  }
}

object XmlStreamPagePerfSpec {

  val Sections = 10_000

  val TableRows = 5
  val TableCols = 4

  /** Items in each section's list group (declared before the counts that use it, so it's initialized first). */
  val ListItems = 3

  /** `<button>`s per section: 7 in the toolbar, 1 in the text field, 3 in the modal (header close, 2 in the footer),
   * 1 that opens the modal, 1 closing the alert and 1 per table row. */
  val ButtonsPerSection: Int = 7 + 1 + 3 + 1 + 1 + TableRows

  /** `<input>`s per section: text (x2), password, checkbox, range, and the text field in the modal. */
  val InputsPerSection = 6

  /** `<i class="bi ...">` icons per section: header, 3 in the toolbar, and 1 per list group item. */
  val IconsPerSection: Int = 1 + 3 + ListItems

  /** Set on every <td>: keep long content to one truncated line. */
  val TdStyle = "max-width: 12rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap"

  /**
   * The Bootstrap class, color and data attribute helpers, on [[ScalaXmlElemUtilsV1]] / ElemOptimized (which also
   * gives withId, withStyle, withAttr, ...). Only these are imported for the literal XML in the page.
   */
  object V1Helpers {
    implicit class RichElemV1(val elem: Elem) extends AnyVal
      with BSClassesHelper[Elem] with BSColorsHelper[Elem] with BSDataHelper[Elem] with ScalaXmlElemUtilsV1 {
      override protected def setAttribute(name: String, value: String): Elem = withAttr(name -> value)
    }
  }

  import V1Helpers.*

  final class Built(val page: FSPage, val html: Elem)

  /** The state the section's fields edit. */
  final class SectionState(s: Int) {
    var name: String = s"Section $s"
    var notes: String = s"Some notes for section $s, with <special> & \"escaped\" characters."
    var password: String = ""
    var enabled: Boolean = s % 2 == 0
    var level: Double = (s % 10).toDouble
    var color: String = Colors(s % Colors.size)
  }

  val Colors: Seq[String] = Seq("red", "green", "blue", "yellow", "black")

  /** A modal with a field and footer buttons, rendered in the page. */
  final class SectionModal(s: Int, state: SectionState) extends BSModal5Base {
    override def modalHeaderTitle: String = s"Edit section $s"

    override def modalBodyContents()(implicit fsc: FSContext): NodeSeq =
      <p>Change the name of <b>{state.name}</b>:</p>.mb_2.text_muted ++
        <div>{ImmediateInputFields.text(() => state.name, v => {state.name = v; Js.Void}, placeholder = "Name")}</div>.mb_3

    override def modalFooterContents()(implicit fsc: FSContext): Option[NodeSeq] = Some(
      BSBtn().BtnSecondary.lbl("Close").onclick(hide()).btn ++
        BSBtn().BtnPrimary.lbl("Save").callback(implicit fsc => hideAndRemoveAndDeleteContext()).btn.ms_2
    )
  }

  def build(sections: Int): Built = {
    val page = PerfFS.newPage()
    implicit val fsc: FSContext = page.rootFSContext

    // A unique id for the literal elements, from a counter.
    var lastId = 0

    def nextId(): String = {
      lastId += 1; s"e$lastId"
    }

    def toolbar(s: Int, state: SectionState): Elem =
      <div class="section-toolbar">{
        BSBtn().BtnPrimary.sm.icn(_.biPlus).lbl("Add").callback(implicit fsc => Js.Void).btn ++
          BSBtn().BtnOutlineSecondary.sm.icn(_.biPencil).lbl("Edit").callback(implicit fsc => Js.Void).btn ++
          BSBtn().BtnDanger.sm.icn(_.biTrash).lbl("Delete").callbackConfirm(s"Delete section $s?", implicit fsc => Js.Void).btn ++
          BSBtn().BtnSuccess.sm.lbl("Run once").callbackRunOnce(implicit fsc => Js.Void).btn ++
          BSBtn().BtnLight.sm.lbl("Disabled").disabled.btn ++
          BSBtn().BtnWarning.sm.lbl(if (state.enabled) "Disable" else "Enable").callback(implicit fsc => {state.enabled = !state.enabled; Js.Void}).btn ++
          BSBtn().BtnInfo.sm.lbl("Info").withAdditionalAttrs("title" -> s"About section $s").callback(implicit fsc => Js.Void).btn ++
          BSBtn().BtnLink.sm.lbl("Details").href(s"/sections/$s").btnLink ++
          BSBtn().BtnSecondary.sm.lbl("Tag").span
      }</div>.withId(nextId()).d_flex.flex_wrap.gap_2.mb_3

    // (Chained helpers only apply to Scala expressions: in XML content, `<a/>.b` is text, so elements with helpers
    // are always inside `{...}`.)

    def field(label: Option[Elem], input: Elem, col: Elem => Elem): Elem =
      col(<div>{label.map(_.form_label).getOrElse(NodeSeq.Empty) ++ input}</div>.withId(nextId()))

    def fields(s: Int, state: SectionState): Elem =
      <div class="section-fields">{
        field(Some(<label>Name</label>.withFor(s"name-$s")), ImmediateInputFields.text(() => state.name, v => {state.name = v; Js.Void}, id = Some(s"name-$s"), placeholder = "Name"), _.col_md_4) ++
          field(Some(<label>Search</label>), ImmediateInputFields.text(() => "", _ => Js.Void, placeholder = "Search...", btn = Some(BSBtn().BtnOutlinePrimary.lbl("Go"))), _.col_md_4) ++
          field(Some(<label>Password</label>), ImmediateInputFields.password(() => state.password, v => {state.password = v; Js.Void}, placeholder = "Password"), _.col_md_4) ++
          field(None, ImmediateInputFields.checkbox(() => state.enabled, v => {state.enabled = v; Js.Void}, "Enabled"), _.col_md_4.align_self_end) ++
          field(Some(<label>Color</label>), ImmediateInputFields.select[String](() => Colors, () => state.color, v => {state.color = v; Js.Void}, toString = _.capitalize), _.col_md_4) ++
          field(Some(<label>Level</label>), ImmediateInputFields.range(() => state.level, v => {state.level = v; Js.Void}, min = 0, max = 10, step = Some(1), classes = "form-range"), _.col_md_4) ++
          field(Some(<label>Notes</label>), ImmediateInputFields.textarea(() => state.notes, v => {state.notes = v; Js.Void}, classes = "form-control", rows = 3), _.col_12)
      }</div>.withId(nextId()).row.g_3.mb_3

    def table(s: Int): Elem =
      <table class="data-table">{
        (1 to TableRows).map { r =>
          <tr class="data-row">{
            (1 until TableCols).map { c =>
              <td class="data-cell">{s"s$s r$r c$c"}</td>.withId(nextId()).withStyle(TdStyle).px_2.py_1.text_center.align_middle
            } :+
              <td class="data-cell">{BSBtn().BtnOutlineDanger.sm.lbl("Remove").callback(implicit fsc => Js.Void).btn}</td>.withId(nextId()).withStyle(TdStyle).px_2.py_1.text_center.align_middle
          }</tr>.withId(nextId()).addClassIf(r % 2 == 0, "table-active").border.border_primary
        }
      }</table>.withId(nextId()).table.table_striped.table_hover.mb_3

    def listGroup(s: Int): Elem =
      <ul class="section-list">{
        (1 to ListItems).map { i =>
          <li>{
            BsIcn.biCheck.icn ++
              <span>{s"Item $i of section $s"}</span>.ms_2 ++
              <span>{(i * s).toString}</span>.badge.text_bg_secondary.ms_auto
          }</li>.withId(nextId()).list_group_item.d_flex.align_items_center
        }
      }</ul>.withId(nextId()).list_group.mb_3

    def section(s: Int): Elem = {
      val state = new SectionState(s)
      val modal = new SectionModal(s, state)
      <div class="card">{
        <div>{
          <h5>{BsIcn.biGear.icn ++ <span>{state.name}</span>.ms_2}</h5>.withId(nextId()).mb_0 ++
            <span>{s"#$s"}</span>.badge.text_bg_primary
        }</div>.withId(nextId()).card_header.d_flex.justify_content_between.align_items_center ++
          <div>{
            toolbar(s, state) ++
              fields(s, state) ++
              table(s) ++
              listGroup(s) ++
              BSProgress().striped.bgSuccess.render(state.level * 10, s"${state.level.toInt * 10}%") ++
              SimpleAlert(<span>{s"Section $s was updated."}</span>, <button></button>).alert_info.mt_3 ++
              <button type="button">{s"Open modal $s"}</button>.withId(nextId()).btn.btn_outline_primary.mt_3.dataBsToggleAsModal
                .withAttr("data-bs-target" -> s"#${modal.modalId}") ++
              modal.modalRenderer.render()
          }</div>.withId(nextId()).card_body
      }</div>.withId(nextId()).withStyle("border-width: 2px").shadow_sm.mb_4.addClassIf(state.enabled, "border-success")
    }

    val html =
      <html>
        <head><title>Big page</title></head>
        <body>{
          <div class="page">{
            <h1>Big page</h1>.withId(nextId()).mb_4.fw_bold ++
              (1 to sections).map(section)
          }</div>.withId(nextId()).container.py_4
        }</body>
      </html>
    new Built(page, html)
  }
}
