package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.*
import com.fastscala.scala_xml.js.JS
import org.scalatest.flatspec.AnyFlatSpec

/**
 * A Rerenderer's context through rerenders ([[FSPage.inContextClearedFor]]): each rerender replaces it with a new
 * one, registered for the Rerenderer, under the same parent, and deletes the old one with its callbacks.
 */
class RerendererContextSpec extends AnyFlatSpec {

  import RerendererContextSpec.*

  private object NoImpl extends FSPageImpl[Unit] {
    override def render()(implicit fsc: FSContext): Unit = ()
  }

  "A Rerenderer" should "keep a single registered context, under the same parent, across rerenders" in {
    val page = new FSPage(session.nextID(), session, null, NoImpl)
    val root = page.rootFSContext
    val rerenderer = new Rerenderer(_ => implicit fsc => <div>{fsc.callback(() => JS.void).cmd}</div>)
    rerenderer.render()(using root)

    val first = page.key2FSContext(rerenderer)
    assert(first.parentFSContext.contains(root))
    assert(page.callbacks.size == 1)

    var previous = first
    for (i <- 1 to 3) {
      rerenderer.rerender()(using root)
      val ctx = page.key2FSContext.get(rerenderer)
      assert(ctx.isDefined, s"rerender $i: no context registered for the rerenderer")
      assert(ctx.get ne previous, s"rerender $i: the context wasn't replaced")
      assert(previous.deleted, s"rerender $i: the previous context wasn't deleted")
      assert(ctx.get.key.contains(rerenderer), s"rerender $i: the new context has no key")
      assert(ctx.get.parentFSContext.contains(root), s"rerender $i: the new context isn't under the same parent")
      assert(root.children.toSet == Set(ctx.get), s"rerender $i: the parent's children")
      assert(page.key2FSContext.size == 1, s"rerender $i: ${page.key2FSContext.size} contexts registered")
      assert(page.callbacks.size == 1, s"rerender $i: ${page.callbacks.size} callbacks (the old ones should be deleted)")
      previous = ctx.get
    }
  }
}

object RerendererContextSpec {
  // FSStats registers its metrics globally: a single FSSystem per JVM.
  lazy val system: FSSystem = new FSSystem(appName = "rerenderer-context-spec")
  lazy val session: FSSession = new FSSession("rerenderer-context-spec-session", system)
}
