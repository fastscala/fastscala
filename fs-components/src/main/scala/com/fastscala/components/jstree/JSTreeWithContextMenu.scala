package com.fastscala.components.jstree

import com.fastscala.components.jstree.config.{ContextMenu, Core, Data, JSTreeConfig}
import com.fastscala.components.jstree.{JSTree, JSTreeNode}
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import scala.util.chaining.scalaUtilChainingOps

trait JSTreeContextMenuAction {

  def separatorBefore: Boolean

  def separatorAfter: Boolean

  def disabled: Boolean

  /**
   * Must be unique!
   */
  def label: String

  /**
   * keyCode which will trigger the action if the menu is open
   */
  def shortcut: Option[Int]

  def shortcutLabel: Option[String]

  def icon: Option[String]

  def subactions: Seq[JSTreeContextMenuAction]

  def action: Option[FSContext => Js]
}

class DefaultJSTreeContextMenuAction(
                                      val label: String,
                                      val action: Option[FSContext => Js] = None,
                                      val shortcut: Option[Int] = None,
                                      val shortcutLabel: Option[String] = None,
                                      val separatorBefore: Boolean = false,
                                      val separatorAfter: Boolean = true,
                                      val disabled: Boolean = false,
                                      val icon: Option[String] = None,
                                      val subactions: Seq[JSTreeContextMenuAction] = Nil
                                    ) extends JSTreeContextMenuAction

trait JSTreeNodeWithContextMenu[N <: JSTreeNodeWithContextMenu[N]] extends JSTreeNode[N] {
  self: N =>

  def actions: Seq[JSTreeContextMenuAction]
}

trait JSTreeWithContextMenu[N <: JSTreeNodeWithContextMenu[N]] extends JSTree[N] {

  case class RenderableMenuAction(
                                   action: Option[Js],
                                   _disabled: Option[Boolean],
                                   icon: Option[String],
                                   label: Option[String],
                                   separator_after: Option[Boolean],
                                   separator_before: Option[Boolean],
                                   shortcut: Option[Int],
                                   shortcut_label: Option[String],
                                   submenu: Option[String],
                                 ) {

    def this(node: N, action: JSTreeContextMenuAction)(implicit fsc: FSContext) = this(
      action = Some {
        JS.function0(
          action.action.map(js => {
            fsc.runInNewOrRenewedChildContextFor((this, node.id, action.label))(implicit fsc => {
              js(fsc)
            })
          }).getOrElse(JS.void)
        )
      },
      _disabled = Some(action.disabled),
      icon = action.icon,
      label = Some(action.label),
      separator_after = Some(action.separatorAfter),
      separator_before = Some(action.separatorBefore),
      shortcut = action.shortcut,
      shortcut_label = action.shortcutLabel,
      submenu = None,
    )
  }

  object RenderableMenuAction {

    import upickle.default._
    import com.fastscala.components.utils.upickle.JsWriter

    implicit lazy val writer: Writer[RenderableMenuAction] = macroW
  }

  override def plugins: List[String] = "contextmenu" :: super.plugins

  /**
   * Indicates if the node should be selected when the context menu is invoked on it
   */
  def selectNode: Boolean = true

  /**
   * Indicates if the menu should be shown aligned with the node. Otherwise the mouse coordinates are used.
   */
  def showAtNode: Boolean = true

  def renderJSTreeContextMenuAction(node: N, action: JSTreeContextMenuAction)(implicit fsc: FSContext): String = {

    import upickle.default._
    import upickle.default.{ReadWriter => RW, macroRW}

    write(new RenderableMenuAction(node, action))
  }

  override def jsTreeConfig(implicit fsc: FSContext): JSTreeConfig = {
    import com.softwaremill.quicklens._
    val menuItemsCallback = fsc.callback(Js("item.id"), id => {
      nodeById.get(id) match {
        case Some(node) =>
          val menuItems = node.actions.map(action => JS.asJsStr(action.label).cmd + ": " + renderJSTreeContextMenuAction(node, action)).mkString("({", ",", "})")
          Js(s"env.callback(eval(${JS.asJsStr(menuItems)}));")
        case None =>
          throw new Exception(s"Could not find node for id '$id' (Existing ids: ${nodeById.keys.toSeq.sorted.mkString("; ")})")
      }
    }, env = Js("{callback: callback}"))
    super.jsTreeConfig.pipe(config =>
      config.modify(_.contextmenu).setTo(Some(ContextMenu(
        select_node = Some(selectNode),
        show_at_node = Some(showAtNode),
        items = Some(Js(s"function(item, callback) { $menuItemsCallback }"))
      )))
    )
  }
}
