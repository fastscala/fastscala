package com.fastscala.templates.form4

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.utils.{Button, ElemWithRandomId}
import com.fastscala.xml.scala_xml.JS
import com.fastscala.xml.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromNodeSeq

import scala.xml.NodeSeq

trait FormField {

  def render()(implicit form: Form, fsc: FSContext): NodeSeq

  def reRender()(implicit form: Form, fsc: FSContext): Js

  def fields: List[FormField]
}

trait StandardFormField extends FormField with ElemWithRandomId {

  def reRender()(implicit form: Form, fsc: FSContext): Js = JS.replace(elemId, render())

  def enabled: () => Boolean = () => true
}

class RawHtmlField(gen: => NodeSeq) extends StandardFormField {
  override def render()(implicit form: Form, fsc: FSContext): NodeSeq = gen

  override def fields: List[FormField] = List(this)
}

class SurroundWithHtmlField[T <: FormField](gen: T => NodeSeq)(field: T) extends StandardFormField {
  override def render()(implicit form: Form, fsc: FSContext): NodeSeq = gen(field)

  override def fields: List[FormField] = this :: field.fields
}

class VerticalField(children: FormField*) extends StandardFormField {
  override def render()(implicit form: Form, fsc: FSContext): NodeSeq = {
    children.map(_.render()).mkNS
  }

  override def fields: List[FormField] = this :: children.toList.flatMap(_.fields)
}

class TextField(
                                get: () => String,
                                set: String => Unit,
                                label: Option[String] = None,
                                name: Option[String] = None,
                                placeholder: Option[String] = None,
                                tabindex: Option[Int] = None,
                                maxlength: Option[Int] = None,
                                required: Boolean = false
                              ) extends StandardFormField {

  override def render()(implicit form: Form, fsc: FSContext): NodeSeq = {
    <div class="col-sm-12 kl-fancy-form">
      <input type="text"
             name={name.getOrElse(null)}
             class="form-control"
             id={elemId}
             onblur={fsc.callback(JS.elementValueById(elemId), value => {
               set(value);
               JS.void
             }).cmd}
             placeholder={placeholder.getOrElse(null)}
             value={get()}
             tabindex={tabindex.map(_ + "").getOrElse(null)}
             maxlength={maxlength.map(_ + "").getOrElse(null)}
             required={if (required) "true" else null}/>
      {label.map(label => <label class="control-label">{label}</label>).getOrElse(NodeSeq.Empty)}
    </div>
  }

  override def fields: List[FormField] = List(this)
}

class TextAreaField(
                                    get: () => String,
                                    set: String => Unit,
                                    label: Option[String] = None,
                                    name: Option[String] = None,
                                    placeholder: Option[String] = None,
                                    tabindex: Option[Int] = None,
                                    maxlength: Option[Int] = None,
                                    nRows: Int = 3,
                                    required: Boolean = false
                                  ) extends StandardFormField {

  override def render()(implicit form: Form, fsc: FSContext): NodeSeq = {
    <div class="col-sm-12 kl-fancy-form">
      <textarea type="text"
                name={name.getOrElse(null)}
                class="form-control"
                id={elemId}
                onblur={fsc.callback(JS.elementValueById(elemId), value => {
                  set(value);
                  JS.void
                }).cmd}
                placeholder={placeholder.getOrElse(null)}
                rows={nRows.toString}
                tabindex={tabindex.map(_ + "").getOrElse(null)}
                maxlength={maxlength.map(_ + "").getOrElse(null)}
                required={if (required) "true" else null}>{get()}</textarea>
      {label.map(label => <label class="control-label">{label}</label>).getOrElse(NodeSeq.Empty)}
    </div>
  }

  override def fields: List[FormField] = List(this)
}

class ButtonField(btn: Button) extends StandardFormField {

  override def fields: List[FormField] = Nil

  override def render()(implicit form: Form, fsc: FSContext): NodeSeq = <div class="col-sm-12">{btn.id(elemId).renderButton}</div>
}