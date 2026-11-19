package com.fastscala.components.bootstrap5.utils

import com.fastscala.core.{FSContext, FSPage}
import com.fastscala.js.Js
import com.fastscala.js.JsHelper.RichJs
import com.fastscala.scala_xml.xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.xml.ElemOptimized
import com.fastscala.utils.IdGen

import org.eclipse.jetty.server.Request

import scala.xml.{Elem, NodeSeq, Text}

/**
 * The elements are built directly as [[ElemOptimized]] (not XML literals): attributes are added with `attr`, in
 * print order, and, like in an XML literal, an attribute whose value is null is left out.
 *
 * A field with a `name` and `persistInQueryString = true` keeps its value in the query string of the page's URL, under
 * that name (e.g. `?search=abc`): so reloading the page, coming back to it or sharing its link gets the value back
 * (like the tables' Table6PaginatedPersistedInQueryString). The value in the URL the page was loaded with is given to
 * `set` the first time the field is rendered in the page (if it's valid for the field and isn't what `get` returns
 * already; the Js `set` returns is dropped: the field is rendered next, with what `get` returns), and each change of
 * the field writes its value to the URL in the browser, without reloading (the URL's other parameters are left as they
 * are). The name is to be unique among what the page keeps in its query string.
 */
object ImmediateInputFields {

  private type Attrs = List[(String, ElemOptimized.AttrValue)]

  private val NoAttrs: Attrs = Nil

  /** Adds the attribute, unless `value` is null or empty (attrs are kept in reverse print order, so it's printed after the others). */
  extension (attrs: Attrs) {
    private inline def attr(name: String, value: String): Attrs = if ((value eq null) || value == "") attrs else (name -> value) :: attrs

    /**
     * Adds the attribute with `js` as its value, kept as a Js (XmlJsStream writes it part by part, without building its
     * text), unless `js` is null or Js.Void. (Not `js.isVoid`: that builds the text.)
     */
    private inline def attr(name: String, js: Js): Attrs = if ((js eq null) || (js eq Js.Void)) attrs else (name -> js) :: attrs
  }

  /** The names of the fields that already got their value from the query string, per page (see [[restoreFromQueryString]]). */
  private val restoredFromQueryString = new java.util.WeakHashMap[FSPage, java.util.Set[String]]()

  /**
   * If the field is persisted: gives `restore` the field's value in the query string of the URL the page was loaded with,
   * if there's one. Only the first time the field is rendered in the page: after that the request's URL is outdated
   * (the URL in the browser changes with the field), and the field's value is what `get` returns.
   */
  private def restoreFromQueryString(name: String, persistInQueryString: Boolean)(restore: String => Unit)(implicit fsc: FSContext): Unit =
    if (persistInQueryString) {
      require(name != "", "A field persisted in the query string needs a name.")
      val firstTime = restoredFromQueryString.synchronized {
        restoredFromQueryString.computeIfAbsent(fsc.page, _ => new java.util.HashSet[String]()).add(name)
      }
      if (firstTime) {
        Option(fsc.page.req).flatMap(req => Option(Request.extractQueryParameters(req).getValue(name))).foreach(restore)
      }
    }

