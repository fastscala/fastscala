package com.fastscala.templates.form6.fields

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem

import scala.util.chaining.scalaUtilChainingOps

trait F6FieldWithNumRows[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
  var _rows: () => Option[Int] = () => None

  def rows() = _rows()

  def rows(v: Option[Int]): this.type = mutate {
    _rows = () => v
  }

  def rows(v: Int): this.type = mutate {
    _rows = () => Some(v)
  }

  def rows(f: () => Option[Int]): this.type = mutate {
    _rows = f
  }

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    _rows().map(rows => input.withAttr("rows", rows.toString)).getOrElse(input)
  }
}

abstract class F6TextareaField[E <: FSXmlEnv, T]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: TextareaF6FieldRenderer[E]) extends StandardF6Field[E]
  with ValidatableField[E]
  with StringSerializableField[E]
  with FocusableF6Field[E]
  with F6FieldWithNumRows[E]
  with F6FieldWithDisabled[E]
  with F6FieldWithRequired[E]
  with F6FieldWithReadOnly[E]
  with F6FieldWithEnabled[E]
  with F6FieldWithTabIndex[E]
  with F6FieldWithName[E]
  with F6FieldWithPlaceholder[E]
  with F6FieldWithLabel[E]
  with F6FieldWithMaxlength[E]
  with F6FieldWithInputType[E]
  with F6FieldWithAdditionalAttrs[E]
  with F6FieldWithDependencies[E]
  with F6FieldWithValue[E, T] {

  def toString(value: T): String

  def fromString(str: String): Either[String, T]

  override def loadFromString(str: String): Seq[(ValidatableField[E], E#NodeSeq)] = {
    fromString(str) match {
      case Right(value) =>
        currentValue = value
        _setter(currentValue)
        Nil
      case Left(error) =>
        List((this, fsXmlSupport.buildText(s"Could not parse value '$str': $error")))
    }
  }

  override def saveToString(): Option[String] = Some(toString(currentValue)).filter(_ != "")

  override def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => _setter(currentValue)
    case _ => Js.void
  })

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem = {
    import com.fastscala.core.FSXmlUtils._
    if (!enabled()) <div style="display:none;" id={aroundId}></div>.asFSXml()
    else {
      withFieldRenderHints { implicit hints =>
        renderer.render(this)(
          _label().map(lbl => <label for={elemId}>{lbl}</label>.asFSXml()),
          processInputElem(<textarea
                      type="text"
                      id={elemId}
                      onblur={
                      fsc.callback(Js.elementValueById(elemId), str => {
                        fromString(str).foreach(currentValue = _)
                        form.onEvent(ChangedField(this)) &
                          Js.evalIf(hints.contains(ShowValidationsHint))(reRender()) // TODO: is this wrong? (running on the client side, but should be server?)
                      }).cmd
                      }
                      onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13 && event.ctrlKey) {${Js.evalIf(hints.contains(SaveOnEnterHint))(Js.blur(elemId) & form.onSaveClientSide())}}"}
          >{this.toString(currentValue)}</textarea>.asFSXml()).withAttrs(finalAdditionalAttrs: _*),
          errors().headOption.map(_._2)
        )
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F6Field[E], Boolean]): List[F6Field[E]] = if (predicate.applyOrElse[F6Field[E], Boolean](this, _ => false)) List(this) else Nil
}

class F6StringTextareaField[E <: FSXmlEnv]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: TextareaF6FieldRenderer[E]) extends F6TextareaField[E, String] {

  override def defaultValue: String = ""

  def toString(value: String): String = value

  def fromString(str: String): Either[String, String] = Right(str)

  override def errors(): Seq[(ValidatableField[E], E#NodeSeq)] = super.errors() ++
    (if (required() && currentValue == "") Seq((this, fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6StringOptTextareaField[E <: FSXmlEnv]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: TextareaF6FieldRenderer[E]) extends F6TextareaField[E, Option[String]] {

  override def defaultValue: Option[String] = None

  def toString(value: Option[String]): String = value.getOrElse("")

  def fromString(str: String): Either[String, Option[String]] = Right(Some(str).filter(_ != ""))

  override def errors(): Seq[(ValidatableField[E], E#NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

