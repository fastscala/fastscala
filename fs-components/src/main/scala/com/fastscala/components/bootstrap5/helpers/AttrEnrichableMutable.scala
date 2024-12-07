package com.fastscala.components.bootstrap5.helpers

trait AttrEnrichableMutable {

  def setAttribute(name: String, value: String): this.type
}
