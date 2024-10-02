package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.Form7
import com.fastscala.templates.utils.ElemWithRandomId
import com.fastscala.xml.scala_xml.JS

import scala.xml.{Elem, NodeSeq}


trait F7Field {

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem

  def reRender()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js

  /**
   * Ignores fields not matching the predicate, and their children.
   */
  def fieldsMatching(predicate: PartialFunction[F7Field, Boolean]): List[F7Field]

  def enabledFields: List[F7Field] = fieldsMatching(_.enabled())

  def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = Js.void

  def deps: Set[F7Field]

  def enabled(): Boolean
}

trait FocusableF7Field extends F7Field {

  def focusJs: Js
}

abstract class StandardF7Field() extends F7Field with ElemWithRandomId {

  val aroundId: String = randomElemId

  def reRender()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = {
    JS.replace(aroundId, render())
  }

  def visible: () => Boolean = () => enabled()

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
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

trait ValidatableF7Field extends StandardF7Field {
  def hasErrors_?() = errors().nonEmpty

  def errors(): Seq[(ValidatableF7Field, NodeSeq)] = Nil
}

trait StringSerializableF7Field extends StandardF7Field {

  def loadFromString(str: String): Seq[(ValidatableF7Field, NodeSeq)]

  def saveToString(): Option[String]
}

trait QuerySerializableStringF7Field extends StringSerializableF7Field {

  def queryStringParamName: String
}
