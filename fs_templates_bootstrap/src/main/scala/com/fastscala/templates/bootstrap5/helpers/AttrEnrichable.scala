package com.fastscala.templates.bootstrap5.helpers

trait AttrEnrichable {

  def setAttribute(name: String, value: String): this.type
}
