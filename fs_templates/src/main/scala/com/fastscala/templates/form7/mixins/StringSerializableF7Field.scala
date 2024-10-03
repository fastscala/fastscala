package com.fastscala.templates.form7.mixins

import com.fastscala.templates.form7.F7Field

import scala.xml.NodeSeq


trait StringSerializableF7Field extends StandardF7Field {

  def loadFromString(str: String): Seq[(F7Field, NodeSeq)]

  def saveToString(): Option[String]
}
