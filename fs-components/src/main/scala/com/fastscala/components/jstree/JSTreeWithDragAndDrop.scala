package com.fastscala.components.jstree

import com.fastscala.components.bootstrap5.toast.BSToast2
import com.fastscala.components.jstree.config.{ContextMenu, Core, Data, JSTreeConfig}
import com.fastscala.components.jstree.{JSTree, JSTreeNode}
import com.fastscala.core.FSContext
import com.fastscala.core.circe.CirceSupport.FSContextWithCirceSupport
import com.fastscala.js.{Js, JsFunc2}
import com.fastscala.scala_xml.js.{JS, printBeforeExec}
import io.circe.Decoder
import io.circe.generic.semiauto

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.NodeSeq

trait JSTreeNodeWithDragAndDrop[N <: JSTreeNodeWithDragAndDrop[N]] extends JSTreeNode[N] {
  self: N =>

  def removedChildNode(child: N, fromIdx: Int): Js

  def addedChildNode(child: N, toIdx: Int): Js
}

object JSTreeNodeWithDragAndDrop {
  case class OnEditData(id: String, text: String)

  import io.circe.generic.semiauto.*

  implicit lazy val decoder: io.circe.Decoder[OnEditData] = deriveDecoder
}

trait JSTreeWithDragAndDrop[N <: JSTreeNodeWithDragAndDrop[N]] extends JSTree[N] {

  override def plugins: List[String] = "dnd" :: super.plugins

  def movedNode(node: N, fromParent: N, fromIdx: Int, toParent: N, toIdx: Int): Js = {
    fromParent.removedChildNode(node, fromIdx) &
      toParent.addedChildNode(node, toIdx)
  }

  def onMoveNode(onMoveNodeFromTo: JsFunc2)(implicit fsc: FSContext): Js = Js(s"""$$('#$elemId').on("move_node.jstree", $onMoveNodeFromTo);""")

  override def init(using fsc: FSContext)(config: JSTreeConfig, onSelect: Js): Js = super.init(config, onSelect) & fsc.runInContextFor(this) { implicit fsc =>
    onMoveNode(new JsFunc2((event, data) => {

      case class Data(nodeId: String, parentId: String, position: Int, oldParentId: String, oldPosition: Int)
      implicit val jsonDecoder: Decoder[Data] = semiauto.deriveDecoder[Data]
      fsc.callbackJSONDecoded[Data](
        Js(s"{nodeId: $data.node.id, parentId: $data.parent, position: $data.position, oldParentId: $data.old_parent, oldPosition: $data.old_position}"),
        { case Data(nodeId, parentId, position, oldParentId, oldPosition) =>
          (for {
            node <- nodeById.get(nodeId)
            parent <- nodeById.get(parentId)
            oldParent <- nodeById.get(oldParentId)
          } yield {
            movedNode(node, oldParent, oldPosition, parent, position)
          }).get
        }
      )
    }))
  }
}
