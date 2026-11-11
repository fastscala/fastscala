package com.fastscala.scala_xml.utils

import org.scalatest.flatspec.AnyFlatSpec

import java.io.StringWriter
import scala.xml.*

/** In every constructor of [[ElemWithFastAttrsV1]], a transform returning null removes the attribute. */
class ElemWithFastAttrsV1Spec extends AnyFlatSpec {

  private type T = String => String

  private def stream(n: Node): String = { val w = new StringWriter; XmlStream.writeNode(n, w); w.toString }

  /** attrs never holds a null value nor a repeated name, and toString / XmlStream agree. */
  private def wellFormed(e: ElemWithFastAttrsV1): Unit = {
    assert(e.attrs.forall(_._2 ne null), e.attrs)
    assert(e.attrs.map(_._1).distinct.size == e.attrs.size, e.attrs)
    assert(stream(e) == e.toString)
  }

  private def names(e: Elem): Set[String] = e.attributes.toList.map(_.key).toSet

  private val remove: T = _ => null

  "(Elem, name, transform)" should "remove an existing attribute when the transform returns null" in {
    val e = new ElemWithFastAttrsV1(<p a="1" b="2"/>, "a", remove)
    assert(e.attrs.toMap == Map("b" -> "2"))
    assert(e.toString == """<p b="2"/>""")
    wellFormed(e)
  }

  it should "not add a missing attribute when the transform returns null" in {
    val e = new ElemWithFastAttrsV1(<p a="1"/>, "x", remove)
    assert(e.attrs.toMap == Map("a" -> "1"))
    wellFormed(e)
  }

  "(Elem, transforms)" should "remove existing attributes and not add missing ones when a transform returns null" in {
    val e = new ElemWithFastAttrsV1(<p a="1" b="2" c="3"/>, Seq("a" -> remove, "c" -> ((_: String) + "!"), "x" -> remove, "y" -> ((_: String) => "new")))
    assert(e.attrs.toMap == Map("b" -> "2", "c" -> "3!", "y" -> "new"))
    wellFormed(e)
  }

  "(ElemWithFastAttrsV1, name, transform)" should "remove the attribute when the transform returns null, wherever it is" in {
    val base = new ElemWithFastAttrsV1(<p a="1" b="2" c="3"/>, "d", (_: String) => "4")
    for (name <- Seq("a", "b", "c", "d")) { // "d" is the head of the list, the others further down
      val e = new ElemWithFastAttrsV1(base, name, remove)
      assert(names(e) == Set("a", "b", "c", "d") - name)
      assert(!e.attrs.exists(_._1 == name))
      wellFormed(e)
    }
    val missing = new ElemWithFastAttrsV1(base, "x", remove)
    assert(missing.attrs.toMap == base.attrs.toMap)
    wellFormed(missing)
  }

  it should "remove it from a single-attribute and from an empty list" in {
    val one = new ElemWithFastAttrsV1(<p/>, "a", (_: String) => "1")
    val none = new ElemWithFastAttrsV1(one, "a", remove)
    assert(none.attrs == Nil)
    assert(none.toString == "<p/>")
    val stillNone = new ElemWithFastAttrsV1(none, "a", remove)
    assert(stillNone.attrs == Nil)
    wellFormed(stillNone)
  }

  "(ElemWithFastAttrsV1, transforms)" should "remove existing attributes and not add missing ones when a transform returns null" in {
    val base = new ElemWithFastAttrsV1(<p a="1" b="2"/>, "c", (_: String) => "3")
    val e = new ElemWithFastAttrsV1(base, Seq("a" -> remove, "c" -> remove, "b" -> ((_: String) + "!"), "x" -> remove, "y" -> ((_: String) => "new")))
    assert(e.attrs.toMap == Map("b" -> "2!", "y" -> "new"))
    wellFormed(e)
  }

  "A transform after a removal" should "see \"\", in every constructor" in {
    def seenAfterRemoval(apply: (ElemWithFastAttrsV1, T) => ElemWithFastAttrsV1): String = {
      var seen = "unset"
      val removed = new ElemWithFastAttrsV1(new ElemWithFastAttrsV1(<p a="1"/>, "a", (_: String) + "2"), "a", remove)
      val e = apply(removed, s => { seen = s; s + "x" })
      assert(e.attrs.toMap == Map("a" -> "x"))
      wellFormed(e)
      seen
    }
    assert(seenAfterRemoval((e, t) => new ElemWithFastAttrsV1(e, "a", t)) == "")
    assert(seenAfterRemoval((e, t) => new ElemWithFastAttrsV1(e, Seq("a" -> t))) == "")
    assert(seenAfterRemoval((e, t) => new ElemWithFastAttrsV1(e: Elem, "a", t)) == "")
    assert(seenAfterRemoval((e, t) => new ElemWithFastAttrsV1(e: Elem, Seq("a" -> t))) == "")
  }

  "Every constructor" should "call each transform exactly once" in {
    var calls = 0
    val t: T = s => { calls += 1; s + "!" }
    val plain = <p a="1"/>
    val fast = new ElemWithFastAttrsV1(plain, "b", (_: String) => "2")
    for ((name, make) <- Seq[(String, () => Elem)](
      "Elem, existing" -> (() => new ElemWithFastAttrsV1(plain, "a", t)),
      "Elem, missing" -> (() => new ElemWithFastAttrsV1(plain, "x", t)),
      "Elem, Seq" -> (() => new ElemWithFastAttrsV1(plain, Seq("a" -> t))),
      "V1, head" -> (() => new ElemWithFastAttrsV1(fast, "b", t)),
      "V1, further down" -> (() => new ElemWithFastAttrsV1(fast, "a", t)),
      "V1, missing" -> (() => new ElemWithFastAttrsV1(fast, "x", t)),
      "V1, empty" -> (() => new ElemWithFastAttrsV1(new ElemWithFastAttrsV1(<p/>, "a", remove), "a", t)),
      "V1, Seq" -> (() => new ElemWithFastAttrsV1(fast, Seq("a" -> t))),
    )) {
      calls = 0
      make()
      assert(calls == 1, s"$name: called $calls times")
    }
  }
}
