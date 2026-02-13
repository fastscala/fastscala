package com.fastscala.components.form7.fields.html

import com.fastscala.components.form7.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, NodeSeq}

object F7HtmlField {
  def label(str: String) = new F7HtmlField(implicit fsc => <label>{str}</label>)
}

class F7HtmlField(
                   var gen: FSContext => NodeSeq
                 ) extends F7Field
  with F7FieldWithValidations
  with F7FieldWithDisabled
  with F7FieldWithDependencies
  with F7FieldWithEnabled {

  def label(str: String) = mutate {
    gen = fsc => <label>{str}</label>
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): Try[Js] =
    Failure(new Exception("Cannot update F7HtmlField without re-rendering"))

  override protected def renderImpl()(implicit form: Form7, fsc: FSContext): Elem =
    if (!enabled) <div style="display:none;" id={aroundId}></div>
    else <div id={aroundId}>{gen(fsc)}</div>

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}

