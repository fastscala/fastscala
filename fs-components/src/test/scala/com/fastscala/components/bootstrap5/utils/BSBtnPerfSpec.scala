package com.fastscala.components.bootstrap5.utils

import com.fastscala.core.FSContext
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.utils.XmlStream
import com.fastscala.testutils.{AsyncProf, AsyncProfAssertions, PerfFS}
import org.scalatest.flatspec.AnyFlatSpec

import java.io.{StringWriter, Writer}
import scala.xml.*

/**
 * Performance test for rendering a [[BSBtn]] with each of [[BSBtn.btn]], [[BSBtn.span]] and [[BSBtn.btnLink]]:
 * [[BSBtnPerfSpec.N]] buttons (classes, label, title, id and a callback, as `BSBtn().BtnPrimary.sm.lbl(...)...`),
 * rendered on a new page each time (so rendering also registers each button's callback), then written with
 * [[XmlStream]].
 *
 * Run with: `sbt -J-Xmx8g "fs_components/testOnly com.fastscala.components.bootstrap5.utils.BSBtnPerfSpec"`
 * (one method only: add `-- -z btnLink`, for example).
 *
 * The profiling tests write a JFR + HTML flame graph per method to [[AsyncProf.OutDir]], named `BSBtn-<method>-...`.
 */
class BSBtnPerfSpec extends AnyFlatSpec with AsyncProfAssertions {

  import BSBtnPerfSpec.*

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

  private def write(elems: Seq[Elem]): Long = {
    val w = new CountingWriter
    XmlStream.writeNode(<div>{elems}</div>, w)
    w.count
  }

  private def ms(nanos: Long): String = f"${nanos / 1e6}%.2f ms"

  private def stats(name: String, times: Seq[Long]): String = {
    val sorted = times.sorted
    val median = sorted(sorted.size / 2)
    f"$name: min ${ms(sorted.head)}, median ${ms(median)} (${median / 1e3 / N}%.2f µs per button), avg ${ms(times.sum / times.size)}, max ${ms(sorted.last)}"
  }

  /** The first start tag of `out`, and its attributes. */
  private def startTag(out: String): (String, Map[String, String]) = {
    val m = """<(\w+)( [^>]*)?>""".r.findFirstMatchIn(out).get
    val attrs = """ ([\w-]+)="([^"]*)"""".r.findAllMatchIn(Option(m.group(2)).getOrElse("")).map(a => a.group(1) -> a.group(2)).toMap
    (m.group(1), attrs)
  }

