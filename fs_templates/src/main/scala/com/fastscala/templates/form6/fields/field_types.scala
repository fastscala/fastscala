package com.fastscala.templates.form6.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6
import com.fastscala.templates.utils.ElemWithRandomId
import com.fastscala.xml.scala_xml.JS

import scala.xml.{Elem, NodeSeq}


trait F6Field {

  def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem

  def reRender()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js

  /**
   * Ignores fields not matching the predicate, and their children.
   */
  def fieldsMatching(predicate: PartialFunction[F6Field, Boolean]): List[F6Field]

  def enabledFields: List[F6Field] = fieldsMatching(_.enabled())

  def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = Js.void

  def deps: Set[F6Field]

  def enabled(): Boolean
}

trait FocusableF6Field extends F6Field {

  def focusJs: Js
}

abstract class StandardF6Field() extends F6Field with ElemWithRandomId {

  val aroundId: String = randomElemId

  def reRender()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = {
    JS.replace(aroundId, render())
  }

  def visible: () => Boolean = () => enabled()

  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
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

trait ValidatableF6Field extends StandardF6Field {
  def hasErrors_?() = errors().nonEmpty

  def errors(): Seq[(ValidatableF6Field, NodeSeq)] = Nil
}

trait StringSerializableF6Field extends StandardF6Field {

  def loadFromString(str: String): Seq[(ValidatableF6Field, NodeSeq)]

  def saveToString(): Option[String]
}

trait QuerySerializableStringF6Field extends StringSerializableF6Field {

  def queryStringParamName: String
}
