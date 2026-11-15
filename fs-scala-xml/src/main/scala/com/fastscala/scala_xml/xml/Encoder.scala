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

  /**
   * Precomputed escaping table for a specific `(xmlLevels, jsLevels)` pair.
   * Resolved once per scope and passed directly to `write` / `writeAscii`.
   */
  final class Escaping private[Encoder](
                                         val xmlLevels: Int,
                                         val jsLevels: Int,
                                         private[Encoder] val bytes: Array[Array[Byte]],
                                         private[Encoder] val packed: Array[Long],
                                         private[Encoder] val packed2: Array[Long],
                                         private[Encoder] val u2028Bytes: Array[Byte],
                                         private[Encoder] val u2029Bytes: Array[Byte]
                                       ) {
    private[Encoder] val xml: Boolean = xmlLevels > 0

    @volatile private[Encoder] var _nextXml: Escaping = null
    @volatile private[Encoder] var _nextJs: Escaping = null
    @volatile private[Encoder] var _enterJs: Escaping = null

    /** Transition to one additional XML escaping level (e.g. entering an attribute value). */
    def nextXml: Escaping = {
      val next = _nextXml
      if (next ne null) next
      else {
        val resolved = escaping(xmlLevels + 1, jsLevels)
        _nextXml = resolved
        resolved
      }
    }

    /** Transition to one additional JS escaping level when `xmlLevels == 0`. */
    def nextJs: Escaping = {
      val next = _nextJs
      if (next ne null) next
      else {
        val resolved = escaping(xmlLevels, jsLevels + 1)
        _nextJs = resolved
        resolved
      }
    }

    /** Transition to `(0, jsLevels + 1)` when entering `EncJs` or `JsWithScriptDetection` (XML levels inside JS are absorbed). */
    def enterJs: Escaping = {
      if (xmlLevels == 0) return nextJs
      val next = _enterJs
      if (next ne null) next
      else {
        val resolved = escaping(0, jsLevels + 1)
        _enterJs = resolved
        resolved
      }
    }
  }
  
  private final val TableSize = 0xA0
  private final val NotPacked = -1L
  private final val TabledXmlLevels = 3
  private final val TabledJsLevels = 8

  private val Escapings: Array[Escaping] = {
    val arr = Array.tabulate(TabledXmlLevels * TabledJsLevels) { i =>
      makeEscaping(i / TabledJsLevels, i % TabledJsLevels)
    }
    var x = 0
    while (x < TabledXmlLevels) {
      var y = 0
      while (y < TabledJsLevels) {
        val cur = arr(x * TabledJsLevels + y)
        if (x + 1 < TabledXmlLevels) cur._nextXml = arr((x + 1) * TabledJsLevels + y)
        if (y + 1 < TabledJsLevels) cur._nextJs = arr(x * TabledJsLevels + (y + 1))
        y += 1
      }
      x += 1
    }
    arr
  }

  private val OtherEscapings = new ConcurrentHashMap[java.lang.Long, Escaping]()

  /** Pre-resolved common escaping levels for zero-lookup entry. */
  val Unescaped: Escaping = Escapings(0)
  val Xml1: Escaping = Escapings(TabledJsLevels)
  val Js1: Escaping = Escapings(1)
  val Xml1Js1: Escaping = Escapings(TabledJsLevels + 1)

  /** Resolves the `Escaping` for `(xmlLevels, jsLevels)`. Call once per scope, not per write. */
  def escaping(xmlLevels: Int, jsLevels: Int): Escaping =
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

  // ---------- the entry points ----------

  /** `s`, escaped according to `esc` and encoded in UTF-8 into `out`. */
  def write(s: String, out: EncoderOutput, esc: Escaping): Unit =
    if (esc eq Unescaped) writeRaw(s, out)
    else writePacked(s, 0, out, esc)

  /** `sb`, escaped according to `esc` and encoded in UTF-8 into `out`. */
  def write(sb: java.lang.StringBuilder, out: EncoderOutput, esc: Escaping): Unit = {
    val len = sb.length
    if (len > 0) writeCareful(sb, 0, len, len, out, esc)
  }

  /** An ASCII markup char (`<`, `>`, `/`, `=`, `"`, ` `, `:`), escaped according to `esc`. */
  inline def writeAscii(c: Char, out: EncoderOutput, esc: Escaping): Unit =
    if (esc eq Unescaped) {
      val p = out.pos
      val buf = out.buf
      if (p == buf.length) {
        out.flush()
        out.buf(0) = c.toByte
        out.pos = 1
      } else {
        buf(p) = c.toByte
        out.pos = p + 1
      }
    } else writeEscapedAscii(c, out, esc)

  /** Two ASCII markup chars (`</`, `/>`, `="`), escaped according to `esc`. */
  inline def writeAscii2(c1: Char, c2: Char, out: EncoderOutput, esc: Escaping): Unit =
    if (esc eq Unescaped) {
      val p = out.pos
      val buf = out.buf
      if (p + 2 <= buf.length) {
        buf(p) = c1.toByte
        buf(p + 1) = c2.toByte
        out.pos = p + 2
      } else {
        writeAscii(c1, out, Unescaped)
        writeAscii(c2, out, Unescaped)
      }
    } else {
      writeEscapedAscii(c1, out, esc)
      writeEscapedAscii(c2, out, esc)
    }

  // ---------- nothing escaped ----------

  private final val ShortString = 32

  private def writeRaw(s: String, out: EncoderOutput): Unit = {
    val len = s.length
    if (len == 0) return
    val buf = out.buf
    var p = out.pos
    if (len <= ShortString && len <= buf.length - p) {
      var i = 0
      while (i < len) {
        val c = s.charAt(i)
        if (c >= 0x80) {
          out.pos = p
          writePacked(s, i, out, Unescaped)
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
        writePacked(s, first, out, Unescaped)
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

  // ---------- table construction ----------

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
    new Escaping(xmlLevels, jsLevels, bytes, packed, packed2, u2028, u2029)
  }

  // ---------- escaped (or not ASCII): the fast way ----------

  private final val MinSlice = 32

  private def writePacked(s: String, from: Int, out: EncoderOutput, esc: Escaping): Unit = {
    val len = s.length
    var i = from
    while (i < len) {
      val buf = out.buf
      val p = out.pos
      val room = (buf.length - p) >> 3
      if (room < len - i && room < MinSlice) {
        i = writeCareful(s, i, math.min(len, i + MinSlice), len, out, esc)
      } else {
        val end = math.min(len, i + room)
        i = writeSlice(s, i, end, buf, p, out, esc)
        if (i < end) i = writeCareful(s, i, i + 1, len, out, esc)
      }
    }
  }

  private def writeSlice(s: String, from: Int, initialEnd: Int, buf: Array[Byte], pos: Int, out: EncoderOutput, esc: Escaping): Int = {
    val packed = esc.packed
    val packed2 = esc.packed2
    var p = pos
    var i = from
    var end = initialEnd
    while (i < end) {
      val c = s.charAt(i)
      if (c < TableSize) {
        val entry = packed(c)
        if (entry < 0L) {
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

  @noinline def writeEscapedAscii(c: Char, out: EncoderOutput, esc: Escaping): Unit = {
    val entry = esc.packed(c)
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
        LittleEndian.putLong(buf, p + 7, esc.packed2(c))
        out.pos = p + bLen
        return
      }
    }
    val bytes = esc.bytes(c)
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

  private inline def xmlDrops(c: Char): Boolean = c < ' ' && c != '\t' && c != '\n' && c != '\r'

  private def writeCareful(s: CharSequence, from: Int, until: Int, to: Int, out: EncoderOutput, esc: Escaping): Int = {
    val table = esc.bytes
    val xml = esc.xml
    val jsLevels = esc.jsLevels
    var buf = out.buf
    var p = out.pos
    var i = from
    while (i < until) {
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
        } else {
          bytes = escaped(c.toString, xmlLevels = 0, jsLevels).getBytes(UTF_8)
        }
      } else if (jsLevels > 0 && c == '\u2028') {
        bytes = esc.u2028Bytes
      } else if (jsLevels > 0 && c == '\u2029') {
        bytes = esc.u2029Bytes
      } else {
        p = putBmp(c, buf, p)
      }

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