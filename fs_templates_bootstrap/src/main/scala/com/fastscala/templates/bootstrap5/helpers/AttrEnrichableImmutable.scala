package com.fastscala.templates.bootstrap5.helpers

trait AttrEnrichableImmutable[T] {

  def setAttribute(name: String, value: String): T
}
