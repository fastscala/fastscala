package com.fastscala.scala_xml.xml

import com.fastscala.js.JsEscape

import java.nio.charset.StandardCharsets.UTF_8
import scala.annotation.nowarn
import scala.xml.Utility

/**
 * What [[Encoder]] writes into: a byte buffer, filled from `pos` on, that's emptied when it's full.
 */
trait EncoderOutput {

  /** The buffer being filled. (Another one after a [[flush]], maybe: of the same length.) */
  def buf: Array[Byte]

  /** How many bytes of `buf` are filled: where the next one goes. */
  def pos: Int

  def pos_=(p: Int): Unit

  /** Takes what's in the buffer (if anything): after it, `buf` is empty (`pos` is 0). */
  def flush(): Unit
}

/**
 * Everything XmlJsStream writes goes through here: a String (or a StringBuilder), escaped for XML (text or attribute
 * value) `xmlLevels` times, then for JavaScript string literals `jsLevels` times (once for each EncJs it's in), and
 * encoded in UTF-8, straight into an [[EncoderOutput]]'s byte buffer (flushing it whenever it's full).
 *
 * Every escaping is per char (XML's, and [[JsEscape]]'s), so the Strings written one after the other are escaped as
 * their concatenation would be: `JsEscape.escape` `jsLevels` times of `Utility.escape` `xmlLevels` times, in UTF-8.
 * (Any escaping is XML levels and then JavaScript ones: an XML level outside a JavaScript one changes nothing, as
 * JavaScript escaping leaves none of the chars XML escapes, or drops. So XmlJsStream doesn't count those.) More than
 * one XML level is e.g. the text of a NodeSeqJs's XML in an attribute, without an EncJs: escaped for its own XML, and
 * then for the attribute's value.
 *
 * How it's written:
 *  - nothing escaped, all ASCII: copied in bulk (or, for a short String, char by char, as it's checked);
 *  - anything else: char by char, each char's bytes looked up in the table of that escaping (see [[Escaping]]). Most
 *    are a single 8-byte store, whatever the char is escaped as (see [[writePacked]]); the rest, and whatever is
 *    written when the buffer is nearly full, goes one char at a time, each checked to fit (see [[writeCareful]]).
 *
 * A lone surrogate is `?` (as String.getBytes(UTF_8) writes it) when not escaped for JavaScript, and `\uXXXX` (as
 * JsEscape escapes it) when it is. With XML escaping, a surrogate pair split by dropped chars is a pair again.
 */
object Encoder {

  // ---------- the entry points ----------

  /** `s`, escaped for XML `xmlLevels` times, then for JavaScript `jsLevels` times, in UTF-8, into `out`'s buffer. */
  def write(s: String, out: EncoderOutput, xmlLevels: Int, jsLevels: Int): Unit =
    if ((xmlLevels | jsLevels) == 0) writeRaw(s, out)
    else writePacked(s, 0, out, escapingFor(xmlLevels, jsLevels), xmlLevels, jsLevels)

  /** As the other [[write]], for what's in `sb` (e.g. what scala-xml wrote for the nodes only it knows how to). */
  def write(sb: java.lang.StringBuilder, out: EncoderOutput, xmlLevels: Int, jsLevels: Int): Unit = {
    val len = sb.length
    writeCareful(sb, 0, len, len, out, escapingFor(xmlLevels, jsLevels), xmlLevels, jsLevels)
  }

  /** An ASCII char of the markup (`<`, `>`, `/`, `=`, `"`, ` `, `:`), escaped as [[write]] does. */
  inline def writeAscii(c: Char, out: EncoderOutput, xmlLevels: Int, jsLevels: Int): Unit =
    if ((xmlLevels | jsLevels) == 0) {
      if (out.pos == out.buf.length) out.flush()
      out.buf(out.pos) = c.toByte
      out.pos += 1
    } else writeEscapedAscii(c, out, xmlLevels, jsLevels)

  // ---------- nothing escaped ----------

  /** Strings up to this long are copied char by char: for a few bytes, a String.getBytes call costs more. */
  private final val ShortString = 32

