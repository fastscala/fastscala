package com.fastscala.components.utils

import scala.xml.NodeSeq

case class DivBuilder(classes: String = "") {

  def containerfluid = copy(classes = classes + " container-fluid")

  def row = copy(classes = classes + " row")

  def colMd1 = copy(classes = classes + " col-md-1")

  def colMd2 = copy(classes = classes + " col-md-2")

  def colMd3 = copy(classes = classes + " col-md-3")

  def colMd4 = copy(classes = classes + " col-md-4")

  def colMd5 = copy(classes = classes + " col-md-5")

  def colMd6 = copy(classes = classes + " col-md-6")

  def colMd7 = copy(classes = classes + " col-md-7")

  def colMd8 = copy(classes = classes + " col-md-8")

  def colMd9 = copy(classes = classes + " col-md-9")

  def colMd10 = copy(classes = classes + " col-md-10")

  def colMd11 = copy(classes = classes + " col-md-11")

  def colMd12 = copy(classes = classes + " col-md-12")

  def colSm1 = copy(classes = classes + " col-sm-1")

  def colSm2 = copy(classes = classes + " col-sm-2")

  def colSm3 = copy(classes = classes + " col-sm-3")

  def colSm4 = copy(classes = classes + " col-sm-4")

  def colSm5 = copy(classes = classes + " col-sm-5")

  def colSm6 = copy(classes = classes + " col-sm-6")

  def colSm7 = copy(classes = classes + " col-sm-7")

  def colSm8 = copy(classes = classes + " col-sm-8")

  def colSm9 = copy(classes = classes + " col-sm-9")

  def colSm10 = copy(classes = classes + " col-sm-10")

  def colSm11 = copy(classes = classes + " col-sm-11")

  def colSm12 = copy(classes = classes + " col-sm-12")

  def apply(ns: => NodeSeq) = <div class={classes}>{ns}</div>

  def apply(classes: String) = copy(classes = this.classes + classes)
}

object Div extends DivBuilder()
