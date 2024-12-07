package com.fastscala.scala_xml.routing.resp

import com.fastscala.routing.resp.{Ok, OkHtml}

import scala.xml.NodeSeq

object ResponseScalaXmlSupport {

  implicit class RichOk(ok: Ok.type) {
    def html(ns: NodeSeq) = new OkHtml(ns.toString)
  }
}