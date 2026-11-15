package com.fastscala.js

import java.io.Writer

/**
 * Escaping for the inside of a JavaScript string literal: the chars OWASP's `Encode.forJavaScript` escapes, escaped as
 * it escapes them, but each one the same wherever it is. (OWASP also hex-escapes its input's last char when it's a
 * letter of "script", `cCiIpPrRsStT`: not done here.) So a String's escaping is its parts' escapings, one after the
 * other: it can be escaped piece by piece, as it's written.
 *
 * Escaped: the C0 and C1 controls, DEL, U+2028 and U+2029, lone surrogates (as `\uXXXX`), and
 * `space ! " $ & ' - / < > \ ` {`. A surrogate pair is written as it is.
 */
object JsEscape {

  /** The escape of each char below U+00A0 (null: written as it is). */
  private val Table: Array[String] = {
    val t = new Array[String](0xA0)
    def hex(c: Int): String = f"\\x$c%02x"
    (0 until 0x20).foreach(c => t(c) = hex(c))
    t('\b') = "\\b"
    t('\t') = "\\t"
    t('\n') = "\\n"
    t('\f') = "\\f"
    t('\r') = "\\r"
    " !\"$&'-<>`{".foreach(c => t(c) = hex(c))
    t('/') = "\\/"
    t('\\') = "\\\\"
    (0x7F until 0xA0).foreach(c => t(c) = hex(c))
    t
  }

  /** `c`'s escape, or null if it's written as it is; for a surrogate, its escape as a lone one. */
  private def escapeOf(c: Char): String =
    if (c < 0xA0) Table(c)
    else if (c == ' ') "\\u2028"
    else if (c == ' ') "\\u2029"
    else if (Character.isSurrogate(c)) f"\\u${c.toInt}%04x"
    else null

  /** `s`, escaped (`s` itself if nothing in it is); null as `"null"`, as OWASP does. */
  def escape(s: String): String = if (s eq null) "null" else {
    val first = firstToEscape(s, 0, s.length)
    if (first == s.length) s
    else {
      val sb = new java.lang.StringBuilder(s.length + 16)
      sb.append(s, 0, first)
      escapeInto(s, first, s.length, sb)
      sb.toString
    }
  }

  /** `s(from until to)`, escaped, appended to `out`. */
  def escapeInto(s: CharSequence, from: Int, to: Int, out: java.lang.StringBuilder): Unit = {
    var i = from
    while (i < to) {
      val run = firstToEscape(s, i, to)
      out.append(s, i, run)
      i = run
      if (i < to) {
        out.append(escapeOf(s.charAt(i)))
        i += 1
      }
    }
  }

  /** Where the first char to escape in `s(from until to)` is (`to`: none); a surrogate pair isn't escaped. */
  private def firstToEscape(s: CharSequence, from: Int, to: Int): Int = {
    var i = from
    while (i < to) {
      val c = s.charAt(i)
      if (Character.isHighSurrogate(c) && i + 1 < to && Character.isLowSurrogate(s.charAt(i + 1))) i += 2
      else if (escapeOf(c) ne null) return i
      else i += 1
    }
    to
  }

  /**
   * A Writer that writes what's written to it into `out`, escaped. The escaped chars are collected in a buffer, and
   * written to `out` in blocks (when it's full, and on [[flush]]): few, big writes. A high surrogate at the end of a
   * write is held until the next char (or [[flush]]) shows whether it's a pair. Neither flushing nor closing it closes
   * `out`.
   */
  final class EscapingWriter(out: Writer) extends Writer {

    /** The escaped chars not written to `out` yet: `buf(0 until pos)`. (Room for any escape: at most 6 chars.) */
    private val buf = new Array[Char](8192)
    private var pos = 0

    /** A high surrogate written last, not written yet (0: none). */
    private var pendingHigh: Char = 0

    private def drain(): Unit = if (pos > 0) {
      out.write(buf, 0, pos)
      pos = 0
    }

    private def put(c: Char): Unit = {
      if (pos == buf.length) drain()
      buf(pos) = c
      pos += 1
    }

    private def put(esc: String): Unit = {
      if (pos + esc.length > buf.length) drain()
      esc.getChars(0, esc.length, buf, pos)
      pos += esc.length
    }

    override def write(cbuf: Array[Char], off: Int, len: Int): Unit = {
      val end = off + len
      var i = off
      if (pendingHigh != 0 && i < end) {
        val high = pendingHigh
        pendingHigh = 0
        if (Character.isLowSurrogate(cbuf(i))) {
          put(high)
          put(cbuf(i))
          i += 1
        } else put(escapeOf(high))
      }
      while (i < end) {
        val c = cbuf(i)
        if (Character.isHighSurrogate(c)) {
          if (i + 1 < end) {
            if (Character.isLowSurrogate(cbuf(i + 1))) { // (a pair: written as it is)
              put(c)
              put(cbuf(i + 1))
              i += 2
            } else {
              put(escapeOf(c))
              i += 1
            }
          } else { // (the last char: maybe the first half of a pair the next write ends)
            pendingHigh = c
            i += 1
          }
        } else {
          val esc = escapeOf(c)
          if (esc eq null) put(c) else put(esc)
          i += 1
        }
      }
    }

    /** Writes the held high surrogate, if any (as a lone one: nothing written after this continues it), and flushes. */
    override def flush(): Unit = {
      if (pendingHigh != 0) {
        put(escapeOf(pendingHigh))
        pendingHigh = 0
      }
      drain()
      out.flush()
    }

    override def close(): Unit = flush()
  }
}
