package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7Field, F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js


trait F7FieldWithoutChildren extends F7Field with Mutable {

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] =
    if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}
