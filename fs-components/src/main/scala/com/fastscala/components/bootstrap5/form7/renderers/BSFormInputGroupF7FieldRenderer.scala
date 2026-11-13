package com.fastscala.components.bootstrap5.form7.renderers

import com.fastscala.components.bootstrap5.form7.BSStandardModifiableF7InputElemFieldRenderer
import com.fastscala.components.bootstrap5.form7.BSInputSize
import com.fastscala.components.bootstrap5.form7.layout.F7BSFormInputGroup
import com.fastscala.components.bootstrap5.helpers.BSHelpers.{div, is_invalid}
import com.fastscala.components.form7.RenderHint
import com.fastscala.components.form7.fields.submit.F7SubmitButtonField
import com.fastscala.components.form7.mixins.InputSize
import com.fastscala.components.utils.Mutable

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, Node, NodeSeq}

trait BSFormInputGroupF7FieldRenderer extends Mutable {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  protected var onAroundDivTransforms: Elem => Elem = identity[Elem]
  protected var onInputElemTransforms: Elem => Elem = identity[Elem]
  protected var onInputGroupDivTransforms: Elem => Elem = identity[Elem]

  def onAroundDiv(f: Elem => Elem): this.type = mutate {
    onAroundDivTransforms = onAroundDivTransforms andThen f
  }

  def onInputElem(f: Elem => Elem): this.type = mutate {
    onInputElemTransforms = onInputElemTransforms andThen f
  }

  def onInputGroupDiv(f: Elem => Elem): this.type = mutate {
    onInputGroupDivTransforms = onInputGroupDivTransforms andThen f
  }

  def render(field: F7BSFormInputGroup)(
    nodesBeforeInputGroup: Seq[Node],
    nodesInsideInputGroup: Seq[Node],
    nodesAfterInputGroup: Seq[Node],
    inputSize: InputSize.Value,
    showErrors: Boolean
  ): Elem = {
    div.withId(field.aroundId).mb_3.apply {
      (nodesBeforeInputGroup ++
        input_group.withId(field.inputGroupId)
          .addClassIf(showErrors, is_invalid.getClassAttr)
          .pipe(elem => BSInputSize.inputGroupClass(inputSize).map(elem.addClass).getOrElse(elem))
          .apply(nodesInsideInputGroup.map({
            case elem: Elem if elem.label == "input" => onInputElemTransforms(elem)
            case node => node
          }): NodeSeq)
          .pipe(onInputGroupDivTransforms) ++
        nodesAfterInputGroup): NodeSeq
    }.pipe(onAroundDivTransforms)
  }
}
