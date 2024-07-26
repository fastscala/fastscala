package com.fastscala.templates.bootstrap5.tables

trait Table5WithDataStorage {

  def getProp(name: String): Option[String]

  def setProp(name: String, value: Option[String]): Unit

  def setProp(name: String, value: String): Unit = setProp(name, Some(value))

  def clearProp(name: String): Unit = setProp(name, None)
}
