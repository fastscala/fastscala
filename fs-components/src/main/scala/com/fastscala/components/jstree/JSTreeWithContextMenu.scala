package com.fastscala.components.jstree

import com.fastscala.components.bootstrap5.toast.BSToast2
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

  def runF: FSContext => String => Js
}

abstract class DefaultJSTreeContextMenuAction(
                                      val label: String,
                                      val shortcut: Option[Int] = None,
                                      val shortcutLabel: Option[String] = None,
                                      val separatorBefore: Boolean = false,
                                      val separatorAfter: Boolean = true,
                                      val disabled: Boolean = false,
                                      val icon: Option[String] = None,
                                      val subactions: Seq[JSTreeContextMenuAction] = Nil
                                    ) extends JSTreeContextMenuAction

object DefaultJSTreeContextMenuAction {
  def apply(
              label: String,
              run: FSContext => String => Js,
              shortcut: Option[Int] = None,
              shortcutLabel: Option[String] = None,
              separatorBefore: Boolean = false,
              separatorAfter: Boolean = true,
              disabled: Boolean = false,
              icon: Option[String] = None,
              subactions: Seq[JSTreeContextMenuAction] = Nil,
            ): DefaultJSTreeContextMenuAction =
    new DefaultJSTreeContextMenuAction(
      label = label,
      shortcut = shortcut,
      shortcutLabel = shortcutLabel,
      separatorBefore = separatorBefore,
      separatorAfter = separatorAfter,
      disabled = disabled,
      icon = icon,
      subactions = subactions,
    ):
      override val runF: FSContext => String => Js = run
}

trait JSTreeNodeWithContextMenu[T, N <: JSTreeNodeWithContextMenu[T, N]]
        (implicit jsTree: JSTreeWithContextMenu[T, N]) extends JSTreeNode[T, N] {
  self: N =>

  def actions: Seq[JSTreeContextMenuAction]

  var title: String
  def titleNs = scala.xml.Text(title)

  val children: collection.mutable.ArrayBuffer[N]
  def childrenF = () => children

  def allowDuplicated: Boolean = true

  import JSTreeNodeWithContextMenu.*

  def onEditJs(onEdit: (N, String) => Js)(implicit fsc: FSContext): Js = {
    import com.fastscala.core.circe.CirceSupport.*

    fsc.callbackJSONDecoded[OnEditData](
      Js("{id: node.id, text: node.text}"), data => {
        val node = jsTree.findNode(data.id)
        if (node.title != data.text && !allowDuplicated) {
          val (pid, _) = data.id.splitAt(data.id.lastIndexOf("_"))
          if (pid.nonEmpty && jsTree.findNode(pid).children.exists(_.title == data.text)) {
            BSToast2.VerySimple(<label class="text-danger">Error</label>)
              (<p class="text-danger">Duplicated title found: {data.text}.</p>)
                .installAndShow() &
              jsTree.editJSTreeNode(data.id, onEditJs(onEdit), text = Some(node.title))
          } else
            onEdit(node, data.text)
        } else
          onEdit(node, data.text)
    })
  }

  class DefaultCreateAction(
    label: String,
    shortcut: Option[Int] = None,
    shortcutLabel: Option[String] = None,
    separatorBefore: Boolean = false,
    separatorAfter: Boolean = true,
    disabled: Boolean = false,
    icon: Option[String] = None,
    subactions: Seq[JSTreeContextMenuAction] = Nil,
    onCreate: String => N,
    onEdit: (N, String) => Js,
  ) extends DefaultJSTreeContextMenuAction(
    label = label,
    shortcut = shortcut,
    shortcutLabel = shortcutLabel,
    separatorBefore = separatorBefore,
    separatorAfter = separatorAfter,
    disabled = disabled,
    icon = icon,
    subactions = subactions,
  ) {
    override val runF: FSContext => String => Js =
      implicit fsc => id =>
        jsTree.findNode(id).children.pipe { children =>
          val subId = s"${id}_Sub${children.length}"
          children.append(onCreate(subId))
          jsTree.loadAndEditJSTreeNode(id, subId, onEditJs(onEdit))
        }
  }

  class DefaultRenameAction(
    label: String,
    shortcut: Option[Int] = None,
    shortcutLabel: Option[String] = None,
    separatorBefore: Boolean = false,
    separatorAfter: Boolean = true,
    disabled: Boolean = false,
    icon: Option[String] = None,
    subactions: Seq[JSTreeContextMenuAction] = Nil,
    onEdit: (N, String) => Js,
  ) extends DefaultJSTreeContextMenuAction(
    label = label,
    shortcut = shortcut,
    shortcutLabel = shortcutLabel,
    separatorBefore = separatorBefore,
    separatorAfter = separatorAfter,
    disabled = disabled,
    icon = icon,
    subactions = subactions,
  ) {
    override val runF: FSContext => String => Js =
      implicit fsc => id => jsTree.editJSTreeNode(id, onEditJs(onEdit))
  }

  class DefaultRemoveAction(
    label: String,
    shortcut: Option[Int] = None,
    shortcutLabel: Option[String] = None,
    separatorBefore: Boolean = false,
    separatorAfter: Boolean = true,
    disabled: Boolean = false,
    icon: Option[String] = None,
    subactions: Seq[JSTreeContextMenuAction] = Nil,
    onRemove: (N, String) => Js,
  ) extends DefaultJSTreeContextMenuAction(
    label = label,
    shortcut = shortcut,
    shortcutLabel = shortcutLabel,
    separatorBefore = separatorBefore,
    separatorAfter = separatorAfter,
    disabled = disabled,
    icon = icon,
    subactions = subactions,
  ) {
    override val runF: FSContext => String => Js =
      implicit fsc => id =>
        val (pid, _) = id.splitAt(id.lastIndexOf("_"))
        if (pid.nonEmpty) {
          jsTree.findNode(pid).children.pipe{ children =>
            onRemove(children.remove(children.indexWhere(_.id == id)), pid) &
                jsTree.refreshJSTreeNode(pid)
          }
        }
        else JS.void
  }
}

