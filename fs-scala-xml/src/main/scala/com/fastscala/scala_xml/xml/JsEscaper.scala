package com.fastscala.scala_xml.xml

import org.owasp.encoder.Encode

import java.nio.charset.StandardCharsets.UTF_8

/**
 * Escapes UTF-8 bytes for a JavaScript string literal, `depth` times over (an EncJs inside `depth - 1` others), as
 * applying OWASP's `Encode.forJavaScript` `depth` times would: in one pass, with one precomputed table (the layers are
 * fused, inside-out: each entry is a char escaped `depth` times).
 *
 * The tables are taken from OWASP itself, not written by hand, so the output is the same as
 * [[com.fastscala.js.EncJs.cmd]]'s. OWASP escapes per char, with no context (the same char the same way wherever it
 * is: checked when the tables are built), but for the last char of its input: if it's a letter of "script"
 * (`cCiIpPrRsStT`), it's hex-escaped too, so that a `</script` begun before the input can't be completed by it. So the
 * tables are of chars that aren't the last ([[JsEscaper.escapeNotLast]]); the last one of each layer is resolved
 * apart, by XmlJsStream.Out ([[JsEscaper.resolve]]).
 *  - ASCII: a 128-entry table (null: the byte stays as it is).
 *  - Anything else stays as it is, but for the few chars OWASP escapes (the C1 controls, U+0080 to U+009F; U+2028 and
 *    U+2029, the line terminators): found by trying every non-ASCII BMP char (and some others), and looked up by code
 *    point. (Lone surrogates aren't handled: XmlJsStream never writes one, but `?` instead.)
 */
private[xml] final class JsEscaper private(val depth: Int) {

  /** For each ASCII byte, its escape (UTF-8 bytes), or null if it stays as it is. */
  private val ascii: Array[Array[Byte]] = Array.tabulate(128)(c => {
    val esc = JsEscaper.escapeNotLast(c.toChar.toString, depth)
    if (esc == c.toChar.toString) null else esc.getBytes(UTF_8)
  })

  /** The non-ASCII chars that are escaped (code point -> bytes). */
  private val special: Map[Int, Array[Byte]] =
    JsEscaper.SpecialCodePoints.map(cp => cp -> JsEscaper.escapeNotLast(new String(Character.toChars(cp)), depth).getBytes(UTF_8)).toMap

  private val specialMin: Int = if (special.isEmpty) Int.MaxValue else special.keys.min

  /** The longest escape of a char: the room the escaping loop needs before each one. */
  val maxEscapeLength: Int = (ascii.iterator.filter(_ ne null).map(_.length) ++ special.valuesIterator.map(_.length) ++ Iterator(4)).max

  /**
   * Escapes `raw(0 until n)` (well-formed UTF-8: whole chars), none of them the last of its layers' input, into `out`'s
   * buffer, queuing it whenever it's full ([[XmlJsStream.Out.queueFull]]).
   */
  def escape(raw: Array[Byte], n: Int, out: XmlJsStream.Out): Unit = {
    val table = ascii
    val room = XmlJsStream.ChunkSize - maxEscapeLength
    var buf = out.buf
    var p = out.pos
    var i = 0
    while (i < n) {
      if (p > room) {
        out.pos = p
        out.queueFull()
        buf = out.buf
        p = out.pos
      }
      val b = raw(i)
      if (b >= 0) { // ASCII
        val esc = table(b)
        if (esc eq null) {
          buf(p) = b
          p += 1
        } else {
          var j = 0
          while (j < esc.length) {
            buf(p + j) = esc(j)
            j += 1
          }
          p += esc.length
        }
        i += 1
      } else { // a non-ASCII char: copied as it is, unless it's one of the few escaped
        val lead = b & 0xFF
        val len = if (lead >= 0xF0) 4 else if (lead >= 0xE0) 3 else 2
        val esc =
          if (special.isEmpty) null
          else {
            val cp =
              if (len == 2) ((lead & 0x1F) << 6) | (raw(i + 1) & 0x3F)
              else if (len == 3) ((lead & 0x0F) << 12) | ((raw(i + 1) & 0x3F) << 6) | (raw(i + 2) & 0x3F)
              else ((lead & 0x07) << 18) | ((raw(i + 1) & 0x3F) << 12) | ((raw(i + 2) & 0x3F) << 6) | (raw(i + 3) & 0x3F)
            if (cp < specialMin) null else special.getOrElse(cp, null)
          }
        if (esc eq null) {
          var j = 0
          while (j < len) {
            buf(p + j) = raw(i + j)
            j += 1
          }
          p += len
        } else {
          var j = 0
          while (j < esc.length) {
            buf(p + j) = esc(j)
            j += 1
          }
          p += esc.length
        }
        i += len
      }
    }
    out.pos = p
  }
}

private[xml] object JsEscaper {

  /** OWASP's escaping of `s`, followed by more input (so its last char isn't escaped as the input's last). */
  private def notLast(s: String): String = {
    val esc = Encode.forJavaScript(s + "a") // ("a": not escaped; and `s`'s last char isn't the last now)
    esc.substring(0, esc.length - 1)
  }

  /** `s` escaped `times` times, as a part of the input followed by more, in every layer. */
  private[xml] def escapeNotLast(s: String, times: Int): String = {
    var r = s
    var i = 0
    while (i < times) {
      r = notLast(r)
      i += 1
    }
    r
  }

  /**
   * `s` (the last char written, at escaping `depth`), escaped by its layers, inside-out: as the last of their input by
   * those that have ended since (the layers deeper than `minDepth`, the shallowest the escaping has been since), as
   * followed by more by the others.
   */
  private[xml] def resolve(s: String, depth: Int, minDepth: Int): String = {
    var r = s
    var layer = depth
    while (layer >= 1) {
      r = if (layer > minDepth) Encode.forJavaScript(r) else notLast(r)
      layer -= 1
    }
    r
  }

  private val Sample = "b" + new String(Character.toChars(0xE9)) + new String(Character.toChars(0x1F600))

  /**
   * The non-ASCII code points OWASP's forJavaScript changes: found by trying all the BMP's (but surrogates), and the
   * start of each other plane. Each (and each ASCII char) is checked to be escaped the same in context as alone.
   */
  private[xml] lazy val SpecialCodePoints: Seq[Int] = {
    val bmp = (0x80 until 0x10000).filterNot(c => Character.isSurrogate(c.toChar))
    val others = (1 to 16).flatMap(plane => (plane << 16) until ((plane << 16) + 0x400))
    val found = (bmp ++ others).filter { cp =>
      val s = new String(Character.toChars(cp))
      notLast(s) != s
    }
    for (cp <- found ++ (0 until 128)) {
      val s = new String(Character.toChars(cp))
      val esc = notLast(s)
      val inContext = notLast("a" + s + Sample + s + s)
      assert(inContext == "a" + esc + Sample + esc + esc, s"forJavaScript isn't per char for U+${cp.toHexString}")
    }
    found
  }

  private val cache = new java.util.concurrent.ConcurrentHashMap[Int, JsEscaper]()

  /** The escaper for `depth` EncJs's, one inside the other (depth >= 1). */
  def forDepth(depth: Int): JsEscaper = cache.computeIfAbsent(depth, d => new JsEscaper(d))
}
