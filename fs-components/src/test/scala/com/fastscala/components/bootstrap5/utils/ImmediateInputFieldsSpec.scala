package com.fastscala.components.bootstrap5.utils

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.testutils.PerfFS
import com.fastscala.testutils.AttributeOrder.normalized
import com.fastscala.testutils.XmlStreamWrite.{writtenNode}
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.*

/**
 * [[ImmediateInputFields]] builds its elements directly (as ElemOptimized), instead of with XML literals. Checked:
 *  - against the previous, XML-literal implementations ([[ImmediateInputFieldsSpec.Literal]]): the same elements,
 *    attributes and text, apart from what the direct version intentionally drops (empty attributes, and whitespace-only
 *    text between elements) and the generated ids (input ids, callback ids);
 *  - that toString and [[XmlStream]] write the same (attributes in any order), that no attribute is empty, and each field registers one callback;
 *  - a few behaviours directly (checked, additionalAttrs, the selected option, the button's input group, ...).
 *
 * Run with: `sbt "fs_components/testOnly com.fastscala.components.bootstrap5.utils.ImmediateInputFieldsSpec"`
 */
class ImmediateInputFieldsSpec extends AnyFlatSpec {

  import ImmediateInputFieldsSpec.*

  private val F = ImmediateInputFields
  private val L = Literal

  private def newFsc(): FSContext = PerfFS.newPage().rootFSContext

  private def written(n: Node): String = writtenNode(n)

