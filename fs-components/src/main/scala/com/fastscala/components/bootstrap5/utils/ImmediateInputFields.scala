package com.fastscala.components.bootstrap5.utils

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.js.JsHelper.RichJs
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.utils.ElemOptimized
import com.fastscala.utils.IdGen

import scala.xml.{Elem, NodeSeq, Text}

/**
 * The elements are built directly as [[ElemOptimized]] (not XML literals): attributes are added with `attr`, in
 * print order, and, like in an XML literal, an attribute whose value is null is left out.
 */
object ImmediateInputFields {

  private type Attrs = List[(String, String)]

  private val NoAttrs: Attrs = Nil

  /** Adds the attribute, unless `value` is null or empty (attrs are kept in reverse print order, so it's printed after the others). */
  extension (attrs: Attrs) {
    private inline def attr(name: String, value: String): Attrs = if ((value eq null) || value == "") attrs else (name -> value) :: attrs
  }

  /** An element without a closing tag (like `<input/>` in a literal). */
  private def empty(label: String, attrs: Attrs): Elem = ElemOptimized(label = label, minimizeEmpty = true, attrs = attrs)()

  /** `content` as a text node; nothing for null (as in an XML literal). */
  private def textNode(content: String): Seq[Text] = if (content eq null) Nil else Seq(Text(content))

  def checkbox(
                get: () => Boolean,
                set: Boolean => Js,
                label: String,
                inputClass: String = "form-check-input",
                inputStyle: String = "",
                labelClass: String = "form-check-label",
                labelStyle: String = "",
                name: String = "",
                tabindex: Option[Int] = None,
                onSubmitClientSide: Js => Js = _ => JS.void,
                additionalAttrs: List[(String, String)] = Nil,
                containerClasses: String = ""
              )(implicit fsc: FSContext): Elem = {
    val inputId = "input" + fsc.session.nextID()
    val submit = JS.withVarStmt("value", JS.checkboxIsChecked(inputId))(value => {
      onSubmitClientSide(value) & fsc.callback(value, v => set(v.toBoolean))
    }).cmd
    val input = empty("input", NoAttrs
      .attr("class", inputClass)
      .attr("style", inputStyle)
      .attr("type", "checkbox")
      .attr("checked", if (get()) "checked" else null)
      .attr("id", inputId)
      .attr("name", name)
      .attr("tabindex", if (tabindex.isDefined) tabindex.get.toString else null)
      .attr("onchange", submit)
    ).withAttrs(additionalAttrs *)
    val labelElem = ElemOptimized(
      label = "label",
      attrs = NoAttrs.attr("class", labelClass).attr("style", labelStyle).attr("for", inputId),
    )(textNode(label) *)
    ElemOptimized(
      label = "div",
      attrs = NoAttrs.attr("class", "form-check form-check-custom form-check-solid " + containerClasses),
    )(Seq(input, labelElem) *)
  }

  def text(
            get: () => String,
            set: String => Js,
            classes: String = "form-control",
            style: String = "",
            name: String = "",
            autocomplete: String = "off",
            `type`: String = "text",
            placeholder: String = "",
            changeOnEnter: Boolean = true,
            btn: Option[BSBtn] = None,
            id: Option[String] = None,
            onSubmitClientSide: Js => Js = _ => JS.void,
            ignoreUnchangedValue: Boolean = true
          )(implicit fsc: FSContext): Elem = {
    val inputId = id.getOrElse("input" + fsc.session.nextID())

    val initialValue = get()

    val submit = JS.withVarStmt("value", JS.elementValueById(inputId))(value => {
      JS._if(
        if (ignoreUnchangedValue) value `_!=` JS.varOrElseUpdate(Js(s"window.currentValue$inputId"), JS.asJsStr(initialValue))
        else JS._true,
        _then = onSubmitClientSide(value) & fsc.callback(value, str => set(str.trim))
      )
    }).cmd
    val onkeypress = if (changeOnEnter) s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {${JS.blur(inputId).cmd}}" else null
    val onblur = if (btn.isEmpty) submit else null

    val inputNS = empty("input", NoAttrs
      .attr("id", inputId)
      .attr("type", `type`)
      .attr("value", initialValue)
      .attr("onblur", onblur)
      .attr("onkeypress", onkeypress)
      .attr("class", classes)
      .attr("style", style)
      .attr("autocomplete", autocomplete)
      .attr("placeholder", if (placeholder == "") null else placeholder)
      .attr("name", if (name == "") null else name)
    )

    if (btn.isDefined) ElemOptimized(
      label = "div",
      attrs = NoAttrs.attr("class", "input-group "),
    )(Seq(inputNS, btn.get.addClass("input-group-text").onclick(Js(submit)).btn) *)
    else inputNS
  }

