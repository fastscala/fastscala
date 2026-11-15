package com.fastscala.scala_xml.xml

import com.fastscala.js.JsEscape

import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.ConcurrentHashMap
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

object Encoder {

  // ---------- the entry points ----------

  /** `s`, escaped for XML `xmlLevels` times, then for JavaScript `jsLevels` times, in UTF-8, into `out`'s buffer. */
  def write(s: String, out: EncoderOutput, xmlLevels: Int, jsLevels: Int): Unit =
    if ((xmlLevels | jsLevels) == 0) writeRaw(s, out)
    else writePacked(s, 0, out, escapingFor(xmlLevels, jsLevels), xmlLevels, jsLevels)

  /** As the other [[write]], for what's in `sb` (e.g. what scala-xml wrote for the nodes only it knows how to). */
  def write(sb: java.lang.StringBuilder, out: EncoderOutput, xmlLevels: Int, jsLevels: Int): Unit = {
    val len = sb.length
    if (len > 0) {
      writeCareful(sb, 0, len, len, out, escapingFor(xmlLevels, jsLevels), xmlLevels, jsLevels)
    }
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
   * one is checked, and then copied in bulk if it's all ASCII (or its ASCII prefix copied in bulk before encoding).
   */
  private def writeRaw(s: String, out: EncoderOutput): Unit = {
    val len = s.length
    if (len == 0) return
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
      val first = firstNonAscii(s, len)
      if (first == len) {
        writeAsciiBulk(s, 0, len, out)
      } else {
        if (first > 0) writeAsciiBulk(s, 0, first, out)
        writePacked(s, first, out, Unescaped, 0, 0)
      }
    }
  }

  private def firstNonAscii(s: String, len: Int): Int = {
    var i = 0
    while (i < len) {
      if (s.charAt(i) >= 0x80) return i
      i += 1
    }
    len
  }

  /** `s(from until to)`, all ASCII, in as few copies as the buffer allows. */
  private def writeAsciiBulk(s: String, from: Int, to: Int, out: EncoderOutput): Unit = {
    val n = to - from
    var buf = out.buf
    var p = out.pos
    val rem = buf.length - p
    if (n <= rem) {
      copyAscii(s, from, to, buf, p)
      out.pos = p + n
    } else {
      var start = from
      while (start < to) {
        if (p == buf.length) {
          out.pos = p
          out.flush()
          buf = out.buf
          p = out.pos
        }
        val chunk = math.min(to - start, buf.length - p)
        copyAscii(s, start, start + chunk, buf, p)
        p += chunk
        start += chunk
      }
      out.pos = p
    }
  }

  @nowarn("cat=deprecation")
  private inline def copyAscii(s: String, from: Int, to: Int, buf: Array[Byte], p: Int): Unit =
    s.getBytes(from, to, buf, p)

  // ---------- the escapings' tables ----------

  /** The chars whose bytes are in the tables: below U+00A0 (ASCII and the C1 controls). */
  private final val TableSize = 0xA0

  /** In [[Escaping.packed]]: a char whose bytes don't fit in two packed stores (more than 15). */
  private final val NotPacked = -1L

