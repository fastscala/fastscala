package com.fastscala.templates.utils

trait Mutable {

  def mutate(f: => Unit): this.type = {
    f
    this
  }
}
