package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.Form7
import com.fastscala.xml.scala_xml.FSScalaXmlSupport
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

trait F7FieldWithNumRows extends F7FieldInputFieldMixin {
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

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>

    _rows().map(rows => input.withAttr("rows", rows.toString)).getOrElse(input)
  }
}

abstract class F7TextareaField[T]()(implicit renderer: TextareaF7FieldRenderer) extends StandardF7Field
  with ValidatableF7Field
  with StringSerializableF7Field
  with FocusableF7Field
  with F7FieldWithNumRows
  with F7FieldWithDisabled
  with F7FieldWithRequired
  with F7FieldWithReadOnly
  with F7FieldWithEnabled
  with F7FieldWithTabIndex
  with F7FieldWithName
  with F7FieldWithPlaceholder
  with F7FieldWithLabel
  with F7FieldWithMaxlength
  with F7FieldWithInputType
  with F7FieldWithAdditionalAttrs
  with F7FieldWithDependencies
  with F7FieldWithValue[T] {

  def toString(value: T): String

  def fromString(str: String): Either[String, T]

  override def loadFromString(str: String): Seq[(ValidatableF7Field, NodeSeq)] = {
    fromString(str) match {
      case Right(value) =>
        currentValue = value
        _setter(currentValue)
        Nil
      case Left(error) =>
        List((this, FSScalaXmlSupport.fsXmlSupport.buildText(s"Could not parse value '$str': $error")))
    }
  }

  override def saveToString(): Option[String] = Some(toString(currentValue)).filter(_ != "")

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case Submit => _setter(currentValue)
    case _ => Js.void
  })

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        renderer.render(this)(
          _label().map(lbl => <label for={elemId}>{lbl}</label>),
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
          >{this.toString(currentValue)}</textarea>).withAttrs(finalAdditionalAttrs: _*),
          errors().headOption.map(_._2)
        )
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}

class F7StringTextareaField()(implicit renderer: TextareaF7FieldRenderer) extends F7TextareaField[String] {

  override def defaultValue: String = ""

  def toString(value: String): String = value

  def fromString(str: String): Either[String, String] = Right(str)

  override def errors(): Seq[(ValidatableF7Field, NodeSeq)] = super.errors() ++
    (if (required() && currentValue == "") Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F7StringOptTextareaField()(implicit renderer: TextareaF7FieldRenderer) extends F7TextareaField[Option[String]] {

  override def defaultValue: Option[String] = None

  def toString(value: Option[String]): String = value.getOrElse("")

  def fromString(str: String): Either[String, Option[String]] = Right(Some(str).filter(_ != ""))

  override def errors(): Seq[(ValidatableF7Field, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

