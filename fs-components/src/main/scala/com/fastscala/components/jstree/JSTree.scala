package com.fastscala.components.jstree

import com.fastscala.components.jstree.config.{Core, Data, JSTreeConfig, Themes}
import com.fastscala.components.utils.{ElemWithId, ElemWithRandomId}
import com.fastscala.core.FSContext
import com.fastscala.js.{Js, JsFunc2}
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.scala_xml.fsc.anonymousPageURLScalaXml
import com.fastscala.scala_xml.js.{JS, inScriptTag, printBeforeExec}
import org.eclipse.jetty.server.Request

import scala.collection.mutable.ListBuffer
import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

class JSTreeLazyLoadNode[T](title: String, val value: T, val id: String, val open: Boolean = false, val disabled: Boolean = false, val icon: Option[String] = None)(
  val childrenF: () => Seq[JSTreeLazyLoadNode[T]]
) extends JSTreeNode[JSTreeLazyLoadNode[T]] {
  override def titleNs: NodeSeq = scala.xml.Text(title)
}

class JSTreeSimpleNode[T](title: String, val value: T, val id: String, val open: Boolean = false, val disabled: Boolean = false, val icon: Option[String] = None)(children: Seq[JSTreeSimpleNode[T]])
    extends JSTreeNode[JSTreeSimpleNode[T]] {
  override def titleNs: NodeSeq = scala.xml.Text(title)

  def childrenF = () => children
}

abstract class JSTreeNode[+N <: JSTreeNode[N]] {
  self: N =>

  def titleNs: NodeSeq

  def id: String

  def open: Boolean

  def selected: Boolean = false

  def disabled: Boolean

  def icon: Option[String]

  def childrenF: () => collection.Seq[N]

  def allNodes: Seq[N] = this +: childrenF().flatMap(_.allNodes).toSeq

  def renderLi(registerNodes: collection.Seq[N] => Unit): Elem = {
    val loadedChildren: collection.Seq[N] = if (open) childrenF() else Nil
    if (loadedChildren.nonEmpty) {
      registerNodes(loadedChildren.toSeq)
    }
    val appendedChildren = if (open) <ul>{loadedChildren.map(_.renderLi(registerNodes)).mkNS}</ul> else NodeSeq.Empty
    val dataJSTree = Some(
      List(
        icon.map(icon => s""""icon":"$icon""""),
        Some(disabled).filter(_ == true).map(disabled => s""""disabled":$disabled"""),
        Some(open).filter(_ == true).map(open => s""""opened":$open"""),
        Some(selected).filter(_ == true).map(open => s""""selected":$selected""")
      ).flatten
    ).filter(_.nonEmpty).map(_.mkString("{", ",", "}")).getOrElse(null)
    <li id={id} data-jstree={dataJSTree} class={if (!open) "jstree-closed" else ""}>{titleNs}{appendedChildren}</li>
  }

  def renameNode(onRename: String => Js)(implicit fsc: FSContext): Js =
    Js(s"""$$('#$id').jstree(true).edit('$id', null, function(node, success, cancelled){ if (!cancelled && success) {${fsc.callback(Js("node.text"), text => onRename(text)).cmd}}})""")
}

abstract class JSTree[N <: JSTreeNode[N]] extends ElemWithRandomId {

  def plugins: List[String] = Nil

  def rootNodes: Seq[N]

  def allNodes: Seq[N] = rootNodes.flatMap(_.allNodes)

  def render()(implicit fsc: FSContext): Elem = <div id={elemId}></div>

  def renderAndInit()(implicit fsc: FSContext): NodeSeq = render() ++ init().onDOMContentLoaded.inScriptTag

  //  protected val childrenOfId = collection.mutable.Map[String, Seq[N]]()
  protected val nodeById = collection.mutable.Map[String, N]()

  def findNode(id: String): N = nodeById(id)

  def jsTreeConfig(implicit fsc: FSContext): JSTreeConfig = {
    implicit def nonOption2Option[T](v: T): Option[T] = Some(v)

    JSTreeConfig(core = Core(check_callback = true, data = Data(data = Js("""function (node) { return { 'id' : node.id }; }""")), themes = Themes()), plugins = this.plugins)
  }

