package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.F7Field
import com.fastscala.components.utils.Mutable

import scala.xml.NodeSeq


trait StringSerializableF7Field extends Mutable {

  def loadFromString(str: String): Seq[(F7Field, NodeSeq)]

  def saveToString(): Option[String]
}