  /**
   * An escaping (so many XML levels, then so many JavaScript ones) of each char below [[TableSize]]:
   *
   * @param bytes   its bytes (null: the char itself, as one byte);
   * @param packed  bytes 0..6 (lowest byte first) and length `0..15` in bits 56..59 (`>= 0L`); [[NotPacked]] (`-1L`)
   *                if length > 15;
   *
   * @param packed2 bytes 7..14 when length is `8..15` (written at `p + 7` after `packed` is written at `p`).
   */
  private final class Escaping(
                                val bytes: Array[Array[Byte]],
                                val packed: Array[Long],
                                val packed2: Array[Long],
                                val u2028Bytes: Array[Byte],
                                val u2029Bytes: Array[Byte]
                              )

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
    val packed = new Array[Long](TableSize)
    val packed2 = new Array[Long](TableSize)
    var c = 0
    while (c < TableSize) {
      val bs = bytes(c)
      if (bs eq null) {
        packed(c) = c.toLong | (1L << 56)
      } else if (bs.length > 15) {
        packed(c) = NotPacked
      } else {
        var lo = bs.length.toLong << 56
        val firstLim = math.min(bs.length, 7)
        var i = 0
        while (i < firstLim) {
          lo |= (bs(i) & 0xFFL) << (8 * i)
          i += 1
        }
        packed(c) = lo
        if (bs.length > 7) {
          var hi = 0L
          var j = 7
          while (j < bs.length) {
            hi |= (bs(j) & 0xFFL) << (8 * (j - 7))
            j += 1
          }
          packed2(c) = hi
        }
      }
      c += 1
    }
    val u2028 = if (jsLevels > 0) escaped("\u2028", 0, jsLevels).getBytes(UTF_8) else null
    val u2029 = if (jsLevels > 0) escaped("\u2029", 0, jsLevels).getBytes(UTF_8) else null
    new Escaping(bytes, packed, packed2, u2028, u2029)
  }

  private final val TabledXmlLevels = 3
  private final val TabledJsLevels = 8

  private val Escapings: Array[Escaping] =
    Array.tabulate(TabledXmlLevels * TabledJsLevels)(i => makeEscaping(i / TabledJsLevels, i % TabledJsLevels))

  private val OtherEscapings = new ConcurrentHashMap[java.lang.Long, Escaping]()

  /** No escaping: each char's UTF-8. */
  private val Unescaped: Escaping = Escapings(0)

  private def escapingFor(xmlLevels: Int, jsLevels: Int): Escaping =
    if (xmlLevels < TabledXmlLevels && jsLevels < TabledJsLevels) {
      Escapings(xmlLevels * TabledJsLevels + jsLevels)
    } else {
      val key = java.lang.Long.valueOf((xmlLevels.toLong << 32) | (jsLevels & 0xFFFFFFFFL))
      val cached = OtherEscapings.get(key)
      if (cached ne null) cached
      else {
        val created = makeEscaping(xmlLevels, jsLevels)
        val prev = OtherEscapings.putIfAbsent(key, created)
        if (prev ne null) prev else created
      }
    }

  // ---------- escaped (or not ASCII): the fast way ----------

  /** With less room than for this many chars, and more to write, the chars are written one by one ([[writeCareful]]). */
  private final val MinSlice = 32

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
        i = writeSlice(s, i, end, buf, p, out, escaping)
        if (i < end) i = writeCareful(s, i, i + 1, len, out, escaping, xmlLevels, jsLevels) // (the char it stopped at)
      }
    }
  }

  /**
   * `s(from until end)`, without checking for room (the caller guarantees 8 bytes for each char), into `buf` from `p`
   * on: until `end`, or a char that isn't written this way (see [[writePacked]]). Sets `out`'s position, and returns
   * where it stopped.
   */
  private def writeSlice(s: String, from: Int, initialEnd: Int, buf: Array[Byte], pos: Int, out: EncoderOutput, escaping: Escaping): Int = {
    val packed = escaping.packed
    val packed2 = escaping.packed2
    var p = pos
    var i = from
    var end = initialEnd
    while (i < end) {
      val c = s.charAt(i)
      if (c < TableSize) {
        val entry = packed(c)
        if (entry < 0L) { // NotPacked (> 15 bytes)
          out.pos = p
          return i
        }
        val bLen = (entry >>> 56).toInt
        if (bLen <= 7) {
          LittleEndian.putLong(buf, p, entry)
          p += bLen
        } else if (p <= buf.length - 15) {
          LittleEndian.putLong(buf, p, entry)
          LittleEndian.putLong(buf, p + 7, packed2(c))
          p += bLen
          // Tighten `end` if extra bytes (> 8) reduced remaining 8-byte-per-char headroom
          val maxEnd = i + 1 + ((buf.length - p) >> 3)
          if (maxEnd < end) end = maxEnd
        } else {
          out.pos = p
          return i
        }
      } else if (c < 0x800) {
        val word = (0xC0L | (c >> 6)) | ((0x80L | (c & 0x3F)) << 8)
        LittleEndian.putLong(buf, p, word)
        p += 2
      } else if (!Character.isSurrogate(c) && (c & 0xFFFE) != 0x2028) {
        val word = (0xE0L | (c >> 12)) | ((0x80L | ((c >> 6) & 0x3F)) << 8) | ((0x80L | (c & 0x3F)) << 16)
        LittleEndian.putLong(buf, p, word)
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
    if (entry >= 0L) {
      val bLen = (entry >>> 56).toInt
      if (bLen <= 7 && p <= buf.length - 8) {
        LittleEndian.putLong(buf, p, entry)
        out.pos = p + bLen
        return
      } else if (bLen > 7 && p <= buf.length - 15) {
        LittleEndian.putLong(buf, p, entry)
        LittleEndian.putLong(buf, p + 7, escaping.packed2(c))
        out.pos = p + bLen
        return
      }
    }
    val bytes = escaping.bytes(c)
    if (bytes eq null) {
      if (p == buf.length) {
        out.flush()
        out.buf(0) = c.toByte
        out.pos = 1
      } else {
        buf(p) = c.toByte
        out.pos = p + 1
      }
    } else writeBytes(bytes, out)
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
      } else if (jsLevels > 0 && c == '\u2028') {
        bytes = escaping.u2028Bytes
      } else if (jsLevels > 0 && c == '\u2029') {
        bytes = escaping.u2029Bytes
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

  private def lowSurrogateAt(s: CharSequence, i: Int, to: Int, xml: Boolean): Int =
    if (!Character.isHighSurrogate(s.charAt(i))) -1
    else {
      var j = i + 1
      if (xml) while (j < to && xmlDrops(s.charAt(j))) j += 1
      if (j < to && Character.isLowSurrogate(s.charAt(j))) j else -1
    }

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

  private inline def putCodePoint(cp: Int, buf: Array[Byte], p: Int): Int = {
    buf(p) = (0xF0 | (cp >> 18)).toByte
    buf(p + 1) = (0x80 | ((cp >> 12) & 0x3F)).toByte
    buf(p + 2) = (0x80 | ((cp >> 6) & 0x3F)).toByte
    buf(p + 3) = (0x80 | (cp & 0x3F)).toByte
    p + 4
  }
}