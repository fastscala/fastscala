package com.fastscala.components.bootstrap5.form7.layout

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.bootstrap5.form7.mixins.F7FieldWithInputGroupSize
import com.fastscala.components.form7.*
import com.fastscala.components.form7.fields.F7HtmlField
import com.fastscala.components.form7.fields.layout.F7ContainerFieldBase
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, Node, NodeSeq}

class F7BSFormInputGroup()(groupChildren: F7Field*)
  extends F7ContainerFieldBase
    with F7FieldWithInputGroupSize {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  override def aroundClass: String = ""

  override def children: Seq[(String, F7Field)] = groupChildren.map("" -> _)

  override def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    currentlyEnabled = enabled
    if (!currentlyEnabled) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>

        val showErrors = shouldShowValidation_? && groupChildren.exists(_.validate().nonEmpty)
        showingValidation = showErrors

        val renderedChildren: Seq[(F7Field, Elem, Seq[Node], Seq[Node])] = groupChildren.map(f => {
          val rendered = f.render()
          val inputIdx: Option[Int] = Some(rendered.child.indexWhere({
            case elem: Elem if Set("input", "textarea", "select").contains(elem.label) => true
            case _ => false
          })).filter(_ != -1)
          val (beforeInput: Seq[Node], inputAndAfter: Seq[Node]) = inputIdx.map(inputElemIdx => {
            rendered.child.splitAt(inputElemIdx)
          }).getOrElse((Nil, Nil))
          (
            f,
            rendered,
            beforeInput ++ inputAndAfter.collect({
              case elem: Elem if elem.getClassAttr.contains("form-check-label") => elem
            }),
            inputAndAfter.drop(1).filter({
              case elem: Elem if elem.getClassAttr.contains("form-check-label") => false
              case _ => true
            })
          )
        })
        div.withId(aroundId).mb_3.apply {
          (renderedChildren.map(_._3).flatten.map(adaptNodes) ++
            input_group.withId(elemId)
              .withClassIf(showErrors, is_invalid.getClassAttr)
              .withClass(inputGroupSize.`class`)
              .apply(renderedChildren.map({
                case (field: F7HtmlField, rendered, _, _) => rendered.child.map({
                  case elem: Elem if elem.label == "span" || elem.label == "label" => elem.input_group_text
                  case elem => elem
                })
                case (field, rendered, _, _) => rendered.child.collect({
                  case elem: Elem if elem.label == "textarea" => Seq(elem)
                  case elem: Elem if elem.label == "select" => Seq(elem)
                  case elem: Elem if elem.label == "input" && Set("radio", "checkbox").contains(elem.attr("type").getOrElse("")) => Seq(input_group_text.apply(elem.mt_0))
                  case elem: Elem if elem.label == "input" => Seq(elem)
                  case _ => Seq()
                }).flatten
              }).flatten: _*) ++
            renderedChildren.map(_._4).flatten.map(adaptNodes): NodeSeq)
        }
      }
    }
  }

  def adaptNodes(node: Node): Node = node match {
    case elem: Elem if elem.getClassAttr.contains("form-check-label") => elem.removeClass("form-check-label").addClass("form-label")
    case other => other
  }

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] =
    List(this).filter(_ => predicate.applyOrElse[F7Field, Boolean](this, _ => false)) :::
      groupChildren.toList.flatMap(_.fieldAndChildreenMatchingPredicate(predicate))

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js =
    super.onEvent(event) & groupChildren.map(_.onEvent(event)).reduceOption(_ & _).getOrElse(JS.void)

  var showingValidation = false

  override def postValidation(errors: Seq[(F7Field, NodeSeq)])(implicit form: Form7, fsc: FSContext): Js = {
    implicit val renderHints: Seq[RenderHint] = form.formRenderHits()
    updateValidation()
  }

  def updateValidation()(implicit form7: Form7): Js = {
    val shouldShowValidation = shouldShowValidation_?
    if (shouldShowValidation) {
      val hasErrors = groupChildren.exists(_.validate().nonEmpty)
      if (hasErrors) {
        showingValidation = true
        showValidation()
      } else {
        showingValidation = false
        hideValidation()
      }
    } else if (showingValidation) {
      showingValidation = false
      hideValidation()
    } else JS.void
  }

  def showValidation(): Js =
    JS.addClass(elemId, "is-invalid") &
      JS.removeClass(elemId, "is-valid")

  def hideValidation(): Js =
    JS.removeClass(elemId, "is-invalid")
}
