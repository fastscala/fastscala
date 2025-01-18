package com.fastscala.components.jstree

import com.fastscala.components.jstree.{JSTree, JSTreeNode}
import com.fastscala.core.FSContext
import com.fastscala.js.Js

trait JSTreeContextMenuAction {

  def separatorBefore: Boolean

  def separatorAfter: Boolean

  def disabled: Boolean

  def action: Boolean

  def label: String

  /**
   * keyCode which will trigger the action if the menu is open
   */
  def shortcut: Option[Int]

  def shortcutLabel: Option[String]

  def icon: Option[String]

  def submenu: Seq[JSTreeContextMenuAction]

  def action(implicit fsc: FSContext): Js
}

abstract class DefaultJSTreeContextMenuAction(
                                               val label: String,
                                               val shortcut: Option[Int],
                                               val shortcutLabel: Option[String],
                                               val separatorBefore: Boolean = false,
                                               val separatorAfter: Boolean = true,
                                               val disabled: Boolean = false,
                                               val icon: Option[String] = None,
                                               val subactions: Seq[JSTreeContextMenuAction] = Nil
                                             ) {
  def action: Boolean = subactions.nonEmpty
}

trait JSTreeNodeWithContextMenu[T, N <: JSTreeNodeWithContextMenu[T, N]] extends JSTreeNode[T, N] {
  self: N =>

  def actions: Seq[JSTreeContextMenuAction]
}

trait JSTreeWithContextMenu[T, N <: JSTreeNodeWithContextMenu[T, N]] extends JSTree[T, N] {

  override def plugins: List[String] = "contextmenu" :: super.plugins

  /**
   * Indicates if the node should be selected when the context menu is invoked on it
   */
  def selectNode: Boolean = true

  /**
   * Indicates if the menu should be shown aligned with the node. Otherwise the mouse coordinates are used.
   */
  def showAtNode: Boolean = true
}

