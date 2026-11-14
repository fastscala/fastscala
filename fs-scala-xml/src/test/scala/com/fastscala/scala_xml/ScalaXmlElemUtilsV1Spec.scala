package com.fastscala.scala_xml

import com.fastscala.scala_xml.testutils.ElemUtilsV1.RichElem
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.*

class ScalaXmlElemUtilsV1Spec extends AnyFlatSpec {

  /** The previous implementation: one withAttr call per pair. */
  private def reference(elem: Elem, attrs: (String, String)*): Elem =
    attrs.foldLeft(elem)((acc, kv) => acc.withAttr(kv))

  /** Same element, ignoring attribute order. */
  extension (e: Elem) private def attrs: Map[String, String] = e.attributes.toList.map(md => md.prefixedKey -> md.value.text).toMap

  private def same(a: Elem, b: Elem): Boolean = {
    def attrs(e: Elem) = e.attributes.toList.map(md => (md.prefixedKey, md.value.text)).toSet
    a.prefix == b.prefix && a.label == b.label && attrs(a) == attrs(b) && a.child == b.child &&
      a.attributes.length == b.attributes.length // no duplicates
  }

  private val pfx = new PrefixedAttribute("xlink", "href", "old", Null)

  // Names are unique within each call: for a name repeated in one withAttrs call, the resulting value is undefined.
  private val cases: Seq[(String, Elem, Seq[(String, String)])] = Seq(
    ("no attributes, new ones added", <div/>, Seq("a" -> "1", "b" -> "2")),
    ("existing attribute replaced", <div a="x" b="y" c="z"/>, Seq("b" -> "2")),
    ("mix of replaced and added", <div a="x" c="z">t</div>, Seq("d" -> "4", "a" -> "1", "e" -> "5", "c" -> "3")),
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

  it should "let a later call override an earlier one" in {
    assert(<div/>.withAttrs("a" -> "1").withAttrs("a" -> "3").attr("a") == Some("3"))
    assert(<div a="x"/>.withAttrs("a" -> "1").withAttrs("a" -> "3").attr("a") == Some("3"))
    assert(<div/>.withAttrs("a" -> "1", "b" -> "2").withAttrs("a" -> "3").attrs == Map("a" -> "3", "b" -> "2"))
    assert(<div/>.withAttrs("a" -> "1").withAttr("a" -> "3").attr("a") == Some("3"))
    assert(<div/>.withAttr("a" -> "1").withAttrs("a" -> "3").attr("a") == Some("3"))
    val chained = <div/>.withAttrs("a" -> "1").withAttrs("a" -> "3")
    assert(chained.attributes.length == 1, chained.toString)
    assert(chained.toString == """<div a="3"/>""")
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

  "withAttr(name)(transform)" should "transform a value made of several nodes, seeing it as its text" in {
    var seen: String = null
    val result = multiNode("x").withAttr("x")(existing => { seen = existing; existing + "!" })
    assert(result.attr("x") == Some("a&b!")) // transforms run when the attributes are first read
    assert(seen == "a&b")
    assert(result.toString == """<p x="a&amp;b!"/>""")
  }

  it should "pass \"\" for a missing attribute and append it" in {
    var seen: String = "unset"
    val result = <p a="1"/>.withAttr("x")(existing => { seen = existing; "v" })
    assert(result.attrs == Map("a" -> "1", "x" -> "v")) // attribute order doesn't matter
    assert(seen == "")
  }

  it should "remove the attribute when the transform returns null" in {
    val result = <p a="1" b="2"/>.withAttr("a")(_ => null)
    assert(result.toString == """<p b="2"/>""")
    assert(result.attr("a").isEmpty)
    assert(result.addClass("c").attr("a").isEmpty)
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
    val out = elem.addClass(elem.getClassAttr).toString // attribute order doesn't matter
    assert(out.contains("""class="a &amp; b c a &amp; b c""""), out)
    assert(out.contains("""title="x &lt; y""""), out)
  }
}
