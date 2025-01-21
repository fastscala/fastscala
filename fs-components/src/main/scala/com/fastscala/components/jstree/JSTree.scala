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
                           )(val childrenF: () => Seq[JSTreeLazyLoadNode[T]]) extends JSTreeNode[T, JSTreeLazyLoadNode[T]] {
  override def titleNs: NodeSeq = scala.xml.Text(title)
}

class JSTreeSimpleNode[T](
                           title: String,
                           val value: T,
                           val id: String,
                           val open: Boolean = false,
                           val disabled: Boolean = false,
                           val icon: Option[String] = None,
                         )(children: Seq[JSTreeSimpleNode[T]]) extends JSTreeNode[T, JSTreeSimpleNode[T]] {
  override def titleNs: NodeSeq = scala.xml.Text(title)

  def childrenF = () => children
}

abstract class JSTreeNode[T, +N <: JSTreeNode[T, N]] {
  self: N =>

  def titleNs: NodeSeq

  def value: T

  def id: String

  def open: Boolean

  def disabled: Boolean

  def icon: Option[String]

  def childrenF: () => Seq[N]

  def renderLi(): Elem = {
    val appendedChildren = if (open) <ul>{childrenF().map(_.renderLi()).mkNS}</ul> else NodeSeq.Empty
    val dataJSTree = Some(List(icon.map(icon => s""""icon":"$icon""""), Some(disabled).filter(_ == true).map(disabled => s""""disabled":$disabled""")).flatten).filter(_.nonEmpty).map(_.mkString("{", ",", "}")).getOrElse(null)
    <li id={id} data-jstree={dataJSTree} class={if (!open) "jstree-closed" else ""}>{titleNs}{appendedChildren}</li>
  }
}

abstract class JSTree[T, N <: JSTreeNode[T, N]] extends ElemWithRandomId {

  def plugins: List[String] = Nil

  def rootNodes: Seq[N]

  def render()(implicit fsc: FSContext): Elem = <div id={elemId}></div>

  def renderAndInit()(implicit fsc: FSContext): NodeSeq = render() ++ init().onDOMContentLoaded.inScriptTag

  def rerender()(implicit fsc: FSContext): Js = JS.void

  //  protected val childrenOfId = collection.mutable.Map[String, Seq[N]]()
  protected val nodeById = collection.mutable.Map[String, N]()

  def jsTreeConfig(implicit fsc: FSContext): JSTreeConfig = {
    implicit def nonOption2Option[T](v: T): Option[T] = Some(v)

    JSTreeConfig(
      core = Core(
        check_callback = true,
        data = Data(
          data = Js("""function (node) { console.log(node); return { 'id' : node.id }; }""")
        )
      ),
      plugins = this.plugins,
    )
  }

  def init()(implicit fsc: FSContext): Js = Js {
    val callback = fsc.anonymousPageURLScalaXml(implicit fsc => {
      Option(Request.getParameters(fsc.page.req).getValue("id")) match {
        case Some("#") => <ul>{rootNodes.tap(_.foreach(node => nodeById += (node.id -> node))).map(_.renderLi()).mkNS}</ul>
        case Some(id) => <ul>{nodeById(id).childrenF().tap(_.foreach(node => nodeById += (node.id -> node))).map(_.renderLi()).mkNS}</ul>
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
