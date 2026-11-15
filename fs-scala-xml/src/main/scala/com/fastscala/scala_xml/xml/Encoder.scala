package com.fastscala.scala_xml.xml

import com.fastscala.js.JsEscape

import java.nio.charset.StandardCharsets.UTF_8
import scala.annotation.{nowarn, switch}
import scala.xml.Utility

/**
 * Everything XmlJsStream writes goes through here: a String (or a StringBuilder), optionally escaped for XML (text or
 * attribute value), then escaped for JavaScript string literals `jsLevels` times (once for each EncJs it's in), and
 * encoded in UTF-8, straight into [[XmlJsStream.Out]]'s byte buffer (queuing the buffer whenever it's full).
 *
 * Every escaping is per char (XML's, and [[JsEscape]]'s), so the Strings written one after the other are escaped as
 * their concatenation would be: `JsEscape.escape` `jsLevels` times of `Utility.escape` (if `xml`), in UTF-8.
 *
 * Three ways of writing, by what's needed:
 *  - nothing escaped: ASCII copied in bulk (or, for a short String, char by char), anything else char by char;
 *  - XML only: one pass, without bounds checks, in slices that surely fit (see [[writeXml]]);
 *  - any JavaScript levels: char by char, each char's bytes looked up in a table for that escaping (see [[writeTable]]).
 *
 * A lone surrogate is `?` (as String.getBytes(UTF_8) writes it) when not escaped for JavaScript, and `\uXXXX` (as
 * JsEscape escapes it) when it is. With XML escaping, a surrogate pair split by dropped chars is a pair again.
 */
object Encoder {

  import XmlJsStream.{ChunkSize, Out}

  // ---------- the entry points ----------

  /** `s`, escaped for XML if `xml`, then for JavaScript `jsLevels` times, in UTF-8, into `out`'s buffer. */
  def write(s: String, out: Out, xml: Boolean, jsLevels: Int): Unit =
    if (jsLevels == 0) {
      if (xml) writeXml(s, out) else writeRaw(s, out)
    } else writeTable(s, 0, s.length, out, xml, jsLevels)

  /** As the other [[write]], for what's in `sb` (e.g. what scala-xml wrote for the nodes only it knows how to). */
  def write(sb: java.lang.StringBuilder, out: Out, xml: Boolean, jsLevels: Int): Unit =
    writeTable(sb, 0, sb.length, out, xml, jsLevels)

  /** An ASCII char of the markup (`<`, `>`, `/`, `=`, `"`, ` `, `:`), escaped for JavaScript `jsLevels` times. */
  inline def writeAscii(c: Char, out: Out, jsLevels: Int): Unit =
    if (jsLevels == 0) {
      if (out.pos == ChunkSize) out.flush()
      out.buf(out.pos) = c.toByte
      out.pos += 1
    } else writeTable(c, out, jsLevels)

  // ---------- nothing escaped ----------

  /** Strings up to this long are copied char by char: for a few bytes, a String.getBytes call costs more. */
  private final val ShortString = 32

  /**
   * `s` in UTF-8. A short one (names: tags, attributes) is copied char by char as it's checked, in one pass; a longer
   * one is checked, and then copied in bulk if it's all ASCII. Anything not ASCII is encoded char by char.
   */
  private def writeRaw(s: String, out: Out): Unit = {
    val len = s.length
    if (len <= ShortString && len <= ChunkSize - out.pos) {
      val buf = out.buf
      var p = out.pos
      var i = 0
      while (i < len) {
        val c = s.charAt(i)
        if (c >= 0x80) { // not ASCII: the rest is encoded
          out.pos = p
          writeTable(s, i, len, out, xml = false, jsLevels = 0)
          return
        }
        buf(p) = c.toByte
        p += 1
        i += 1
      }
      out.pos = p
    } else {
      var bits = 0
      var i = 0
      while (i < len) {
        bits |= s.charAt(i)
        i += 1
      }
      if (bits < 0x80) writeAsciiBulk(s, out) else writeTable(s, 0, len, out, xml = false, jsLevels = 0)
    }
  }

