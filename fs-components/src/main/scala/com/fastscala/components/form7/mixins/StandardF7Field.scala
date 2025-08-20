package com.fastscala.components.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.*
import com.fastscala.components.form7.renderers.StandardF7FieldRenderer

import scala.xml.NodeSeq

trait StandardF7Field extends F7Field
  with F7FieldWithOnChangedField
  with F7FieldWithValidations {

  def renderer: StandardF7FieldRenderer

  def visible: () => Boolean = () => enabled

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ &
      updateValidation())

  override def postValidation(errors: Seq[(F7Field, NodeSeq)])(implicit form: Form7, fsc: FSContext): Js = {
    updateValidation()
  }

  def showOrUpdateValidation(ns: NodeSeq): Js

  def hideValidation(): Js

  var showingValidation = false

  def updateValidation()(implicit form7: Form7): Js = {
    val shouldShowValidation = shouldShowValidation_?
    if (shouldShowValidation) {
      val errors = this.validate()
      if (errors.nonEmpty) {
        val validation = errors.headOption.map(error => <div>{error._2}</div>).getOrElse(<div></div>)
        showingValidation = true
        showOrUpdateValidation(validation)
      } else {
        showingValidation = false
        hideValidation()
      }
    } else if (showingValidation) {
      showingValidation = false
      hideValidation()
    } else JS.void
  }

  override def postSubmit()(implicit form: Form7, fsc: FSContext): Js = super.postSubmit() & {
    setFilled()
    JS.void
  }

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}
