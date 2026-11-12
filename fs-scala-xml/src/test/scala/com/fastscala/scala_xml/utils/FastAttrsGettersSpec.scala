package com.fastscala.scala_xml.utils

import com.fastscala.scala_xml.{ScalaXmlElemUtilsV1, ScalaXmlElemUtilsV2}
import com.fastscala.scala_xml.testutils.{ElemUtilsV1, ElemUtilsV2}
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.*

/**
 * The getters of [[ScalaXmlElemUtilsV1]] / [[ScalaXmlElemUtilsV2]] read an ElemWithFastAttrsV1 / V2's attrs directly:
 * they must not compute the attributes, and must return exactly what they return for a plain Elem with the same ones.
 */
class FastAttrsGettersSpec extends AnyFlatSpec {

  /** A plain Elem with `e`'s attributes (in the same order), so its getters take the plain path. */
  private def plainCopy(e: Elem): Elem = {
    val md = e.attributes.toList.foldRight(Null: MetaData)((a, next) => new UnprefixedAttribute(a.key, a.value.text, next))
    Elem(e.prefix, e.label, md, e.scope, e.minimizeEmpty, e.child *)
  }

  private val names = Seq("a", "b", "class", "style", "id", "title", "missing")

  /** Every getter's result, for comparison. */
  private def getters(attrs: Seq[String] => Seq[Any], getAttrs: => Any): Seq[Any] = attrs(names) :+ getAttrs

  private def v2Getters(e: Elem): Seq[Any] = {
    val r = new ElemUtilsV2.RichElem(e)
    getters(ns => ns.map(r.attr) ++ Seq(r.getClassAttr, r.getStyleAttr, r.getIdOpt) ++
      ns.map(n => r.hasAttribute(n, r.attr(n).map(Utility.escape).getOrElse("?"))) ++
      Seq(r.hasAttribute("title", "t & u"), r.hasAttribute("title", "t &amp; u"), r.hasAttribute("missing", "")), r.getAttrs)
  }

  private def v1Getters(e: Elem): Seq[Any] = {
    val r = new ElemUtilsV1.RichElem(e)
    getters(ns => ns.map(r.attr) ++ Seq(r.getClassAttr, r.getStyleAttr, r.getIdOpt) ++
      ns.map(n => r.hasAttribute(n, r.attr(n).map(Utility.escape).getOrElse("?"))) ++
      Seq(r.hasAttribute("title", "t & u"), r.hasAttribute("title", "t &amp; u"), r.hasAttribute("missing", "")), r.getAttrs)
  }

  private def notComputed(e: Elem, attributeClass: Class[?]): Boolean = attributeClass.getField("cached").get(e.attributes) == null

  {
    import ElemUtilsV2.RichElem
    val cases: Seq[(String, () => Elem)] = Seq(
      "classes added" -> (() => <td/>.addClass("px-2").addClass("py-1")),
      "existing attributes, escaping" -> (() => <div id="i&amp;d" title="t &amp; u" class="a"/>.addClass("b <c>").withAttr("style" -> "x: \"y\"")),
      "set several times, removed" -> (() => <p a="1" b="2"/>.withAttr("a" -> "3").withAttr("b" -> null).withAttr("a" -> "4")),
      "withAttrs" -> (() => <p/>.withAttrs("a" -> "1", "b" -> "2").withAttrs("a" -> "3")),
    )
    cases.foreach { case (name, make) =>
      "ScalaXmlElemUtilsV2's getters" should s"read ElemWithFastAttrsV2 directly, as for a plain Elem ($name)" in {
        val e = make()
        assert(e.isInstanceOf[ElemWithFastAttrsV2])
        val fast = v2Getters(e)
        assert(notComputed(e, ElemWithFastAttrsV2.UnprefixedAttributeClass), "a getter computed the attributes")
        assert(fast == v2Getters(plainCopy(e)))
        assert(new ElemUtilsV2.RichElem(e).getAttrs.toMap == e.attributes.toList.map(a => a.key -> a.value.toString).toMap)
      }
    }
  }

  {
    import ElemUtilsV1.RichElem
    val cases: Seq[(String, () => Elem)] = Seq(
      "classes added" -> (() => <td/>.addClass("px-2").addClass("py-1")),
      "existing attributes, escaping" -> (() => <div id="i&amp;d" title="t &amp; u" class="a"/>.addClass("b <c>").withAttr("style" -> "x: \"y\"")),
      "set several times, removed" -> (() => <p a="1" b="2"/>.withAttr("a" -> "3").withAttr("b" -> null).withAttr("a" -> "4")),
      "removed, then set again" -> (() => <p a="1"/>.withAttr("a" -> null).withAttr("a" -> "back")),
      "withAttrs" -> (() => <p/>.withAttrs("a" -> "1", "b" -> "2").withAttrs("a" -> "3")),
    )
    cases.foreach { case (name, make) =>
      "ScalaXmlElemUtilsV1's getters" should s"read ElemWithFastAttrsV1 directly, as for a plain Elem ($name)" in {
        val e = make()
        assert(e.isInstanceOf[ElemWithFastAttrsV1])
        val fast = v1Getters(e)
        assert(notComputed(e, ElemWithFastAttrsV1.UnprefixedAttributeClass), "a getter computed the attributes")
        assert(fast == v1Getters(plainCopy(e)))
        // same order as elem.attributes, too
        assert(new ElemUtilsV1.RichElem(e).getAttrs == e.attributes.toList.map(a => a.key -> a.value.toString))
      }
    }
  }
}
