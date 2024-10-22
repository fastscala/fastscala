package com.fastscala.templates.form7.mixins

import com.fastscala.templates.form7.fields.text.F7FieldInputFieldMixin


trait F7FieldWithSyncToServerOnChange extends F7FieldInputFieldMixin {
  var _syncToServerOnChange: () => Boolean = () => false

  def syncToServerOnChange: Boolean = _syncToServerOnChange()

  def doSyncToServerOnChange: this.type = syncToServerOnChange(true)

  def dontSyncToServerOnChange: this.type = syncToServerOnChange(false)

  def syncToServerOnChange(v: Boolean): this.type = mutate {
    _syncToServerOnChange = () => v
  }

  def syncToServerOnChange(f: () => Boolean): this.type = mutate({
    _syncToServerOnChange = f
  })
}