  /** `s`, all ASCII, in as few copies as the buffer allows. */
  private def writeAsciiBulk(s: String, out: Out): Unit = {
    var start = 0
    val len = s.length
    while (start < len) {
      if (out.pos == ChunkSize) out.flush()
      val n = math.min(len - start, ChunkSize - out.pos)
      copyAscii(s, start, start + n, out.buf, out.pos)
      out.pos += n
      start += n
    }
  }

  /**
   * Copies `s(from until to)`, which must be all ASCII, into `buf` at `p`: for a Latin-1 String this is an array copy
   * of its bytes. (String.getBytes(int, int, byte[], int) keeps the low 8 bits of each char: deprecated, as that's
   * only right for ASCII, which is all it's used for here.)
   */
  @nowarn("cat=deprecation")
  private inline def copyAscii(s: String, from: Int, to: Int, buf: Array[Byte], p: Int): Unit =
    s.getBytes(from, to, buf, p)

  // ---------- XML only ----------

  private final val ActionKeep: Byte = 0
  private final val ActionDrop: Byte = 1
  private final val ActionAmp: Byte = 2
  private final val ActionLt: Byte = 3
  private final val ActionGt: Byte = 4
  private final val ActionQuot: Byte = 5

  private val XmlActions: Array[Byte] = {
    val arr = new Array[Byte](128)
    (0 until 32).foreach(i => arr(i) = ActionDrop)
    arr('\t') = ActionKeep
    arr('\n') = ActionKeep
    arr('\r') = ActionKeep
    arr('&') = ActionAmp
    arr('<') = ActionLt
    arr('>') = ActionGt
    arr('"') = ActionQuot
    arr
  }

  /** Whether XML escaping drops `c` (the control chars but \t, \n and \r). */
  private inline def xmlDrops(c: Char): Boolean = c < 32 && XmlActions(c) == ActionDrop

  /** Text escaped in slices of at least this many chars: rather than a tiny one, the buffer is written out first. */
  private final val MinSlice = 64

  /**
   * `text` escaped as Utility.escape does (`&`, `<`, `>` and `"` become entities; control chars other than \n, \r and
   * \t are dropped), in UTF-8 (a lone surrogate is `?`).
   *
   * In slices that surely fit in the buffer (6 bytes per char: the longest entity, `&quot;`, and more than UTF-8's 3
   * bytes per char), each written without any bounds check; usually the whole text is one slice.
   */
  private def writeXml(text: String, out: Out): Unit = {
    val len = text.length
    var i = 0
    while (i < len) {
      var room = (ChunkSize - out.pos) / 6
      if (room < len - i && room < MinSlice) {
        out.flush()
        room = ChunkSize / 6 // 1,365 chars for an 8,192-byte buffer
      }
      i = writeXmlSlice(text, i, math.min(len, i + room), out)
    }
  }

  // (The loop tests `c < 128` and then looks the char up. Testing `c > '>'` first, to skip most chars with one
  // comparison, measured ~35% slower on a page with a lot of JavaScript in attributes: that branch is mispredicted
  // whenever letters and digits / punctuation alternate, while `c < 128` is almost always true.)

