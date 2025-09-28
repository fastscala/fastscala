package com.fastscala.components.form5.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form5.Form5
import com.fastscala.components.utils.ElemWithRandomId
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.utils.IdGen

import scala.xml.{Elem, NodeSeq}


trait FormField {

  def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem

  def reRender()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js

  /**
   * Ignores fields not matching the predicate, and their children.
   */
  def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField]

  def enabledFields: List[FormField] = fieldsMatching(_.enabled())

  def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = JS.void

  def deps: Set[FormField]

  def enabled: () => Boolean
}

trait FocusableFormField extends FormField {

  def focusJs: Js
}

trait StandardFormField extends FormField with ElemWithRandomId {

  val aroundId: String = IdGen.id

  def reRender()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = JS.replace(aroundId, render())

  def visible: () => Boolean = () => enabled()

  override def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case ChangedField(field) if deps.contains(field) => reRender() & form.onEvent(ChangedField(this))
    case _ => JS.void
  })

  def disabled: () => Boolean

  def readOnly: () => Boolean

  def withFieldRenderHints[T](f: Seq[RenderHint] => T)(implicit renderHints: Seq[RenderHint]): T = f {
    List(DisableFieldsHint).filter(_ => disabled()) ++
      List(ReadOnlyFieldsHint).filter(_ => readOnly()) ++
      renderHints
  }
}

trait ValidatableField extends StandardFormField {
  def hasErrors_?() = errors().nonEmpty

  def errors(): Seq[(ValidatableField, NodeSeq)] = Nil
}

trait StringSerializableField extends StandardFormField {

  def loadFromString(str: String): Seq[(ValidatableField, NodeSeq)]

  def saveToString(): Option[String]
}

trait QuerySerializableStringField extends StringSerializableField {

  def queryStringParamName: String
}
