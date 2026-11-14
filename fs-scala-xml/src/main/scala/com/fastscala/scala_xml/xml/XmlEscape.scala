package com.fastscala.scala_xml.xml

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

  /** Text escaped in slices of at least this many chars: rather than a tiny one, the buffer is written out first. */
  private final val MinSlice = 64

  /**
   * Escapes `text` into XmlJsStream's byte buffer, in UTF-8, as Utility.escape does (`&`, `<`, `>` and `"` become
   * entities; control chars other than \n, \r and \t are dropped) and then UTF-8 encoding (a lone surrogate is `?`).
   *
   * In slices that surely fit in the buffer (6 bytes per char: the longest entity, `&quot;`, and more than UTF-8's 3
   * bytes per char), each written without any bounds check; usually the whole text is one slice.
   */
  def escape(text: String, out: XmlJsStream.Out): Unit = {
    val len = text.length
    var i = 0
    while (i < len) {
      var room = (XmlJsStream.ChunkSize - out.pos) / 6
      if (room < len - i && room < MinSlice) {
        out.flush()
        room = XmlJsStream.ChunkSize / 6 // 1,365 chars for an 8,192-byte buffer
      }
      i = escapeSlice(text, i, math.min(len, i + room), out)
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
  private def escapeSlice(text: String, from: Int, to: Int, out: XmlJsStream.Out): Int = {
    val buf = out.buf
    val actions = EscapeActions
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
          case _ => // ActionDrop
        }
      } else if (Character.isHighSurrogate(c)) {
        // Its low surrogate may come after chars that are dropped: they're removed by the escaping, before the UTF-8
        // encoding (as toString's output is encoded), so then it's a pair. Otherwise it's a lone surrogate: '?'.
        var j = i + 1
        while (j < len && {
          val d = text.charAt(j); d < 128 && actions(d) == ActionDrop
        }) j += 1
        if (j < len && Character.isLowSurrogate(text.charAt(j))) {
          val cp = Character.toCodePoint(c, text.charAt(j))
          buf(p) = (0xF0 | (cp >> 18)).toByte
          buf(p + 1) = (0x80 | ((cp >> 12) & 0x3F)).toByte
          buf(p + 2) = (0x80 | ((cp >> 6) & 0x3F)).toByte
          buf(p + 3) = (0x80 | (cp & 0x3F)).toByte
          p += 4 // (4 bytes for 2 or more chars: within the room for this one)
          i = j
        } else {
          buf(p) = '?'
          p += 1
        }
      } else {
        p = XmlJsStream.encodeUtf8(text, i, i + 1, buf, p) // (a lone low surrogate: '?')
      }
      i += 1
    }
    out.pos = p
    i
  }
}