  /** The generated ids replaced by placeholders, so two renders (registering different callbacks) can be compared. */
  private def normIds(s: String): String = s
    .replaceAll("""window\._fs\.callback\(value,"[^"]*","[^"]*"""", """window._fs.callback(value,"PAGE","FUNC"""")
    .replaceAll("input[0-9a-zA-Z]+", "inputID")

  /** An element as compared: label, attributes (without empty ones, ids normalized), and children (without whitespace-only text). */
  private def canon(n: Node): Any = n match {
    case e: Elem =>
      val attrs = e.attributes.toList.map(m => m.key -> normIds(m.value.text)).filter(_._2 != "").toSet
      val children = e.child.filter({ case t: Atom[?] => t.text.trim.nonEmpty; case _ => true }).map(canon).toList
      (e.label, attrs, children, children.isEmpty && e.minimizeEmpty)
    // (a literal embeds a String as an Atom[String], the direct version as a Text: both are written the same)
    case t: Atom[?] => "text:" + normIds(t.text).trim
    case other => other.getClass.getSimpleName + ":" + normIds(other.text)
  }

  private def allElems(n: Node): Seq[Elem] = n match {
    case e: Elem => e +: e.child.flatMap(allElems)
    case _ => Nil
  }

  /** Each case: the same call to ImmediateInputFields and to its previous (literal) implementation. */
  private val Cases: Seq[(String, FSContext => Elem, FSContext => Elem)] = Seq(
    ("checkbox, defaults",
      implicit fsc => F.checkbox(() => true, _ => JS.void, "Lbl"),
      implicit fsc => L.checkbox(() => true, _ => JS.void, "Lbl")),
    ("checkbox, unchecked, tabindex, additionalAttrs, null styles",
      implicit fsc => F.checkbox(() => false, _ => JS.void, "Lbl <&>", inputStyle = null, labelStyle = null, name = "n", tabindex = Some(3), additionalAttrs = List("data-x" -> "1", "class" -> "override"), containerClasses = "c1"),
      implicit fsc => L.checkbox(() => false, _ => JS.void, "Lbl <&>", inputStyle = null, labelStyle = null, name = "n", tabindex = Some(3), additionalAttrs = List("data-x" -> "1", "class" -> "override"), containerClasses = "c1")),
    ("text, defaults (value to escape)",
      implicit fsc => F.text(() => "v\"<", _ => JS.void),
      implicit fsc => L.text(() => "v\"<", _ => JS.void)),
    ("text, all options",
      implicit fsc => F.text(() => "v", _ => JS.void, classes = "c", style = "s", name = "nm", placeholder = "ph", changeOnEnter = false, id = Some("myid"), ignoreUnchangedValue = false),
      implicit fsc => L.text(() => "v", _ => JS.void, classes = "c", style = "s", name = "nm", placeholder = "ph", changeOnEnter = false, id = Some("myid"), ignoreUnchangedValue = false)),
    ("text, null value and style",
      implicit fsc => F.text(() => null, _ => JS.void, style = null),
      implicit fsc => L.text(() => null, _ => JS.void, style = null)),
    ("text, with a button",
      implicit fsc => F.text(() => "v", _ => JS.void, btn = Some(BSBtn().BtnPrimary.lbl("Go")), id = Some("myid2")),
      implicit fsc => L.text(() => "v", _ => JS.void, btn = Some(BSBtn().BtnPrimary.lbl("Go")), id = Some("myid2"))),
    ("password",
      implicit fsc => F.password(() => "p", _ => JS.void, placeholder = "pw", id = Some("pid")),
      implicit fsc => L.password(() => "p", _ => JS.void, placeholder = "pw", id = Some("pid"))),
    ("range, defaults",
      implicit fsc => F.range(() => 1.5, _ => JS.void, 0, 10, id = Some("r1")),
      implicit fsc => L.range(() => 1.5, _ => JS.void, 0, 10, id = Some("r1"))),
    ("range, step and name",
      implicit fsc => F.range(() => 1.5, _ => JS.void, 0, 10, step = Some(0.5), classes = "c", style = "s", name = "nm", id = Some("r2"), ignoreUnchangedValue = false),
      implicit fsc => L.range(() => 1.5, _ => JS.void, 0, 10, step = Some(0.5), classes = "c", style = "s", name = "nm", id = Some("r2"), ignoreUnchangedValue = false)),
    ("select",
      implicit fsc => F.select[String](() => Seq("a", "b<", "c"), () => "b<", _ => JS.void, elemId = "sel1"),
      implicit fsc => L.select[String](() => Seq("a", "b<", "c"), () => "b<", _ => JS.void, elemId = "sel1")),
    ("select, none selected, null style",
      implicit fsc => F.select[Int](() => Seq(1, 2), () => 3, _ => JS.void, style = null, toString = i => s"#$i", elemId = "sel2"),
      implicit fsc => L.select[Int](() => Seq(1, 2), () => 3, _ => JS.void, style = null, toString = i => s"#$i", elemId = "sel2")),
    ("select, no values",
      implicit fsc => F.select[Int](() => Nil, () => 1, _ => JS.void, elemId = "sel3"),
      implicit fsc => L.select[Int](() => Nil, () => 1, _ => JS.void, elemId = "sel3")),
    ("textarea, defaults",
      implicit fsc => F.textarea(() => "line1\nline2 <&>", _ => JS.void),
      implicit fsc => L.textarea(() => "line1\nline2 <&>", _ => JS.void)),
    ("textarea, all options",
      implicit fsc => F.textarea(() => "x", _ => JS.void, classes = "c", style = "s", name = "nm", placeholder = "ph", rows = 3),
      implicit fsc => L.textarea(() => "x", _ => JS.void, classes = "c", style = "s", name = "nm", placeholder = "ph", rows = 3)),
  )

  Cases.foreach { case (name, current, literal) =>
    s"ImmediateInputFields: $name" should "render as its XML-literal version (apart from empty attributes, whitespace and ids)" in {
      val (c, l) = (current(newFsc()), literal(newFsc()))
      assert(canon(c) == canon(l), s"\n current: ${canon(c)}\n literal: ${canon(l)}")
    }

    it should "write the same with toString and XmlStream (attributes in any order), without empty or duplicate attributes" in {
      val e = current(newFsc())
      assert(normalized(written(e)) == normalized(e.toString))
      for (el <- allElems(e)) {
        val attrs = el.attributes.toList.map(m => m.key -> m.value.text)
        assert(attrs.forall(_._2 != ""), s"empty attribute in <${el.label}>: $attrs")
        assert(attrs.map(_._1).distinct.size == attrs.size, s"duplicate attribute in <${el.label}>: $attrs")
      }
    }

    it should "register one callback" in {
      val fsc = newFsc()
      current(fsc)
      assert(fsc.page.callbacks.size == 1)
    }
  }

  private def attrsOf(e: Elem): Map[String, String] = e.attributes.toList.map(m => m.key -> m.value.text).toMap

  private def find(e: Elem, label: String): Seq[Elem] = allElems(e).filter(_.label == label)

  "ImmediateInputFields.checkbox" should "mark the input checked only when get() is true, and label it for the input" in {
    implicit val fsc: FSContext = newFsc()
    val checked = find(F.checkbox(() => true, _ => JS.void, "Lbl"), "input").head
    val unchecked = find(F.checkbox(() => false, _ => JS.void, "Lbl"), "input").head
    assert(attrsOf(checked).get("checked").contains("checked"))
    assert(!attrsOf(unchecked).contains("checked"))
    val cb = F.checkbox(() => true, _ => JS.void, "Lbl")
    val (input, label) = (find(cb, "input").head, find(cb, "label").head)
    assert(attrsOf(label).get("for") == attrsOf(input).get("id"))
    assert(label.text == "Lbl")
    assert(input.minimizeEmpty && written(input).endsWith("/>"))
  }

  it should "apply additionalAttrs over the built-in attributes" in {
    implicit val fsc: FSContext = newFsc()
    val input = find(F.checkbox(() => true, _ => JS.void, "Lbl", additionalAttrs = List("class" -> "mine", "data-x" -> "1")), "input").head
    assert(attrsOf(input).get("class").contains("mine"))
    assert(attrsOf(input).get("data-x").contains("1"))
    assert(attrsOf(input).get("type").contains("checkbox"))
  }

  "ImmediateInputFields.text" should "submit on blur without a button, and from the button (in an input group) with one" in {
    implicit val fsc: FSContext = newFsc()
    val plain = F.text(() => "v", _ => JS.void, id = Some("t1"))
    assert(plain.label == "input" && attrsOf(plain).contains("onblur"))
    assert(attrsOf(plain).get("value").contains("v"))

    val withBtn = F.text(() => "v", _ => JS.void, btn = Some(BSBtn().BtnPrimary.lbl("Go")), id = Some("t2"))
    assert(withBtn.label == "div" && attrsOf(withBtn).get("class").exists(_.contains("input-group")))
    val input = find(withBtn, "input").head
    val button = find(withBtn, "button").head
    assert(!attrsOf(input).contains("onblur"))
    assert(attrsOf(button).get("class").exists(_.split(' ').contains("input-group-text")))
    assert(attrsOf(button).get("onclick").exists(_.contains("window._fs.callback(")))
  }

  it should "leave out empty attributes (placeholder, name, style)" in {
    implicit val fsc: FSContext = newFsc()
    val attrs = attrsOf(F.text(() => "v", _ => JS.void, style = "", name = "", placeholder = ""))
    assert(!attrs.contains("placeholder") && !attrs.contains("name") && !attrs.contains("style"))
  }

  it should "use the autocomplete parameter (\"off\" by default)" in {
    implicit val fsc: FSContext = newFsc()
    assert(attrsOf(F.text(() => "v", _ => JS.void)).get("autocomplete").contains("off"))
    assert(attrsOf(F.text(() => "v", _ => JS.void, autocomplete = "email")).get("autocomplete").contains("email"))
    assert(attrsOf(F.password(() => "v", _ => JS.void, autocomplete = "new-password")).get("autocomplete").contains("new-password"))
  }

  "ImmediateInputFields.select" should "have an option per value, with only the current one selected" in {
    implicit val fsc: FSContext = newFsc()
    val options = find(F.select[String](() => Seq("a", "b", "c"), () => "b", _ => JS.void, elemId = "s"), "option")
    assert(options.map(o => attrsOf(o).get("value")) == Seq(Some("0"), Some("1"), Some("2")))
    assert(options.map(_.text) == Seq("a", "b", "c"))
    assert(options.map(o => attrsOf(o).contains("selected")) == Seq(false, true, false))
  }

  "ImmediateInputFields.textarea" should "contain the value (escaped when written), with the rows" in {
    implicit val fsc: FSContext = newFsc()
    val ta = F.textarea(() => "a <b>", _ => JS.void, rows = 4)
    assert(ta.text.trim == "a <b>")
    assert(attrsOf(ta).get("rows").contains("4"))
    assert(written(ta).contains("a &lt;b&gt;"))
  }

  "ImmediateInputFields.range" should "have min, max, step and value" in {
    implicit val fsc: FSContext = newFsc()
    val attrs = attrsOf(F.range(() => 2.5, _ => JS.void, 0, 10, step = Some(0.5)))
    assert(attrs.get("min").contains("0.0") && attrs.get("max").contains("10.0"))
    assert(attrs.get("step").contains("0.5") && attrs.get("value").contains("2.5"))
    assert(attrs.get("type").contains("range"))
  }
}

object ImmediateInputFieldsSpec {

  /** The previous implementations of [[ImmediateInputFields]], with XML literals, for comparison. */
  object Literal {

    import com.fastscala.js.JsHelper.RichJs
    import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
    import com.fastscala.utils.IdGen

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
      val input = <input class={inputClass}
                         style={inputStyle}
                         type="checkbox"
                         checked={if (get()) "checked" else null}
                         id={inputId}
                         name={name}
                         tabindex={tabindex.map(_ + "").getOrElse(null)}
                         onchange={submit}/>.withAttrs(additionalAttrs *)
      <div class={"form-check form-check-custom form-check-solid " + containerClasses}>
        {input}<label class={labelClass} style={labelStyle} for={inputId}>{label}</label>
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
          _then = onSubmitClientSide(value) & fsc.callback(value, str => set(str.trim))
        )
      }).cmd
      val onkeypress = if (changeOnEnter) s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {${JS.blur(inputId).cmd}}" else null
      val onblur = if (btn.isEmpty) submit else null

      val inputNS = <input id={inputId} type={`type`} value={initialValue} onblur={onblur} onkeypress={onkeypress} class={classes} style={style} autocomplete={autocomplete}
                           placeholder={if (placeholder == "") null else placeholder}
                           name={if (name == "") null else name}/>

      btn.map(btn => {
        <div class="input-group ">
          {inputNS}{btn.addClass("input-group-text").onclick(Js(submit)).btn}
        </div>
      }).getOrElse(inputNS)
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

      <input min={min.toString} max={max.toString} step={step.map(_.toString).getOrElse(null)} id={inputId} type={`type`} value={initialValue.toString} class={classes} style={style}
             name={if (name == "") null else name}
             onchange={submit}/>
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

      <select id={elemId} class={classes} style={style} onchange={submit}>{
        values.zipWithIndex.map({
          case (value, idx) if value == get() => <option value={idx.toString} selected="selected">{toString(value)}</option>
          case (value, idx) => <option value={idx.toString}>{toString(value)}</option>
        })
      }</select>
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

      <textarea
      id={inputId} onblur={submit} class={classes} style={style}
      placeholder={if (placeholder == "") null else placeholder}
      name={if (name == "") null else name}
      rows={rows + ""}>
        {get()}
      </textarea>
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
}