  def refresh(): Js = Js(s"""$$('#$elemId').jstree(true).refresh($$('#$elemId').jstree(true).get_node('#'));""")

  def registerNodes(nodes: collection.Seq[N]): Unit = {
    nodes.foreach(node => {
      nodeById += (node.id -> node)
    })
  }

  def init(using fsc: FSContext)(config: JSTreeConfig = jsTreeConfig, onSelect: Js = JS.void): Js = Js {
    val callback = fsc.anonymousPageURLScalaXml(
      implicit fsc => {
        Option(Request.getParameters(fsc.page.req).getValue("id")) match {
          case Some("#") => <ul>{rootNodes.tap(registerNodes).map(_.renderLi(registerNodes)).mkNS}</ul>
          case Some(id)  => <ul>{nodeById(id).childrenF().tap(registerNodes).map(_.renderLi(registerNodes)).mkNS}</ul>
          case None      => throw new Exception(s"Id parameter not found")
        }
      },
      "nodes.html"
    )

    import com.softwaremill.quicklens.*
    val jsTreeConfig =
      config.modify(_.core).setToIf(config.core.isEmpty)(Some(Core())).pipe(config =>
        config.modify(_.core.each.data).setToIf(config.core.get.data.isEmpty)(Some(Data())).pipe(config => config.modify(_.core.each.data.each.url).setTo(Some(callback)))
      )

    import upickle.default.*
    s"""$$('#$elemId').on("changed.jstree", function(e, data){${onSelect.cmd}}).jstree(${write(jsTreeConfig)});"""
  }

  def jsTreeRef: Js = Js(s"""$$('#$elemId')""")

  /** triggered after all events are bound
    */
  def onInit(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("init.jstree", $func);""")

  /** triggered after the loading text is shown and before loading starts
    */
  def onLoading(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("loading.jstree", $func);""")

  /** triggered before the tree is destroyed
    */
  def onDestroy(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("destroy.jstree", $func);""")

  /** triggered after the root node is loaded for the first time
    */
  def onLoaded(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("loaded.jstree", $func);""")

  /** triggered after all nodes are finished loading
    */
  def onReady(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("ready.jstree", $func);""")

  /** triggered after a node is loaded
    */
  def onLoadNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("load_node.jstree", $func);""")

  /** triggered after a load_all call completes
    */
  def onLoadAll(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("load_all.jstree", $func);""")

  /** triggered when new data is inserted to the tree model
    */
  def onModel(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("model.jstree", $func);""")

  /** triggered after nodes are redrawn
    */
  def onRedraw(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("redraw.jstree", $func);""")

  /** triggered when a node is about to be opened (if the node is supposed to be in the DOM, it will be, but it won't be visible yet)
    */
  def onBeforeOpen(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("before_open.jstree", $func);""")

  /** triggered when a node is opened (if there is an animation it will not be completed yet)
    */
  def onOpenNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("open_node.jstree", $func);""")

  /** triggered when a node is opened and the animation is complete
    */
  def onAfterOpen(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("after_open.jstree", $func);""")

