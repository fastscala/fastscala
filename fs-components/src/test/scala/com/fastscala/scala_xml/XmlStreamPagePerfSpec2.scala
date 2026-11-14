package com.fastscala.scala_xml

import com.fastscala.scala_xml.utils.{XmlStream, XmlStream2}
import com.fastscala.testutils.AttributeOrder.normalized
import com.fastscala.testutils.{AsyncProf, AsyncProfAssertions}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.{ByteArrayOutputStream, OutputStream, OutputStreamWriter, StringWriter, Writer}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import scala.xml.*

/**
 * Performance test: [[XmlStream]] vs [[XmlStream2]], writing the big page of [[XmlStreamPagePerfSpec]] (built by
 * [[XmlStreamPagePerfSpec.build]], with [[XmlStreamPagePerfSpec.Sections]] sections). Compared:
 *
 *  - `XmlStream (chars)`: XmlStream into a Writer that just counts the chars (no encoding);
 *  - `XmlStream + UTF-8`: XmlStream through an OutputStreamWriter (UTF-8) into an OutputStream that counts the bytes:
 *    the full path to bytes, as a response needs (what OkHtmlStreaming did before XmlStream2);
 *  - `XmlStream2`: XmlStream2, writing UTF-8 bytes straight into a Content.Sink that counts them.
 *
 * Measured, for each: the first write of a page (when the class transforms of the helpers are computed and cached),
 * on fresh pages, alternating which goes first; and writing an already-written page again, alternating them.
 *
 * Run with: `sbt -J-Xmx8g "fs_components/testOnly com.fastscala.scala_xml.XmlStreamPagePerfSpec2"`
 *
 * The profiling tests write a JFR + HTML flame graph to [[AsyncProf.OutDir]], named `XmlStreamPage2-write-...`.
 */
class XmlStreamPagePerfSpec2 extends AnyFlatSpec with AsyncProfAssertions {

  import XmlStreamPagePerfSpec.{Sections, SanitySections, build}

  private val WarmupIterations = 10
  private val MeasuredIterations = 20
  private val ProfiledIterations = 10

  /** Discards output, counting the chars. */
  private final class CountingWriter extends Writer {
    var count = 0L

    override def write(cbuf: Array[Char], off: Int, len: Int): Unit = count += len

    override def write(str: String, off: Int, len: Int): Unit = count += len

    override def flush(): Unit = ()

    override def close(): Unit = ()
  }

  /** Discards output, counting the bytes. */
  private final class CountingOutputStream extends OutputStream {
    var count = 0L

    override def write(b: Int): Unit = count += 1

    override def write(b: Array[Byte], off: Int, len: Int): Unit = count += len
  }

  /** Discards output, counting the bytes; each write completes at once. */
  private final class CountingSink extends Content.Sink {
    var count = 0L

    override def write(last: Boolean, byteBuffer: ByteBuffer, callback: Callback): Unit = {
      count += byteBuffer.remaining()
      byteBuffer.position(byteBuffer.limit())
      callback.succeeded()
    }
  }

  /** Collects the bytes written. */
  private final class CollectingSink extends Content.Sink {
    val bytes = new ByteArrayOutputStream()

    override def write(last: Boolean, byteBuffer: ByteBuffer, callback: Callback): Unit = {
      while (byteBuffer.hasRemaining) bytes.write(byteBuffer.get())
      callback.succeeded()
    }
  }

  /** What's compared: writing a page, returning how much was written; and writing it into a String (for checking). */
  private final case class Target(name: String, write: Elem => Long, render: Elem => String)

  private val Targets: Seq[Target] = Seq(
    Target("XmlStream (chars)",
      page => { val w = new CountingWriter; XmlStream.writeNode(page, w); w.count },
      page => { val w = new StringWriter; XmlStream.writeNode(page, w); w.toString }),
    Target("XmlStream + UTF-8",
      page => { val os = new CountingOutputStream; val w = new OutputStreamWriter(os, UTF_8); XmlStream.writeNode(page, w); w.flush(); os.count },
      page => { val os = new ByteArrayOutputStream; val w = new OutputStreamWriter(os, UTF_8); XmlStream.writeNode(page, w); w.flush(); os.toString(UTF_8) }),
    Target("XmlStream2",
      page => { val s = new CountingSink; XmlStream2.writeNode(page, s); s.count },
      page => { val s = new CollectingSink; XmlStream2.writeNode(page, s); s.bytes.toString(UTF_8) }),
  )

