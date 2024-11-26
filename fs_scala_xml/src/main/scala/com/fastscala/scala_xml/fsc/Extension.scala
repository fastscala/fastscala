package com.fastscala.scala_xml.fsc

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS

import scala.xml.{Elem, NodeSeq}

extension (fsc: FSContext) {

  def anonymousPageURLScalaXml(
                                render: FSContext => NodeSeq
                                , name: String
                              ): String = fsc.anonymousPageURL(fsc => render(fsc).toString, name)
}