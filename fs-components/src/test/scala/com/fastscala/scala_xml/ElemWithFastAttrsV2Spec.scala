package com.fastscala.scala_xml

import com.fastscala.components.bootstrap5.helpers.BSHelpers.*
import com.fastscala.scala_xml.utils.{ElemWithFastAttrsV2, XmlStream}
import org.scalatest.flatspec.AnyFlatSpec

import java.io.StringWriter
import scala.xml.*

/**
 * [[ElemWithFastAttrsV2]] defers attribute transforms (addClass, withAttr, ...) until the attributes are
 * read, so a chain like `<span/>.btn_sm.btn.ms_2` computes the final attributes once, when printed, instead of once
 * per step.
 *
 * In package `com.fastscala.scala_xml` to read [[ElemWithFastAttrsV2.Computations]]. Lives in fs-components
 * because it uses [[com.fastscala.components.bootstrap5.helpers.BSHelpers]].
 */
class ElemWithFastAttrsV2Spec extends AnyFlatSpec {

  /** How many times attributes were computed while running `f`. Tests in this suite run sequentially. */
  private def computations[A](f: => A): (A, Long) = {
    val before = ElemWithFastAttrsV2.Computations.get()
    val result = f
    (result, ElemWithFastAttrsV2.Computations.get() - before)
  }

  private def serialize(n: Node): String = {
    val sb = new StringBuilder
    Utility.serialize(n, sb = sb)
    sb.toString
  }

  private def stream(n: Node): String = {
    val w = new StringWriter
    XmlStream.writeNode(n, w)
    w.toString
  }

  private def classesOf(n: Node): List[String] =
    n.attribute("class").map(_.text.trim.split("\\s+").toList).getOrElse(Nil)

  "A chain of BSHelpers classes" should "compute nothing until printed" in {
    val (elem, n) = computations(<span></span>.btn_sm.btn.ms_2)
    assert(n == 0)
    assert(elem.isInstanceOf[ElemWithFastAttrsV2])
  }

  it should "compute the attributes exactly once when printed to a StringBuilder" in {
    val elem = <span></span>.btn_sm.btn.ms_2
    val (out, n) = computations(serialize(elem))
    assert(n == 1, s"computed $n times")
    assert(out == """<span class="btn-sm btn ms-2"></span>""" || out == """<span class=" btn-sm btn ms-2"></span>""", out)
  }

  it should "apply each class once, in order" in {
    val elem = <span></span>.btn_sm.btn.ms_2
    assert(classesOf(elem) == List("btn-sm", "btn", "ms-2"))
  }

  it should "compute the attributes exactly once when written with XmlStream" in {
    val elem = <span></span>.btn_sm.btn.ms_2
    val (out, n) = computations(stream(elem))
    assert(n == 1, s"computed $n times")
    assert(out == serialize(elem))
  }

  it should "not compute again when printed again" in {
    val elem = <span></span>.btn_sm.btn.ms_2
    val (first, n1) = computations(serialize(elem))
    val (second, n2) = computations(serialize(elem) + stream(elem) + elem.toString)
    assert(n1 == 1)
    assert(n2 == 0, s"computed $n2 more times")
    assert(second == first * 3)
  }

  it should "print the same as applying the same attributes to a plain Elem" in {
    val elem = <span></span>.btn_sm.btn.ms_2
    val plain = Elem(null, "span", new UnprefixedAttribute("class", elem.attribute("class").get.text, Null), TopScope, false)
    assert(serialize(elem) == serialize(plain))
    assert(stream(elem) == stream(plain))
    assert(elem.toString == plain.toString)
  }

  it should "not compute intermediate elements of the chain" in {
    val step1 = <span></span>.btn_sm
    val step2 = step1.btn
    val step3 = step2.ms_2
    val (_, n) = computations(serialize(step3))
    assert(n == 1, s"computed $n times")
  }

  it should "leave intermediate elements usable on their own" in {
    val step1 = <span></span>.btn_sm
    val step2 = step1.btn
    assert(classesOf(step2.ms_2) == List("btn-sm", "btn", "ms-2"))
    assert(classesOf(step2) == List("btn-sm", "btn"))
    assert(classesOf(step1) == List("btn-sm"))
  }

  it should "keep each element's own attributes (no state shared between elements)" in {
    val a = <span></span>.btn_sm.ms_2
    val b = <div></div>.btn.me_3
    assert(classesOf(a) == List("btn-sm", "ms-2"))
    assert(classesOf(b) == List("btn", "me-3"))
    assert(serialize(b).startsWith("<div "))
  }

  it should "keep existing attributes and append to an existing class" in {
    val elem = <span id="x" class="a"></span>.btn.ms_2
    assert(classesOf(elem) == List("a", "btn", "ms-2"))
    assert(elem.attribute("id").map(_.text) == Some("x"))
    val out = serialize(elem) // attribute order doesn't matter
    assert(out.contains(""" id="x"""") && out.contains(""" class="a btn ms-2""""), out)
    assert(out.startsWith("<span ") && out.endsWith("></span>") && !out.contains("  "), out)
  }

  it should "mix classes and other attributes" in {
    val elem = <span></span>.btn.withAttr("data-x" -> "1").ms_2.withAttr("data-x" -> "2")
    val (out, n) = computations(serialize(elem))
    assert(n == 1, s"computed $n times")
    assert(classesOf(elem) == List("btn", "ms-2"))
    assert(elem.attribute("data-x").map(_.text) == Some("2"))
    assert(!out.contains("data-x=\"1\""), out)
  }

  it should "keep children" in {
    val elem = <span><b>x</b>y</span>.btn.ms_2
    assert(serialize(elem).endsWith("><b>x</b>y</span>"))
  }

  it should "expose no placeholder attribute" in {
    val elem = <span></span>.btn.ms_2
    assert(elem.attributes.toList.map(_.key) == List("class"))
    assert(elem.attributes.length == 1)
    assert(!serialize(elem).contains("  "), serialize(elem))
    assert(!stream(elem).contains("  "), stream(elem))
  }
}
