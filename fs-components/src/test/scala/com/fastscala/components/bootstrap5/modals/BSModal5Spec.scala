package com.fastscala.components.bootstrap5.modals

import com.fastscala.core.FSContext
import com.fastscala.js.{Js, JsEscape}
import com.fastscala.testutils.PerfFS
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.{Elem, Node, NodeSeq}

class BSModal5Spec extends AnyFlatSpec {

  private def elems(n: Node): Seq[Elem] = n match {
    case e: Elem => e +: e.child.flatMap(elems)
    case _ => Nil
  }

  private def classes(e: Elem): Set[String] = e.attribute("class").map(_.text.split("\\s+").filter(_.nonEmpty).toSet).getOrElse(Set())

  private def withClass(n: Node, c: String): Seq[Elem] = elems(n).filter(e => classes(e).contains(c))

  private class TestModal(footer: Boolean = true, size: BSModal5Size.Value = BSModal5Size.Normal) extends BSModal5Base {
    override def modalSize: BSModal5Size.Value = size

    override def modalHeaderTitle: String = "Title & more"

    override def modalBodyContents()(implicit fsc: FSContext): NodeSeq = <p class="the-body">Body</p>

    override def modalFooterContents()(implicit fsc: FSContext): Option[NodeSeq] = if (footer) Some(<button class="the-footer">OK</button>) else None
  }

  private def newRoot(): FSContext = PerfFS.newPage().rootFSContext

  "renderModal" should "be Bootstrap's modal: dialog, content, header (title, close), body and footer" in {
    given FSContext = newRoot()
    val m = new TestModal()
    val modal = m.renderModal()
    assert(classes(modal) == Set("modal", "fade") && modal.attribute("id").map(_.text).contains(m.modalId))
    assert(modal.attribute("tabindex").map(_.text).contains("-1"))
    assert(withClass(modal, "modal-dialog").size == 1)
    val content = withClass(modal, "modal-content")
    assert(content.size == 1 && content.head.attribute("id").map(_.text).contains(m.modalContentId))
    assert(withClass(modal, "modal-title").head.text.trim == "Title & more")
    assert(withClass(modal, "btn-close").head.attribute("data-bs-dismiss").map(_.text).contains("modal"))
    assert(withClass(withClass(modal, "modal-body").head, "the-body").size == 1)
    assert(withClass(withClass(modal, "modal-footer").head, "the-footer").size == 1)
  }

  it should "hide the footer when there's none" in {
    given FSContext = newRoot()
    val modal = new TestModal(footer = false).renderModal()
    assert(withClass(modal, "modal-footer").isEmpty)
    assert(modal.toString.contains("""<div style="display:none;" id="""), modal) // (the footer's rerenderer's placeholder)
  }

  it should "give the dialog the size's class, and the classes added" in {
    given FSContext = newRoot()
    val m = new TestModal(size = BSModal5Size.Large)
    m.addClass("extra")
    val dialog = withClass(m.renderModal(), "modal-dialog").head
    assert(classes(dialog).contains("modal-lg") && classes(dialog).contains("extra"), classes(dialog))
    assert(BSModal5Size.values.toSeq.map(_.toString).filter(_.nonEmpty).forall(_.startsWith("modal-")))
  }

  "install" should "append the modal to the body, and create it with the options given" in {
    given FSContext = newRoot()
    val m = new TestModal()
    val js = m.install(backdropStatic = true, focus = false, keyboard = false).cmd
    assert(js.contains("document.body.appendChild("), js.take(500))
    assert(js.contains(s"new bootstrap.Modal(document.getElementById('${m.modalId}'), {"))
    assert(js.contains("backdrop: 'static',") && js.contains("keyboard: false,") && js.contains("focus: false,"))
    assert(new TestModal().install(backdrop = false).cmd.contains("backdrop: false,"))
  }

  it should "remove the modal, and delete its context, when it's hidden" in {
    val root = newRoot()
    given FSContext = root
    val m = new TestModal()
    var serverSide = List[String]()
    m.onHideDefaultListenerServerSide(_ => js => { serverSide :+= "first"; js & Js("first()") })
    m.onHideDefaultListenerServerSide(_ => js => { serverSide :+= "second"; js & Js("second()") })
    val js = m.install().cmd
    assert(js.contains(s"document.getElementById('${m.modalId}').addEventListener('hide.bs.modal', function (e) {"), js.takeRight(800))
    assert(root.page.key2FSContext.contains(m.modalRenderer))
    val onHide = root.page.callbacks.values.find(_.debugLbl.contains("eventListenerOnHide")).get
    val result = onHide.func("").cmd
    assert(!root.page.key2FSContext.get(m.modalRenderer).exists(!_.deleted), "the modal's context wasn't deleted")
    assert(serverSide == List("first", "second") && result == "first();second()", (serverSide, result))
  }

  "installAndShow" should "install and show it" in {
    given FSContext = newRoot()
    val m = new TestModal()
    assert(m.installAndShow().cmd.endsWith(m.show().cmd))
  }

  "show / hide / toggle / dispose / handleUpdate" should "call the Bootstrap modal's methods" in {
    val m = new TestModal()
    for ((js, method) <- Seq(m.show() -> "show", m.hide() -> "hide", m.toggle() -> "toggle", m.dispose() -> "dispose", m.handleUpdate() -> "handleUpdate"))
      assert(js.cmd == s"bootstrap.Modal.getInstance(document.getElementById('${m.modalId}')).$method()")
    given FSContext = newRoot()
    assert(m.hideAndRemoveAndDeleteContext().cmd == m.hide().cmd)
  }

  "The event listeners" should "listen to Bootstrap's modal events" in {
    val m = new TestModal()
    for ((js, event) <- Seq(m.addEventListenerOnShow(Js("a()")) -> "show", m.addEventListenerOnShown(Js("a()")) -> "shown", m.addEventListenerOnHide(Js("a()")) -> "hide", m.addEventListenerOnHidden(Js("a()")) -> "hidden"))
      assert(js.cmd == s"document.getElementById('${m.modalId}').addEventListener('$event.bs.modal', function (e) {a()});")
  }

  "verySimple / okCancel" should "install and show a modal with the contents, and the buttons" in {
    given FSContext = newRoot()
    // (The modal's HTML is in a JavaScript string: escaped.)
    def inJs(s: String) = JsEscape.escape(s)
    val simple = BSModal5.verySimple("Hi", "Close")(_ => _ => <p>simple body</p>).cmd
    assert(simple.contains(inJs("simple body")) && simple.contains(inJs(">Close<")) && simple.contains("'hidden.bs.modal'"), simple.take(300))
    val okCancel = BSModal5.okCancel("Sure?", _ => _ => Js("ok()"), okBtnText = "Yes", cancelBtnText = "No")(_ => _ => <p>question</p>).cmd
    assert(okCancel.contains(inJs("question")) && okCancel.contains(inJs(">Yes<")) && okCancel.contains(inJs(">No<")))
  }
}
