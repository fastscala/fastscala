package com.fastscala.templates.form6.fields

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem

class F6CheckboxField[E <: FSXmlEnv]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: CheckboxF6FieldRenderer[E]) extends StandardF6Field[E]
  with ValidatableField[E]
  with StringSerializableField[E]
  with FocusableF6Field[E]
  with F6FieldWithDisabled[E]
  with F6FieldWithReadOnly[E]
  with F6FieldWithEnabled[E]
  with F6FieldWithTabIndex[E]
  with F6FieldWithName[E]
  with F6FieldWithLabel[E]
  with F6FieldWithAdditionalAttrs[E]
  with F6FieldWithDependencies[E]
  with F6FieldWithValue[E, Boolean] {

  import com.fastscala.core.FSXmlUtils._

  override def defaultValue: Boolean = false

  override def loadFromString(str: String): Seq[(ValidatableField[E], E#NodeSeq)] = str.toBooleanOption match {
    case Some(value) =>
      currentValue = value
      _setter(currentValue)
      Nil
    case None =>
      List((this, implicitly[FSXmlSupport[E]].buildText(s"Could not parse value '$str' as boolean")))
  }


  override def saveToString(): Option[String] = Some(currentValue.toString).filter(_ != "")

  override def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => _setter(currentValue)
    case _ => Js.void
  })

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem = {
    if (!enabled()) <div style="display:none;" id={aroundId}></div>.asFSXml()
    else {
      withFieldRenderHints { implicit hints =>
        renderer.render(this)(
          _label().map(lbl => <label for={elemId}>{lbl}</label>.asFSXml()),
          processInputElem(<input type="checkbox"
                      id={elemId}
                      onchange={
                      fsc.callback(Js.checkboxIsCheckedById(elemId), str => {
                        println("STR: " + str)
                        str.toBooleanOption.foreach(currentValue = _)
                        form.onEvent(ChangedField(this)) &
                          Js.evalIf(hints.contains(ShowValidationsHint))(reRender()) // TODO: is this wrong? (running on the client side, but should be server?)
                      }).cmd
                      }
                      checked={if (currentValue) "true" else null}
          ></input>.asFSXml()).withAttrs(finalAdditionalAttrs: _*),
          errors().headOption.map(_._2)
        )
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F6Field[E], Boolean]): List[F6Field[E]] = if (predicate.applyOrElse[F6Field[E], Boolean](this, _ => false)) List(this) else Nil
}
