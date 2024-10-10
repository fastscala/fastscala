package com.fastscala.templates.form7.mixins

import com.fastscala.js.Js
import com.fastscala.templates.form7.F7Field
import com.fastscala.utils.Lazy


trait F7FieldWithValue[T] extends F7Field {

  def defaultValue: T

  private lazy val currentValueHolder: Lazy[T] = Lazy(_getter())
  private lazy val internalValue: Lazy[T] = Lazy(defaultValue)

  def currentValue = currentValueHolder()

  def currentValue_=(v: T) = currentValueHolder() = v

  var _getter: () => T = () => internalValue()

  def getter(): () => T = _getter

  def get(): T = getter()()

  def getInternalValue(): T = internalValue()

  var _setter: T => Js = v => Js.void(() => {
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
      Js.void
    }
  }
}
