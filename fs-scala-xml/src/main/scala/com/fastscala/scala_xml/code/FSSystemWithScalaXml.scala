package com.fastscala.scala_xml.code

import com.fastscala.core.FSSystem
import com.fastscala.routing.resp.{Ok, Response}
import com.fastscala.scala_xml.routing.resp.ResponseScalaXmlSupport.RichOk

import scala.xml.NodeSeq

trait FSSystemWithScalaXml extends FSSystem {

  override def respondWithAnonymousPage(rendered: AnyRef): Response = rendered match {
    case ns: NodeSeq => Ok.htmlStreaming(ns)
    case other => Ok.html(other.toString)
  }

}
