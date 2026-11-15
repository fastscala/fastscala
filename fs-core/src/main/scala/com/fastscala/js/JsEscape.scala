package com.fastscala.js

/**
 * Escaping for the inside of a JavaScript string literal: the chars OWASP's `Encode.forJavaScript` escapes, escaped as
 * it escapes them, but each one the same wherever it is. (OWASP also hex-escapes its input's last char when it's a
 * letter of "script", `cCiIpPrRsStT`: not done here.) So a String's escaping is its parts' escapings, one after the
 * other: it can be escaped piece by piece, as it's written (as fs-scala-xml's Encoder does).
 *
 * Escaped: the C0 and C1 controls, DEL, U+2028 and U+2029, lone surrogates (as `\uXXXX`), and
 * `space ! " $ & ' - / < > \ ` {`. A surrogate pair is written as it is.
 */
object JsEscape {

  private final val HexDigits = "0123456789abcdef"

  /** The escape of each char below U+00A0 (null: written as it is). */
  private val Table: Array[String] = {
    val t = new Array[String](0xA0)
    def hex(c: Int): String = {
      val buf = new Array[Char](4)
      buf(0) = '\\'
      buf(1) = 'x'
      buf(2) = HexDigits.charAt((c >>> 4) & 0xF)
      buf(3) = HexDigits.charAt(c & 0xF)
      new String(buf)
    }
    var c = 0
    while (c < 0x20) {
      t(c) = hex(c)
      c += 1
    }
    t('\b') = "\\b"
    t('\t') = "\\t"
    t('\n') = "\\n"
    t('\f') = "\\f"
    t('\r') = "\\r"
    val extra = " !\"$&'-<>`{"
    var i = 0
    while (i < extra.length) {
      val ch = extra.charAt(i)
      t(ch) = hex(ch)
      i += 1
    }
    t('/') = "\\/"
    t('\\') = "\\\\"
    c = 0x7F
    while (c < 0xA0) {
      t(c) = hex(c)
      c += 1
    }
    t
  }

  private def unicodeEscape(c: Char): String = {
    val buf = new Array[Char](6)
    buf(0) = '\\'
    buf(1) = 'u'
    buf(2) = HexDigits.charAt((c >>> 12) & 0xF)
    buf(3) = HexDigits.charAt((c >>> 8) & 0xF)
    buf(4) = HexDigits.charAt((c >>> 4) & 0xF)
    buf(5) = HexDigits.charAt(c & 0xF)
    new String(buf)
  }

  /** `c`'s escape, or null if it's written as it is; for a surrogate, its escape as a lone one. */
  private def escapeOf(c: Char): String =
    if (c < 0xA0) Table(c)
    else if (c < 0x2028) null
    else if (c == '\u2028') "\\u2028"
    else if (c == '\u2029') "\\u2029"
    else if (Character.isSurrogate(c)) unicodeEscape(c)
    else null

  /** `s`, escaped (`s` itself if nothing in it is); null as `"null"`, as OWASP does. */
  def escape(s: String): String = if (s eq null) "null" else {
    val len = s.length
    val first = firstToEscape(s, 0, len)
    if (first == len) s
    else {
      val sb = new java.lang.StringBuilder(len + 16)
      sb.append(s, 0, first)
      sb.append(escapeOf(s.charAt(first)))
      escapeInto(s, first + 1, len, sb)
      sb.toString
    }
  }

  /** `s(from until to)`, escaped, appended to `out`. */
  def escapeInto(s: CharSequence, from: Int, to: Int, out: java.lang.StringBuilder): Unit = {
    var i = from
    while (i < to) {
      val run = firstToEscape(s, i, to)
      if (run > i) out.append(s, i, run)
      i = run
      if (i < to) {
        out.append(escapeOf(s.charAt(i)))
        i += 1
      }
    }
  }

  /** Where the first char to escape in `s(from until to)` is (`to`: none); a surrogate pair isn't escaped. */
  private def firstToEscape(s: CharSequence, from: Int, to: Int): Int = {
    val table = Table
    var i = from
    while (i < to) {
      val c = s.charAt(i)
      if (c < 0xA0) {
        if (table(c) ne null) return i
        i += 1
      } else if (c < 0x2028) {
        i += 1
      } else if (Character.isHighSurrogate(c) && i + 1 < to && Character.isLowSurrogate(s.charAt(i + 1))) {
        i += 2
      } else if (c == '\u2028' || c == '\u2029' || Character.isSurrogate(c)) {
        return i
      } else {
        i += 1
      }
    }
    to
  }
}