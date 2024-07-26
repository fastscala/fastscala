package com.fastscala.templates.form6.fields

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6
import com.fastscala.templates.utils.ElemWithRandomId


trait F6Field[E <: FSXmlEnv] {

  implicit def fsXmlSupport: FSXmlSupport[E]

  def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem

  def reRender()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js

  /**
   * Ignores fields not matching the predicate, and their children.
   */
  def fieldsMatching(predicate: PartialFunction[F6Field[E], Boolean]): List[F6Field[E]]

  def enabledFields: List[F6Field[E]] = fieldsMatching(_.enabled())

  def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = Js.void

  def deps: Set[F6Field[_]]

  def enabled(): Boolean
}

trait FocusableF6Field[E <: FSXmlEnv] extends F6Field[E] {

  def focusJs: Js
}

abstract class StandardF6Field[E <: FSXmlEnv]()(implicit val fsXmlSupport: FSXmlSupport[E]) extends F6Field[E] with ElemWithRandomId {

  val aroundId: String = randomElemId

  def reRender()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = {
    import com.fastscala.core.FSXmlUtils._
    Js.replace(aroundId, render())
  }

  def visible: () => Boolean = () => enabled()

  override def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case ChangedField(field) if deps.contains(field) => reRender() & form.onEvent(ChangedField(this))
    case _ => Js.void
  })

  def disabled(): Boolean

  def readOnly(): Boolean

  def withFieldRenderHints[T](f: Seq[RenderHint] => T)(implicit renderHints: Seq[RenderHint]): T = f {
    List(DisableFieldsHint).filter(_ => disabled()) ++
      List(ReadOnlyFieldsHint).filter(_ => readOnly()) ++
      renderHints
  }
}

trait ValidatableField[E <: FSXmlEnv] extends StandardF6Field[E] {
  def hasErrors_?() = errors().nonEmpty

  def errors(): Seq[(ValidatableField[E], E#NodeSeq)] = Nil
}

trait StringSerializableField[E <: FSXmlEnv] extends StandardF6Field[E] {

  def loadFromString(str: String): Seq[(ValidatableField[E], E#NodeSeq)]

  def saveToString(): Option[String]
}

trait QuerySerializableStringField[E <: FSXmlEnv] extends StringSerializableField[E] {

  def queryStringParamName: String
}