  // Each method against its previous implementation, the XML literal (see BSBtnPerfSpec.Literal), for buttons with
  // only class, id and onclick (plus type for btn, href for btnLink): same attributes, and which is faster.
  Methods.foreach { case (method, render) =>
    val literal = Literal(method)

    s"BSBtn.$method" should "render the same attributes as its XML-literal version" in {
      implicit val fsc: FSContext = PerfFS.newPage().rootFSContext
      def attrsOf(e: Elem) = e.attributes.toList.map(m => m.key -> m.value.text).toMap
      for (b <- Seq(minimalButton(1), button(1), button(2).withStyle("color: red").withAdditionalAttrs("data-x" -> "1"))) {
        val current = render(b)(fsc)
        val previous = literal(b)(fsc)
        assert(current.label == previous.label)
        // (a NodeSeq only equals another NodeSeq, so compare the children as lists)
        assert(current.child.toList == previous.child.toList, (current.child, previous.child))
        // and the written output: the same tag, attributes (ignoring their order and the callback ids) and content
        def written(e: Elem): (String, Set[String], String) = {
          val out = { val w = new StringWriter; XmlStream.writeNode(e, w); w.toString }
          val m = """^<(\w+)((?: [\w-]+="[^"]*")*)>(.*)$""".r.findFirstMatchIn(out).getOrElse(fail(out))
          val attrs = """ [\w-]+="[^"]*"""".r.findAllIn(m.group(2)).toSet.map(_.replaceAll("&quot;\\d+&quot;", "&quot;ID&quot;"))
          (m.group(1), attrs, m.group(3))
        }
        assert(written(current) == written(previous))
        // the same, apart from the callback ids in onclick
        assert(attrsOf(current).keySet == attrsOf(previous).keySet, (attrsOf(current), attrsOf(previous)))
        assert((attrsOf(current) - "onclick") == (attrsOf(previous) - "onclick"))
      }
      val expected = Set("class", "id", "onclick") ++ ExtraAttrs(method)
      assert(attrsOf(render(minimalButton(1))(fsc)).keySet == expected)
    }

    it should s"be compared with its XML-literal version, rendering $N minimal buttons ($method)" in {
      val buttons = (1 to N).map(minimalButton)

      def time(r: BSBtn => FSContext => Elem): Long = {
        implicit val fsc: FSContext = PerfFS.newPage().rootFSContext
        val t0 = System.nanoTime()
        val elems = buttons.map(b => r(b)(fsc))
        val t = System.nanoTime() - t0
        assert(elems.size == N)
        t
      }

      // Warm up both, then alternate them (and which one goes first), so they share JIT and GC conditions.
      (1 to WarmupIterations).foreach { _ => time(render); time(literal) }
      val rounds = 2 * MeasuredIterations
      val (currentTimes, literalTimes) = (1 to rounds).map { i =>
        if (i % 2 == 0) { val a = time(render); val b = time(literal); (a, b) }
        else { val b = time(literal); val a = time(render); (a, b) }
      }.unzip
      val currentFaster = currentTimes.zip(literalTimes).count(p => p._1 < p._2)

      info(stats(s"[$method]         Render (with callbacks)", currentTimes))
      info(stats(s"[$method literal] Render (with callbacks)", literalTimes))
      info(s"[$method] faster than the XML-literal version in $currentFaster of $rounds rounds")
    }
  }

  Methods.foreach { case (method, render) =>

    s"BSBtn.$method" should "render the button's tag, classes, id, title, onclick and label" in {
      implicit val fsc: FSContext = PerfFS.newPage().rootFSContext
      val elem = render(button(1))(fsc)
      val w = new StringWriter
      XmlStream.writeNode(elem, w)
      val out = w.toString
      assert(out == elem.toString, "XmlStream output differs from toString")
      val (tag, attrs) = startTag(out)
      assert(tag == ExpectedTag(method), out)
      assert(attrs.get("class").map(_.trim.split("\\s+").toSet).contains(Set("btn", "btn-primary", "btn-sm")), out)
      assert(attrs.get("id").contains("btn1"), out)
      assert(attrs.get("title").contains("Save row 1"), out)
      assert(attrs.get("onclick").exists(_.contains("window._fs.callback(")), out)
      assert(out.contains(">Save</"), out)
      if (method == "btn") assert(attrs.get("type").contains("button"), out)
      if (method == "btnLink") assert(attrs.get("href").contains("javascript:void(0)"), out)
      assert(fsc.page.callbacks.size == 1, "rendering should register the button's callback")
    }

    it should s"be measured: rendering $N buttons, then writing them with XmlStream ($method)" in {
      val buttons = (1 to N).map(button)
      def renderAll(): Seq[Elem] = {
        implicit val fsc: FSContext = PerfFS.newPage().rootFSContext
        buttons.map(b => render(b)(fsc))
      }
      // Warm up the JIT.
      (1 to WarmupIterations).foreach(_ => write(renderAll()))

      val renderTimes = Seq.newBuilder[Long]
      val writeTimes = Seq.newBuilder[Long]
      var chars = 0L
      (1 to MeasuredIterations).foreach { _ =>
        val t0 = System.nanoTime()
        val elems = renderAll()
        val t1 = System.nanoTime()
        chars = write(elems)
        val t2 = System.nanoTime()
        renderTimes += t1 - t0
        writeTimes += t2 - t1
      }

      // For comparison: creating the BSBtn values themselves (not rendering them).
      val buildTimes = (1 to MeasuredIterations).map { _ =>
        val t0 = System.nanoTime()
        val built = (1 to N).map(button)
        val t1 = System.nanoTime()
        assert(built.size == N)
        t1 - t0
      }

      info(f"[$method] $N buttons, ${chars / N} chars each")
      info(stats(s"[$method] Render (with callbacks)   ", renderTimes.result()))
      info(stats(s"[$method] Write (XmlStream)         ", writeTimes.result()))
      info(stats(s"[$method] (Creating the BSBtn values)", buildTimes))
    }

    it should s"profile rendering the buttons with async-profiler ($method)" in {
      val buttons = (1 to N).map(button)
      def renderAll(): Seq[Elem] = {
        implicit val fsc: FSContext = PerfFS.newPage().rootFSContext
        buttons.map(b => render(b)(fsc))
      }
      (1 to WarmupIterations).foreach(_ => renderAll())
      var rendered = 0
      val (_, files) = AsyncProf.profileWithFiles(s"BSBtn-$method", threshold = 0) {
        (1 to ProfiledIterations).foreach(_ => rendered += renderAll().size)
      }
      assert(rendered == N * ProfiledIterations)
      assertProfileWritten(files)
      info(s"[$method] Profiled $ProfiledIterations x $N renders: ${files.mkString(", ")}")
    }
  }
}

