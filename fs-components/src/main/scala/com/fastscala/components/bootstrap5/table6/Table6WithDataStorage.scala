package com.fastscala.components.bootstrap5.table6

import com.fastscala.core.FSPage
import com.fastscala.js.Js

/** A table that keeps named values somewhere they outlive it (e.g. [[Table6WithDataStorageInQueryString]]: in the URL). */
trait Table6WithDataStorage {

  def getProp(name: String)(implicit fsPage: FSPage): Option[String]

  def setProp(name: String, value: Option[String])(implicit fsPage: FSPage): Js

  def setProp(name: String, value: String)(implicit fsPage: FSPage): Js = setProp(name, Some(value))

  def clearProp(name: String)(implicit fsPage: FSPage): Js = setProp(name, None)
}
