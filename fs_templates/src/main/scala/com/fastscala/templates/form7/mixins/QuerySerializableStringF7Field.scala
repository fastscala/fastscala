package com.fastscala.templates.form7.mixins

trait QuerySerializableStringF7Field extends StringSerializableF7Field {

  def queryStringParamName: String
}
