package com.fastscala.templates.bootstrap5.utils

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.js.JsOps.RichJs
import com.fastscala.utils.IdGen
import com.fastscala.xml.scala_xml.JS

import scala.xml.Elem

object ImmediateInputFields {

  def checkbox(
                get: () => Boolean,
                set: Boolean => Js,
                label: String,
                classes: String = "",
                name: String = "",
                tabindex: Option[Int] = None,
                onSubmitClientSide: Js => Js = _ => JS.void
              )(implicit fsc: FSContext): Elem = {
    val inputId = "input" + fsc.session.nextID()
    val submit = JS.withVarStmt("value", JS.checkboxIsChecked(inputId))(value => {
      onSubmitClientSide(value) & fsc.callback(value, v => set(v.toBoolean))
    }).cmd
    <div class="form-check form-check-custom form-check-solid">
      <input class={"form-check-input " + classes}
             type="checkbox"
             checked={if (get()) "checked" else null}
             id={inputId}
             name={name}
             tabindex={tabindex.map(_ + "").getOrElse(null)}
             onchange={submit}
      />
      <label class="form-check-label" for={inputId}>{label}</label>
    </div>
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
        _then =
          JS.consoleLog(value) &
            onSubmitClientSide(value) & fsc.callback(value, str => set(str.trim))
      )
    }).cmd
    val onkeypress = if (changeOnEnter) s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {${JS.blur(inputId).cmd}}" else null
    val onblur = if (btn.isEmpty) submit else null

    val inputNS = <input id={inputId} type={`type`} value={initialValue} onblur={onblur} onkeypress={onkeypress} class={classes} style={style} autocomplete="autocomplete"
       placeholder={if (placeholder == "") null else placeholder}
       name={if (name == "") null else name}
      />

    btn.map(btn => {
      <div class="input-group ">
        {inputNS}
        {btn.addClass("input-group-text").onclick(Js(submit)).btn}
      </div>
    }).getOrElse(inputNS)
  }

  def range(
             get: () => Int,
             set: Int => Js,
             min: Int,
             max: Int,
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
          _then = onSubmitClientSide(value) & fsc.callback(value, str => set(str.toInt))
        ).cmd + s"}, $timeBeforeChangeMs);"
      )
    }).cmd

    val inputNS = <input min={min.toString} max={max.toString} id={inputId} type={`type`} value={initialValue.toString} class={classes} style={style}
       name={if (name == "") null else name}
        onchange={submit}
      />
    inputNS
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

    <select id={elemId} class={classes} style={style} onchange={submit}>
      {
      values.zipWithIndex.map({
        case (value, idx) if value == get() => <option value={idx.toString} selected="selected">{toString(value)}</option>
        case (value, idx) => <option value={idx.toString}>{toString(value)}</option>
      })
      }
    </select>
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

    val inputNS = <textarea
      id={inputId} onblur={submit} class={classes} style={style}
        placeholder={if (placeholder == "") null else placeholder}
        name={if (name == "") null else name}
        rows={rows + ""}
      >{get()}</textarea>

    inputNS
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
