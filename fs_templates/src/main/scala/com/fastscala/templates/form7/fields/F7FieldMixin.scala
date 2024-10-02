package com.fastscala.templates.form7.fields


trait F7FieldMixin extends F7Field {

  def mutate(code: => Unit): this.type = {
    code
    this
  }
}
