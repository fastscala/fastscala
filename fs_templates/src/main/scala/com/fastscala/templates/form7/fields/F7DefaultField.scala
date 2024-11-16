package com.fastscala.templates.form7.fields

import com.fastscala.templates.form7.mixins.*
import com.fastscala.templates.utils.Mutable

trait F7DefaultField extends StandardF7Field
  with Mutable
  with F7FieldWithOnChangedField
