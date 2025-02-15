package com.fastscala.components.jstree

import com.fastscala.components.jstree.config.{Core, Data, JSTreeConfig}
import com.fastscala.components.utils.{ElemWithId, ElemWithRandomId}
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.scala_xml.fsc.anonymousPageURLScalaXml
import com.fastscala.scala_xml.js.{JS, inScriptTag, printBeforeExec}
import org.eclipse.jetty.server.Request

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

class JSTreeLazyLoadNode[T](
                             title: String,
                             val value: T,
                             val id: String,
                             val open: Boolean = false,
                             val disabled: Boolean = false,
                             val icon: Option[String] = None,
                           )(val childrenF: () => Seq[JSTreeLazyLoadNode[T]]) extends JSTreeNode[JSTreeLazyLoadNode[T]] {
  override def titleNs: NodeSeq = scala.xml.Text(title)
}

class JSTreeSimpleNode[T](
                           title: String,
                           val value: T,
                           val id: String,
                           val open: Boolean = false,
                           val disabled: Boolean = false,
                           val icon: Option[String] = None,
                         )(children: Seq[JSTreeSimpleNode[T]]) extends JSTreeNode[JSTreeSimpleNode[T]] {
  override def titleNs: NodeSeq = scala.xml.Text(title)

  def childrenF = () => children
}

abstract class JSTreeNode[+N <: JSTreeNode[N]] {
  self: N =>

  def titleNs: NodeSeq

  def id: String

  def open: Boolean

  def disabled: Boolean

  def icon: Option[String]

  def childrenF: () => Seq[N]

  def renderLi(registerNodes: Seq[N] => Unit): Elem = {
    val loadedChildren = if (open) childrenF() else Nil
    if (loadedChildren.nonEmpty) {
      registerNodes(loadedChildren)
    }
    val appendedChildren = if (open) <ul>{loadedChildren.map(_.renderLi(registerNodes)).mkNS}</ul> else NodeSeq.Empty
    val dataJSTree = Some(List(icon.map(icon => s""""icon":"$icon""""), Some(disabled).filter(_ == true).map(disabled => s""""disabled":$disabled""")).flatten).filter(_.nonEmpty).map(_.mkString("{", ",", "}")).getOrElse(null)
    <li id={id} data-jstree={dataJSTree} class={if (!open) "jstree-closed" else ""}>{titleNs}{appendedChildren}</li>
  }
}

abstract class JSTree[N <: JSTreeNode[N]] extends ElemWithRandomId {

  def plugins: List[String] = Nil

  def rootNodes: Seq[N]

  def render()(implicit fsc: FSContext): Elem = <div id={elemId}></div>

  def renderAndInit()(implicit fsc: FSContext): NodeSeq = render() ++ init().onDOMContentLoaded.inScriptTag

  //  protected val childrenOfId = collection.mutable.Map[String, Seq[N]]()
  protected val nodeById = collection.mutable.Map[String, N]()

  def jsTreeConfig(implicit fsc: FSContext): JSTreeConfig = {
    implicit def nonOption2Option[T](v: T): Option[T] = Some(v)

    JSTreeConfig(
      core = Core(
        check_callback = true,
        data = Data(
          data = Js("""function (node) { return { 'id' : node.id }; }""")
        )
      ),
      plugins = this.plugins,
    )
  }

  def refresh(): Js = Js(s"""$$('#$elemId').jstree(true).refresh($$('#$elemId').jstree(true).get_node('#'));""").printBeforeExec

  def registerNodes(nodes: Seq[N]): Unit = {
    nodes.foreach(node => {
      nodeById += (node.id -> node)
    })
  }

  def init()(implicit fsc: FSContext): Js = Js {
    val callback = fsc.anonymousPageURLScalaXml(implicit fsc => {
      Option(Request.getParameters(fsc.page.req).getValue("id")) match {
        case Some("#") => <ul>{rootNodes.tap(registerNodes).map(_.renderLi(registerNodes)).mkNS}</ul>
        case Some(id) => <ul>{nodeById(id).childrenF().tap(registerNodes).map(_.renderLi(registerNodes)).mkNS}</ul>
        case None => throw new Exception(s"Id parameter not found")
      }
    }, "nodes.html")

    import com.softwaremill.quicklens._
    val config: JSTreeConfig = jsTreeConfig.pipe(config =>
      config.modify(_.core).setToIf(config.core.isEmpty)(Some(Core())).pipe(config =>
        config.modify(_.core.each.data).setToIf(config.core.get.data.isEmpty)(Some(Data())).pipe(config =>
          config.modify(_.core.each.data.each.url).setTo(Some(callback))
        )
      )
    )

    import upickle.default._
    s"""$$('#$elemId').jstree(${write(config)});"""
  }
}