object JSTreeNodeWithContextMenu {
  case class OnEditData(id: String, text: String)

  import io.circe.generic.semiauto.*
  implicit lazy val decoder: io.circe.Decoder[OnEditData] = deriveDecoder
}

trait JSTreeWithContextMenu[T, N <: JSTreeNodeWithContextMenu[T, N]] extends JSTree[T, N] {

  case class RenderableMenuAction(
                                   action: Option[Js],
                                   _disabled: Option[Boolean],
                                   icon: Option[String],
                                   label: Option[String],
                                   separator_after: Option[Boolean],
                                   separator_before: Option[Boolean],
                                   shortcut: Option[Int],
                                   shortcut_label: Option[String],
                                   submenu: Option[Map[String, RenderableMenuAction]],
                                 ) {

    def this(node: N, action: JSTreeContextMenuAction)(implicit fsc: FSContext) = this(
      action = if (action.subactions.nonEmpty) Some(JS._false) else {
        fsc.runInNewOrRenewedChildContextFor((this, node.id, action.label))(implicit fsc => {
          Some(JS.function()(fsc.callback(() => action.runF(fsc)(node.id))))
        })
      },
      _disabled = Some(action.disabled),
      icon = action.icon,
      label = Some(action.label),
      separator_after = Some(action.separatorAfter),
      separator_before = Some(action.separatorBefore),
      shortcut = action.shortcut,
      shortcut_label = action.shortcutLabel,
      submenu = action.subactions match {
        case Nil => None
        case actions => Some(
          actions.map(action =>
            JS.asJsStr(action.label).cmd -> new RenderableMenuAction(node, action)
          ).toMap
        )
      },
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
        case None => throw new Exception(s"Could not find node for id '$id'")
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
