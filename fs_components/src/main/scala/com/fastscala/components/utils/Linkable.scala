package com.fastscala.components.utils

trait Linkable {

  def linkHref: Option[String]

  def linkTarget: Option[String]

  def linkOnclick: Option[String]
}
