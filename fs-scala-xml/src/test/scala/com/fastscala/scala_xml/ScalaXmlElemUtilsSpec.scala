package com.fastscala.scala_xml

import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.*

class ScalaXmlElemUtilsSpec extends AnyFlatSpec {

  /** The previous implementation: one withAttr call per pair. */
  private def reference(elem: Elem, attrs: (String, String)*): Elem =
    attrs.foldLeft(elem)((acc, kv) => acc.withAttr(kv))

  private def same(a: Elem, b: Elem): Boolean =
    a.toString == b.toString && a.attributes.toList.map(_.key) == b.attributes.toList.map(_.key)

  private val pfx = new PrefixedAttribute("xlink", "href", "old", Null)

  private val cases: Seq[(String, Elem, Seq[(String, String)])] = Seq(
    ("no attributes, new ones appended in order", <div/>, Seq("a" -> "1", "b" -> "2")),
    ("existing attribute replaced in place", <div a="x" b="y" c="z"/>, Seq("b" -> "2")),
    ("mix of replaced and appended", <div a="x" c="z">t</div>, Seq("d" -> "4", "a" -> "1", "e" -> "5", "c" -> "3")),
    ("last value for a repeated name wins", <div/>, Seq("a" -> "1", "b" -> "2", "a" -> "3")),
    ("repeated name that already exists", <div a="x"/>, Seq("a" -> "1", "a" -> "2")),
    ("prefixed attribute with the same key replaced", Elem(null, "a", pfx, TopScope, true), Seq("href" -> "new")),
    ("value with several nodes replaced", Elem(null, "p", new UnprefixedAttribute("a", Seq(Text("x"), EntityRef("amp")), Null), TopScope, true), Seq("a" -> "new", "b" -> "1")),
    ("special characters", <div/>, Seq("title" -> "a \"q\" & <b>")),
    ("children, scope and minimizeEmpty preserved", <div a="x"><span/>text</div>, Seq("a" -> "y")),
  )

  cases.foreach { case (name, elem, attrs) =>
    "withAttrs" should s"match applying withAttr one by one ($name)" in {
      val actual = elem.withAttrs(attrs *)
      val expected = reference(elem, attrs *)
      assert(same(actual, expected), s"\n  got:      $actual\n  expected: $expected")
      assert(actual.child == elem.child)
      assert(actual.minimizeEmpty == elem.minimizeEmpty)
    }
  }

  it should "return the element itself when given no attributes" in {
    val elem = <div a="x"/>
    assert(elem.withAttrs() eq elem)
  }

  it should "not modify the original element" in {
    val elem = <div a="x"/>
    elem.withAttrs("a" -> "1", "b" -> "2")
    assert(elem.toString == """<div a="x"/>""")
  }

  private def multiNode(attr: String): Elem =
    Elem(null, "p", new UnprefixedAttribute(attr, Seq(Text("a"), EntityRef("amp"), Text("b")), Null), TopScope, true)

  it should "replace a value made of several nodes" in {
    assert(multiNode("x").withAttrs("x" -> "new").attr("x") == Some("new"))
  }

  "attributeTransform" should "transform a value made of several nodes, seeing it as its text" in {
    var seen: Option[String] = None
    val result = multiNode("x").attributeTransform("x", existing => { seen = existing; existing.get + "!" })
    assert(seen == Some("a&b"))
    assert(result.attr("x") == Some("a&b!"))
    assert(result.toString == """<p x="a&amp;b!"/>""")
  }

  it should "pass None for a missing attribute and append it" in {
    var seen: Option[String] = Some("unset")
    val result = <p a="1"/>.attributeTransform("x", existing => { seen = existing; "v" })
    assert(seen.isEmpty)
    assert(result.toString == """<p a="1" x="v"/>""")
  }

  "addClass" should "append to a class value made of several nodes" in {
    assert(multiNode("class").addClass("c").getClassAttr == "a&b c")
  }

  // ---------- getters ----------

  private val special = <div id="i&amp;d" class="a &lt;b&gt;" style="content: &quot;x&quot;" title="t &amp; u"/>

  "attr" should "return the unescaped value" in {
    assert(special.attr("title") == Some("t & u"))
  }

  it should "find prefixed attributes by key, and return None when missing" in {
    assert(Elem(null, "a", pfx, TopScope, true).attr("href") == Some("old"))
    assert(special.attr("missing") == None)
  }

  it should "concatenate a value made of several nodes" in {
    assert(multiNode("x").attr("x") == Some("a&b"))
  }

  "getClassAttr / getStyleAttr / getId / getIdOpt" should "return the unescaped values" in {
    assert(special.getClassAttr == "a <b>")
    assert(special.getStyleAttr == "content: \"x\"")
    assert(special.getId == "i&d")
    assert(special.getIdOpt == Some("i&d"))
  }

  they should "default when the attribute is missing" in {
    assert(<div/>.getClassAttr == "")
    assert(<div/>.getStyleAttr == "")
    assert(<div/>.getIdOpt == None)
  }

  "getters and setters" should "round-trip without double escaping" in {
    val elem = <div/>.withClass("a & b").addClass("c").withAttr("title" -> "x < y")
    assert(elem.getClassAttr == "a & b c")
    assert(elem.attr("title") == Some("x < y"))
    assert(elem.addClass(elem.getClassAttr).toString == """<div class="a &amp; b c a &amp; b c" title="x &lt; y"/>""")
  }
}
