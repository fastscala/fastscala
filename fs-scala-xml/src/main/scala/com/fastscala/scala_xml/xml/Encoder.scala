package com.fastscala.scala_xml.xml

import com.fastscala.js.JsEscape

/** A layer of encoding, applied to a String: XML (text or attribute value) or JavaScript (string literal) escaping. */
sealed trait EncodingLayer {

  /** `s` escaped by this layer: char by char, so a String can be escaped in pieces. */
  def encode(s: String): String
}

object EncodingLayer {

  /**
   * XML escaping, as Utility.escape (and so XmlEscape) does: `&`, `<`, `>` and `"` become entities; control chars other
   * than \n, \r and \t are dropped. (Surrogates are kept, paired or not: a pair split by dropped chars is a pair again.)
   */
  case object Xml extends EncodingLayer {
    override def encode(s: String): String = {
      val sb = new java.lang.StringBuilder(s.length + 16)
      var i = 0
      while (i < s.length) {
        val c = s.charAt(i)
        c match {
          case '&' => sb.append("&amp;")
          case '<' => sb.append("&lt;")
          case '>' => sb.append("&gt;")
          case '"' => sb.append("&quot;")
          case '\n' | '\r' | '\t' => sb.append(c)
          case _ if c < ' ' => // dropped
          case _ => sb.append(c)
        }
        i += 1
      }
      sb.toString
    }
  }

  /** JavaScript string literal escaping: [[JsEscape]] (as EncJs.cmd does). */
  case object Js extends EncodingLayer {
    override def encode(s: String): String = JsEscape.escape(s)
  }
}

/**
 * Applies `layers` (inside-out: the first is applied first) to raw Strings, and writes the result, in UTF-8, straight
 * into XmlJsStream's byte buffer (queuing it whenever it's full). Every layer escapes char by char, so the Strings
 * written one after the other are escaped as their concatenation would be.
 *
 * Not tuned for speed (each String goes through each layer as a String): what it writes is what's simple to check.
 */
final class Encoder private(val layers: List[EncodingLayer]) {

  def encode(s: String): String = layers.foldLeft(s)((acc, layer) => layer.encode(acc))

  def write(s: String, out: XmlJsStream.Out): Unit = Encoder.writeUtf8(encode(s), out)
}

object Encoder {

  private val cache = new java.util.concurrent.ConcurrentHashMap[List[EncodingLayer], Encoder]()

  def apply(layers: List[EncodingLayer]): Encoder = cache.computeIfAbsent(layers, l => new Encoder(l))

  /** The Encoders of 0 to 15 JavaScript layers (more are made when asked for). */
  private val jsEncoders: Array[Encoder] = Array.tabulate(16)(n => apply(List.fill(n)(EncodingLayer.Js)))

  /** `n` JavaScript layers (`n` EncJs's, one inside the other). */
  def js(n: Int): Encoder = if (n < jsEncoders.length) jsEncoders(n) else apply(List.fill(n)(EncodingLayer.Js))

  /** `s` in UTF-8 (a lone surrogate as `?`, as String.getBytes does), into `out`'s buffer, queuing it as it fills. */
  def writeUtf8(s: String, out: XmlJsStream.Out): Unit = {
    var i = 0
    while (i < s.length) {
      if (XmlJsStream.ChunkSize - out.pos < 4) out.queueFull()
      val n = if (Character.isHighSurrogate(s.charAt(i)) && i + 1 < s.length && Character.isLowSurrogate(s.charAt(i + 1))) 2 else 1
      out.pos = XmlJsStream.encodeUtf8(s, i, i + n, out.buf, out.pos)
      i += n
    }
  }
}
