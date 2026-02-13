package com.fastscala.components.bootstrap5.form7

import com.fastscala.components.utils.Mutable

import scala.xml.Elem

trait BSStandardF7ModifiableFieldRenderer extends Mutable {

  protected var onAroundDivTransforms: Elem => Elem = identity[Elem]
  protected var onLabelTransforms: Elem => Elem = identity[Elem]
  protected var onInputElemTransforms: Elem => Elem = identity[Elem]
  protected var onInvalidFeedbackTransforms: Elem => Elem = identity[Elem]
  protected var onValidFeedbackTransforms: Elem => Elem = identity[Elem]
  protected var onHelpTransforms: Elem => Elem = identity[Elem]

  def onAroundDiv(f: Elem => Elem): this.type = mutate {
    onAroundDivTransforms = onAroundDivTransforms andThen f
  }

  def onLabel(f: Elem => Elem): this.type = mutate {
    onLabelTransforms = onLabelTransforms andThen f
  }

  def onInputElem(f: Elem => Elem): this.type = mutate {
    onInputElemTransforms = onInputElemTransforms andThen f
  }

  def onInvalidFeedback(f: Elem => Elem): this.type = mutate {
    onInvalidFeedbackTransforms = onInvalidFeedbackTransforms andThen f
  }

  def onValidFeedback(f: Elem => Elem): this.type = mutate {
    onValidFeedbackTransforms = onValidFeedbackTransforms andThen f
  }

  def onHelp(f: Elem => Elem): this.type = mutate {
    onHelpTransforms = onHelpTransforms andThen f
  }

}
