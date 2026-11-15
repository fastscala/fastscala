package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JsWithScriptDetection
import com.fastscala.scala_xml.xml.ScalaXmlElemUtils.RichElem
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.{Elem, NodeSeq, Text}

/** Rerenderer, RerendererP, ContentRerenderer, ContentRerendererP, and the debug overlay (RerendererDebugStatus). */
class RerenderersSpec extends AnyFlatSpec {

  import RerendererContextSpec.session

  private object NoImpl extends FSPageImpl[Unit] {
    override def render()(implicit fsc: FSContext): Unit = ()
  }

  private def newRoot(): FSContext = new FSPage(session.nextID(), session, null, NoImpl).rootFSContext

  "Rerenderer.render" should "give the element an id (the element's own, if it has one) and render it in its own context" in {
    val root = newRoot()
    var ctx: FSContext = null
    val r = new Rerenderer(_ => fsc => { ctx = fsc; <div>a</div> })
    val first = r.render()(using root)
    assert(first.getIdOpt.isDefined && first.getIdOpt == r.aroundId)
    assert(ctx.parentFSContext.contains(root))
    val own = new Rerenderer(_ => _ => <div id="mine">a</div>)
    assert(own.render()(using root).getId == "mine")
    assert(own.aroundId.contains("mine"))
  }

  it should "use the id given" in {
    val root = newRoot()
    val r = new Rerenderer(_ => _ => <span>x</span>, idOpt = Some("given"))
    assert(r.render()(using root).getId == "given")
  }

  it should "keep the first id across renders, even if the element's changes" in {
    val root = newRoot()
    var n = 0
    val r = new Rerenderer(_ => _ => { n += 1; <div id={s"e$n"}>x</div> })
    assert(r.render()(using root).getId == "e1")
    assert(r.render()(using root).getId == "e1")
  }

  "Rerenderer.rerender" should "replace the element (by its id), with its scripts run after it" in {
    val root = newRoot()
    var label = "first"
    val r = new Rerenderer(_ => _ => <p>{label}</p>, idOpt = Some("r1"))
    r.render()(using root)
    label = "second"
    val js = r.rerender()(using root)
    assert(js.isInstanceOf[JsWithScriptDetection])
    val code = js.cmd
    assert(code.contains("document.getElementById(\"r1\")") && code.contains("second") && !code.contains("first"), code)
  }

  "map" should "transform what's rendered (in the order the transforms were added)" in {
    val root = newRoot()
    val r = new Rerenderer(_ => _ => <div>x</div>).map(_.addClass("a")).map(_.addClass("b"))
    assert(r.render()(using root).getClassAttr.trim == "a b")
  }

  "RerendererP" should "render with the param, and rerender with a transform of the last one" in {
    val root = newRoot()
    val r = new RerendererP[Int](_ => _ => n => <b>{s"n=$n"}</b>, idOpt = Some("p1"))
    assert(r.render(1)(using root).text == "n=1")
    assert(r.renderedWithParam.contains(1))
    val js = r.rerenderTransform(_ + 41)(using root).cmd
    assert(js.contains("n=42"), js)
    assert(r.renderedWithParam.contains(42))
  }

  it should "refuse rerenderTransform before it was rendered" in {
    val root = newRoot()
    val r = new RerendererP[Int](_ => _ => n => <b>{n.toString}</b>)
    assertThrows[Exception](r.rerenderTransform(_ + 1)(using root))
  }

  it should "replace the element, or its contents, by another" in {
    val r = new RerendererP[Int](_ => _ => n => <b>{n.toString}</b>, idOpt = Some("p2"))
    assert(r.replaceBy(<i>new</i>).cmd.contains("document.getElementById(\"p2\")"))
    val contents = r.replaceContentsBy(<i>inner</i>).cmd
    assert(contents.contains("p2") && contents.contains("inner"), contents)
  }

  "ContentRerenderer" should "render the contents in a div with its id, and rerender it" in {
    val root = newRoot()
    var items = Seq("a", "b")
    val r = new ContentRerenderer(_ => _ => NodeSeq.fromSeq(items.map(i => <li>{i}</li>)), idOpt = Some("c1"))
    val rendered = r.render()(using root)
    assert(rendered.label == "div" && rendered.getId == "c1")
    assert((rendered \ "li").map(_.text) == Seq("a", "b"))
    items = Seq("z")
    val code = r.rerender()(using root).cmd
    // (The HTML is in a JavaScript string literal: escaped, `<` as \x3c.)
    assert(code.contains("c1") && code.contains("li\\x3ez\\x3c") && !code.contains("li\\x3ea\\x3c"), code)
  }

  "ContentRerendererP" should "render with the param, and transform the contents with map" in {
    val root = newRoot()
    val r = new ContentRerendererP[String](_ => _ => s => Text(s)).map(ns => <em>{ns}</em>)
    val rendered = r.render("hi")(using root)
    assert(rendered.getId == r.aroundId)
    assert((rendered \ "em").text == "hi")
  }

  "RerendererDebugStatus" should "leave what's rendered as it is when disabled" in {
    val e = <div id="x">a</div>
    assert(RerendererDebugStatus.Disabled.render(e) eq e)
    val js = Js("f()")
    assert(RerendererDebugStatus.Disabled.rerender("x", js) eq js)
  }

  it should "add an overlay when enabled (not to table elements)" in {
    val e = RerendererDebugStatus.Enabled.render(<div id="x">a</div>)
    assert(e.getStyleAttr.contains("position:relative"))
    val overlay = (e \ "span").head.asInstanceOf[Elem]
    assert(overlay.getId == "x-overlay")
    val row = <tr id="r"><td>1</td></tr>
    assert(RerendererDebugStatus.Enabled.render(row) eq row)
    val js = RerendererDebugStatus.Enabled.rerender("x", Js("f()")).cmd
    assert(js.contains("""$("#x").fadeOut(1500, function() {f()});"""), js)
  }

  it should "not add an overlay to a tfoot" in {
    // Regression (fixed): the table elements left alone are "table", "thead", "tbody", "tfooter", "tr": "tfooter" isn't an element
    // (it's <tfoot>), so a tfoot gets a <span> overlay, which isn't valid inside it.
    val foot = <tfoot id="f"><tr><td>1</td></tr></tfoot>
    assert(RerendererDebugStatus.Enabled.render(foot) eq foot)
  }

  "RerendererDebugStatusState" should "be Disabled by default (per session)" in {
    assert(RerendererDebugStatusState()(using session) == RerendererDebugStatus.Disabled)
  }
}
