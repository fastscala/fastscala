package com.fastscala.utils

import com.fastscala.core.{FSContext, FSPage}

class LazyWithPage[T](compute: FSPage => T) {

  var computed = Option.empty[T]

  def apply()(implicit fsc: FSContext) = computed.getOrElse({
    computed = Some(compute(fsc.page))
    computed.get
  })

  def update(value: T): Unit = computed = Some(value)

  def reset(): Unit = computed = None
}

object LazyWithPage {
  def apply[T](compute: FSPage => T): LazyWithPage[T] = new LazyWithPage[T](compute)
}
