package com.fastscala.scala_xml.xml

import com.fastscala.scala_xml.xml.baseline.{EncoderBaseline, EncoderBaselineOutput}
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Performance test: [[Encoder]] against [[EncoderBaseline]] (a frozen copy of what it was at the last baseline reset),
 * on what XmlJsStream writes: names, markup chars, text and attribute values, plain and escaped (in a rerender's string
 * literal). At the end: the geometric mean of the ratios, for a single number to follow from one change to the next.
 *
 * For each workload: how fast each encodes it (MB of input chars per second; the best of several runs), and how many
 * times faster the current one is. Only what's written is asserted on (the same number of bytes), not the times:
 *
 *  - the same code measures up to 25% apart from one run of this to the next (the JIT compiles it differently), so only
 *    a ratio well away from 1, in several runs, means something;
 *  - run it alone. With the other specs, more classes implement [[EncoderOutput]] (theirs, and XmlJsStream's), so its
 *    calls (`buf`, `pos`) aren't inlined any more, which slows down what's written in small pieces: not what happens
 *    in an application, where XmlJsStream's is the only one.
 *
 * Run with: `sbt "fs_scala_xml/testOnly com.fastscala.scala_xml.xml.EncoderPerfSpec"`
 */
class EncoderPerfSpec extends AnyFlatSpec {

  import EncoderPerfSpec.*

  private val Warmups = 8
  private val Runs = 15

  private val ratios = collection.mutable.ArrayBuffer[Double]()

  /**
   * Runs `base` and `now` in turn (which one first alternates, so neither is favoured by when it runs), each `Warmups`
   * times and then `Runs` times measured: reports the best of each, in MB of input chars per second, and how many times faster `now` is.
   */
  private def compare(name: String, chars: Long)(base: => Long)(now: => Long): Unit = {
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
    assert(bytesBase == bytesNow, s"$name: EncoderBaseline wrote $bytesBase bytes, Encoder $bytesNow")
    def mbs(nanos: Long) = chars / 1e6 / (nanos / 1e9)
    ratios += bestBase.toDouble / bestNow
    info(f"$name%-46s baseline ${mbs(bestBase)}%6.0f MB/s   now ${mbs(bestNow)}%6.0f MB/s   x${bestBase.toDouble / bestNow}%.2f")
  }

  /** The Strings of `strings`, written one by one, `reps` times. */
  private def strings(name: String, strings: Array[String], x: Int, j: Int): Unit = {
    val chars = strings.iterator.map(_.length.toLong).sum
    val reps = math.max(1, (TargetChars / chars).toInt)
    compare(s"$name ($x,$j)", chars * reps)(stringsBase(strings, x, j, reps))(stringsNow(strings, x, j, reps))
  }

  /** The markup's own chars, one by one. */
  private def markupChars(x: Int, j: Int): Unit = {
    val reps = (TargetChars / MarkupChars.length / 4).toInt
    compare(s"markup chars, one by one ($x,$j)", MarkupChars.length.toLong * reps)(markupBase(x, j, reps))(markupNow(x, j, reps))
  }

  /** Whole elements, as XmlJsStream writes them: `<tag attr="value" ...>text</tag>`, piece by piece. */
  private def elements(x: Int, j: Int): Unit = {
    val reps = (TargetChars / ElementChars).toInt
    compare(s"whole elements, piece by piece ($x,$j)", ElementChars * reps)(elementsBase(x, j, reps))(elementsNow(x, j, reps))
  }

  "Encoder" should "be measured against EncoderBaseline (xmlLevels,jsLevels)" in {
    // A page: nothing escaped, and text and attribute values escaped for XML.
    elements(0, 0)
    strings("names", Names, 0, 0)
    strings("long ASCII (scripts)", LongAscii, 0, 0)
    strings("text, not ASCII", Portuguese, 0, 0)
    strings("text", Texts, 1, 0)
    strings("text, not ASCII", Portuguese, 1, 0)
    strings("attribute values: classes, ids", ClassValues, 1, 0)
    strings("attribute values: JavaScript", JsValues, 1, 0)
    strings("long text", LongTexts, 1, 0)
    // The page in a rerender (a JavaScript string literal).
    elements(0, 1)
    markupChars(0, 1)
    strings("names", Names, 0, 1)
    strings("text", Texts, 1, 1)
    strings("text, not ASCII", Portuguese, 1, 1)
    strings("attribute values: classes, ids", ClassValues, 1, 1)
    strings("attribute values: JavaScript", JsValues, 1, 1)
    strings("long text", LongTexts, 1, 1)
    // Deeper: a rerender in a rerender; XML in an attribute.
    elements(0, 2)
    strings("attribute values: JavaScript", JsValues, 1, 2)
    strings("attribute values: JavaScript", JsValues, 2, 0)
    elements(1, 0)
    info(f"geometric mean of ${ratios.size} workloads: x${math.exp(ratios.map(math.log).sum / ratios.size)}%.2f")
  }
}

object EncoderPerfSpec {

  /** Discards what's written (as XmlJsStream's sink does in its own tests): only encoding is measured. */
  final class DiscardingOutput extends EncoderOutput {
    var buf = new Array[Byte](8192)
    var pos = 0
    var total = 0L

    override def flush(): Unit = {
      total += pos
      pos = 0
    }
  }

  /** The same, for [[EncoderBaseline]]. */
  final class BaselineDiscardingOutput extends EncoderBaselineOutput {
    var buf = new Array[Byte](8192)
    var pos = 0
    var total = 0L

    override def flush(): Unit = {
      total += pos
      pos = 0
    }
  }

  // The measured loops: each one a method of its own (so it's compiled as one), once for each Encoder.

  def stringsBase(strings: Array[String], x: Int, j: Int, reps: Int): Long = {
    val out = new BaselineDiscardingOutput
    val esc = EncoderBaseline.escaping(x, j)
    var r = 0
    while (r < reps) {
      var i = 0
      while (i < strings.length) { EncoderBaseline.write(strings(i), out, esc); i += 1 }
      r += 1
    }
    out.total + out.pos
  }

  def stringsNow(strings: Array[String], x: Int, j: Int, reps: Int): Long = {
    val out = new DiscardingOutput
    val esc = Encoder.escaping(x, j)
    var r = 0
    while (r < reps) {
      var i = 0
      while (i < strings.length) { Encoder.write(strings(i), out, esc); i += 1 }
      r += 1
    }
    out.total + out.pos
  }

  val MarkupChars: Array[Char] = "<>/=\" :<></=\"\" >".toCharArray

  def markupBase(x: Int, j: Int, reps: Int): Long = {
    val out = new BaselineDiscardingOutput
    val chars = MarkupChars
    val esc = EncoderBaseline.escaping(x, j)
    var r = 0
    while (r < reps) {
      var i = 0
      while (i < chars.length) { EncoderBaseline.writeAscii(chars(i), out, esc); i += 1 }
      r += 1
    }
    out.total + out.pos
  }

  def markupNow(x: Int, j: Int, reps: Int): Long = {
    val out = new DiscardingOutput
    val chars = MarkupChars
    val esc = Encoder.escaping(x, j)
    var r = 0
    while (r < reps) {
      var i = 0
      while (i < chars.length) { Encoder.writeAscii(chars(i), out, esc); i += 1 }
      r += 1
    }
    out.total + out.pos
  }

  def elementsBase(x: Int, j: Int, reps: Int): Long = {
    val out = new BaselineDiscardingOutput
    // As XmlJsStream does it: the level's two escapings, resolved once (markup, and one more XML level for text and
    // attribute values), not per write.
    val markup = EncoderBaseline.escaping(x, j)
    val text = markup.nextXml
    var r = 0
    while (r < reps) {
      var e = 0
      while (e < Elements.length) {
        val el = Elements(e)
        EncoderBaseline.writeAscii('<', out, markup)
        EncoderBaseline.write(el.tag, out, markup)
        var a = 0
        while (a < el.attrs.length) {
          EncoderBaseline.writeAscii(' ', out, markup)
          EncoderBaseline.write(el.attrs(a), out, markup)
          EncoderBaseline.writeAscii('=', out, markup)
          EncoderBaseline.writeAscii('"', out, markup)
          EncoderBaseline.write(el.attrs(a + 1), out, text)
          EncoderBaseline.writeAscii('"', out, markup)
          a += 2
        }
        EncoderBaseline.writeAscii('>', out, markup)
        EncoderBaseline.write(el.text, out, text)
        EncoderBaseline.writeAscii('<', out, markup)
        EncoderBaseline.writeAscii('/', out, markup)
        EncoderBaseline.write(el.tag, out, markup)
        EncoderBaseline.writeAscii('>', out, markup)
        e += 1
      }
      r += 1
    }
    out.total + out.pos
  }

  def elementsNow(x: Int, j: Int, reps: Int): Long = {
    val out = new DiscardingOutput
    // As XmlJsStream does it: the level's two escapings, resolved once (markup, and one more XML level for text and
    // attribute values), not per write.
    val markup = Encoder.escaping(x, j)
    val text = markup.nextXml
    var r = 0
    while (r < reps) {
      var e = 0
      while (e < Elements.length) {
        val el = Elements(e)
        Encoder.writeAscii('<', out, markup)
        Encoder.write(el.tag, out, markup)
        var a = 0
        while (a < el.attrs.length) {
          Encoder.writeAscii(' ', out, markup)
          Encoder.write(el.attrs(a), out, markup)
          Encoder.writeAscii('=', out, markup)
          Encoder.writeAscii('"', out, markup)
          Encoder.write(el.attrs(a + 1), out, text)
          Encoder.writeAscii('"', out, markup)
          a += 2
        }
        Encoder.writeAscii('>', out, markup)
        Encoder.write(el.text, out, text)
        Encoder.writeAscii('<', out, markup)
        Encoder.writeAscii('/', out, markup)
        Encoder.write(el.tag, out, markup)
        Encoder.writeAscii('>', out, markup)
        e += 1
      }
      r += 1
    }
    out.total + out.pos
  }

  /** About how many chars each run encodes. */
  val TargetChars = 12_000_000L

  val Names: Array[String] = Array("div", "span", "class", "id", "button", "type", "td", "tr", "input", "style", "onclick", "a", "href", "label", "for", "table", "tbody", "li", "ul", "h5", "i",
    "data-bs-toggle", "placeholder", "value", "name", "role", "aria-label", "tabindex", "select", "option", "textarea", "form", "p", "b")

  val Texts: Array[String] = Array("Section 12", "Save", "Cancel", "Person 3 of s2 t1", "s1 r1 c1", "Open modal 7", "Lisboa City", "42", "Some notes for section 4, with <special> & \"escaped\" characters.",
    "Name", "Age", "City", "Actions", "View 15", "Remove", "Add", "Edit", "Delete", "Run once", "Details", "Section 3 was updated.", "70%", "#12", "Change the name of ", "Big page")

  val Portuguese: Array[String] = Array("Inscrição concluída com sucesso", "Não foi possível guardar as alterações", "Catequese — 3º ano", "Paróquia de São João", "Número de telefone", "Código postal inválido",
    "Encarregado de educação", "Reinscrição para o próximo ano", "€ 25,00", "Ação", "José António", "Sessão expirada: volte a iniciar sessão")

  val ClassValues: Array[String] = Array("btn btn-primary btn-sm", "card-header d-flex justify-content-between align-items-center", "form-control", "e1234", "id6c54b43ce1138243", "data-cell px-2 py-1 text-center align-middle",
    "table table-striped table-hover", "border-width: 2px", "modal fade", "text/javascript", "button", "row g-3 mt-3", "col-md-4 border rounded p-2", "badge text-bg-primary", "bi bi-gear", "#id43d6f70a6e4bfa9f-modal")

  val JsValues: Array[String] = Array(
    """window._fs.callback('',"14356439","14405925",false,true,true,{});""",
    """if (confirm("Delete section 12?")) {window._fs.callback('',"14356439","14405926",false,true,true,{});}""",
    """bootstrap.Modal.getInstance(document.getElementById('id650c9f8da6ccf7c5-modal')).hide()""",
    """window._fs.callback(document.getElementById("id1a2b3c4d").value,"14356439","14405927",false,true,true,{});""",
    """event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {document.getElementById("x").blur();}""",
    """alert("Time on server: 2026-11-15");""")

  val LongAscii: Array[String] = Array(
    ("var template = document.createElement('template');template.setAttribute('id', 'x');template.innerHTML = html.trim(); return template;" * 3),
    ("window.addEventListener('load', function() { console.log('loaded'); document.body.classList.add('ready'); });" * 5))

  val LongTexts: Array[String] = Array(
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " * 6,
    "O catecismo é entregue no início do ano; a inscrição só é válida depois de confirmada pelo encarregado de educação & pela secretaria. " * 4,
    "<p>Text with <b>markup</b> in it, & \"quotes\", that's all escaped.</p> " * 8)

  final class Element(val tag: String, val attrs: Array[String], val text: String)

  val Elements: Array[Element] = Array(
    new Element("div", Array("class", "card-header d-flex justify-content-between align-items-center", "id", "e1234"), ""),
    new Element("button", Array("type", "button", "class", "btn btn-primary btn-sm", "onclick", JsValues(0), "id", "id6c54b43ce1138243"), "Add"),
    new Element("td", Array("class", "data-cell px-2 py-1 text-center align-middle", "id", "e99", "style", "border-width: 2px"), "s1 r1 c1"),
    new Element("span", Array("class", "badge text-bg-primary"), "#12"),
    new Element("button", Array("class", "btn btn-danger btn-sm", "onclick", JsValues(1)), "Delete"),
    new Element("p", Array("class", "mb-2 text-muted"), "Some notes for section 4, with <special> & \"escaped\" characters."),
    new Element("label", Array("for", "id1a2b3c4d", "class", "form-label"), "Encarregado de educação"),
    new Element("input", Array("type", "text", "class", "form-control", "value", "Section 12", "placeholder", "Name", "onchange", JsValues(3)), ""),
    new Element("i", Array("class", "bi bi-gear"), ""),
    new Element("td", Array(), "Person 3 of s2 t1"),
  )

  val ElementChars: Long = Elements.iterator.map(e => 5L + 2 * e.tag.length + e.attrs.iterator.map(_.length + 2L).sum + e.text.length).sum
}
