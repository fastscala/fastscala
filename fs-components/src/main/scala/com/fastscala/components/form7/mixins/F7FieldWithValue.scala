package com.fastscala.components.form7.mixins

import com.fastscala.components.utils.Mutable
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.Lazy


trait F7FieldWithValue[T] extends Mutable {

  def defaultValue: T

  /**
   * This is the value that is currently visible on the client side.
   */
  protected var currentRenderedValue: Option[T] = None

  /**
   * The current value of the field. Because this can be changed on the server side it may yet to be updated on the client side. currentRenderedValue should hold what is currently rendered on the client side.
   */
  private lazy val currentValueHolder: Lazy[T] = Lazy(_getter())

  /**
   * Fields can get and set (on submit) the value from an external variable. But they can also simply store this value internally. This is what this field is for.
   *
   * So, for example, when the user changes a text-field the currentValue is changed immediately. The internal value, if in use, would be changed to that value on form submit. But the form could be unable to be submitted because it doesn't pass validations.
   */
  private lazy val internalValue: Lazy[T] = Lazy(defaultValue)

  def currentValue = currentValueHolder()

  /**
   * Changing this value will update on the client side.
   */
  def currentValue_=(v: T) = currentValueHolder() = v

  var _getter: () => T = () => internalValue()

  def getter(): () => T = _getter

  def get(): T = getter()()

  def getInternalValue(): T = internalValue()

  var _setter: T => Js = v => JS.void(() => internalValue() = v)

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