  /** If the field is persisted: the Js that sets the field's value in the query string of the URL in the browser. */
  private def saveToQueryString(name: String, persistInQueryString: Boolean, value: String): Js =
    if (persistInQueryString) Js(
      s"(function(){if(!window.history||!window.history.replaceState)return;var u=new URL(window.location.href);" +
        s"u.searchParams.set(${JS.asJsStr(name)},${JS.asJsStr(value)});" +
        "if(u.href!==window.location.href)window.history.replaceState(window.history.state,'',u.href);})();"
    )
    else Js.Void

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
                containerClasses: String = "",
                persistInQueryString: Boolean = false
              )(implicit fsc: FSContext): Elem = {
    restoreFromQueryString(name, persistInQueryString)(_.toBooleanOption.filter(_ != get()).foreach(set))
    val inputId = "input" + fsc.session.nextID()
    val submit = JS.withVarStmt("value", JS.checkboxIsChecked(inputId))(value => {
      onSubmitClientSide(value) & fsc.callback(value, v => set(v.toBoolean) & saveToQueryString(name, persistInQueryString, v.toBoolean.toString))
    }) // (a Js: kept as it is in the attribute)
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
            ignoreUnchangedValue: Boolean = true,
            persistInQueryString: Boolean = false
          )(implicit fsc: FSContext): Elem = {
    restoreFromQueryString(name, persistInQueryString)(str => if (str.trim != get()) set(str.trim))
    val inputId = id.getOrElse("input" + fsc.session.nextID())

    val initialValue = get()

    val submit = JS.withVarStmt("value", JS.elementValueById(inputId))(value => {
      JS._if(
        if (ignoreUnchangedValue) value `_!=` JS.varOrElseUpdate(Js(s"window.currentValue$inputId"), JS.asJsStr(initialValue))
        else JS._true,
        _then = onSubmitClientSide(value) & fsc.callback(value, str => set(str.trim) & saveToQueryString(name, persistInQueryString, str.trim))
      )
    }) // (a Js: kept as it is in the attribute)
    val onkeypress: Js =
      if (changeOnEnter) Js(Js("event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {"), JS.blur(inputId), Js("}"))
      else null
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
    )(Seq(inputNS, btn.get.addClass("input-group-text").onclick(submit).btn) *)
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
             timeBeforeChangeMs: Long = 600,
             persistInQueryString: Boolean = false
           )(implicit fsc: FSContext): Elem = {
    restoreFromQueryString(name, persistInQueryString)(_.toDoubleOption.filter(v => v >= min && v <= max && v != get()).foreach(set))
    val inputId = id.getOrElse("input" + fsc.session.nextID())

    val initialValue = get()

    val submit = JS.withVarStmt("value", JS.elementValueById(inputId))(value => {
      Js(
        Js(
          s"""try {
             |  window.clearTimeout(window.timeout$inputId);
             |} catch(err) { }
             |window.timeout$inputId = window.setTimeout(function () { """.stripMargin),
        JS._if(
          if (ignoreUnchangedValue) value `_!=` JS.varOrElseUpdate(Js(s"window.currentValue$inputId"), JS.asJsStr(initialValue.toString))
          else JS._true,
          _then = onSubmitClientSide(value) & fsc.callback(value, str => set(str.toDouble) & saveToQueryString(name, persistInQueryString, str.toDouble.toString))
        ),
        Js(s"}, $timeBeforeChangeMs);")
      )
    }) // (a Js: kept as it is in the attribute)

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
                 onSubmitClientSide: Js => Js = _ => JS.void,
                 name: String = "",
                 persistInQueryString: Boolean = false
               )(implicit fsc: FSContext): Elem = {
    val values = all()

    // (Kept in the URL as the option's text, see `toString`: the first option with that text.)
    restoreFromQueryString(name, persistInQueryString)(str => values.find(toString(_) == str).filter(_ != get()).foreach(set))

    val submit = JS.withVarStmt("value", JS.elementValueById(elemId))(value => {
      onSubmitClientSide(value) & fsc.callback(value, idxStr => set(values(idxStr.toInt)) & saveToQueryString(name, persistInQueryString, toString(values(idxStr.toInt))))
    }) // (a Js: kept as it is in the attribute)

    val selected = get()
    val options = values.zipWithIndex.map({ case (value, idx) =>
      ElemOptimized(
        label = "option",
        attrs = NoAttrs.attr("value", idx.toString).attr("selected", if (value == selected) "selected" else null),
      )(textNode(toString(value)) *)
    })
    ElemOptimized(
      label = "select",
      attrs = NoAttrs.attr("id", elemId).attr("class", classes).attr("style", style).attr("name", name).attr("onchange", submit),
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
                onSubmitClientSide: Js => Js = _ => JS.void,
                persistInQueryString: Boolean = false
              )(implicit fsc: FSContext): Elem = {
    restoreFromQueryString(name, persistInQueryString)(str => if (str.trim != get()) set(str.trim))
    val inputId = "input" + fsc.session.nextID()

    val submit = JS.withVarStmt("value", JS.elementValueById(inputId))(value => {
      onSubmitClientSide(value) & fsc.callback(value, str => set(str.trim) & saveToQueryString(name, persistInQueryString, str.trim))
    }) // (a Js: kept as it is in the attribute)

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

  /** (Never kept in the query string: a password isn't to be in a URL.) */
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
