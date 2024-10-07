package com.fastscala.templates.form7.fields.text

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7._
import com.fastscala.templates.form7.mixins._
import com.fastscala.templates.form7.renderers._
import com.fastscala.xml.scala_xml.FSScalaXmlSupport
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}


abstract class F7TextField[T]()(implicit renderer: TextF7FieldRenderer) extends StandardF7Field
  with StringSerializableF7Field
  with FocusableF7Field
  with F7FieldWithDisabled
  with F7FieldWithRequired
  with F7FieldWithReadOnly
  with F7FieldWithEnabled
  with F7FieldWithTabIndex
  with F7FieldWithName
  with F7FieldWithPlaceholder
  with F7FieldWithLabel
  with F7FieldWithValidFeedback
  with F7FieldWithHelp
  with F7FieldWithMaxlength
  with F7FieldWithInputType
  with F7FieldWithAdditionalAttrs
  with F7FieldWithDependencies
  with F7FieldWithValue[T] {

  var showingValidation = false

  def toString(value: T): String

  def fromString(str: String): Either[String, T]

  override def loadFromString(str: String): Seq[(F7Field, NodeSeq)] = {
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

  override def submit()(implicit form: Form7, fsc: FSContext): Js = super.submit() & _setter(currentValue)

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  override def postValidation(errors: Seq[(F7Field, NodeSeq)])(implicit form: Form7, fsc: FSContext): Js = {
    implicit val renderHints: Seq[RenderHint] = form.formRenderHits()
    updateValidation()
  }

  def shouldShowValidation_?(implicit form: Form7): Boolean = {
    import F7FormValidationStrategy._
    import Form7State._
    def aux(validationStrategy: F7FormValidationStrategy.Value): Boolean = {
      validationStrategy match {
        case ValidateBeforeUserInput => true
        case ValidateEachFieldAfterUserInput => state match {
          case F7FieldState.Filled => true
          case F7FieldState.AwaitingInput => aux(ValidateOnAttemptSubmitOnly)
        }
        case ValidateOnAttemptSubmitOnly => form.state match {
          case Filling => false
          case ValidationFailed => true
          case Saved => false
        }
      }
    }

    aux(form.validationStrategy)
  }

  def updateValidation()(implicit form7: Form7): Js = {
    lazy val errors = this.validate()
    val shouldShowValidation = shouldShowValidation_? && errors.nonEmpty
    if (shouldShowValidation != showingValidation) {
      if (shouldShowValidation) {
        val validation = errors.headOption.map(error => <div>{error._2}</div>).getOrElse(<div></div>)
        showingValidation = true
        renderer.showValidation(this)(validation)
      } else {
        showingValidation = false
        renderer.hideValidation(this)()
      }
    } else {
      Js.void
    }
  }

  override def postSubmit()(implicit form: Form7, fsc: FSContext): Js = super.postSubmit() & {
    setFilled()
    Js.void
  }

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = event match {
    case ChangedField(f) if f == this => updateValidation()
    case _ => Js.void
  }

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>

        val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
        showingValidation = errorsToShow.nonEmpty

        renderer.render(this)(
          inputElem = processInputElem(
            <input
              type={inputType}
              id={elemId}
              onblur={
                     fsc.callback(Js.elementValueById(elemId), str => {
                       if (currentValue != str) {
                         setFilled()
                         fromString(str).foreach(currentValue = _)
                         form.onEvent(ChangedField(this))
                       } else {
                         Js.void
                       }
                     }).cmd
                     }
              onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {${Js.evalIf(hints.contains(SaveOnEnterHint))(Js.blur(elemId) & form.submitFormClientSide())}}"}
              value={this.toString(currentValue)}
            />
          ).withAttrs(finalAdditionalAttrs: _*),
          label = _label(),
          invalidFeedback = errorsToShow.headOption.map(error => <div>{error._2}</div>),
          validFeedback = if (errorsToShow.isEmpty) validFeedback() else None,
          help = help()
        )
      }
    }
  }

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}
