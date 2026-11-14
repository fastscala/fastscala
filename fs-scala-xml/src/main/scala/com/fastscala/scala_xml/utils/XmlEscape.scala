package com.fastscala.scala_xml.utils

import scala.annotation.switch

object XmlEscape {

  private final val ActionKeep: Byte = 0
  private final val ActionDrop: Byte = 1
  private final val ActionAmp: Byte = 2
  private final val ActionLt: Byte = 3
  private final val ActionGt: Byte = 4
  private final val ActionQuot: Byte = 5

  private val EscapeActions: Array[Byte] = {
    val arr = new Array[Byte](128)
    var i = 0
    while (i < 32) {
      arr(i) = ActionDrop
      i += 1
    }
    arr('\t') = ActionKeep
    arr('\n') = ActionKeep
    arr('\r') = ActionKeep
    arr('&') = ActionAmp
    arr('<') = ActionLt
    arr('>') = ActionGt
    arr('"') = ActionQuot
    arr
  }

  /** Escapes `text` into XmlStream2's byte buffer, in UTF-8. */
  def escape(text: String, out: XmlStream2.Out): Unit = {
    val len = text.length
    if (len == 0) return

    // Room for 6 bytes per char: the longest entity (`&quot;`), and more than UTF-8's 3 bytes per char.
    val maxUnchecked = XmlStream2.ChunkSize / 6 // 1,365 chars for an 8,192-byte buffer
    if (len <= (XmlStream2.ChunkSize - out.pos) / 6) {
      escapeUnchecked(text, len, out)
    } else if (len <= maxUnchecked) {
      out.flush()
      escapeUnchecked(text, len, out)
    } else {
      escapeStreaming(text, len, out)
    }
  }

  // (The scans test `c < 128` and then look the char up. Testing `c > '>'` first, to skip most chars with one
  // comparison, measured ~35% slower on a page with a lot of JavaScript in attributes: that branch is mispredicted
  // whenever letters and digits / punctuation alternate, while `c < 128` is almost always true.)

  /**
   * Zero-bounds-check loop: caller guarantees `(ChunkSize - out.pos) >= text.length * 6`,
   * so even worst-case expansion (`"` -> `&quot;`, or 3 UTF-8 bytes per char) cannot overflow `out.buf`.
   *
   * One pass: each char is written (as a byte, an entity, or encoded to UTF-8) as it's read. (Scanning for the next
   * escape and then copying the run in between, with String.getBytes, measured ~25% slower: most runs between
   * escapes, e.g. in JavaScript in attributes, are a few chars, so each char was read twice plus a call per run.)
   */
  private def escapeUnchecked(text: String, len: Int, out: XmlStream2.Out): Unit = {
    val buf = out.buf
    val actions = EscapeActions
    var p = out.pos
    var i = 0
    while (i < len) {
      val c = text.charAt(i)
      if (c < 128) {
        (actions(c): @switch) match {
          case ActionKeep =>
            buf(p) = c.toByte
            p += 1
          case ActionAmp =>
            buf(p) = '&'; buf(p + 1) = 'a'; buf(p + 2) = 'm'; buf(p + 3) = 'p'; buf(p + 4) = ';'
            p += 5
          case ActionLt =>
            buf(p) = '&'; buf(p + 1) = 'l'; buf(p + 2) = 't'; buf(p + 3) = ';'
            p += 4
          case ActionGt =>
            buf(p) = '&'; buf(p + 1) = 'g'; buf(p + 2) = 't'; buf(p + 3) = ';'
            p += 4
          case ActionQuot =>
            buf(p) = '&'; buf(p + 1) = 'q'; buf(p + 2) = 'u'; buf(p + 3) = 'o'; buf(p + 4) = 't'; buf(p + 5) = ';'
            p += 6
          case _ => // ActionDrop
        }
      } else {
        // Not ASCII: this char (or surrogate pair) encoded.
        val end = if (Character.isHighSurrogate(c) && i + 1 < len) i + 2 else i + 1
        p = XmlStream2.encodeUtf8(text, i, end, buf, p)
        i = end - 1
      }
      i += 1
    }
    out.pos = p
  }

  /** Streaming path for large text nodes (> 1,365 chars): copies runs in bulk and writes entities directly. */
  private def escapeStreaming(text: String, len: Int, out: XmlStream2.Out): Unit = {
    val actions = EscapeActions
    var runStart = 0
    var i = 0

    while (i < len) {
      val c = text.charAt(i)
      val action = if (c < 128) actions(c) else ActionKeep
      if (action != ActionKeep) {
        if (i > runStart) out.append(text, runStart, i)
        if (action != ActionDrop) {
          if (out.pos + 6 > XmlStream2.ChunkSize) out.flush()
          val buf = out.buf
          var p = out.pos
          (action: @switch) match {
            case ActionAmp =>
              buf(p) = '&'; buf(p + 1) = 'a'; buf(p + 2) = 'm'; buf(p + 3) = 'p'; buf(p + 4) = ';'
              p += 5
            case ActionLt =>
              buf(p) = '&'; buf(p + 1) = 'l'; buf(p + 2) = 't'; buf(p + 3) = ';'
              p += 4
            case ActionGt =>
              buf(p) = '&'; buf(p + 1) = 'g'; buf(p + 2) = 't'; buf(p + 3) = ';'
              p += 4
            case ActionQuot =>
              buf(p) = '&'; buf(p + 1) = 'q'; buf(p + 2) = 'u'; buf(p + 3) = 'o'; buf(p + 4) = 't'; buf(p + 5) = ';'
              p += 6
            case _ =>
          }
          out.pos = p
        }
        runStart = i + 1
      }
      i += 1
    }

    if (len > runStart) out.append(text, runStart, len)
  }

  /** Fallback for non-`Out` `StringBuilderLike` targets. */
  private def escapeGeneric(text: String, out: StringBuilderLike): Unit = {
    val len = text.length
    val actions = EscapeActions
    var runStart = 0
    var i = 0

    while (i < len) {
      val c = text.charAt(i)
      val action = if (c < 128) actions(c) else ActionKeep
      if (action != ActionKeep) {
        if (i > runStart) out.append(text, runStart, i)
        (action: @switch) match {
          case ActionAmp => out.append("&amp;")
          case ActionLt => out.append("&lt;")
          case ActionGt => out.append("&gt;")
          case ActionQuot => out.append("&quot;")
          case _ => // ActionDrop
        }
        runStart = i + 1
      }
      i += 1
    }

    if (len > runStart) out.append(text, runStart, len)
  }
}