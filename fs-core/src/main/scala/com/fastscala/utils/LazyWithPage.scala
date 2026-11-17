package com.fastscala.utils

import com.fastscala.core.{FSContext, FSPage}

class LazyWithPage[T](compute: FSPage => T) {

  var computed = Option.empty[T]

  def apply()(implicit page: FSPage): T = computed.getOrElse({
    computed = Some(compute(page))
    computed.get
  })

  def update(value: T): Unit = computed = Some(value)

  def reset(): Unit = computed = None
}

object LazyWithPage {
  def apply[T](compute: FSPage => T): LazyWithPage[T] = new LazyWithPage[T](compute)
}