  def range(
             get: () => Double,
             set: Double => Js,
             min: Double,
             max: Double,
             step: Option[Double] = None,
             classes: String = "",
             style: String = "",
             name: String = "",
             `type`: String = "range",
             id: Option[String] = None,
             onSubmitClientSide: Js => Js = _ => JS.void,
             ignoreUnchangedValue: Boolean = true,
             timeBeforeChangeMs: Long = 600
           )(implicit fsc: FSContext): Elem = {
    val inputId = id.getOrElse("input" + fsc.session.nextID())

    val initialValue = get()

    val submit = JS.withVarStmt("value", JS.elementValueById(inputId))(value => {
      Js(
        s"""try {
           |  window.clearTimeout(window.timeout$inputId);
           |} catch(err) { }
           |window.timeout$inputId = window.setTimeout(function () { """.stripMargin + JS._if(
          if (ignoreUnchangedValue) value `_!=` JS.varOrElseUpdate(Js(s"window.currentValue$inputId"), JS.asJsStr(initialValue.toString))
          else JS._true,
          _then = onSubmitClientSide(value) & fsc.callback(value, str => set(str.toDouble))
        ).cmd + s"}, $timeBeforeChangeMs);"
      )
    }).cmd

    empty("input", NoAttrs
      .attr("min", min.toString)
      .attr("max", max.toString)
      .attr("step", if (step.isDefined) step.get.toString else null)
      .attr("id", inputId)
      .attr("type", `type`)
      .attr("value", initialValue.toString)
      .attr("class", classes)
      .attr("style", style)
      .attr("name", if (name == "") null else name)
      .attr("onchange", submit)
    )
  }

  def select[T](
                 all: () => Seq[T],
                 get: () => T,
                 set: T => Js,
                 classes: String = "form-select",
                 style: String = "",
                 toString: T => String = (_: T).toString,
                 elemId: String = IdGen.id,
                 onSubmitClientSide: Js => Js = _ => JS.void
               )(implicit fsc: FSContext): Elem = {
    val values = all()

    val submit = JS.withVarStmt("value", JS.elementValueById(elemId))(value => {
      onSubmitClientSide(value) & fsc.callback(value, idxStr => set(values(idxStr.toInt)))
    }).cmd

    val selected = get()
    val options = values.zipWithIndex.map({ case (value, idx) =>
      ElemOptimized(
        label = "option",
        attrs = NoAttrs.attr("value", idx.toString).attr("selected", if (value == selected) "selected" else null),
      )(textNode(toString(value)) *)
    })
    ElemOptimized(
      label = "select",
      attrs = NoAttrs.attr("id", elemId).attr("class", classes).attr("style", style).attr("onchange", submit),
    )(options *)
  }

  def textarea(
                get: () => String,
                set: String => Js,
                classes: String = "",
                style: String = "",
                name: String = "",
                placeholder: String = "",
                rows: Int = 8,
                onSubmitClientSide: Js => Js = _ => JS.void
              )(implicit fsc: FSContext): Elem = {
    val inputId = "input" + fsc.session.nextID()

    val submit = JS.withVarStmt("value", JS.elementValueById(inputId))(value => {
      onSubmitClientSide(value) & fsc.callback(value, str => set(str.trim))
    }).cmd

    ElemOptimized(
      label = "textarea",
      attrs = NoAttrs
        .attr("id", inputId)
        .attr("onblur", submit)
        .attr("class", classes)
        .attr("style", style)
        .attr("placeholder", if (placeholder == "") null else placeholder)
        .attr("name", if (name == "") null else name)
        .attr("rows", rows.toString),
    )(textNode(get()) *)
  }

  def password(
                get: () => String,
                set: String => Js,
                classes: String = "",
                name: String = "",
                autocomplete: String = "off",
                placeholder: String = "",
                id: Option[String] = None,
                onSubmitClientSide: Js => Js = _ => JS.void
              )(implicit fsc: FSContext): Elem = text(
    get = get,
    set = set,
    classes = classes,
    name = name,
    autocomplete = autocomplete,
    `type` = "password",
    placeholder = placeholder,
    id = id,
    onSubmitClientSide = onSubmitClientSide
  )

}
