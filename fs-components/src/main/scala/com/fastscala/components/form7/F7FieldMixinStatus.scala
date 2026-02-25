package com.fastscala.components.form7

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS

object F7FieldMixinStatus {
  def apply[T](valueFunc: () => T) = new F7FieldMixinStatus[T](valueFunc, None)

  def apply[T](value: T) = new F7FieldMixinStatus[T](() => value, None)
}

class F7FieldMixinStatus[T] private(
                                     var valueFunc: () => T,
                                     var renderedAs: Option[T]
                                   ) {

  def apply(): T = valueFunc()

  def update(newValueFunc: () => T): Unit = valueFunc = newValueFunc

  def setRendered(): Unit = renderedAs = Some(valueFunc())

  def hasChanged: Boolean = renderedAs.exists(_ != valueFunc())

  def updateIfChanged[R](update: (T, T) => R, dflt: R): R =
    if (hasChanged) {
      val old = renderedAs.get
      val updated = valueFunc()
      renderedAs = Some(updated)
      update(old, updated)
    } else dflt
}
