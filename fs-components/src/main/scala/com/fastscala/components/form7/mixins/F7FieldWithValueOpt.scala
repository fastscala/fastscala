package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.F7Field

import scala.xml.NodeSeq

trait F7FieldWithValueOpt[V] extends F7FieldWithValue[Option[V]] {

  override def defaultValue: Option[V] = None
}
