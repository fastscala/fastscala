package com.fastscala.templates.bootstrap5.utils

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.utils.IdGen
import com.fastscala.utils.NodeSeqUtils.MkNSFromNodeSeq
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

import scala.xml.{Elem, NodeSeq}

object ClickToEditFields {

  def Select[T](
                 all: () => List[T],
                 get: () => T,
                 set: T => Js,
                 toString: T => String,
                 tabindex: Int = 0,
                 transformDisplayElem: T => Elem => Elem = (_: T) => identity[Elem](_)
               )(implicit fsc: FSContext): NodeSeq = {
    val inputId = IdGen.id("input")

    Js.rerenderableP[Boolean](rerenderer => implicit fsc => displaying => displaying match {
      case true =>
        val toEditJs = fsc.callback(() => rerenderer.rerender(false)).cmd
        val cur = get()
        transformDisplayElem(cur)(<span tabindex={tabindex + ""} onclick={toEditJs} onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {$toEditJs}"}>{toString(cur)}</span>)
      case false =>
        val valuesAndIndexes = all().zipWithIndex
        val submit = fsc.callback(Js.elementValueById(inputId), str => valuesAndIndexes.find(_._2.toString == str).map(_._1).map(set).getOrElse(Js.void) & rerenderer.rerender(true).debug("rerender"), ignoreErrors = true).cmd
        <div>
          <select id={inputId} class="form-select" onchange={submit} onblur={submit}>
            {
            valuesAndIndexes.map({ case (v, idx) => <option value={idx + ""}>{toString(v)}</option> }).mkNS
            }
          </select>
        </div>
    }).render(true)
  }

  def Str(
           get: () => String,
           set: String => Js,
           emptyString: String = "",
           emptyDisplayClasses: String = "",
           nonEmptyDisplayClasses: String = "",
           placeholder: String = "",
           tabindex: Int = 0
         )(implicit fsc: FSContext): NodeSeq = {
    val aroundId = fsc.session.nextID("around")
    val inputId = s"input$aroundId"

    def editing: NodeSeq = {
      val submit = fsc.callback(Js.elementValueById(inputId), str => set(str.trim) & Js.replace(aroundId, displaying)).cmd
      <div id={aroundId}>
        <input id={inputId} value={get()} onblur={submit} type="text" onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {$submit}"} class="form-control" placeholder={if (placeholder == "") null else placeholder}/>
      </div>
    }

    def displaying: NodeSeq = {
      val toEditJs = fsc.callback(() => Js.replace(aroundId, editing) & Js.focus(inputId)).cmd
      <span id={aroundId}><span tabindex={tabindex + ""} class={if (get() == "") emptyDisplayClasses else nonEmptyDisplayClasses} onclick={toEditJs} onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {$toEditJs}"}>{Some(get()).filter(_ != "").getOrElse(emptyString)}</span></span>
    }

    displaying
  }

  def Textarea(
                get: () => String,
                set: String => Js,
                emptyString: String = "",
                emptyDisplayClasses: String = "",
                nonEmptyDisplayClasses: String = "",
                placeholder: String = "",
                tabindex: Int = 0,
                rows: Int = 4,
                alwaysEdit: Boolean = false
              )(implicit fsc: FSContext): NodeSeq = {
    val aroundId = fsc.session.nextID("around")
    val inputId = s"input$aroundId"

    def editing: NodeSeq = {
      val submit = fsc.callback(Js.elementValueById(inputId), str => {
        set(str.trim) & Js.evalIf(!alwaysEdit)(Js.replace(aroundId, displaying))
      }).cmd
      <div id={aroundId}>
        <textarea id={inputId}
                  onblur={submit}
                  class="form-control"
                  rows={rows.toString}
                  placeholder={if (placeholder == "") null else placeholder}
        >{get()}</textarea>
      </div>
    }

    def displaying: NodeSeq = {
      val toEditJs = fsc.callback(() => Js.replace(aroundId, editing) & Js.focus(inputId)).cmd
      <div id={aroundId}><span tabindex={tabindex + ""} class={if (get() == "") emptyDisplayClasses else nonEmptyDisplayClasses} onclick={toEditJs} onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {$toEditJs}"}>{Some(get()).filter(_ != "").getOrElse(emptyString)}</span></div>
    }

    if (alwaysEdit) editing else displaying
  }

  def DoubleOpt(
                 get: () => Option[Double],
                 set: Option[Double] => Js,
                 emptyString: String = "",
                 emptyDisplayClasses: String = "",
                 nonEmptyDisplayClasses: String = "",
                 placeholder: String = "",
                 tabindex: Int = 0
               )(implicit fsc: FSContext): NodeSeq = Str(
    () => get().map(_.toString).getOrElse(""),
    str => if (str == "") set(None) else set(scala.util.Try(str.toDouble).toOption),
    emptyString,
    emptyDisplayClasses,
    nonEmptyDisplayClasses,
    placeholder,
    tabindex
  )

  def Date(
            get: () => Option[LocalDate],
            set: Option[LocalDate] => Js,
            emptyString: String = "",
            emptyDisplayClasses: String = "",
            nonEmptyDisplayClasses: String = "",
            placeholder: String = "",
            tabindex: Int = 0
          )(implicit fsc: FSContext): NodeSeq = Str(
    () => get().map(_.toString("yyyy-MM-dd")).getOrElse(""),
    str => if (str == "") set(None) else set(
      scala.util.Try(LocalDate.parse(str, DateTimeFormat.forPattern("yyyy-MM-dd"))).toOption.orElse(
        scala.util.Try(LocalDate.parse(str, DateTimeFormat.forPattern("yyyyMMdd"))).toOption
      )
    ),
    emptyString,
    emptyDisplayClasses,
    nonEmptyDisplayClasses,
    placeholder,
    tabindex
  )
}
