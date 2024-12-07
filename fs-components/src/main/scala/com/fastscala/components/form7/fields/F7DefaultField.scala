package com.fastscala.components.form7.fields

import com.fastscala.components.form7.mixins.*
import com.fastscala.components.utils.Mutable

trait F7DefaultField extends StandardF7Field
  with Mutable
  with F7FieldWithOnChangedField
