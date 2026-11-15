package com.fastscala.js

import org.owasp.encoder.Encode
import org.scalatest.flatspec.AnyFlatSpec

import java.io.StringWriter

class JsEscapeSpec extends AnyFlatSpec {

  /** OWASP's escaping, without its rule for the input's last char: as if more followed it ("a", never escaped). */
  private def owaspNotLast(s: String): String = Encode.forJavaScript(s + "a").dropRight(1)

  private val allChars: Seq[String] = (0 until 0x10000).map(c => c.toChar.toString) // (lone surrogates too)

  "escape" should "escape every char as OWASP does (when it's not the input's last)" in {
    for (c <- allChars) {
      assert(JsEscape.escape(c) == owaspNotLast(c), f"U+${c.charAt(0).toInt}%04X")
      assert(JsEscape.escape("x" + c + "y") == owaspNotLast("x" + c + "y"), f"U+${c.charAt(0).toInt}%04X")
    }
  }

  it should "not escape the last char differently (OWASP hex-escapes a letter of \"script\")" in {
    for (c <- "cCiIpPrRsStT") {
      assert(JsEscape.escape(c.toString) == c.toString)
      assert(JsEscape.escape("ab" + c) == "ab" + c)
    }
    assert(JsEscape.escape("</script") == "\\x3c\\/script")
  }

  it should "keep surrogate pairs, and escape lone surrogates" in {
    assert(JsEscape.escape("a😀b") == "a😀b")
    assert(JsEscape.escape("a\uD83Db") == "a\\ud83db")
    assert(JsEscape.escape("a\uDE00") == "a\\ude00")
    assert(JsEscape.escape("\uD83D😀") == "\\ud83d😀")
    assert(JsEscape.escape("\uDE00\uD83D") == "\\ude00\\ud83d")
  }

  it should "write null as \"null\" (as OWASP does)" in {
    assert(JsEscape.escape(null) == Encode.forJavaScript(null))
  }

  it should "return the String itself when nothing in it is escaped" in {
    val s = "abcé😀"
    assert(JsEscape.escape(s) eq s)
  }

  it should "be per char: a String's escaping is its parts' escapings, one after the other" in {
    val rnd = new scala.util.Random(7)
    val alphabet = "sta<>/\\\"' \t\né€-{} \u0085"
    for (_ <- 1 to 2000) {
      val a = Seq.fill(rnd.nextInt(6))(alphabet(rnd.nextInt(alphabet.length))).mkString
      val b = Seq.fill(rnd.nextInt(6))(alphabet(rnd.nextInt(alphabet.length))).mkString
      assert(JsEscape.escape(a + b) == JsEscape.escape(a) + JsEscape.escape(b), (a, b))
    }
  }

  "EscapingWriter" should "write what escape returns, however the input is split into writes" in {
    val rnd = new scala.util.Random(11)
    val alphabet = Seq("s", "a", "<", "\\", "\"", " ", "é", "€", "😀", "\uD83D", "\uDE00", " ", "\u0000")
    for (_ <- 1 to 2000) {
      val s = Seq.fill(rnd.nextInt(12))(alphabet(rnd.nextInt(alphabet.size))).mkString
      val out = new StringWriter()
      val w = new JsEscape.EscapingWriter(out)
      var i = 0
      while (i < s.length) { // (splitting surrogate pairs too)
        val n = math.min(s.length - i, 1 + rnd.nextInt(4))
        w.write(s.toCharArray, i, n)
        i += n
      }
      w.flush()
      assert(out.toString == JsEscape.escape(s), s.map(c => f"U+${c.toInt}%04X").mkString(" "))
    }
  }
}
