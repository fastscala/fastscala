package com.fastscala.scala_xml.routing.resp

import com.fastscala.routing.resp.Ok

import scala.xml.NodeSeq

object ResponseScalaXmlSupport {

  implicit class RichOk(ok: Ok.type) {

    /** As [[html]], but streamed as it's serialized, without a Content-Length (see [[OkHtmlStreaming]]). */
    def htmlStreaming(ns: NodeSeq) = new OkHtmlStreaming(ns)
  }
}