package com.fastscala.components.bootstrap5.utils

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen

import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

    JS.rerenderableP[Boolean](rerenderer => implicit fsc => displaying => displaying match {
      case true =>
        val toEditJs = fsc.callback(() => rerenderer.rerender(false)).cmd
        val cur = get()
        transformDisplayElem(cur)(<span tabindex={tabindex.toString} onclick={toEditJs} onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {$toEditJs}"}>
          {toString(cur)}
        </span>)
      case false =>
        val valuesAndIndexes = all().zipWithIndex
        val submit = fsc.callback(JS.elementValueById(inputId), str => valuesAndIndexes.find(_._2.toString == str).map(_._1).map(set).getOrElse(JS.void) & rerenderer.rerender(true), ignoreErrors = true).cmd
        <div>
          <select id={inputId} class="form-select" onchange={submit} onblur={submit}>
            {valuesAndIndexes.map({ case (v, idx) => <option value={idx + ""}>
            {toString(v)}
          </option>
          }).mkNS}
          </select>
        </div>
    }).render(true)
  }

  def Str(
           get: () => String,
           set: String => Js,
           emptyString: String = "",
           displayElemClasses: String = "",
           editElemClasses: String = "",
           placeholder: String = "",
           transformDisplayElem: String => Elem => Elem = _ => identity[Elem](_),
           transformEditElem: String => Elem => Elem = _ => identity[Elem],
           confirm: Option[String => String] = None,
           tabindex: Int = 0,
           editing: Boolean = false,
           elemId: Option[String] = None
         )(implicit fsc: FSContext): NodeSeq = {
    val elemIdFinal = elemId.getOrElse(fsc.session.nextID("editable_str"))

    def renderEditing(): NodeSeq = {
      val submit = fsc.callback(JS.elementValueById(elemIdFinal), str => {
        if (get() != str) {
          confirm.map(_(str)).map(confirmMessage => JS.confirm(confirmMessage, fsc.callback(() => set(str.trim) & JS.replace(elemIdFinal, renderDisplaying())))).getOrElse({
            set(str.trim) & JS.replace(elemIdFinal, renderDisplaying())
          })
        } else JS.replace(elemIdFinal, renderDisplaying())
      }).cmd
      val cancel = JS.replace(elemIdFinal, renderDisplaying())
      transformEditElem(get())(<input class={editElemClasses} id={elemIdFinal} value={get()} onblur={submit} type="text" onkeypress={JS.onkeypress(13, 27)(JS.blur(elemIdFinal)).cmd} placeholder={if (placeholder == "") null else placeholder}/>)
    }

    def renderDisplaying(): NodeSeq = {
      val toEditJs = fsc.callback(() => JS.replace(elemIdFinal, renderEditing()) & JS.focus(elemIdFinal))
      val value = get()
      transformDisplayElem(value)(<span id={elemIdFinal}>
        <span class={displayElemClasses} tabindex={tabindex.toString} onclick={toEditJs.cmd} onkeypress={JS.onkeypress(13)(toEditJs).cmd}>
          {Some(value).filter(_ != "").getOrElse(emptyString)}
        </span>
      </span>)
    }

    if (editing) renderEditing() else renderDisplaying()
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
      val submit = fsc.callback(JS.elementValueById(inputId), str => {
        set(str.trim) & JS.evalIf(!alwaysEdit)(JS.replace(aroundId, displaying))
      }).cmd
      <div id={aroundId}>
        <textarea id={inputId}
                  onblur={submit}
                  class="form-control"
                  rows={rows.toString}
                  placeholder={if (placeholder == "") null else placeholder}>
          {get()}
        </textarea>
      </div>
    }

    def displaying: NodeSeq = {
      val toEditJs = fsc.callback(() => JS.replace(aroundId, editing) & JS.focus(inputId)).cmd
      <div id={aroundId}>
        <span tabindex={tabindex + ""} class={if (get() == "") emptyDisplayClasses else nonEmptyDisplayClasses} onclick={toEditJs} onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {$toEditJs}"}>
          {Some(get()).filter(_ != "").getOrElse(emptyString)}
        </span>
      </div>
    }

    if (alwaysEdit) editing else displaying
  }

  def DoubleOpt(
                 get: () => Option[Double],
                 set: Option[Double] => Js,
                 emptyString: String = "",
                 displayElemClasses: String = "",
                 editElemClasses: String = "",
                 placeholder: String = "",
                 transformDisplayElem: String => Elem => Elem = _ => identity[Elem],
                 transformEditElem: String => Elem => Elem = _ => identity[Elem],
                 confirm: Option[String => String] = None,
                 tabindex: Int = 0
               )(implicit fsc: FSContext): NodeSeq = Str(
    () => get().map(_.toString).getOrElse(""),
    str => if (str == "") set(None) else set(scala.util.Try(str.toDouble).toOption),
    emptyString = emptyString,
    displayElemClasses = displayElemClasses,
    editElemClasses = editElemClasses,
    placeholder = placeholder,
    transformDisplayElem = transformDisplayElem,
    transformEditElem = transformEditElem,
    tabindex = tabindex
  )

  def LocalDate(
                 get: () => Option[LocalDate],
                 set: Option[LocalDate] => Js,
                 emptyString: String = "",
                 displayElemClasses: String = "",
                 editElemClasses: String = "",
                 placeholder: String = "",
                 transformDisplayElem: String => Elem => Elem = _ => identity[Elem],
                 transformEditElem: String => Elem => Elem = _ => identity[Elem],
                 confirm: Option[String => String] = None,
                 mainDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                 otherFormats: List[String] = List("yyyy MM dd", "dd MM yyyy", "yyyy/MM/dd", "dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy", "yyyyMMdd", "ddMMyyyy"),
                 failedToParseDate: String = "Could not parse date.",
                 tabindex: Int = 0
               )(implicit fsc: FSContext): NodeSeq = {
    def fromStr(str: String): Option[LocalDate] = scala.util.Try(java.time.LocalDate.parse(str, mainDateFormat)).toOption.orElse(
      otherFormats.iterator.map(format => scala.util.Try(java.time.LocalDate.parse(str, DateTimeFormatter.ofPattern(format))).toOption).flatten.nextOption()
    )

    Str(
      () => get().map(date => mainDateFormat.format(date)).getOrElse(""),
      str => if (str == "") set(None) else {
        fromStr(str) match {
          case Some(date) => set(Some(date))
          case None => JS.alert(failedToParseDate)
        }
      },
      emptyString = emptyString,
      displayElemClasses = displayElemClasses,
      editElemClasses = editElemClasses,
      placeholder = placeholder,
      transformDisplayElem = transformDisplayElem,
      transformEditElem = transformEditElem,
      confirm = confirm.map(origFunc => dateStr => origFunc(fromStr(dateStr).map(date => mainDateFormat.format(date)).getOrElse(emptyString))),
      tabindex = tabindex
    )
  }

  def LocalDateNative(
                       get: () => Option[LocalDate],
                       set: Option[LocalDate] => Js,
                       emptyString: String = "",
                       displayElemClasses: String = "",
                       editElemClasses: String = "",
                       placeholder: String = "",
                       transformDisplayElem: Option[LocalDate] => Elem => Elem = _ => identity[Elem],
                       transformEditElem: Option[LocalDate] => Elem => Elem = _ => identity[Elem],
                       confirm: Option[Option[LocalDate] => String] = None,
                       mainDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                       otherFormats: List[String] = List("yyyy MM dd", "dd MM yyyy", "yyyy/MM/dd", "dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy", "yyyyMMdd", "ddMMyyyy"),
                       failedToParseDate: String = "Could not parse date.",
                       editing: Boolean = false,
                       tabindex: Int = 0,
                       elemId: Option[String] = None
                     )(implicit fsc: FSContext): NodeSeq = {
    def fromStr(str: String): Option[LocalDate] = scala.util.Try(java.time.LocalDate.parse(str, mainDateFormat)).toOption.orElse(
      otherFormats.iterator.map(format => scala.util.Try(java.time.LocalDate.parse(str, DateTimeFormatter.ofPattern(format))).toOption).flatten.nextOption()
    )

    val elemIdFinal = elemId.getOrElse(fsc.session.nextID("editable_str"))

    def renderEditing(): NodeSeq = {
      val submit = fsc.callback(JS.elementValueById(elemIdFinal), str => {
        val parsed = scala.util.Try(java.time.LocalDate.parse(str)).toOption
        if (get() != parsed) {
          confirm.map(_(parsed)).map(confirmMessage => JS.confirm(confirmMessage, fsc.callback(() => set(parsed) & JS.replace(elemIdFinal, renderDisplaying())))).getOrElse({
            set(parsed) & JS.replace(elemIdFinal, renderDisplaying())
          })
        } else
          JS.replace(elemIdFinal, renderDisplaying())
      }).cmd
      val cancel = JS.replace(elemIdFinal, renderDisplaying())
      transformEditElem(get())(<input class={editElemClasses} id={elemIdFinal} value={get().map(_.toString).getOrElse(null)} onblur={submit} type="date" onkeypress={JS.onkeypress(13, 27)(JS.blur(elemIdFinal)).cmd} placeholder={if (placeholder == "") null else placeholder}/>)
    }

    def renderDisplaying(): NodeSeq = {
      val toEditJs = fsc.callback(() => JS.replace(elemIdFinal, renderEditing()) & JS.focus(elemIdFinal))
      val value = get()
      transformDisplayElem(value)(<span id={elemIdFinal}>
        <span class={displayElemClasses} tabindex={tabindex.toString} onclick={toEditJs.cmd} onkeypress={JS.onkeypress(13)(toEditJs).cmd}>
          {value.map(_.toString).getOrElse(emptyString)}
        </span>
      </span>)
    }

    if (editing) renderEditing() else renderDisplaying()
  }
}
