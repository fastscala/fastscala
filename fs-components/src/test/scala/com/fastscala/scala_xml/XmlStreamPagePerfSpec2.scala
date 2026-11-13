package com.fastscala.scala_xml

import com.fastscala.scala_xml.utils.{XmlStream, XmlStream2}
import com.fastscala.testutils.{AsyncProf, AsyncProfAssertions}
import org.scalatest.flatspec.AnyFlatSpec

import java.io.{StringWriter, Writer}
import scala.xml.*

/**
 * Performance test: [[XmlStream]] vs [[XmlStream2]], writing the big page of [[XmlStreamPagePerfSpec]] (built by
 * [[XmlStreamPagePerfSpec.build]], with [[XmlStreamPagePerfSpec.Sections]] sections).
 *
 * Measured, for each:
 *  - the first write of a page (when the class transforms of the helpers are computed and cached): each iteration builds
 *    two fresh pages, one for each writer, and alternates which goes first;
 *  - writing an already-written page again, alternating the writers on the same page.
 *
 * Run with: `sbt -J-Xmx8g "fs_components/testOnly com.fastscala.scala_xml.XmlStreamPagePerfSpec2"`
 *
 * The profiling tests write a JFR + HTML flame graph to [[AsyncProf.OutDir]], named `XmlStreamPage2-write-XmlStream-...`
 * and `XmlStreamPage2-write-XmlStream2-...`.
 */
class XmlStreamPagePerfSpec2 extends AnyFlatSpec with AsyncProfAssertions {

  import XmlStreamPagePerfSpec.{Sections, SanitySections, build}

  private val WarmupIterations = 10
  private val MeasuredIterations = 20
  private val ProfiledIterations = 10

  /** The writers compared, by name. */
  private val Writers: Seq[(String, (Node, Writer) => Unit)] = Seq(
    "XmlStream" -> ((n, w) => XmlStream.writeNode(n, w)),
    "XmlStream2" -> ((n, w) => XmlStream2.writeNode(n, w)),
  )

  /** Discards output, so only writing is measured (no growing buffer to copy). */
  private final class CountingWriter extends Writer {
    var count = 0L

    override def write(cbuf: Array[Char], off: Int, len: Int): Unit = count += len

    override def flush(): Unit = ()

    override def close(): Unit = ()
  }

  private def write(writer: (Node, Writer) => Unit, page: Elem): Long = {
    val w = new CountingWriter
    writer(page, w)
    w.count
  }

  private def writeToString(writer: (Node, Writer) => Unit, page: Elem): String = {
    val w = new StringWriter
    writer(page, w)
    w.toString
  }