  /** The target the others are compared with: the full path to bytes before XmlStream2. */
  private val Reference = "XmlStream + UTF-8"

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
    // toString. Outputs are compared after sorting each tag's attributes (see AttributeOrder): attribute order doesn't
    // matter. Compared as Booleans so a failure doesn't print the multi-MB output.)
    Seq(SanitySections, Sections).foreach { n =>
      // A fresh page for each target, written first by it, so it also computes the class transforms (as on a page's
      // first write).
      Targets.foreach { t =>
        val page = build(n).html
        val out = normalized(t.render(page))
        val expected = normalized(page.toString)
        val same = out == expected
        assert(same, s"${t.name} output differs from toString ($n sections, first write)${firstDiff(out, expected)}")
      }
      // All on the same page: the same output, written first or again.
      val page = build(n).html
      val outputs = (Targets ++ Targets.reverse).map(t => normalized(t.render(page)))
      val allSame = outputs.forall(_ == outputs.head)
      assert(allSame, s"outputs differ ($n sections)${firstDiff(outputs.head, outputs.last)}")
      val expected = normalized(page.toString)
      val sameAsToString = outputs.head == expected
      assert(sameAsToString, s"output differs from toString ($n sections)${firstDiff(outputs.head, expected)}")
    }
  }

  it should "be measured against XmlStream: first write and writing again" in {
    // Warm up the JIT, for all.
    (1 to WarmupIterations).foreach(_ => Targets.foreach(t => t.write(build(Sections).html)))

    val firstTimes = Targets.map(_.name -> Seq.newBuilder[Long]).toMap
    val written = collection.mutable.Map[String, Long]()
    (1 to MeasuredIterations).foreach { i =>
      val pages = Targets.map(_ => build(Sections).html)
      val order = if (i % 2 == 0) Targets.indices else Targets.indices.reverse
      order.foreach { idx =>
        val t = Targets(idx)
        firstTimes(t.name) += time(written(t.name) = t.write(pages(idx)))
      }
    }

    // Writing an already-written page again (attributes already computed).
    val prebuilt = build(Sections).html
    Targets.foreach(_.write(prebuilt))
    val againTimes = Targets.map(_.name -> Seq.newBuilder[Long]).toMap
    (1 to MeasuredIterations).foreach { i =>
      (if (i % 2 == 0) Targets else Targets.reverse).foreach(t => againTimes(t.name) += time(t.write(prebuilt)))
    }

    info(s"Page: $Sections sections; written: ${Targets.map(t => s"${t.name} ${written(t.name)}").mkString(", ")}")
    Targets.foreach(t => info(stats(f"Write (first)  ${t.name}%-18s", firstTimes(t.name).result())))
    Targets.foreach(t => info(stats(f"Write (again)  ${t.name}%-18s", againTimes(t.name).result())))
    val (refFirst, refAgain) = (median(firstTimes(Reference).result()), median(againTimes(Reference).result()))
    Targets.filter(_.name != Reference).foreach { t =>
      info(f"${t.name} / $Reference (median): first ${median(firstTimes(t.name).result()).toDouble / refFirst}%.2f, " +
        f"again ${median(againTimes(t.name).result()).toDouble / refAgain}%.2f")
    }
  }

  Targets.filter(_.name != "XmlStream (chars)").foreach { t =>
    it should s"profile writing it with async-profiler (${t.name})" in {
      // Fresh pages, so each write also computes the class transforms, as the first write of a real page does.
      val pages = (1 to ProfiledIterations).map(_ => build(Sections).html)
      (1 to WarmupIterations).foreach(_ => t.write(build(Sections).html))
      var written = 0L
      val (_, files) = AsyncProf.profileWithFiles(s"XmlStreamPage2-write-${t.name.replaceAll("[^A-Za-z0-9]+", "")}", threshold = 0) {
        pages.foreach(p => written += t.write(p))
      }
      assert(written > 0)
      assertProfileWritten(files)
      info(s"[${t.name}] Profiled $ProfiledIterations writes: ${files.mkString(", ")}")
    }
  }
}