  /**
   * Escapes `text(from until to)` without bounds checks (the caller guarantees room for 6 bytes per char); returns
   * where it stopped: `to`, or after it if a surrogate pair started just before `to`.
   *
   * One pass: each char is written (as a byte, an entity, or encoded to UTF-8) as it's read. (Scanning for the next
   * escape and then copying the run in between, with String.getBytes, measured ~25% slower: most runs between
   * escapes, e.g. in JavaScript in attributes, are a few chars, so each char was read twice plus a call per run.)
   */
  private def writeXmlSlice(text: String, from: Int, to: Int, out: Out): Int = {
    val buf = out.buf
    val actions = XmlActions
    val len = text.length
    var p = out.pos
    var i = from
    while (i < to) {
      val c = text.charAt(i)
      if (c < 128) {
        (actions(c): @switch) match {
          case ActionKeep =>
            buf(p) = c.toByte
            p += 1
          case ActionAmp =>
            buf(p) = '&'
            buf(p + 1) = 'a'
            buf(p + 2) = 'm'
            buf(p + 3) = 'p'
            buf(p + 4) = ';'
            p += 5
          case ActionLt =>
            buf(p) = '&'
            buf(p + 1) = 'l'
            buf(p + 2) = 't'
            buf(p + 3) = ';'
            p += 4
          case ActionGt =>
            buf(p) = '&'
            buf(p + 1) = 'g'
            buf(p + 2) = 't'
            buf(p + 3) = ';'
            p += 4
          case ActionQuot =>
            buf(p) = '&'
            buf(p + 1) = 'q'
            buf(p + 2) = 'u'
            buf(p + 3) = 'o'
            buf(p + 4) = 't'
            buf(p + 5) = ';'
            p += 6
          case _ => // ActionDrop
        }
      } else if (Character.isSurrogate(c)) {
        // A high one's low surrogate may come after chars that are dropped: they're removed by the escaping, before
        // the UTF-8 encoding (as toString's output is encoded), so then it's a pair. Otherwise it's lone: '?'.
        val low = lowSurrogateAt(text, i, len, xml = true)
        if (low >= 0) {
          p = putCodePoint(Character.toCodePoint(c, text.charAt(low)), buf, p) // (4 bytes for 2 or more chars)
          i = low
        } else {
          buf(p) = '?'
          p += 1
        }
      } else p = putBmp(c, buf, p)
      i += 1
    }
    out.pos = p
    i
  }

  // ---------- with JavaScript levels (and anything else) ----------

  /** The chars whose bytes are in the tables: below U+00A0 (ASCII and the C1 controls). */
  private final val TableSize = 0xA0

  /**
   * The reference: `s` escaped for XML (as Utility.escape does) if `xml`, then for JavaScript `jsLevels` times. Each
   * table entry is this of one char, in UTF-8; it's used as it is for the rare chars that have none.
   */
  private def escaped(s: String, xml: Boolean, jsLevels: Int): String = {
    var r = if (xml) Utility.escape(s) else s
    var level = 0
    while (level < jsLevels) {
      r = JsEscape.escape(r)
      level += 1
    }
    r
  }

  /** For each char below [[TableSize]]: its bytes, escaped as [[escaped]] does (null: itself, as one byte). */
  private def makeTable(xml: Boolean, jsLevels: Int): Array[Array[Byte]] =
    Array.tabulate(TableSize) { c =>
      val bytes = escaped(c.toChar.toString, xml, jsLevels).getBytes(UTF_8)
      if (bytes.length == 1 && bytes(0) == c) null else bytes
    }

  /**
   * The tables of fewer JavaScript levels than this are made at once; deeper ones when first needed (an escape doubles
   * its backslashes at each level, so a table of n levels is about 2^n * 100 bytes: deep ones are big, and rare).
   */
  private final val TabledLevels = 8

  private val Tables: Array[Array[Array[Byte]]] =
    Array.tabulate(TabledLevels * 2)(i => makeTable(xml = i % 2 == 1, jsLevels = i / 2))

  private val DeepTables = new java.util.concurrent.ConcurrentHashMap[Integer, Array[Array[Byte]]]()

  private def tableFor(xml: Boolean, jsLevels: Int): Array[Array[Byte]] =
    if (jsLevels < TabledLevels) Tables(jsLevels * 2 + (if (xml) 1 else 0))
    else DeepTables.computeIfAbsent(jsLevels * 2 + (if (xml) 1 else 0), key => makeTable(xml = key % 2 == 1, jsLevels = key / 2))

