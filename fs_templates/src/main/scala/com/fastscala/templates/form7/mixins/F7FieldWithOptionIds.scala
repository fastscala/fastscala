package com.fastscala.templates.form7.mixins

import com.fastscala.templates.form7.fields.F7DefaultField


trait F7FieldWithOptionIds[T] extends F7DefaultField {
  var _option2Id: (T, Seq[T]) => String = (opt, options) => "%X".formatted(options.indexOf(opt).toString)

  var _id2Option: (String, Seq[T]) => Option[T] = (id, options) => id.toIntOption.map(idx => options(idx))

  def option2Id(f: (T, Seq[T]) => String): this.type = mutate {
    _option2Id = f
  }

  def id2Option(f: (String, Seq[T]) => Option[T]): this.type = mutate {
    _id2Option = f
  }

  def optionIdsFromIdentityHashCode(): this.type = mutate {
    _option2Id = (opt, options) => "%X".format(math.abs(System.identityHashCode(opt)))
    _id2Option = (id, options) => options.find(opt => _option2Id(opt, options) == id)
  }
}
