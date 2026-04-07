package com.fastscala.components.form7.fields.number

import com.fastscala.components.form7.fields.{F7InputFieldBase, F7InputOptFieldBase}
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.mixins.mainelem.*
import com.fastscala.components.form7.renderers.{F7ValidatableFieldWithMainElemRenderer, F7InputFieldRenderer}

trait F7NumericOptFieldBase[T]()(implicit renderer: F7InputFieldRenderer) extends F7NumericFieldBase[Option[T]] with F7InputOptFieldBase[T] {
}
