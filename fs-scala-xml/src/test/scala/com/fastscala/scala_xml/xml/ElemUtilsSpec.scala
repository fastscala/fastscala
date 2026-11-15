package com.fastscala.scala_xml.xml

import com.fastscala.js.Js
import com.fastscala.scala_xml.xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.xml.ScalaXmlNodeSeqUtils.*
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.*

/** The Elem helpers (ScalaXmlElemUtils) not covered by ScalaXmlElemUtilsV1Spec, and ScalaXmlNodeSeqUtils. */
class ElemUtilsSpec extends AnyFlatSpec {

  /** Both kinds of Elem the helpers work on. */
  private def both(f: Elem => Unit): Unit = {
    f(<div class="a" style="color: red" id="x">t</div>)
    f(ElemOptimized("div", "class" -> "a", "style" -> "color: red", "id" -> "x")(Text("t")))
  }

  "addClass / withClass / addClassIf" should "append a class, set the classes, or append one only if asked" in both { e =>
    assert(e.addClass("b").getClassAttr == "a b")
    assert(e.addClass("") eq e)
    assert(e.withClass("z").getClassAttr == "z")
    assert(e.addClassIf(true, "b").getClassAttr == "a b")
    assert(e.addClassIf(false, "b") eq e)
    assert(<p/>.addClass("b").getClassAttr.trim == "b")
  }

  "removeClass" should "remove the class" in both { e =>
    assert(e.addClass("b").removeClass("b").getClassAttr.trim == "a")
  }

  it should "leave other classes that contain its name alone" in {
    // Regression (fixed): it removes the name wherever it appears, case-insensitively: "btn btn-primary" without "btn" is " -primary".
    assert(<a class="btn btn-primary"/>.removeClass("btn").getClassAttr.trim == "btn-primary")
  }

  "addStyle / withStyle" should "append a style (after a ;), or set it" in both { e =>
    assert(e.addStyle("margin: 0").getStyleAttr == "color: red;margin: 0")
    assert(e.addStyle("") eq e)
    assert(e.withStyle("x: 1").getStyleAttr == "x: 1")
  }

  "addOnClick" should "append the code to the onclick, keeping it a Js" in {
    val e = <button onclick="a()"/>.addOnClick("b()").addOnClick(Js("c()"))
    assert(e.attr("onclick").contains("a();b();c()"))
    e match {
      case fast: ElemOptimized => assert(fast.attrs.exists(_._2.isInstanceOf[Js]))
      case other => fail(s"not an ElemOptimized: ${other.getClass}")
    }
  }

  "The attribute setters" should "set their attribute" in both { e =>
    assert(e.withFor("i").attr("for").contains("i"))
    assert(e.withType("text").attr("type").contains("text"))
    assert(e.withTypeSubmit().attr("type").contains("submit"))
    assert(e.withValue("v").attr("value").contains("v"))
    assert(e.withTitle("t").attr("title").contains("t"))
    assert(e.withName("n").attr("name").contains("n"))
    assert(e.withRole("r").attr("role").contains("r"))
    assert(e.withRoleButton.attr("role").contains("button"))
    assert(e.withRoleAlert.attr("role").contains("alert"))
    assert(e.withHref("/a?b&c").attr("href").contains("/a?b&c"))
    assert(e.withId("y").getId == "y")
    assert(e.withAttrIf(true, "data-x" -> "1").attr("data-x").contains("1"))
    assert(e.withAttrIf(false, "data-x" -> "1") eq e)
  }

  "withIdIfNotSet" should "set the id only if there's none" in both { e =>
    assert(e.withIdIfNotSet("new").getId == "x")
    assert(<p/>.withIdIfNotSet("new").getId == "new")
  }

  "The contents helpers" should "set, prepend and append to the children" in both { e =>
    assert(e.withContents(<b>1</b>).child.map(_.toString) == Seq("<b>1</b>"))
    assert(e.withContents(NodeSeq.fromSeq(Seq(<b>1</b>, <i>2</i>))).child.size == 2)
    assert(e.withPrependedToContents(<b>0</b>).child.map(_.toString) == Seq("<b>0</b>", "t"))
    assert(e.withAppendedToContents(<b>2</b>).child.map(_.toString) == Seq("t", "<b>2</b>"))
    assert(RichElem(e)("text").child.map(_.text) == Seq("text"))
    assert(RichElem(e)(NodeSeq.fromSeq(Seq(<b>1</b>)), NodeSeq.fromSeq(Seq(<i>2</i>))).child.size == 2)
    assert(e.withContents(<b>1</b>).attr("class").contains("a"))
  }

  "showIf" should "be the element, or nothing" in both { e =>
    assert(e.showIf(true) == e)
    assert(e.showIf(false).isEmpty)
    assert(ScalaXmlElemUtils.showIf(false)(throw new Exception("not evaluated")).isEmpty)
  }

  "getAttrs / hasAttribute" should "be the attributes, escaped as toString writes them" in both { e =>
    val withAmp = e.withTitle("a & b")
    assert(withAmp.getAttrs.toMap.get("title").contains("a &amp; b"))
    assert(withAmp.hasAttribute("title", "a &amp; b"))
    assert(!withAmp.hasAttribute("title", "a & b"))
    assert(withAmp.getAttrs.map(_._1).toSet == Set("class", "style", "id", "title"))
  }

  "mkNS" should "join NodeSeqs and Elems, with a separator between them" in {
    val parts: Seq[NodeSeq] = Seq(<a/>, NodeSeq.fromSeq(Seq(<b/>, <c/>)), NodeSeq.Empty)
    assert(parts.mkNS.map(_.label) == Seq("a", "b", "c"))
    assert(parts.mkNS(<hr/>).map(_.label) == Seq("a", "hr", "b", "c", "hr"))
    assert(Seq.empty[NodeSeq].mkNS(<hr/>).isEmpty)
    val elems: Seq[Elem] = Seq(<a/>, <b/>)
    assert(elems.mkNS.map(_.label) == Seq("a", "b"))
    assert(elems.mkNS(Text(", ")).toString == "<a/>, <b/>")
    assert(Seq.empty[Elem].mkNS(<hr/>).isEmpty)
  }

  "NodeSeq.showIf" should "be the nodes, or nothing" in {
    val ns: NodeSeq = <a/>
    assert(ns.showIf(true) eq ns)
    assert(ns.showIf(false).isEmpty)
  }
}
