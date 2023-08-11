package com.fastscala.utils

class Lazy[T](compute: => T) {

  var computed = Option.empty[T]

  def apply() = computed.getOrElse({
    computed = Some(compute)
    computed.get
  })

  def update(value: T): Unit = computed = Some(value)

  def reset(): Unit = computed = None
}

object Lazy {
  def apply[T](compute: => T): Lazy[T] = new Lazy[T](compute)
}