  /**
   * `s` in UTF-8. A short one (names: tags, attributes) is copied char by char as it's checked, in one pass; a longer
   * one is checked, and then copied in bulk if it's all ASCII. Anything not ASCII is encoded char by char.
   */
  private def writeRaw(s: String, out: EncoderOutput): Unit = {
    val len = s.length
    val buf = out.buf
    var p = out.pos
    if (len <= ShortString && len <= buf.length - p) {
      var i = 0
      while (i < len) {
        val c = s.charAt(i)
        if (c >= 0x80) { // not ASCII: the rest is encoded
          out.pos = p
          writePacked(s, i, out, Unescaped, 0, 0)
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
      if (bits < 0x80) writeAsciiBulk(s, out) else writePacked(s, 0, out, Unescaped, 0, 0)
    }
  }

  /** `s`, all ASCII, in as few copies as the buffer allows. */
  private def writeAsciiBulk(s: String, out: EncoderOutput): Unit = {
    var start = 0
    val len = s.length
    while (start < len) {
      if (out.pos == out.buf.length) out.flush()
      val n = math.min(len - start, out.buf.length - out.pos)
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

  // ---------- the escapings' tables ----------

  /** The chars whose bytes are in the tables: below U+00A0 (ASCII and the C1 controls). */
  private final val TableSize = 0xA0

  /** In [[Escaping.packed]]: a char whose bytes don't fit in a packed entry (more than 7). */
  private final val NotPacked = -1L

  /**
   * An escaping (so many XML levels, then so many JavaScript ones) of each char below [[TableSize]]:
   *
   * @param bytes  its bytes (null: the char itself, as one byte);
   * @param packed the same, as a long: the bytes (up to 7; the first one lowest) and, in its highest byte, how many
   *               they are; so the char is written with one 8-byte store, and the position advanced by that many
   *               (the bytes stored after them are written over by what's written next). [[NotPacked]] if they're
   *               more than 7 (e.g. a `"` in an attribute, in a rerender: `\x26quot;`).
   */
  private final class Escaping(val bytes: Array[Array[Byte]], val packed: Array[Long])

  /**
   * The reference: `s` escaped for XML (as Utility.escape does) `xmlLevels` times, then for JavaScript `jsLevels`
   * times. Each table entry is this of one char, in UTF-8; it's used as it is for the rare chars that have none.
   */
  private def escaped(s: String, xmlLevels: Int, jsLevels: Int): String = {
    var r = s
    var level = 0
    while (level < xmlLevels) {
      r = Utility.escape(r)
      level += 1
    }
    level = 0
    while (level < jsLevels) {
      r = JsEscape.escape(r)
      level += 1
    }
    r
  }

  private def makeEscaping(xmlLevels: Int, jsLevels: Int): Escaping = {
    val bytes = Array.tabulate(TableSize) { c =>
      val bs = escaped(c.toChar.toString, xmlLevels, jsLevels).getBytes(UTF_8)
      if (bs.length == 1 && bs(0) == c) null else bs
    }
    val packed = Array.tabulate(TableSize) { c =>
      val bs = bytes(c)
      if (bs eq null) c.toLong | (1L << 56)
      else if (bs.length > 7) NotPacked
      else bs.indices.foldLeft(bs.length.toLong << 56)((acc, i) => acc | ((bs(i) & 0xFFL) << (8 * i)))
    }
    new Escaping(bytes, packed)
  }

  /**
   * The tables of fewer XML and JavaScript levels than these are made at once; others when first needed (an escape
   * doubles its backslashes at each JavaScript level, so a table of n is about 2^n * 100 bytes: deep ones are big,
   * and rare).
   */
  private final val TabledXmlLevels = 3
  private final val TabledJsLevels = 8

  private val Escapings: Array[Escaping] =
    Array.tabulate(TabledXmlLevels * TabledJsLevels)(i => makeEscaping(i / TabledJsLevels, i % TabledJsLevels))

  private val OtherEscapings = new java.util.concurrent.ConcurrentHashMap[(Int, Int), Escaping]()

  /** No escaping: each char's UTF-8. */
  private val Unescaped: Escaping = Escapings(0)

  private def escapingFor(xmlLevels: Int, jsLevels: Int): Escaping =
    if (xmlLevels < TabledXmlLevels && jsLevels < TabledJsLevels) Escapings(xmlLevels * TabledJsLevels + jsLevels)
    else OtherEscapings.computeIfAbsent((xmlLevels, jsLevels), key => makeEscaping(key._1, key._2))

  // ---------- escaped (or not ASCII): the fast way ----------

  /** With less room than for this many chars, and more to write, the chars are written one by one ([[writeCareful]]). */
  private final val MinSlice = 32

  /**
   * `s(from until its end)`, escaped by `escaping` (that of `xmlLevels` and `jsLevels`), in UTF-8.
   *
   * In slices of as many chars as surely fit in the buffer at 8 bytes each (usually the whole String): so there's no
   * check for room in the loop. A char below U+00A0 is one 8-byte store of its packed entry (see [[Escaping]]),
   * whether it's escaped or not: no branch on what it is. Above, a char is its UTF-8 (2 or 3 bytes: escaping leaves it
   * as it is). The few others (an escape of more than 7 bytes; the line separators, escaped for JavaScript; the
   * surrogates) are written by [[writeCareful]], one at a time; and so is everything when the buffer is nearly full,
   * so it's filled to its end.
   *
   * (Tried, and not kept: writing the escapes of 8 to 15 bytes with two stores, from a second table. No faster, on
   * attribute values with JavaScript in a rerender, where they're most frequent: each one ended the slice.)
   */
  private def writePacked(s: String, from: Int, out: EncoderOutput, escaping: Escaping, xmlLevels: Int, jsLevels: Int): Unit = {
    val len = s.length
    var i = from
    while (i < len) {
      val buf = out.buf
      val p = out.pos
      val room = (buf.length - p) >> 3
      if (room < len - i && room < MinSlice) {
        // Nearly full: char by char (each checked to fit), until past where it's flushed.
        i = writeCareful(s, i, math.min(len, i + MinSlice), len, out, escaping, xmlLevels, jsLevels)
      } else {
        val end = math.min(len, i + room)
        i = writeSlice(s, i, end, buf, p, out, escaping.packed)
        if (i < end) i = writeCareful(s, i, i + 1, len, out, escaping, xmlLevels, jsLevels) // (the char it stopped at)
      }
    }
  }

  /**
   * `s(from until end)`, without checking for room (the caller guarantees 8 bytes for each char), into `buf` from `p`
   * on: until `end`, or a char that isn't written this way (see [[writePacked]]). Sets `out`'s position, and returns
   * where it stopped.
   */
  private def writeSlice(s: String, from: Int, end: Int, buf: Array[Byte], pos: Int, out: EncoderOutput, packed: Array[Long]): Int = {
    var p = pos
    var i = from
    while (i < end) {
      val c = s.charAt(i)
      if (c < TableSize) {
        val entry = packed(c)
        if (entry == NotPacked) {
          out.pos = p
          return i
        }
        LittleEndian.putLong(buf, p, entry)
        p += (entry >>> 56).toInt
      } else if (c < 0x800) {
        buf(p) = (0xC0 | (c >> 6)).toByte
        buf(p + 1) = (0x80 | (c & 0x3F)).toByte
        p += 2
      } else if (!Character.isSurrogate(c) && (c & 0xFFFE) != 0x2028) {
        buf(p) = (0xE0 | (c >> 12)).toByte
        buf(p + 1) = (0x80 | ((c >> 6) & 0x3F)).toByte
        buf(p + 2) = (0x80 | (c & 0x3F)).toByte
        p += 3
      } else {
        out.pos = p
        return i
      }
      i += 1
    }
    out.pos = p
    i
  }

  /** One ASCII char of the markup, escaped as [[write]] does (with some escaping). */
  def writeEscapedAscii(c: Char, out: EncoderOutput, xmlLevels: Int, jsLevels: Int): Unit = {
    val escaping = escapingFor(xmlLevels, jsLevels)
    val entry = escaping.packed(c)
    val p = out.pos
    val buf = out.buf
    if (entry != NotPacked && p <= buf.length - 8) {
      LittleEndian.putLong(buf, p, entry)
      out.pos = p + (entry >>> 56).toInt
    } else {
      val bytes = escaping.bytes(c)
      if (bytes eq null) {
        if (p == buf.length) out.flush()
        out.buf(out.pos) = c.toByte
        out.pos += 1
      } else writeBytes(bytes, out)
    }
  }

  // ---------- escaped (or not ASCII): one char at a time ----------

  /** Whether XML escaping drops `c` (the control chars but \t, \n and \r). */
  private inline def xmlDrops(c: Char): Boolean = c < ' ' && c != '\t' && c != '\n' && c != '\r'

  /**
   * The chars of `s` from `from` until `until` (and what goes with them, up to `to`: a low surrogate after the last
   * one), escaped by `escaping`, in UTF-8: each one checked to fit in the buffer, which is flushed when it doesn't (an
   * escape longer than the room left is written across it). Returns where it stopped: `until`, or after it.
   */
  private def writeCareful(s: CharSequence, from: Int, until: Int, to: Int, out: EncoderOutput, escaping: Escaping, xmlLevels: Int, jsLevels: Int): Int = {
    val table = escaping.bytes
    val xml = xmlLevels > 0
    var buf = out.buf
    var p = out.pos
    var i = from
    while (i < until) {
      // Room for whatever a char but a table one is (4 bytes: a surrogate pair), and for a table one of one byte.
      if (p > buf.length - 4) {
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
        } else bytes = escaped(c.toString, xmlLevels = 0, jsLevels).getBytes(UTF_8) // (`\uXXXX`, escaped further)
      } else if (jsLevels > 0 && (c == ' ' || c == ' ')) {
        bytes = escaped(c.toString, xmlLevels = 0, jsLevels).getBytes(UTF_8)
      } else p = putBmp(c, buf, p)

      if (bytes ne null) {
        if (bytes.length > buf.length - p) {
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
    i
  }

  /** `bytes`, into the buffer, in as many pieces as it takes (one, but for an escape longer than the room left). */
  private def writeBytes(bytes: Array[Byte], out: EncoderOutput): Unit = {
    var start = 0
    while (start < bytes.length) {
      if (out.pos == out.buf.length) out.flush()
      val n = math.min(bytes.length - start, out.buf.length - out.pos)
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
