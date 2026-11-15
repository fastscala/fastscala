package com.fastscala.components.bootstrap5.btn

import com.fastscala.components.bootstrap5.utils.BSBtn

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.testutils.PerfFS
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.Elem

class BSBtnSpec extends AnyFlatSpec {

  private def newRoot(): FSContext = PerfFS.newPage().rootFSContext

  private def attr(e: Elem, name: String): Option[String] = e.attribute(name).map(_.text)

  private def classes(e: Elem): Set[String] = attr(e, "class").map(_.split("\\s+").filter(_.nonEmpty).toSet).getOrElse(Set())

  /** Runs the last callback registered in the page (as a click would). */
  private def click(root: FSContext): Js = root.page.callbacks.values.maxBy(_.id.toLong).func("")

  "btn" should "be a <button type=button> with the classes, content and attributes" in {
    given FSContext = newRoot()
    val b = BSBtn().BtnPrimary.sm.lbl("Save").id("b1").title("Saves it").withAdditionalAttrs("data-x" -> "1").btn
    assert(b.label == "button" && attr(b, "type").contains("button"))
    assert(classes(b) == Set("btn", "btn-primary", "btn-sm"))
    assert(b.text == "Save")
    assert(attr(b, "id").contains("b1") && attr(b, "title").contains("Saves it") && attr(b, "data-x").contains("1"))
    assert(attr(b, "onclick").isEmpty && attr(b, "style").isEmpty)
  }

  "lbl / icn / ns / withContent" should "set what's in the button (an icon, a space, then the label)" in {
    given FSContext = newRoot()
    val b = BSBtn().icn(<i class="bi bi-gear"></i>).lbl("Settings").btn
    assert(b.child.map(_.toString) == Seq("""<i class="bi bi-gear"></i>""", "<span> </span>", "Settings"))
    assert(BSBtn().lbl("A").ns(<b>B</b>).btn.child.map(_.toString) == Seq("<b>B</b>"))
    assert(BSBtn().withContent(<i>C</i>).btn.text == "C")
  }

  "The styles" should "add up, after ;" in {
    given FSContext = newRoot()
    assert(attr(BSBtn().padding(1, 2).btn, "style").contains(";;padding: 1px 2px;"))
    assert(attr(BSBtn().margin(1, 2, 3, 4).withStyle("color: red").btn, "style").contains(";;margin: 1px 2px 3px 4px;;color: red"))
    assert(attr(BSBtn().xs.btn, "style").exists(_.contains("padding: 2px 10px")))
  }

  "The class helpers" should "add Bootstrap's classes, or replace them" in {
    assert(BSBtn().BtnOutlineDanger.lg.disabled.toString.split("\\s+").filter(_.nonEmpty).toSet == Set("btn", "btn-outline", "btn-outline-danger", "btn-lg", "disabled"))
    assert(BSBtn().BtnPrimary.withClass("x").toString == "x")
    assert(BSBtn().icon.toString.contains("btn-icon"))
  }

  "onclick" should "add up the code" in {
    given FSContext = newRoot()
    val b = BSBtn().onclick(Js("a()")).onclick((_: FSContext) => Js("b()")).btn
    assert(attr(b, "onclick").contains("a();b()"))
  }

  "callback" should "call the function on the server when clicked" in {
    val root = newRoot()
    given FSContext = root
    var clicks = 0
    val b = BSBtn().lbl("Go").callback(_ => { clicks += 1; Js("done()") }).btn
    assert(attr(b, "onclick").exists(_.startsWith("window._fs.callback(")))
    assert(click(root).cmd == "done()" && clicks == 1)
  }

  "callbackRunOnce" should "run the function only on the first click" in {
    val root = newRoot()
    given FSContext = root
    var runs = 0
    BSBtn().callbackRunOnce(_ => { runs += 1; Js("ran()") }, onFurtherAttemptsJs = Some(Js("again()"))).btn
    assert(click(root).cmd == "ran()")
    assert(click(root).cmd == "again()")
    assert(runs == 1)
  }

  "ajaxOnce" should "give moreThanOnceRslt on further clicks" in {
    // Regression (fixed): ajaxOnce passes only func on to callbackRunOnce: moreThanOnceRslt is dropped.
    val root = newRoot()
    given FSContext = root
    BSBtn().ajaxOnce(_ => Js("ran()"), moreThanOnceRslt = Some(Js("again()"))).btn
    click(root)
    assert(click(root).cmd == "again()")
  }

  "callbackConfirm" should "ask before calling the function" in {
    given FSContext = newRoot()
    val onclick = attr(BSBtn().callbackConfirm("Delete it?", _ => Js.Void).btn, "onclick").get
    assert(onclick.contains("confirm(") && onclick.contains("""confirm("Delete\x20it?")""") && onclick.contains("window._fs.callback("), onclick)
  }

  "btnLink" should "be an <a>, with its href (or javascript:void(0))" in {
    given FSContext = newRoot()
    val a = BSBtn().BtnLink.lbl("Go").href("/x?a=1&b=2").btnLink
    assert(a.label == "a" && attr(a, "href").contains("/x?a=1&b=2"))
    assert(attr(BSBtn().lbl("Go").btnLink, "href").contains("javascript:void(0)"))
  }

  it should "open in the target given" in {
    // Regression (fixed): btnLink doesn't render targetOpt: .target(...) and .targetBlank have no effect.
    given FSContext = newRoot()
    assert(attr(BSBtn().href("/x").targetBlank.btnLink, "target").contains("_blank"))
  }

  "span" should "be a <span> with the classes and content" in {
    given FSContext = newRoot()
    val s = BSBtn().BtnLight.lbl("x").span
    assert(s.label == "span" && classes(s).contains("btn-light") && attr(s, "type").isEmpty)
  }

  "The rerenders and client-side changes" should "target the button's id" in {
    given FSContext = newRoot()
    val b = BSBtn().BtnPrimary.id("b2")
    val jq = "$('#b2')"
    assert(b.btnRerender.cmd.contains("b2"))
    assert(b.disable().cmd == jq + ".addClass('disabled').attr('disabled','true');")
    assert(b.enable().cmd == jq + ".removeClass('disabled').removeAttr('disabled');")
    assert(b.toDanger.cmd == jq + ".removeClass('" + BSBtn.StyleClasses.mkString(" ") + "').addClass('btn btn-danger');")
    assert(attr(b.disableAfterOneClick().onclick(Js("x()")).btn, "onclick").exists(_.startsWith(b.disable().cmd)))
  }

  "toggle" should "be the selected or unselected button, and switch on click" in {
    val root = newRoot()
    given FSContext = root
    var on = false
    val first = BSBtn().lbl("T").toggle(on, v => { on = v; Js.Void }, selected = _.BtnPrimary, unselected = _.BtnLight)
    assert(classes(first).contains("btn-light") && attr(first, "id").isDefined)
    val js = click(root).cmd
    assert(on && js.contains(attr(first, "id").get), js.take(200))
  }

  "withRandomId" should "give it an id" in {
    assert(BSBtn().withRandomId.id.startsWith("id"))
    assert(BSBtn().id == null)
  }
}
