package com.fastscala.components.form7.fields

import com.fastscala.components.form7.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.{JS, printBeforeExec}

import scala.xml.Elem

class F7SubmitButtonField[B](btn: FSContext => B, val toInitialState: B => B = identity[B], val toChangedState: B => B = identity[B], val toErrorState: B => B = identity[B])(implicit renderer: ButtonF7FieldRenderer, evidence: B => Elem)
    extends F7Field
    with F7FieldWithValidations
    with F7FieldWithReadOnly
    with F7FieldWithDependencies
    with F7FieldWithDisabled
    with F7FieldWithEnabled {

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil

  val btnRenderer = JS.rerenderableP[(B => B, Form7)](_ =>
    implicit fsc => { case (transformer, form) =>
      (evidence(transformer(btn(fsc))): Elem).withId(elemId).addOnClick((JS.focus(elemId) & form.submitFormClientSide()).cmd)
    }
  )

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext): Js = super.onEvent(event) & (event match {
    case ChangedFormState(from, Form7State.Initial)          => Some(toInitialState)
    case ChangedFormState(from, Form7State.Saved)            => Some(toInitialState)
    case ChangedFormState(from, Form7State.Modified)         => Some(toChangedState)
    case ChangedFormState(from, Form7State.ValidationFailed) => Some(toErrorState)
    case _                                                   => None
  }).flatMap(toRelevantStateF => {
    btnRenderer.aroundId.map(id => {
      val rendered: Elem = evidence(toRelevantStateF(btn(fsc)))
      JS.setAttr(id)("class", rendered.getClassAttr) &
        JS.setContents(id, rendered.child)
    })
  }).getOrElse(Js.Void).printBeforeExec

  override def render()(implicit form: Form7, fsc: FSContext): Elem =
    if (!enabled) <div style="display:none;" id={aroundId}></div>
    else {
      renderer.render(this)({
        form.state() match {
          case com.fastscala.components.form7.Form7State.Initial          => btnRenderer.render((toInitialState, form))
          case com.fastscala.components.form7.Form7State.Saved            => btnRenderer.render((toInitialState, form))
          case com.fastscala.components.form7.Form7State.Modified         => btnRenderer.render((toChangedState, form))
          case com.fastscala.components.form7.Form7State.ValidationFailed => btnRenderer.render((toErrorState, form))
        }
      })
    }
}