  /**
   * `s(from until to)`, escaped as [[escaped]] does, in UTF-8: char by char, each char below U+00A0 looked up in the
   * table for this escaping; above, a char is its UTF-8 (escaping leaves it as it is), but for the line separators
   * (escaped for JavaScript) and the surrogates.
   */
  private def writeTable(s: CharSequence, from: Int, to: Int, out: Out, xml: Boolean, jsLevels: Int): Unit = {
    val table = tableFor(xml, jsLevels)
    var buf = out.buf
    var p = out.pos
    var i = from
    while (i < to) {
      // Room for whatever a char but a table one is (4 bytes: a surrogate pair), and for most table ones.
      if (p > ChunkSize - 8) {
        out.pos = p
        out.flush()
        buf = out.buf
        p = out.pos
      }
      val c = s.charAt(i)
      var bytes: Array[Byte] = null
      if (c < TableSize) {
        bytes = table(c)
        if (bytes eq null) {
          buf(p) = c.toByte
          p += 1
        }
      } else if (Character.isSurrogate(c)) {
        val low = lowSurrogateAt(s, i, to, xml)
        if (low >= 0) {
          p = putCodePoint(Character.toCodePoint(c, s.charAt(low)), buf, p)
          i = low
        } else if (jsLevels == 0) {
          buf(p) = '?'
          p += 1
        } else bytes = escaped(c.toString, xml = false, jsLevels).getBytes(UTF_8) // (`\uXXXX`, escaped further)
      } else if (jsLevels > 0 && (c == ' ' || c == ' ')) {
        bytes = escaped(c.toString, xml = false, jsLevels).getBytes(UTF_8)
      } else p = putBmp(c, buf, p)

      if (bytes ne null) {
        if (bytes.length > ChunkSize - p) {
          out.pos = p
          writeBytes(bytes, out)
          buf = out.buf
          p = out.pos
        } else {
          System.arraycopy(bytes, 0, buf, p, bytes.length)
          p += bytes.length
        }
      }
      i += 1
    }
    out.pos = p
  }

  /** One ASCII char of the markup, escaped for JavaScript `jsLevels` times (> 0). */
  private def writeTable(c: Char, out: Out, jsLevels: Int): Unit = {
    val bytes = tableFor(xml = false, jsLevels)(c)
    if (bytes eq null) {
      if (out.pos == ChunkSize) out.flush()
      out.buf(out.pos) = c.toByte
      out.pos += 1
    } else writeBytes(bytes, out)
  }

  /** `bytes`, into the buffer, in as many pieces as it takes (one, but for an escape longer than the room left). */
  private def writeBytes(bytes: Array[Byte], out: Out): Unit = {
    var start = 0
    while (start < bytes.length) {
      if (out.pos == ChunkSize) out.flush()
      val n = math.min(bytes.length - start, ChunkSize - out.pos)
      System.arraycopy(bytes, start, out.buf, out.pos, n)
      out.pos += n
      start += n
    }
  }

  // ---------- UTF-8 ----------

  /**
   * If `s(i)` is a high surrogate with a low one after it (before `to`), where that is (else -1). With `xml`, the low
   * one may come after chars XML escaping drops (they're removed before the UTF-8 encoding, so it's a pair then).
   */
  private def lowSurrogateAt(s: CharSequence, i: Int, to: Int, xml: Boolean): Int =
    if (!Character.isHighSurrogate(s.charAt(i))) -1
    else {
      var j = i + 1
      if (xml) while (j < to && xmlDrops(s.charAt(j))) j += 1
      if (j < to && Character.isLowSurrogate(s.charAt(j))) j else -1
    }

  /** `c` (at least U+0080, not a surrogate) in UTF-8, at `p` in `buf` (2 or 3 bytes); returns the position after it. */
  private inline def putBmp(c: Char, buf: Array[Byte], p: Int): Int =
    if (c < 0x800) {
      buf(p) = (0xC0 | (c >> 6)).toByte
      buf(p + 1) = (0x80 | (c & 0x3F)).toByte
      p + 2
    } else {
      buf(p) = (0xE0 | (c >> 12)).toByte
      buf(p + 1) = (0x80 | ((c >> 6) & 0x3F)).toByte
      buf(p + 2) = (0x80 | (c & 0x3F)).toByte
      p + 3
    }

  /** A supplementary code point in UTF-8 (4 bytes), at `p` in `buf`; returns the position after it. */
  private inline def putCodePoint(cp: Int, buf: Array[Byte], p: Int): Int = {
    buf(p) = (0xF0 | (cp >> 18)).toByte
    buf(p + 1) = (0x80 | ((cp >> 12) & 0x3F)).toByte
    buf(p + 2) = (0x80 | ((cp >> 6) & 0x3F)).toByte
    buf(p + 3) = (0x80 | (cp & 0x3F)).toByte
    p + 4
  }
}
