package com.fastscala.scala_xml.utils

import com.fastscala.scala_xml.ScalaXmlElemUtilsV1.RichElem
import org.scalatest.flatspec.AnyFlatSpec

import java.io.StringWriter
import scala.xml.*

/** XmlStream's fast path for [[ElemWithFastAttrsV1]] must print exactly what toString prints, without computing it. */
class ElemWithFastAttrsV1XmlStreamSpec extends AnyFlatSpec {

  private def stream(n: Node): String = { val w = new StringWriter; XmlStream.writeNode(n, w); w.toString }

  private def cached(e: Elem): AnyRef = ElemWithFastAttrsV1.UnprefixedAttributeClass.getField("cached").get(e.attributes)

  private val cases: Seq[(String, () => Elem)] = Seq(
    "classes added in a chain" -> (() => <td/>.addClass("px-2").addClass("py-1").addClass("text-center")),
    "existing attributes kept, one updated" -> (() => <div id="x" class="a" title="t"/>.addClass("b").withAttr("data-q" -> "1")),
    "an attribute set several times" -> (() => <div/>.withAttr("a" -> "1").withClass("c").withAttr("a" -> "2").withAttr("a" -> "3")),
    "an attribute removed" -> (() => <p a="1" b="2"/>.withAttr("a" -> null)),
    "removed, then set again" -> (() => <p a="1"/>.withAttr("a" -> null).withAttr("a" -> "back")),
    "set, then removed" -> (() => <p/>.withAttr("a" -> "1").addClass("c").withAttr("a" -> null)),
    "escaping" -> (() => <div/>.withAttr("title" -> "x < \"y\" & z").addClass("a&b")),
    "withAttrs, then more" -> (() => <div k="0"/>.withAttrs("a" -> "1", "b" -> "2").withAttrs("a" -> "3").addClass("c")),
    "children kept" -> (() => <ul>{<li/>.addClass("x")}<li>t</li></ul>.addClass("list")),
  )

  cases.foreach { case (name, make) =>
    "XmlStream" should s"write an ElemWithFastAttrsV1 like toString, without computing its attributes ($name)" in {
      val e = make()
      assert(e.isInstanceOf[ElemWithFastAttrsV1])
      val streamed = stream(e)
      assert(cached(e) == null, "XmlStream computed the attributes")
      assert(streamed == e.toString, s"\n  stream:   $streamed\n  toString: ${e.toString}")
    }
  }
}