  private val StartTag = java.util.regex.Pattern.compile("""<([\w:.-]+)((?:\s+[\w:.-]+="[^"]*")*)(\s*/?)>""")
  private val Attr = java.util.regex.Pattern.compile("""\s+([\w:.-]+)="([^"]*)"""")

  /**
   * `xml` with the attributes of every start tag sorted by name, so outputs that differ only in attribute order are
   * equal. (Attribute values are escaped, so they have no `"` or `>`: a regex finds every tag and attribute.)
   */
  private def normalized(xml: String): String = {
    val out = new java.lang.StringBuilder(xml.length)
    val m = StartTag.matcher(xml)
    var last = 0
    while (m.find()) {
      out.append(xml, last, m.start())
      val attrs = Seq.newBuilder[(String, String)]
      val am = Attr.matcher(m.group(2))
      while (am.find()) attrs += am.group(1) -> am.group(2)
      out.append('<').append(m.group(1))
      attrs.result().sortBy(_._1).foreach { case (k, v) => out.append(' ').append(k).append("=\"").append(v).append('"') }
      out.append(m.group(3)).append('>')
      last = m.end()
    }
    out.append(xml, last, xml.length)
    out.toString
  }

  /** Where `a` and `b` first differ, with some context around it (instead of printing both). */
  private def firstDiff(a: String, b: String): String = {
    var i = 0
    while (i < a.length && i < b.length && a.charAt(i) == b.charAt(i)) i += 1
    s": first difference at char $i:\n  got:      ...${a.slice(i - 200, i + 200)}...\n  expected: ...${b.slice(i - 200, i + 200)}..."
  }

  private def time(f: => Unit): Long = {
    val t0 = System.nanoTime()
    f
    System.nanoTime() - t0
  }

  private def ms(nanos: Long): String = f"${nanos / 1e6}%.2f ms"

  private def median(times: Seq[Long]): Long = times.sorted.apply(times.size / 2)

  private def stats(name: String, times: Seq[Long]): String = {
    val sorted = times.sorted
    s"$name: min ${ms(sorted.head)}, median ${ms(median(times))}, avg ${ms(times.sum / times.size)}, max ${ms(sorted.last)}"
  }

  "XmlStream2" should "write the big page as XmlStream and toString (attributes in any order)" in {
    // (Every build has different ids (random, and callback ids), so each output is compared with the same page's
    // toString. Outputs are compared after sorting each tag's attributes (see normalized): attribute order doesn't
    // matter. Compared as Booleans so a failure doesn't print the multi-MB output.)
    Seq(SanitySections, Sections).foreach { n =>
      // A fresh page for each writer, written first by it, so it also computes the class transforms (as on a page's
      // first write).
      Writers.foreach { case (name, writer) =>
        val page = build(n).html
        val out = normalized(writeToString(writer, page))
        val expected = normalized(page.toString)
        val same = out == expected
        assert(same, s"$name output differs from toString ($n sections, first write)${firstDiff(out, expected)}")
      }
      // Both on the same page: the same output, written first or again.
      val page = build(n).html
      val outputs = (Writers ++ Writers.reverse).map { case (_, writer) => normalized(writeToString(writer, page)) }
      val allSame = outputs.forall(_ == outputs.head)
      assert(allSame, s"XmlStream and XmlStream2 outputs differ ($n sections)${firstDiff(outputs.head, outputs(1))}")
      val expected = normalized(page.toString)
      val sameAsToString = outputs.head == expected
      assert(sameAsToString, s"output differs from toString ($n sections)${firstDiff(outputs.head, expected)}")
    }
  }

  it should "be measured against XmlStream: first write and writing again" in {
    // Warm up the JIT, for both.
    (1 to WarmupIterations).foreach(_ => Writers.foreach { case (_, writer) => write(writer, build(Sections).html) })

    val firstTimes = Writers.map(_._1 -> Seq.newBuilder[Long]).toMap
    var chars = 0L
    (1 to MeasuredIterations).foreach { i =>
      val pages = Writers.map(_ => build(Sections).html)
      val order = if (i % 2 == 0) Writers.indices else Writers.indices.reverse
      order.foreach { idx =>
        val (name, writer) = Writers(idx)
        firstTimes(name) += time({chars = write(writer, pages(idx))})
      }
    }

    // Writing an already-written page again (attributes already computed).
    val prebuilt = build(Sections).html
    Writers.foreach { case (_, writer) => write(writer, prebuilt) }
    val againTimes = Writers.map(_._1 -> Seq.newBuilder[Long]).toMap
    (1 to MeasuredIterations).foreach { i =>
      val order = if (i % 2 == 0) Writers else Writers.reverse
      order.foreach { case (name, writer) => againTimes(name) += time(write(writer, prebuilt)) }
    }

    info(s"Page: $Sections sections; $chars chars of output")
    Writers.foreach { case (name, _) => info(stats(f"Write (first)  $name%-10s", firstTimes(name).result())) }
    Writers.foreach { case (name, _) => info(stats(f"Write (again)  $name%-10s", againTimes(name).result())) }
    val Seq(v1, v2) = Writers.map(_._1)
    info(f"XmlStream2 / XmlStream (median): first ${median(firstTimes(v2).result()).toDouble / median(firstTimes(v1).result())}%.2f, " +
      f"again ${median(againTimes(v2).result()).toDouble / median(againTimes(v1).result())}%.2f")
  }

  Writers.foreach { case (name, writer) =>
    it should s"profile writing it with async-profiler ($name)" in {
      // Fresh pages, so each write also computes the class transforms, as the first write of a real page does.
      val pages = (1 to ProfiledIterations).map(_ => build(Sections).html)
      (1 to WarmupIterations).foreach(_ => write(writer, build(Sections).html))
      var chars = 0L
      val (_, files) = AsyncProf.profileWithFiles(s"XmlStreamPage2-write-$name", threshold = 0) {
        pages.foreach(p => chars += write(writer, p))
      }
      assert(chars > 0)
      assertProfileWritten(files)
      info(s"[$name] Profiled $ProfiledIterations writes: ${files.mkString(", ")}")
    }
  }
}
