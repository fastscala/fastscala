package com.fastscala.templates.form5.fields

import com.fastscala.core.{FSContext, FSXmlEnv}
import com.fastscala.js.Js
import com.fastscala.templates.form5.Form5
import com.fastscala.templates.utils.ElemWithRandomId


trait FormField[E <: FSXmlEnv] {

  def render()(implicit form: Form5[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem

  def reRender()(implicit form: Form5[E], fsc: FSContext, hints: Seq[RenderHint]): Js

  /**
   * Ignores fields not matching the predicate, and their children.
   */
  def fieldsMatching(predicate: PartialFunction[FormField[E], Boolean]): List[FormField[E]]

  def enabledFields: List[FormField[E]] = fieldsMatching(_.enabled())

  def onEvent(event: FormEvent)(implicit form: Form5[E], fsc: FSContext, hints: Seq[RenderHint]): Js = Js.void

  def deps: Set[FormField[_]]

  def enabled: () => Boolean
}

trait FocusableFormField[E <: FSXmlEnv] extends FormField[E] {

  def focusJs: Js
}

trait StandardFormField[E <: FSXmlEnv] extends FormField[E] with ElemWithRandomId {

  val aroundId: String = randomElemId

  def reRender()(implicit form: Form5[E], fsc: FSContext, hints: Seq[RenderHint]): Js = {
    import form.fsXmlSupport
    Js.replace(aroundId, render())
  }

  def visible: () => Boolean = () => enabled()

  override def onEvent(event: FormEvent)(implicit form: Form5[E], fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case ChangedField(field) if deps.contains(field) => reRender() & form.onEvent(ChangedField(this))
    case _ => Js.void
  })

  def disabled: () => Boolean

  def readOnly: () => Boolean

  def withFieldRenderHints[T](f: Seq[RenderHint] => T)(implicit renderHints: Seq[RenderHint]): T = f {
    List(DisableFieldsHint).filter(_ => disabled()) ++
      List(ReadOnlyFieldsHint).filter(_ => readOnly()) ++
      renderHints
  }
}

trait ValidatableField[E <: FSXmlEnv] extends StandardFormField[E] {
  def hasErrors_?() = errors().nonEmpty

  def errors(): Seq[(ValidatableField[E], E#NodeSeq)] = Nil
}

trait StringSerializableField[E <: FSXmlEnv] extends StandardFormField[E] {

  def loadFromString(str: String): Seq[(ValidatableField[E], E#NodeSeq)]

  def saveToString(): Option[String]
}

trait QuerySerializableStringField[E <: FSXmlEnv] extends StringSerializableField[E] {

  def queryStringParamName: String
}
