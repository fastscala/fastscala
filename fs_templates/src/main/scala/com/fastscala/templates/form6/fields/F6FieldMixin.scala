package com.fastscala.templates.form6.fields


trait F6FieldMixin extends F6Field {

  def mutate(code: => Unit): this.type = {
    code
    this
  }
}
