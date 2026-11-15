package com.fastscala.components.bootstrap5.utils

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.testutils.PerfFS
import org.scalatest.flatspec.AnyFlatSpec

import java.time.LocalDate
import scala.xml.NodeSeq

/** ClickToEditFields: shown as text, edited (an input) when clicked, saved when the input is left. */
class ClickToEditFieldsSpec extends AnyFlatSpec {

  private def newRoot(): FSContext = PerfFS.newPage().rootFSContext

  /** The callback registered last in the page, called with `arg` (a click, or the input's value). */
  private def callLast(root: FSContext, arg: String = ""): Js = root.page.callbacks.values.maxBy(_.id.toLong).func(arg)

  /**
   * Clicks to edit (the last callback registered by the display), then leaves the input with `value`: calls the first
   * callback the click registered (the input's submit; rendering the editor registers others after it).
   */
  private def edit(root: FSContext, value: String): Js = {
    val before = root.page.callbacks.keySet.toSet
    callLast(root) // the click: renders the input
    val submitId = (root.page.callbacks.keySet.toSet -- before).minBy(_.toLong)
    root.page.callbacks(submitId).func(value)
  }

  "Str" should "show the value (or the empty string), and edit it when clicked" in {
    val root = newRoot()
    given FSContext = root
    val shown = ClickToEditFields.Str(() => "", _ => Js.Void, emptyString = "(none)", elemId = Some("s1"))
    assert(shown.text.trim == "(none)")
    val js = callLast(root).cmd
    assert(js.contains("s1") && js.contains("focus"), js.take(300))
  }

  it should "save the trimmed value when it changed, and show it again" in {
    val root = newRoot()
    given FSContext = root
    var value = "old"
    ClickToEditFields.Str(() => value, v => { value = v; Js("saved()") })
    val js = edit(root, "  new ").cmd
    assert(value == "new" && js.startsWith("saved()"), js.take(200))
  }

  it should "not save an unchanged value" in {
    val root = newRoot()
    given FSContext = root
    var saves = 0
    ClickToEditFields.Str(() => "same", _ => { saves += 1; Js.Void })
    edit(root, "same")
    assert(saves == 0)
  }

  it should "ask for confirmation, when asked to, before saving" in {
    val root = newRoot()
    given FSContext = root
    var value = "a"
    ClickToEditFields.Str(() => value, v => { value = v; Js.Void }, confirm = Some(v => s"Change to $v?"))
    val before = root.page.callbacks.keySet.toSet
    val js = edit(root, "b").cmd
    assert(js.contains("confirm(") && value == "a", js.take(200))
    root.page.callbacks((root.page.callbacks.keySet.toSet -- before).maxBy(_.toLong)).func("") // confirmed
    assert(value == "b")
  }

  it should "start editing, when asked to" in {
    given FSContext = newRoot()
    val ns: NodeSeq = ClickToEditFields.Str(() => "v", _ => Js.Void, editing = true, elemId = Some("e1"), placeholder = "Name")
    assert(ns.head.label == "input" && (ns \@ "value") == "v" && (ns \@ "placeholder") == "Name")
  }

  "Textarea" should "save the trimmed text when it's left" in {
    val root = newRoot()
    given FSContext = root
    var text = ""
    ClickToEditFields.Textarea(() => text, t => { text = t; Js.Void }, emptyString = "Click to add notes")
    edit(root, " notes \n")
    assert(text == "notes")
  }

  "Select" should "save the option chosen (by its index)" in {
    val root = newRoot()
    given FSContext = root
    var color = "red"
    ClickToEditFields.Select[String](() => List("red", "green", "blue"), () => color, c => { color = c; Js.Void }, _.capitalize)
    edit(root, "2")
    assert(color == "blue")
    edit(root, "99") // not an option: nothing saved
    assert(color == "blue")
  }

  "DoubleOpt" should "save the number, or None for an empty input" in {
    val root = newRoot()
    given FSContext = root
    var v: Option[Double] = Some(1.5)
    ClickToEditFields.DoubleOpt(() => v, nv => { v = nv; Js.Void })
    edit(root, "2.25")
    assert(v.contains(2.25))
    edit(root, "")
    assert(v.isEmpty)
  }

  it should "keep the value when the input isn't a number" in {
    // Regression (fixed): an invalid number is saved as None (Try(str.toDouble).toOption): the value is erased.
    val root = newRoot()
    given FSContext = root
    var v: Option[Double] = Some(1.5)
    ClickToEditFields.DoubleOpt(() => v, nv => { v = nv; Js.Void })
    edit(root, "one and a half")
    assert(v.contains(1.5))
  }

  it should "ask for confirmation, when asked to" in {
    // Regression (fixed): DoubleOpt doesn't pass `confirm` on to Str: it's ignored.
    val root = newRoot()
    given FSContext = root
    var v: Option[Double] = None
    ClickToEditFields.DoubleOpt(() => v, nv => { v = nv; Js.Void }, confirm = Some(s => s"Set $s?"))
    assert(edit(root, "3").cmd.contains("confirm("))
  }

  "LocalDate" should "parse the date in any of the formats, and show it in the main one" in {
    val root = newRoot()
    given FSContext = root
    var d: Option[LocalDate] = None
    ClickToEditFields.LocalDate(() => d, nd => { d = nd; Js.Void })
    for (input <- Seq("2026-11-15", "15/11/2026", "2026/11/15", "15-11-2026", "20261115", "15 11 2026")) {
      d = None
      edit(root, input)
      assert(d.contains(LocalDate.of(2026, 11, 15)), input)
    }
  }

  it should "alert, and save nothing, for what isn't a date" in {
    val root = newRoot()
    given FSContext = root
    var d: Option[LocalDate] = Some(LocalDate.of(2000, 1, 1))
    ClickToEditFields.LocalDate(() => d, nd => { d = nd; Js.Void }, failedToParseDate = "Bad date")
    assert(edit(root, "someday").cmd.contains("alert("))
    assert(d.contains(LocalDate.of(2000, 1, 1)))
  }
}
