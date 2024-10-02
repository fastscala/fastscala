package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.Form7
import com.fastscala.xml.scala_xml.FSScalaXmlSupport
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

class F7CheckboxField()(implicit renderer: CheckboxF7FieldRenderer) extends StandardF7Field
  with ValidatableF7Field
  with StringSerializableF7Field
  with FocusableF7Field
  with F7FieldWithDisabled
  with F7FieldWithReadOnly
  with F7FieldWithEnabled
  with F7FieldWithTabIndex
  with F7FieldWithName
  with F7FieldWithLabel
  with F7FieldWithAdditionalAttrs
  with F7FieldWithDependencies
  with F7FieldWithValue[Boolean] {

  override def defaultValue: Boolean = false

  override def loadFromString(str: String): Seq[(ValidatableF7Field, NodeSeq)] = str.toBooleanOption match {
    case Some(value) =>
      currentValue = value
      _setter(currentValue)
      Nil
    case None =>
      List((this, FSScalaXmlSupport.fsXmlSupport.buildText(s"Could not parse value '$str' as boolean")))
  }


  override def saveToString(): Option[String] = Some(currentValue.toString).filter(_ != "")

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
          processInputElem(<input type="checkbox"
                      id={elemId}
                      onchange={
                      fsc.callback(Js.checkboxIsCheckedById(elemId), str => {
                        str.toBooleanOption.foreach(currentValue = _)
                        form.onEvent(ChangedField(this)) &
                          Js.evalIf(hints.contains(ShowValidationsHint))(reRender()) // TODO: is this wrong? (running on the client side, but should be server?)
                      }).cmd
                      }
                      checked={if (currentValue) "true" else null}
          ></input>).withAttrs(finalAdditionalAttrs: _*),
          errors().headOption.map(_._2)
        )
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}
