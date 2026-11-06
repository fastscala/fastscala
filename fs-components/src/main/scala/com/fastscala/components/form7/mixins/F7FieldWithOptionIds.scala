package com.fastscala.components.form7.mixins

import com.fastscala.components.utils.Mutable

import java.util.UUID
import scala.util.Try


trait F7FieldWithOptionIds[T] extends Mutable {
  var _option2Id: (T, Seq[T]) => String = (opt, options) => options.indexOf(opt).toString

  var _id2Option: (String, Seq[T]) => Option[T] = (id, options) => id.toIntOption.map(idx => options(idx))

  def option2Id(opt2Id: (T, Seq[T]) => String, id2Opt: (String, Seq[T]) => Option[T]): this.type = mutate {
    _option2Id = opt2Id
    _id2Option = id2Opt
  }

  def option2IdByUUID(opt2UUID: T => UUID, uuid2Opt: UUID => Option[T]): this.type = mutate {
    _option2Id = (opt, _) => opt2UUID(opt).toString
    _id2Option = (str, _) => Try(UUID.fromString(str)).toOption.flatMap(uuid => uuid2Opt(uuid))
  }

  def optionIdsFromIdentityHashCode(): this.type = mutate {
    _option2Id = (opt, options) => "%X".format(math.abs(System.identityHashCode(opt)))
    _id2Option = (id, options) => options.find(opt => _option2Id(opt, options) == id)
  }
}
