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
}
