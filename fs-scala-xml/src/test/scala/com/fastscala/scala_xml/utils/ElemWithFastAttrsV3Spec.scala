package com.fastscala.scala_xml.utils

import org.scalatest.flatspec.AnyFlatSpec

import java.io.StringWriter
import scala.xml.*

class ElemWithFastAttrsV3Spec extends AnyFlatSpec {

  private type L = List[String]

  private def stream(n: Node): String = { val w = new StringWriter; XmlStream.writeNode(n, w); w.toString }

  private def attr(e: Elem, name: String): Option[String] = e.attributes.get(name).map(_.text)

  /** Like ScalaXmlElemUtils.addClass: pieces are stored newest first, so appending is a prepend. */
  private def addClass(cls: String): L => L = (pieces: L) => (" " + cls) :: pieces

  "A list transform" should "see Nil for a missing attribute, and its pieces be concatenated oldest first" in {
    var seen: L = List("unset")
    val e = new ElemWithFastAttrsV3(<div/>, "class", (pieces: L) => { seen = pieces; List("b", " ", "a") ::: pieces })
    assert(seen == Nil)
    assert(e.attrs == Map("class" -> List("b", " ", "a")))
    assert(attr(e, "class") == Some("a b"))
    assert(e.toString == """<div class="a b"/>""")
    assert(stream(e) == e.toString)
  }

  it should "see an existing attribute as a single piece" in {
    var seen: L = Nil
    val e = new ElemWithFastAttrsV3(<div class="x &amp; y"/>, "class", (pieces: L) => { seen = pieces; pieces })
    assert(seen == List("x & y"))
    assert(attr(e, "class") == Some("x & y"))
  }

  it should "append pieces across a chain without joining them, newest first" in {
    val e = new ElemWithFastAttrsV3(new ElemWithFastAttrsV3(<td/>, "class", addClass("px-2")), "class", addClass("py-1"))
    assert(e.attrs("class") == List(" py-1", " px-2"))
    assert(e.toString == """<td class=" px-2 py-1"/>""")
    assert(stream(e) == e.toString)
  }

  it should "keep an existing value as the oldest piece" in {
    val e = new ElemWithFastAttrsV3(<td class="a"/>, "class", addClass("b"))
    assert(e.attrs("class") == List(" b", "a"))
    assert(e.toString == """<td class="a b"/>""")
    assert(stream(e) == e.toString)
  }

  it should "remove the attribute when returning an empty list" in {
    val e = new ElemWithFastAttrsV3(<p a="1" b="2"/>, "a", (_: L) => Nil)
    assert(e.attrs == Map("b" -> List("2")))
    assert(attr(e, "a").isEmpty)
    assert(e.toString == """<p b="2"/>""")
    assert(new ElemWithFastAttrsV3(e, Seq("b" -> ((_: L) => Nil))).toString == "<p/>")
  }

  it should "not add a missing attribute when returning an empty list" in {
    val e = new ElemWithFastAttrsV3(<p a="1"/>, "x", (pieces: L) => pieces)
    assert(e.attrs == Map("a" -> List("1")))
    assert(e.toString == """<p a="1"/>""")
  }

  it should "keep an attribute whose only piece is empty" in {
    val e = new ElemWithFastAttrsV3(<p/>, "x", (_: L) => List(""))
    assert(e.toString == """<p x=""/>""")
  }

  "A String transform" should "see \"\" for a missing attribute" in {
    var seen = "unset"
    val e = new ElemWithFastAttrsV3(<p a="1"/>, "x", (s: String) => { seen = s; "v" })
    assert(seen == "")
    assert(e.attrs == Map("a" -> List("1"), "x" -> List("v")))
  }

  it should "see the pieces concatenated oldest first, and its result become the only piece" in {
    val listed = new ElemWithFastAttrsV3(new ElemWithFastAttrsV3(<div/>, "class", addClass("btn")), "class", addClass("ms-2"))
    var seen = ""
    val e = new ElemWithFastAttrsV3(listed, "class", (s: String) => { seen = s; s + "!" })
    assert(seen == " btn ms-2")
    assert(e.attrs("class") == List(" btn ms-2!"))
  }

  it should "remove the attribute when returning null" in {
    val e = new ElemWithFastAttrsV3(<p a="1" b="2"/>, "a", (_: String) => null)
    assert(e.attrs == Map("b" -> List("2")))
    assert(new ElemWithFastAttrsV3(e, "b", (_: String) => null).toString == "<p/>")
  }

  "The Seq constructors" should "apply each transform, for both kinds" in {
    val strings = new ElemWithFastAttrsV3(<p a="1"/>, Seq("a" -> ((s: String) => s + "2"), "b" -> ((_: String) => "3")))
    assert(strings.attrs == Map("a" -> List("12"), "b" -> List("3")))
    val lists = new ElemWithFastAttrsV3(strings, Seq("a" -> ((p: L) => "x" :: p), "c" -> ((p: L) => "y" :: p)))
    assert(lists.attrs == Map("a" -> List("x", "12"), "b" -> List("3"), "c" -> List("y")))
    assert(attr(lists, "a") == Some("12x"))
    assert(stream(lists) == lists.toString)
  }

  "joined" should "concatenate the pieces oldest first" in {
    assert(ElemWithFastAttrsV3.joined(Nil) == "")
    assert(ElemWithFastAttrsV3.joined(List("a")) == "a")
    assert(ElemWithFastAttrsV3.joined(List("c", "b", "a")) == "abc")
  }

  "An ElemWithFastAttrsV3" should "keep prefix, label, scope, minimizeEmpty and children" in {
    val orig = <x:a xmlns:x="urn:x"><b/>t</x:a>
    val e = new ElemWithFastAttrsV3(orig, "k", (_: String) => "v")
    assert((e.prefix, e.label, e.scope, e.minimizeEmpty, e.child) == (orig.prefix, orig.label, orig.scope, orig.minimizeEmpty, orig.child))
  }

  it should "compute the attributes once, lazily (not for XmlStream), and print the same with XmlStream as toString" in {
    val e = new ElemWithFastAttrsV3(new ElemWithFastAttrsV3(<div id="i"/>, "class", addClass("a")), "title", (_: String) => "x < \"y\"")
    // XmlStream writes from attrs, so the MetaData isn't computed yet. (Checked through the cache rather than the
    // global Computations counter, which other suites running in parallel also increment.)
    val streamed = stream(e)
    val cachedField = ElemWithFastAttrsV3.UnprefixedAttributeClass.getField("cached")
    assert(cachedField.get(e.attributes) == null)
    val s1 = e.toString
    val computed = e.attributes.next
    assert(cachedField.get(e.attributes) eq computed)
    val s2 = e.toString + stream(e)
    assert(e.attributes.next eq computed) // computed once, then reused
    assert(streamed == s1)
    assert(s2 == s1 * 2)
    assert(s1.contains(""" class=" a"""") && s1.contains(""" title="x &lt; &quot;y&quot;"""") && s1.contains(""" id="i""""), s1)
    assert(e.attributes.length == 3)
  }
}
