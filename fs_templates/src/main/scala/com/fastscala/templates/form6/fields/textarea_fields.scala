package com.fastscala.templates.form6.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6
import com.fastscala.xml.scala_xml.FSScalaXmlSupport
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

trait F6FieldWithNumRows extends F6FieldInputFieldMixin {
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

abstract class F6TextareaField[T]()(implicit renderer: TextareaF6FieldRenderer) extends StandardF6Field
  with ValidatableF6Field
  with StringSerializableF6Field
  with FocusableF6Field
  with F6FieldWithNumRows
  with F6FieldWithDisabled
  with F6FieldWithRequired
  with F6FieldWithReadOnly
  with F6FieldWithEnabled
  with F6FieldWithTabIndex
  with F6FieldWithName
  with F6FieldWithPlaceholder
  with F6FieldWithLabel
  with F6FieldWithMaxlength
  with F6FieldWithInputType
  with F6FieldWithAdditionalAttrs
  with F6FieldWithDependencies
  with F6FieldWithValue[T] {

  def toString(value: T): String

  def fromString(str: String): Either[String, T]

  override def loadFromString(str: String): Seq[(ValidatableF6Field, NodeSeq)] = {
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

  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => _setter(currentValue)
    case _ => Js.void
  })

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
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

  override def fieldsMatching(predicate: PartialFunction[F6Field, Boolean]): List[F6Field] = if (predicate.applyOrElse[F6Field, Boolean](this, _ => false)) List(this) else Nil
}

class F6StringTextareaField()(implicit renderer: TextareaF6FieldRenderer) extends F6TextareaField[String] {

  override def defaultValue: String = ""

  def toString(value: String): String = value

  def fromString(str: String): Either[String, String] = Right(str)

  override def errors(): Seq[(ValidatableF6Field, NodeSeq)] = super.errors() ++
    (if (required() && currentValue == "") Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6StringOptTextareaField()(implicit renderer: TextareaF6FieldRenderer) extends F6TextareaField[Option[String]] {

  override def defaultValue: Option[String] = None

  def toString(value: Option[String]): String = value.getOrElse("")

  def fromString(str: String): Either[String, Option[String]] = Right(Some(str).filter(_ != ""))

  override def errors(): Seq[(ValidatableF6Field, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

