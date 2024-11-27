package com.fastscala.demo.components

import com.fastscala.core.FSContext
import com.fastscala.scala_xml.fsc.anonymousPageURLScalaXml
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.utils.{ElemWithId, ElemWithRandomId}
import org.eclipse.jetty.server.Request

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

class JSTreeSimpleNode[T](
                           title: String,
                           val value: T,
                           val id: String,
                           val open: Boolean = false
                         )(children: Seq[JSTreeNode[T]]) extends JSTreeNode[T] {
  override def titleNs: NodeSeq = scala.xml.Text(title)

  def childrenF = () => children
}

abstract class JSTreeNode[T] {

  def titleNs: NodeSeq

  def value: T

  def id: String

  def open: Boolean

  def childrenF: () => Seq[JSTreeNode[T]]

  def renderLi(): Elem = {
    val appendedChildren = if (open) <ul>{childrenF().map(_.renderLi()).mkNS}</ul> else NodeSeq.Empty
    <li id={id} class={if (!open) "jstree-closed" else ""}>{titleNs}{appendedChildren}</li>
  }
}

abstract class JSTree[T] extends ElemWithRandomId {

  def rootNodes: Seq[JSTreeNode[T]]

  def render()(implicit fsc: FSContext): Elem = <div id={elemId}></div>

  //  protected val childrenOfId = collection.mutable.Map[String, Seq[JSTreeNode[T]]]()
  protected val nodeById = collection.mutable.Map[String, JSTreeNode[T]]()

  def init()(implicit fsc: FSContext): Js = Js {
    val callback = fsc.anonymousPageURLScalaXml(implicit fsc => {
      Option(Request.getParameters(fsc.page.req).getValue("id")) match {
        case Some("#") => <ul>{rootNodes.tap(_.foreach(node => nodeById += (node.id -> node))).map(_.renderLi()).mkNS}</ul>
        case Some(id) => <ul>{nodeById(id).childrenF().tap(_.foreach(node => nodeById += (node.id -> node))).map(_.renderLi()).mkNS}</ul>
        case None => throw new Exception(s"Id parameter not found")
      }
    }, "nodes.html")

    s"""
       |$$('#$elemId').jstree({
       |  'core' : {
       |    'data' : {
       |      'url' : '$callback',
       |      'data' : function (node) {
       |        console.log(node);
       |        return { 'id' : node.id };
       |      }
       |    }
       |  }
       |});
       |""".stripMargin
  }
}