  /** triggered when a node is closed (if there is an animation it will not be complete yet)
    */
  def onCloseNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("close_node.jstree", $func);""")

  /** triggered when a node is closed and the animation is complete
    */
  def onAfterClose(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("after_close.jstree", $func);""")

  /** triggered when an open_all call completes
    */
  def onOpenAll(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("open_all.jstree", $func);""")

  /** triggered when an close_all call completes
    */
  def onCloseAll(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("close_all.jstree", $func);""")

  /** triggered when an node is enabled
    */
  def onEnableNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("enable_node.jstree", $func);""")

  /** triggered when an node is disabled
    */
  def onDisableNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("disable_node.jstree", $func);""")

  /** triggered when an node is hidden
    */
  def onHideNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("hide_node.jstree", $func);""")

  /** triggered when an node is shown
    */
  def onShowNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("show_node.jstree", $func);""")

  /** triggered when all nodes are hidden
    */
  def onHideAll(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("hide_all.jstree", $func);""")

  /** triggered when all nodes are shown
    */
  def onShowAll(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("show_all.jstree", $func);""")

  /** triggered when an node is clicked or intercated with by the user
    */
  def onActivateNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("activate_node.jstree", $func);""")

  /** triggered when an node is hovered
    */
  def onHoverNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("hover_node.jstree", $func);""")

  /** triggered when an node is no longer hovered
    */
  def onDehoverNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("dehover_node.jstree", $func);""")

  /** triggered when an node is selected
    */
  def onSelectNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("select_node.jstree", $func);""")

  /** triggered when selection changes
    */
  def onChanged(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("changed.jstree", $func);""")

  /** triggered when an node is deselected
    */
  def onDeselectNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("deselect_node.jstree", $func);""")

  /** triggered when all nodes are selected
    */
  def onSelectAll(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("select_all.jstree", $func);""")

  /** triggered when all nodes are deselected
    */
  def onDeselectAll(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("deselect_all.jstree", $func);""")

  /** triggered when a set_state call completes
    */
  def onSetState(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("set_state.jstree", $func);""")

  /** triggered when a refresh call completes
    */
  def onRefresh(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("refresh.jstree", $func);""")

  /** triggered when a node is refreshed
    */
  def onRefreshNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("refresh_node.jstree", $func);""")

  /** triggered when a node id value is changed
    */
  def onSetId(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("set_id.jstree", $func);""")

  /** triggered when a node text value is changed
    */
  def onSetText(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("set_text.jstree", $func);""")

  /** triggered when a node is created
    */
  def onCreateNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("create_node.jstree", $func);""")

  /** triggered when a node is renamed
    */
  def onRenameNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("rename_node.jstree", $func);""")

  /** triggered when a node is deleted
    */
  def onDeleteNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("delete_node.jstree", $func);""")

  /** triggered when a node is moved
    */
  def onMoveNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("move_node.jstree", $func);""")

  /** triggered when a node is copied
    */
  def onCopyNode(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("copy_node.jstree", $func);""")

  /** triggered when nodes are added to the buffer for moving
    */
  def onCut(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("cut.jstree", $func);""")

  /** triggered when nodes are added to the buffer for copying
    */
  def onCopy(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("copy.jstree", $func);""")

  /** triggered when paste is invoked
    */
  def onPaste(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("paste.jstree", $func);""")

  /** triggered when the copy / cut buffer is cleared
    */
  def onClearBuffer(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("clear_buffer.jstree", $func);""")

  /** triggered when a theme is set
    */
  def onSetTheme(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("set_theme.jstree", $func);""")

  /** triggered when stripes are shown
    */
  def onShowStripes(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("show_stripes.jstree", $func);""")

  /** triggered when stripes are hidden
    */
  def onHideStripes(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("hide_stripes.jstree", $func);""")

  /** triggered when dots are shown
    */
  def onShowDots(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("show_dots.jstree", $func);""")

  /** triggered when dots are hidden
    */
  def onHideDots(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("hide_dots.jstree", $func);""")

  /** triggered when icons are shown
    */
  def onShowIcons(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("show_icons.jstree", $func);""")

  /** triggered when icons are hidden
    */
  def onHideIcons(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("hide_icons.jstree", $func);""")

  /** triggered when ellisis is shown
    */
  def onShowEllipsis(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("show_ellipsis.jstree", $func);""")

  /** triggered when ellisis is hidden
    */
  def onHideEllipsis(func: JsFunc2): Js = Js(s"""$$('#$elemId').on("hide_ellipsis.jstree", $func);""")

  def refreshJSTreeNode(node: String): Js =
    Js(s"""$$('#$elemId').jstree(true).refresh_node('$node')""")

  def loadAndEditJSTreeNode(parent: String, node: String, onEdit: Js): Js =
    Js(s"""$$('#$elemId').jstree(true).load_node('$parent', function(){ this.edit('$node', null, function(node, success, cancelled){ if (!cancelled && success) {${onEdit.cmd}} }) })""")

  def editJSTreeNode(node: String, onEdit: Js, text: Option[String] = None): Js =
    Js(s"""$$('#$elemId').jstree(true).edit('$node', ${text.map(t => s"'$t'").getOrElse("null")}, function(node, success, cancelled){ if (!cancelled && success) {${onEdit.cmd}} })""")
}
