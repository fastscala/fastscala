package com.fastscala.scala_xml.utils

import com.fastscala.scala_xml.testutils.AttributeOrder.normalized
import com.fastscala.scala_xml.testutils.XmlStreamTestUtils.writtenNode
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.*

/** In every constructor of [[ElemOptimized]], a transform returning null removes the attribute. */
class ElemOptimizedSpec extends AnyFlatSpec {

  private type T = String => String

  private def stream(n: Node): String = writtenNode(n)

  /** attrs never holds a null value nor a repeated name, and toString / XmlStream agree (attributes in any order). */
  private def wellFormed(e: ElemOptimized): Unit = {
    assert(e.attrs.forall(_._2 ne null), e.attrs)
    assert(e.attrs.map(_._1).distinct.size == e.attrs.size, e.attrs)
    assert(normalized(stream(e)) == normalized(e.toString))
  }

  private def names(e: Elem): Set[String] = e.attributes.toList.map(_.key).toSet

  private val remove: T = _ => null

  "(Elem, name, transform)" should "remove an existing attribute when the transform returns null" in {
    val e = new ElemOptimized(<p a="1" b="2"/>, "a", remove)
    assert(e.attrs.toMap == Map("b" -> "2"))
    assert(e.toString == """<p b="2"/>""")
    wellFormed(e)
  }

  it should "not add a missing attribute when the transform returns null" in {
    val e = new ElemOptimized(<p a="1"/>, "x", remove)
    assert(e.attrs.toMap == Map("a" -> "1"))
    wellFormed(e)
  }

  "(Elem, transforms)" should "remove existing attributes and not add missing ones when a transform returns null" in {
    val e = new ElemOptimized(<p a="1" b="2" c="3"/>, Seq("a" -> remove, "c" -> ((_: String) + "!"), "x" -> remove, "y" -> ((_: String) => "new")))
    assert(e.attrs.toMap == Map("b" -> "2", "c" -> "3!", "y" -> "new"))
    wellFormed(e)
  }

  "(ElemOptimized, name, transform)" should "remove the attribute when the transform returns null, wherever it is" in {
    val base = new ElemOptimized(<p a="1" b="2" c="3"/>, "d", (_: String) => "4")
    for (name <- Seq("a", "b", "c", "d")) { // "d" is the head of the list, the others further down
      val e = new ElemOptimized(base, name, remove)
      assert(names(e) == Set("a", "b", "c", "d") - name)
      assert(!e.attrs.exists(_._1 == name))
      wellFormed(e)
    }
    val missing = new ElemOptimized(base, "x", remove)
    assert(missing.attrs.toMap == base.attrs.toMap)
    wellFormed(missing)
  }

  it should "remove it from a single-attribute and from an empty list" in {
    val one = new ElemOptimized(<p/>, "a", (_: String) => "1")
    val none = new ElemOptimized(one, "a", remove)
    assert(none.attrs == Nil)
    assert(none.toString == "<p/>")
    val stillNone = new ElemOptimized(none, "a", remove)
    assert(stillNone.attrs == Nil)
    wellFormed(stillNone)
  }

  "(ElemOptimized, transforms)" should "remove existing attributes and not add missing ones when a transform returns null" in {
    val base = new ElemOptimized(<p a="1" b="2"/>, "c", (_: String) => "3")
    val e = new ElemOptimized(base, Seq("a" -> remove, "c" -> remove, "b" -> ((_: String) + "!"), "x" -> remove, "y" -> ((_: String) => "new")))
    assert(e.attrs.toMap == Map("b" -> "2!", "y" -> "new"))
    wellFormed(e)
  }

  "A transform after a removal" should "see \"\", in every constructor" in {
    def seenAfterRemoval(apply: (ElemOptimized, T) => ElemOptimized): String = {
      var seen = "unset"
      val removed = new ElemOptimized(new ElemOptimized(<p a="1"/>, "a", (_: String) + "2"), "a", remove)
      val e = apply(removed, s => { seen = s; s + "x" })
      assert(e.attrs.toMap == Map("a" -> "x"))
      wellFormed(e)
      seen
    }
    assert(seenAfterRemoval((e, t) => new ElemOptimized(e, "a", t)) == "")
    assert(seenAfterRemoval((e, t) => new ElemOptimized(e, Seq("a" -> t))) == "")
    assert(seenAfterRemoval((e, t) => new ElemOptimized(e: Elem, "a", t)) == "")
    assert(seenAfterRemoval((e, t) => new ElemOptimized(e: Elem, Seq("a" -> t))) == "")
  }

  "Every constructor" should "call each transform exactly once" in {
    var calls = 0
    val t: T = s => { calls += 1; s + "!" }
    val plain = <p a="1"/>
    val fast = new ElemOptimized(plain, "b", (_: String) => "2")
    for ((name, make) <- Seq[(String, () => Elem)](
      "Elem, existing" -> (() => new ElemOptimized(plain, "a", t)),
      "Elem, missing" -> (() => new ElemOptimized(plain, "x", t)),
      "Elem, Seq" -> (() => new ElemOptimized(plain, Seq("a" -> t))),
      "V1, head" -> (() => new ElemOptimized(fast, "b", t)),
      "V1, further down" -> (() => new ElemOptimized(fast, "a", t)),
      "V1, missing" -> (() => new ElemOptimized(fast, "x", t)),
      "V1, empty" -> (() => new ElemOptimized(new ElemOptimized(<p/>, "a", remove), "a", t)),
      "V1, Seq" -> (() => new ElemOptimized(fast, Seq("a" -> t))),
    )) {
      calls = 0
      make()
      assert(calls == 1, s"$name: called $calls times")
    }
  }
}
