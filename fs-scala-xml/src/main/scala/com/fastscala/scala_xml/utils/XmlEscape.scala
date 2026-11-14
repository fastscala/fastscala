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

  /** Fast path when the caller statically holds `XmlStream2.Out`. */
  def escape(text: String, out: XmlStream2.Out): Unit = {
    val len = text.length
    if (len == 0) return

    val maxUnchecked = XmlStream2.ChunkSize / 6 // 1,365 chars for 8,192-char buffer
    if (len <= (XmlStream2.ChunkSize - out.pos) / 6) {
      escapeUnchecked(text, len, out)
    } else if (len <= maxUnchecked) {
      out.flush()
      escapeUnchecked(text, len, out)
    } else {
      escapeStreaming(text, len, out)
    }
  }

  /** Polymorphic entry point for `StringBuilderLike` (e.g., `StringBuilder` or `Out`). */
  def escape(text: String, out: StringBuilderLike): Unit = out match {
    case direct: XmlStream2.Out => escape(text, direct)
    case _ => escapeGeneric(text, out)
  }

  /**
   * Zero-bounds-check loop: caller guarantees `(ChunkSize - out.pos) >= text.length * 6`,
   * so even worst-case expansion (`"` -> `&quot;`) cannot overflow `out.buf`.
   */
  // (The scans test `c < 128` and then look the char up. Testing `c > '>'` first, to skip most chars with one
  // comparison, measured ~35% slower on a page with a lot of JavaScript in attributes: that branch is mispredicted
  // whenever letters and digits / punctuation alternate, while `c < 128` is almost always true.)
  private def escapeUnchecked(text: String, len: Int, out: XmlStream2.Out): Unit = {
    val buf = out.buf
    val actions = EscapeActions
    var p = out.pos
    var runStart = 0
    var i = 0

    while (i < len) {
      val c = text.charAt(i)
      val action = if (c < 128) actions(c) else ActionKeep
      if (action != ActionKeep) {
        val runLen = i - runStart
        if (runLen == 1) {
          buf(p) = text.charAt(runStart)
          p += 1
        } else if (runLen > 1) {
          text.getChars(runStart, i, buf, p)
          p += runLen
        }

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
          case _ => // ActionDrop
        }
        runStart = i + 1
      }
      i += 1
    }

    val tailLen = len - runStart
    if (tailLen == 1) {
      buf(p) = text.charAt(runStart)
      p += 1
    } else if (tailLen > 1) {
      text.getChars(runStart, len, buf, p)
      p += tailLen
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
              buf(p) = '&';
              buf(p + 1) = 'a';
              buf(p + 2) = 'm';
              buf(p + 3) = 'p';
              buf(p + 4) = ';'
              p += 5
            case ActionLt =>
              buf(p) = '&';
              buf(p + 1) = 'l';
              buf(p + 2) = 't';
              buf(p + 3) = ';'
              p += 4
            case ActionGt =>
              buf(p) = '&';
              buf(p + 1) = 'g';
              buf(p + 2) = 't';
              buf(p + 3) = ';'
              p += 4
            case ActionQuot =>
              buf(p) = '&';
              buf(p + 1) = 'q';
              buf(p + 2) = 'u';
              buf(p + 3) = 'o';
              buf(p + 4) = 't';
              buf(p + 5) = ';'
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