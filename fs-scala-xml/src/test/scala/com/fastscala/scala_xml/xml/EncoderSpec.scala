package com.fastscala.scala_xml.xml

import com.fastscala.js.JsEscape
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets.UTF_8
import scala.xml.Utility

/**
 * [[Encoder]]: what it writes is the reference escaping (Utility.escape `xmlLevels` times, then JsEscape `jsLevels`
 * times, in UTF-8), whatever the buffer's size and however full it is: so with tiny buffers, where every escape and
 * every char of several bytes is written across a flush.
 */
class EncoderSpec extends AnyFlatSpec {

  import EncoderSpec.*

  /** Every (xmlLevels, jsLevels) with tables made at once, and some made when asked for. */
  private val levels: Seq[(Int, Int)] = (for (x <- 0 to 2; j <- 0 to 3) yield (x, j)) ++ Seq((3, 0), (3, 1), (0, 8), (1, 9))

  private def check(text: String, x: Int, j: Int, bufferSize: Int, pad: Int = 0): Unit = {
    val expected = reference(text, x, j)
    val what = s"${text.length} chars, xmlLevels=$x, jsLevels=$j, buffer of $bufferSize, after $pad bytes: ${codes(text)}"
    assert(java.util.Arrays.equals(expected, written(bufferSize, pad)(Encoder.write(text, _, Encoder.escaping(x, j)))), s"String: $what")
    assert(java.util.Arrays.equals(expected, written(bufferSize, pad)(Encoder.write(new java.lang.StringBuilder(text), _, Encoder.escaping(x, j)))), s"StringBuilder: $what")
    assert(java.util.Arrays.equals(expected, written(bufferSize, pad)(EncoderV1.write(text, _, x, j))), s"EncoderV1: $what")
  }

  private def codes(s: String): String = s.take(40).map(c => f"${c.toInt}%04x").mkString(" ") + (if (s.length > 40) " ..." else "")

  "Encoder.write" should "write the reference escaping of every char, alone and in text" in {
    for ((x, j) <- levels; c <- (0 until 0x10000).map(_.toChar)) {
      val expected = reference(c.toString, x, j)
      val out = written(64, 0)(Encoder.write(c.toString, _, Encoder.escaping(x, j)))
      if (!java.util.Arrays.equals(expected, out)) fail(f"U+${c.toInt}%04X, xmlLevels=$x, jsLevels=$j")
    }
    val bmp = (0 until 0x10000).map(_.toChar).mkString
    for ((x, j) <- levels) check(bmp, x, j, bufferSize = 8192)
  }

  it should "write the same with any buffer size, down to 8 bytes (everything written across flushes)" in {
    val text = "a<\"é€😀\\&\u2028\uD800z'\u0001\u0085>-/ {`$!" * 4
    for ((x, j) <- levels; size <- Seq(8, 9, 10, 11, 13, 16, 17, 31, 64, 100, 1024)) check(text, x, j, size)
  }

  it should "write the same at every offset of the buffer" in {
    val text = "tag <b class=\"x\">é€😀 & 'q' \\ \u2029</b>\uDC00"
    for ((x, j) <- levels; pad <- 0 to 70) check(text, x, j, bufferSize = 64, pad = pad)
  }

  it should "write surrogates as the reference does (pairs, lone ones, pairs split by chars XML drops)" in {
    val texts = Seq("\uD83D\uDE00", "\uD83D", "\uDE00", "\uD83D\uD83D\uDE00", "\uDE00\uD83D", "a\uD83D\u0001\uDE00b", "\uD83D\u0001\u0002", "\uD83Dx\uDE00",
      "x" * 7 + "\uD83D\uDE00", "x" * 6 + "\uD83D", "\uD83D\u0001")
    for ((x, j) <- levels; text <- texts; size <- Seq(8, 12, 64)) check(text, x, j, size)
  }

  it should "write Strings of every length (short ones are copied as they're checked; long ASCII ones in bulk)" in {
    for ((x, j) <- levels; n <- (0 to 70) ++ Seq(127, 128, 129, 1000, 9000); tail <- Seq("", "é", "<", "\"")) {
      check("abcdefghij".repeat(n / 10 + 1).take(n) + tail, x, j, bufferSize = 8192, pad = 8192 - 40)
      check("abcdefghij".repeat(n / 10 + 1).take(n) + tail, x, j, bufferSize = 256)
    }
  }

