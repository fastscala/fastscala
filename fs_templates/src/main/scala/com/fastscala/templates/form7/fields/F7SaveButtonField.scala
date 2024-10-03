package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7._
import com.fastscala.templates.form7.mixins._
import com.fastscala.templates.form7.renderers._
import com.fastscala.xml.scala_xml.JS
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.Elem

class F7SaveButtonField[B <% Elem](
                                    btn: FSContext => B
                                    , val toInitialState: B => B = identity[B] _
                                    , val toChangedState: B => B = identity[B] _
                                    , val toErrorState: B => B = identity[B] _
                                  )(implicit renderer: ButtonF7FieldRenderer)
  extends StandardF7Field
    with F7FieldWithReadOnly
    with F7FieldWithDependencies
    with F7FieldWithDisabled
    with F7FieldWithEnabled {

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil

  val btnRenderer = JS.rerenderableP[(B => B, Form7)](_ => implicit fsc => {
    case (transformer, form) => (transformer(btn(fsc)): Elem).withId(elemId).addOnClick((Js.focus(elemId) & form.submitFormClientSide()).cmd)
  })

  override def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        renderer.render(this)({
          if (hints.contains(FailedSaveStateHint)) btnRenderer.render((toErrorState, form))
          else btnRenderer.render((toInitialState, form))
        })(hints)
      }
    }
}