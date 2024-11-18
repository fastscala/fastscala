package com.fastscala.templates.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.form7.{F7Field, Form7, RenderHint}
import com.fastscala.templates.utils.Mutable

import scala.util.{Failure, Success}

trait F7FieldWithOptions[T] extends F7Field with Mutable {
  var _options: () => Seq[T] = () => Nil

  def options: Seq[T] = _options()

  def options(v: Seq[T]): this.type = mutate {
    _options = () => v
  }

  def options(f: () => Seq[T]): this.type = mutate {
    _options = f
  }

  protected var currentRenderedOptions = Option.empty[(Seq[T], Map[String, T], Map[T, String])]

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().flatMap(superJs => {
      val currentOptions = options
      // Check if options changed:
      if (!currentRenderedOptions.exists(_._1 == currentOptions)) {
        Failure(new Exception("Options to render changed, rerendering."))
      } else Success(superJs)
    })
}
