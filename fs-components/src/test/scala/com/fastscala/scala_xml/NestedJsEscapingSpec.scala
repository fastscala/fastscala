package com.fastscala.scala_xml

import com.fastscala.components.bootstrap5.modals.BSModal5
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.core.*
import com.fastscala.js.{CompositeJs, EncJs, Js, JsWriter}
import com.fastscala.scala_xml.js.{JS, JsWithScriptDetection, NodeSeqJs}
import com.fastscala.scala_xml.xml.streaming.XmlJsStream
import com.fastscala.testutils.PerfFS
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import com.fastscala.js.JsEscape
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDate
import scala.xml.NodeSeq

/** A modal whose OK callback rerenders a button whose callback alerts: each Js, written by each JsWriter, as its cmd. */
class NestedJsEscapingSpec extends AnyFlatSpec {

  private def written(js: Js, writer: JsWriter): String = {
    val bytes = new ByteArrayOutputStream()
    var outcome: Option[Throwable] = null
    writer.writeJs(js, new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
        while (bb.hasRemaining) bytes.write(bb.get())
        cb.succeeded()
      }
    }, Callback.from(() => outcome = None, (t: Throwable) => outcome = Some(t)), true)
    assert(outcome == None, outcome)
    bytes.toString(UTF_8)
  }

  /**
   * `js.cmd`, but with each NodeSeqJs's XML as XmlJsStream writes it (an ElemOptimized's toString puts its attributes in
   * another order): escaped by JsEscape, as EncJs.cmd does.
   */
  private def reference(js: Js): String = js match {
    case d: JsWithScriptDetection => reference(d.js)
    case c: CompositeJs => c.children.map(reference).mkString
    case n: NodeSeqJs =>
      val bytes = new ByteArrayOutputStream()
      XmlJsStream.writeSeq(n.ns, new Content.Sink {
        override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
          while (bb.hasRemaining) bytes.write(bb.get())
          cb.succeeded()
        }
      })
      bytes.toString(UTF_8)
    case e: EncJs =>
      val esc = JsEscape.escape(reference(e.js))
      if (e.inString) "\"" + esc + "\"" else esc
    case other => other.cmd
  }

  private def check(name: String, js: Js): Unit = {
    val cmd = reference(js)
    assert(cmd.length == js.cmd.length, s"$name: the reference differs from cmd in more than the attributes' order")
    val stream = written(js, XmlJsStream)
    info(s"$name: ${cmd.length} chars, ${"EncJs".r.findAllIn(structure(js)).size} EncJs (max nesting ${maxNesting(js)})")
    assert(stream == cmd, s"$name: XmlJsStream\n  written: $stream\n  cmd:     $cmd")
  }

  private def structure(js: Js): String = js match {
    case d: JsWithScriptDetection => structure(d.js)
    case c: CompositeJs => c.children.map(structure).mkString
    case e: EncJs => "EncJs(" + structure(e.js) + ")"
    case _ => ""
  }

  private def maxNesting(js: Js): Int = js match {
    case d: JsWithScriptDetection => maxNesting(d.js)
    case c: CompositeJs => (c.children.map(maxNesting) :+ 0).max
    case e: EncJs => 1 + maxNesting(e.js)
    case _ => 0
  }

  "A modal, rerendering a button on OK" should "be written as its cmd, and so should its callbacks' Js" in {
    val page = PerfFS.newPage()
    implicit val fsc: FSContext = page.rootFSContext
    val before = page.callbacks.keySet.toSet

    val modalJs: Js = BSModal5.okCancel(
      "Test modal",
      _ => implicit fsc => JS.confirm("Proceed?",
        JS.replace("btn", BSBtn().BtnDanger.lbl("Updated").callback(_ => JS.alert("Time on server: " + LocalDate.now().toString)).btn))) {
      modal => implicit fsc => <span>Test modal</span>
    }
    check("the modal", modalJs)
    check("the modal, in a JS.replace", JS.replace("modal", <div>{NodeSeq.Empty}</div>) & modalJs)

    // Its callbacks (Cancel's, OK's, and the one for when it's hidden), and then the ones they register (the "Updated"
    // button's): each one's are taken right after it runs, in the order they were registered (the one for when it's
    // hidden deletes the modal's context, and so the callbacks registered in it: those are still run, as they were).
    var seen = before
    def fresh(): List[FSFunc] = {
      val fs = page.callbacks.filter((id, _) => !seen(id)).values.toList.sortBy(f => (f.id.length, f.id))
      seen = seen ++ fs.map(_.id)
      fs
    }
    var pending: List[(FSFunc, Int)] = fresh().map(_ -> 0)
    var rounds = 0
    while (pending.nonEmpty) {
      val (f, round) = pending.head
      pending = pending.tail
      check(s"callback ${f.id} (round $round)", f.func(""))
      rounds = math.max(rounds, round + 1)
      if (round < 2) pending = pending ++ fresh().map(_ -> (round + 1))
    }
    assert(rounds >= 2, "the OK callback didn't register the button's")
  }
}
