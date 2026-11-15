package com.fastscala.scala_xml.xml

import com.fastscala.js.{CompositeJs, Js}
import com.fastscala.scala_xml.testutils.AttributeOrder.normalized
import com.fastscala.scala_xml.testutils.ElemUtilsV1.RichElem
import com.fastscala.scala_xml.testutils.XmlStreamTestUtils.writtenNode
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.*

/**
 * Attributes whose value is a [[Js]] in an [[ElemOptimized]]: kept as a Js by addOnClick (a CompositeJs of the
 * existing value and the new one), read as text where text is needed, and written by XmlJsStream part by part.
 */
class JsAttributesSpec extends AnyFlatSpec {

  private def value(e: Elem, name: String): Any = e match {
    case fast: ElemOptimized => fast.attrs.find(_._1 == name).map(_._2).orNull
    case _ => fail(s"not an ElemOptimized: ${e.getClass}")
  }

  /** XmlJsStream writes it as toString does (attributes in any order). */
  private def assertWrittenAsToString(e: Elem): Unit = {
    val out = writtenNode(e)
    assert(normalized(out) == normalized(e.toString), s"\n  XmlJsStream: $out\n  toString:  ${e.toString}")
  }

  "addOnClick" should "keep the onclick a Js, with the same text as before (existing + \";\" + js)" in {
    val e = <button/>.addOnClick(Js("a()")).addOnClick(Js("b()"))
    assert(value(e, "onclick").isInstanceOf[CompositeJs])
    assert(e.attr("onclick").contains(";a();b()"))
    assert(e.toString == """<button onclick=";a();b()"/>""")
    assertWrittenAsToString(e)
  }

  it should "build on an existing onclick that's a String (from the literal, or set as text)" in {
    val fromLiteral = <button onclick="x()"/>.addOnClick(Js("a()"))
    assert(value(fromLiteral, "onclick").isInstanceOf[Js])
    assert(fromLiteral.attr("onclick").contains("x();a()"))
    val fromText = <button/>.withAttr("onclick" -> "y()").addOnClick(Js("b()"))
    assert(value(fromText, "onclick").isInstanceOf[Js])
    assert(fromText.attr("onclick").contains("y();b()"))
    assertWrittenAsToString(fromLiteral)
    assertWrittenAsToString(fromText)
  }

  it should "keep a Js when given a String" in {
    val e = <a/>.addOnClick("f()").addOnClick(Js("g()"))
    assert(value(e, "onclick").isInstanceOf[Js])
    assert(e.attr("onclick").contains(";f();g()"))
  }

  "A Js attribute" should "stay a Js through transforms of other attributes" in {
    val e = <button/>.addOnClick(Js("a()")).addClass("btn").withId("b1").withStyle("color: red")
    assert(value(e, "onclick").isInstanceOf[Js])
    assert(e.getAttrs.toMap.get("onclick").contains(";a()"))
    assertWrittenAsToString(e)
  }

  it should "become its text when transformed as text" in {
    val e = <button/>.addOnClick(Js("a()")).withAttr("onclick")(_ + "!")
    assert(value(e, "onclick") == ";a()!")
    val removed = <button/>.addOnClick(Js("a()")).withAttr("onclick" -> null)
    assert(removed.attr("onclick").isEmpty && removed.toString == "<button/>")
  }

  it should "be read as text by the getters" in {
    val e = <button id="i"/>.addOnClick(Js("a(\"x\") && b < c"))
    assert(e.attr("onclick").contains(";a(\"x\") && b < c"))
    assert(e.getAttrs.toMap.get("onclick").contains(";a(&quot;x&quot;) &amp;&amp; b &lt; c")) // (escaped, as printed)
    assert(e.hasAttribute("onclick", ";a(&quot;x&quot;) &amp;&amp; b &lt; c"))
  }

  it should "be written by XmlJsStream as by toString: escaped, every kind of Js, any nesting" in {
    val cases: Seq[Js] = Seq(
      Js("alert(\"a & b < c > d\")"),
      CompositeJs.joined(Seq(Js("a()"), Js("b(\"q\")"), Js("c()")), Js(";")),
      Js("a()") & Js("b()") & Js.Void & Js("if (x < 1 && y > 2) z()"),
      CompositeJs.joined(Seq(CompositeJs.joined(Seq(Js("é"), Js("€")), Js("|")), Js("😀")), Js(";")),
      new com.fastscala.js.JsFunc1(arg => Js(s"f(${arg.cmd}, '\"')")),
      Js.Void,
      new com.fastscala.js.EncJs(new com.fastscala.scala_xml.js.NodeSeqJs(<p class="c" title="t">{"Hello & <bye> \"q\" é"}</p>)), // (no parts: its cmd)
    )
    cases.foreach { js =>
      val e = <button class="b"/>.addOnClick(js)
      assert(value(e, "onclick").isInstanceOf[Js])
      assertWrittenAsToString(e)
      assertWrittenAsToString(<div>{e}{<a/>.addOnClick(js).addOnClick(js)}</div>)
    }
  }

  it should "be written without building the Js's cmd" in {
    val noCmd = new CompositeJs(Js("a(\"1\")"), Js("b()")) {
      override def cmd: String = throw new IllegalStateException("cmd was built")
    }
    val e = ElemOptimized("button", List("onclick" -> noCmd, "id" -> "x"))()
    // (attributes in the list's order; no children, and not minimized: an end tag)
    assert(writtenNode(e) == """<button onclick="a(&quot;1&quot;)b()" id="x"></button>""")
  }
}
