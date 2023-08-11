package com.fastscala.templates.utils

trait Linkable {

  def linkHref: Option[String]

  def linkTarget: Option[String]

  def linkOnclick: Option[String]
}