object BSBtnPerfSpec {

  /** Buttons rendered per iteration. */
  val N = 100_000

  /** A typical button: style and size classes, a label, a title, an id and a callback. */
  def button(i: Int): BSBtn =
    BSBtn().BtnPrimary.sm.lbl("Save").title(s"Save row $i").id(s"btn$i").callback(_ => JS.void)

  /** A button with only class, id and an onclick (no title, style or other attributes). */
  def minimalButton(i: Int): BSBtn = BSBtn().BtnPrimary.lbl("Save").id(s"btn$i").callback(_ => JS.void)

  val Methods: Seq[(String, BSBtn => FSContext => Elem)] = Seq(
    "btn" -> (b => fsc => b.btn(using fsc)),
    "span" -> (b => fsc => b.span(using fsc)),
    "btnLink" -> (b => fsc => b.btnLink(using fsc)),
  )

  /** The previous implementations, with XML literals, for comparison. */
  val Literal: Map[String, BSBtn => FSContext => Elem] = {
    import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
    Map(
      "btn" -> (b => implicit fsc => <button class={b.cls} id={b.id} type="button" style={b.styleOpt.orNull} title={b.titleOpt.orNull} onclick={b.onclickOpt.map(_(fsc).cmd).orNull}>{b.content}</button>.withAttrs(b.additionalAttrs *)),
      "span" -> (b => implicit fsc => <span class={b.cls} id={b.id} style={b.styleOpt.orNull} title={b.titleOpt.orNull} onclick={b.onclickOpt.map(_(fsc).cmd).orNull}>{b.content}</span>.withAttrs(b.additionalAttrs *)),
      "btnLink" -> (b => implicit fsc => <a class={b.cls} id={b.id} style={b.styleOpt.orNull} title={b.titleOpt.orNull} href={b.hrefOpt.getOrElse("javascript:void(0)")} onclick={b.onclickOpt.map(_(fsc).cmd).orNull}>{b.content}</a>.withAttrs(b.additionalAttrs *)),
    )
  }

  /** Attributes, besides class, id and onclick, that a minimal button gets from each method. */
  val ExtraAttrs: Map[String, Set[String]] = Map("btn" -> Set("type"), "span" -> Set(), "btnLink" -> Set("href"))

  val ExpectedTag: Map[String, String] = Map("btn" -> "button", "span" -> "span", "btnLink" -> "a")
}
