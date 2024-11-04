package com.fastscala.templates.bootstrap5.helpers

trait ClassEnrichableImmutable[T] {

  def addClass(clas: String): T
}
