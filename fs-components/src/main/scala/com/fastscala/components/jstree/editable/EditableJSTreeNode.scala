package com.fastscala.components.jstree.editable


import com.fastscala.components.bootstrap5.toast.BSToast2
import com.fastscala.components.jstree.config.{ContextMenu, Core, Data, JSTreeConfig}
import com.fastscala.components.jstree.*
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.NodeSeq


trait EditableJSTreeNode[N <: EditableJSTreeNode[N]](implicit jsTree: JSTreeWithContextMenu[N]) extends JSTreeNodeWithContextMenu[N] {
  self: N =>

  def actions: Seq[JSTreeContextMenuAction]

  var title: String

  def titleNs = scala.xml.Text(title)

  val children: collection.mutable.ArrayBuffer[N]

  def childrenF = () => children

  def allowDuplicated: Boolean = true

  import EditableJSTreeNode.*

  def onEditJs(onEdit: (N, String) => Js)(implicit fsc: FSContext): Js = {
    import com.fastscala.core.circe.CirceSupport.*

    fsc.callbackJSONDecoded[OnEditData](
      Js("{id: node.id, text: node.text}"), data => {
        val node = jsTree.findNode(data.id)
        if (node.title != data.text && !allowDuplicated) {
          val (pid, _) = data.id.splitAt(data.id.lastIndexOf("_"))
          if (pid.nonEmpty && jsTree.findNode(pid).children.exists(_.title == data.text)) {
            BSToast2.VerySimple(<label class="text-danger">Error</label>)(
                <p class="text-danger">Duplicated title found: {data.text}.</p>
              ).installAndShow() &
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
    action = Some(implicit fsc =>
      jsTree.findNode(id).children.pipe { children =>
        val subId = s"${id}_Sub${children.length}"
        children.append(onCreate(subId))
        jsTree.loadAndEditJSTreeNode(id, subId, onEditJs(onEdit))
      }),
    shortcut = shortcut,
    shortcutLabel = shortcutLabel,
    separatorBefore = separatorBefore,
    separatorAfter = separatorAfter,
    disabled = disabled,
    icon = icon,
    subactions = subactions,
  )

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
    action = Some(implicit fsc => jsTree.editJSTreeNode(id, onEditJs(onEdit))),
    shortcut = shortcut,
    shortcutLabel = shortcutLabel,
    separatorBefore = separatorBefore,
    separatorAfter = separatorAfter,
    disabled = disabled,
    icon = icon,
    subactions = subactions,
  )

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
    action = Some(implicit fsc => {
      val (pid, _) = id.splitAt(id.lastIndexOf("_"))
      if (pid.nonEmpty) {
        jsTree.findNode(pid).children.pipe { children =>
          onRemove(children.remove(children.indexWhere(_.id == id)), pid) &
            jsTree.refreshJSTreeNode(pid)
        }
      } else JS.void
    }),
    shortcut = shortcut,
    shortcutLabel = shortcutLabel,
    separatorBefore = separatorBefore,
    separatorAfter = separatorAfter,
    disabled = disabled,
    icon = icon,
    subactions = subactions,
  )
}

object EditableJSTreeNode {
  case class OnEditData(id: String, text: String)

  import io.circe.generic.semiauto.*

  implicit lazy val decoder: io.circe.Decoder[OnEditData] = deriveDecoder
}
