package com.fastscala.templates.bootstrap5.helpers

trait AttrEnrichableMutable {

  def setAttribute(name: String, value: String): this.type
}
