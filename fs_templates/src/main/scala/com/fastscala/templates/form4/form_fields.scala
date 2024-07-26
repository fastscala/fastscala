package com.fastscala.templates.form4

import com.fastscala.core.FSXmlUtils.EnrichSeqNodeSeq
import com.fastscala.core.{FSContext, FSXmlEnv}
import com.fastscala.js.Js
import com.fastscala.templates.utils.{Button, ElemWithRandomId}

import scala.xml.NodeSeq

trait FormField[E <: FSXmlEnv] {

  def render()(implicit form: Form[E], fsc: FSContext): E#NodeSeq

  def reRender()(implicit form: Form[E], fsc: FSContext): Js

  def fields: List[FormField[E]]
}

trait StandardFormField[E <: FSXmlEnv] extends FormField[E] with ElemWithRandomId {

  def reRender()(implicit form: Form[E], fsc: FSContext): Js = Js.replace(elemId, render())(form.fsXmlSupport)

  def enabled: () => Boolean = () => true
}

class RawHtmlField[E <: FSXmlEnv](gen: => E#NodeSeq) extends StandardFormField[E] {
  override def render()(implicit form: Form[E], fsc: FSContext): E#NodeSeq = gen

  override def fields: List[FormField[E]] = List(this)
}

class SurroundWithHtmlField[E <: FSXmlEnv, T <: FormField[E]](gen: T => E#NodeSeq)(field: T) extends StandardFormField[E] {
  override def render()(implicit form: Form[E], fsc: FSContext): E#NodeSeq = gen(field)

  override def fields: List[FormField[E]] = this :: field.fields
}

class VerticalField[E <: FSXmlEnv](children: FormField[E]*) extends StandardFormField[E] {
  override def render()(implicit form: Form[E], fsc: FSContext): E#NodeSeq = {
    import form.fsXmlSupport
    children.map(_.render()).mkNS()
  }

  override def fields: List[FormField[E]] = this :: children.toList.flatMap(_.fields)
}

class TextField[E <: FSXmlEnv](
                                get: () => String,
                                set: String => Unit,
                                label: Option[String] = None,
                                name: Option[String] = None,
                                placeholder: Option[String] = None,
                                tabindex: Option[Int] = None,
                                maxlength: Option[Int] = None,
                                required: Boolean = false
                              ) extends StandardFormField[E] {

  override def render()(implicit form: Form[E], fsc: FSContext): E#NodeSeq = {
    import com.fastscala.xml.scala_xml.FSScalaXmlSupport._
    import form.fsXmlSupport
    <div class="col-sm-12 kl-fancy-form">
      <input type="text"
             name={name.getOrElse(null)}
             class="form-control"
             id={elemId}
             onblur={fsc.callback(Js.elementValueById(elemId), value => {
               set(value);
               Js.void
             }).cmd}
             placeholder={placeholder.getOrElse(null)}
             value={get()}
             tabindex={tabindex.map(_ + "").getOrElse(null)}
             maxlength={maxlength.map(_ + "").getOrElse(null)}
             required={if (required) "true" else null}/>
      {label.map(label => <label class="control-label">{label}</label>).getOrElse(NodeSeq.Empty)}
    </div>.asFSXml()
  }

  override def fields: List[FormField[E]] = List(this)
}

class TextAreaField[E <: FSXmlEnv](
                                    get: () => String,
                                    set: String => Unit,
                                    label: Option[String] = None,
                                    name: Option[String] = None,
                                    placeholder: Option[String] = None,
                                    tabindex: Option[Int] = None,
                                    maxlength: Option[Int] = None,
                                    nRows: Int = 3,
                                    required: Boolean = false
                                  ) extends StandardFormField[E] {

  override def render()(implicit form: Form[E], fsc: FSContext): E#NodeSeq = {
    import com.fastscala.xml.scala_xml.FSScalaXmlSupport._
    import form.fsXmlSupport
    <div class="col-sm-12 kl-fancy-form">
      <textarea type="text"
                name={name.getOrElse(null)}
                class="form-control"
                id={elemId}
                onblur={fsc.callback(Js.elementValueById(elemId), value => {
                  set(value);
                  Js.void
                }).cmd}
                placeholder={placeholder.getOrElse(null)}
                rows={nRows.toString}
                tabindex={tabindex.map(_ + "").getOrElse(null)}
                maxlength={maxlength.map(_ + "").getOrElse(null)}
                required={if (required) "true" else null}>{get()}</textarea>
      {label.map(label => <label class="control-label">{label}</label>).getOrElse(NodeSeq.Empty)}
    </div>.asFSXml()
  }

  override def fields: List[FormField[E]] = List(this)
}

class ButtonField[E <: FSXmlEnv](btn: Button) extends StandardFormField[E] {

  override def fields: List[FormField[E]] = Nil

  override def render()(implicit form: Form[E], fsc: FSContext): E#NodeSeq = {
    import com.fastscala.xml.scala_xml.FSScalaXmlSupport._
    import form.fsXmlSupport
    <div class="col-sm-12">{btn.id(elemId).renderButton}</div>
  }
}