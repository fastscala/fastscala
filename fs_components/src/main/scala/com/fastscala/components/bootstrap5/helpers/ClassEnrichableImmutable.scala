package com.fastscala.components.bootstrap5.helpers

trait ClassEnrichableImmutable[T] {

  def addClass(clas: String): T
}