  it should "write random texts as the reference does" in {
    val rnd = new scala.util.Random(2026)
    val alphabet = Seq("a", "b", "z", "0", " ", "<", ">", "&", "\"", "'", "\\", "/", "-", "{", "`", "$", "!", "\n", "\t", "\u0000", "\u001f", "\u007f", "\u0085", "\u00a0", "é", "€", "\u2028", "\u2029",
      "😀", "\uD83D", "\uDE00", "class=\"btn btn-primary\"", "window._fs.callback('',\"p\",\"1\");")
    for (_ <- 1 to 4_000) {
      val text = Seq.fill(rnd.nextInt(30))(alphabet(rnd.nextInt(alphabet.size))).mkString
      val (x, j) = levels(rnd.nextInt(levels.size))
      check(text, x, j, bufferSize = Seq(8, 16, 64, 8192)(rnd.nextInt(4)), pad = rnd.nextInt(8))
    }
  }

  it should "write Strings one after the other as their concatenation" in {
    val parts = Seq("<div", " ", "class", "=\"", "a & b", "\"", ">", "é😀", "\uD83D", "\uDE00", "</div>")
    for ((x, j) <- levels; size <- Seq(8, 64, 8192)) {
      val out = written(size, 0)(o => parts.foreach(Encoder.write(_, o, Encoder.escaping(x, j))))
      // (Each String is escaped by itself: a pair split between two Strings is two lone surrogates.)
      assert(java.util.Arrays.equals(out, parts.flatMap(reference(_, x, j)).toArray), s"xmlLevels=$x, jsLevels=$j, buffer of $size")
    }
  }

  "Encoder.writeAscii" should "write a markup char as a String of it is written" in {
    for ((x, j) <- levels; c <- (0x20 until 0x7F).map(_.toChar); size <- Seq(8, 64); pad <- Seq(0, 5, 7)) {
      val out = written(size, pad)(Encoder.writeAscii(c, _, Encoder.escaping(x, j)))
      assert(java.util.Arrays.equals(reference(c.toString, x, j), out), s"'$c', xmlLevels=$x, jsLevels=$j, buffer of $size, after $pad")
    }
  }

  "Encoder" should "write escapes longer than the buffer (many JavaScript levels)" in {
    for (size <- Seq(8, 64, 8192); (x, j) <- Seq((0, 14), (1, 13), (2, 12))) {
      check("a\\\"b", x, j, size)
      val out = written(size, 3)(Encoder.writeAscii('"', _, Encoder.escaping(x, j)))
      assert(java.util.Arrays.equals(reference("\"", x, j), out))
    }
  }
}

object EncoderSpec {

  /** The reference: `Utility.escape` `xml` times, then JsEscape `js` times, in UTF-8 (a lone surrogate: `?`). */
  def reference(text: String, xml: Int, js: Int): Array[Byte] = {
    val xmlEscaped = (1 to xml).foldLeft(text)((acc, _) => Utility.escape(acc))
    (1 to js).foldLeft(xmlEscaped)((acc, _) => JsEscape.escape(acc)).getBytes(UTF_8)
  }

  /** An [[EncoderOutput]] with a buffer of `size` bytes, that keeps everything flushed. */
  final class CollectingOutput(size: Int) extends EncoderOutput {
    var buf = new Array[Byte](size)
    var pos = 0
    val bytes = new ByteArrayOutputStream()

    override def flush(): Unit = {
      bytes.write(buf, 0, pos)
      java.util.Arrays.fill(buf, 0x7E.toByte) // (so bytes written past `pos`, and not written again, would show)
      pos = 0
    }
  }

  /** What `write` writes into a buffer of `size` bytes with `pad` bytes in it already (not returned). */
  def written(size: Int, pad: Int)(write: EncoderOutput => Unit): Array[Byte] = {
    val out = new CollectingOutput(size)
    var i = 0
    while (i < pad) {
      if (out.pos == size) out.flush()
      out.buf(out.pos) = '#'
      out.pos += 1
      i += 1
    }
    write(out)
    out.flush()
    out.bytes.toByteArray.drop(pad)
  }
}
