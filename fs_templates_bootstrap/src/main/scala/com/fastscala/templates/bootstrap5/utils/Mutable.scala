package com.fastscala.templates.bootstrap5.utils

trait Mutable {

  def mutate(f: => Unit): this.type = {
    f
    this
  }
}
