package com.fastscala.scala_xml.code

import com.fastscala.core.FSSystem
import com.fastscala.routing.resp.{Ok, Response}
import com.fastscala.js.JsWriter
import com.fastscala.scala_xml.routing.resp.ResponseScalaXmlSupport.RichOk
import com.fastscala.scala_xml.xml.XmlJsStream

import scala.xml.NodeSeq

trait FSSystemWithScalaXml extends FSSystem {

  /** Responses' JavaScript is written by XmlJsStream (iteratively, into its own buffer). */
  override def jsWriter: JsWriter = XmlJsStream

  override def respondWithAnonymousPage(rendered: AnyRef): Response = rendered match {
    case ns: NodeSeq => Ok.htmlStreaming(ns)
    case other => Ok.html(other.toString)
  }

}
