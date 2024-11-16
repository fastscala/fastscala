package com.fastscala.templates.form7.mixins

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.utils.Mutable
import com.fastscala.utils.Lazy
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem


trait F7FieldWithValue[T] extends Mutable {

  def defaultValue: T

  private lazy val currentValueHolder: Lazy[T] = Lazy(_getter())
  private lazy val internalValue: Lazy[T] = Lazy(defaultValue)

  /**
   * This is the value that is currently visible on the client side.
   */
  protected var currentRenderedValue: Option[T] = None

  def currentValue = currentValueHolder()

  /**
   * Changing this value will update on the client side.
   */
  def currentValue_=(v: T) = currentValueHolder() = v

  var _getter: () => T = () => internalValue()

  def getter(): () => T = _getter

  def get(): T = getter()()

  def getInternalValue(): T = internalValue()

  var _setter: T => Js = v => JS.void(() => {
    internalValue() = v
  })

  def setter(): T => Js = _setter

  def setInternalValue(value: T): this.type = mutate {
    internalValue() = value
  }

  def set(value: T): Js = setter()(value)

  def setter(setter: T => Js): this.type = mutate({
    _setter = setter
  })

  def getter(getter: () => T): this.type = mutate({
    _getter = getter
  })

  def rw(get: => T, set: T => Unit): this.type = mutate {
    _getter = () => get
    _setter = v => {
      set(v)
      JS.void
    }
  }
}
