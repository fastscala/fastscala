package com.fastscala.components.bootstrap5.helpers

trait AttrEnrichableImmutable[T] {

  def setAttribute(name: String, value: String): T
}
