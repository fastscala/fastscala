package com.fastscala.components.utils

trait Mutable {

  def mutate(f: => Unit): this.type = {
    f
    this
  }
}
