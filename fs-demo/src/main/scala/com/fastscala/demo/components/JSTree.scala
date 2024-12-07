package com.fastscala.demo.components

import com.fastscala.core.FSContext
import com.fastscala.scala_xml.fsc.anonymousPageURLScalaXml
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.scala_xml.js.{JS, inScriptTag}
import com.fastscala.components.utils.{ElemWithId, ElemWithRandomId}
import org.eclipse.jetty.server.Request

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

class JSTreeSimpleNode[T](
                           title: String,
                           val value: T,
                           val id: String,
                           val open: Boolean = false,
                           val disabled: Boolean = false,
                           val icon: Option[String] = None,
                         )(children: Seq[JSTreeNode[T]]) extends JSTreeNode[T] {
  override def titleNs: NodeSeq = scala.xml.Text(title)

  def childrenF = () => children
}

abstract class JSTreeNode[T] {

  def titleNs: NodeSeq

  def value: T

  def id: String

  def open: Boolean

  def disabled: Boolean

  def icon: Option[String]

  def childrenF: () => Seq[JSTreeNode[T]]

  def renderLi(): Elem = {
    val appendedChildren = if (open) <ul>{childrenF().map(_.renderLi()).mkNS}</ul> else NodeSeq.Empty
    val dataJSTree = Some(List(icon.map(icon => s""""icon":"$icon""""), Some(disabled).filter(_ == true).map(disabled => s""""disabled":$disabled""")).flatten).filter(_.nonEmpty).map(_.mkString("{", ",", "}")).getOrElse(null)
    <li id={id} data-jstree={dataJSTree} class={if (!open) "jstree-closed" else ""}>{titleNs}{appendedChildren}</li>
  }
}

abstract class JSTree[T] extends ElemWithRandomId {

  def rootNodes: Seq[JSTreeNode[T]]

  def render()(implicit fsc: FSContext): Elem = <div id={elemId}></div>

  def renderAndInit()(implicit fsc: FSContext): NodeSeq = render() ++ init().onDOMContentLoaded.inScriptTag

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
