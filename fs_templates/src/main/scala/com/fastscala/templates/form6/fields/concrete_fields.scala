package com.fastscala.templates.form6.fields

import com.fastscala.core.{FSContext, FSUploadedFile}
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6
import com.fastscala.utils.ElemTransformers.RichElem
import com.fastscala.utils.IdGen
import org.joda.time.{DateTime, LocalDate}
import org.joda.time.format.DateTimeFormat

import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import scala.util.{Failure, Success, Try}
import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq, Unparsed}

class F6RawHtmlField(
                      gen: => NodeSeq
                    ) extends StandardF6Field
  with F6FieldWithDisabled
  with F6FieldWithReadOnly
  with F6FieldWithDependencies
  with F6FieldWithEnabled {
  override def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else <div id={aroundId}>{gen}</div>

  override def fieldsMatching(predicate: PartialFunction[F6Field, Boolean]): List[F6Field] = if (predicate.applyOrElse[F6Field, Boolean](this, _ => false)) List(this) else Nil
}

class F6SurroundWithHtmlField[T <: F6Field](
                                               wrap: Elem => Elem
                                             )(
                                               field: T
                                             )
  extends StandardF6Field
    with F6FieldWithReadOnly
    with F6FieldWithDependencies
    with F6FieldWithDisabled
    with F6FieldWithEnabled {
  override def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else <div id={aroundId}>{wrap(field.render())}</div>


  override def fieldsMatching(predicate: PartialFunction[F6Field, Boolean]): List[F6Field] =
    List(this).filter(_ => predicate.applyOrElse[F6Field, Boolean](this, _ => false)) :::
      List(field).flatMap(_.fieldsMatching(predicate))

  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = field.onEvent(event)
}

class F6VerticalField()(children: F6Field*)
  extends StandardF6Field
    with F6FieldWithEnabled
    with F6FieldWithDependencies
    with F6FieldWithDisabled
    with F6FieldWithReadOnly {

  var currentlyEnabled = enabled()

  override def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    currentlyEnabled = enabled()
    if (!currentlyEnabled) <div style="display:none;" id={aroundId}></div>
    else <div id={aroundId}>{children.map(_.render()).reduceOption[NodeSeq](_ ++ _).getOrElse(NodeSeq.Empty)}</div>
  }

  override def reRender()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = {
    if (enabled() != currentlyEnabled) {
      Js.replace(aroundId, render())
    } else {
      children.map(_.reRender()).reduceOption[Js](_ & _).getOrElse(Js.void)
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F6Field, Boolean]): List[F6Field] =
    List(this).filter(_ => predicate.applyOrElse[F6Field, Boolean](this, _ => false)) :::
      children.toList.flatMap(_.fieldsMatching(predicate))

  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & children.map(_.onEvent(event)).reduceOption(_ & _).getOrElse(Js.void)
}

object F6VerticalField {
  def apply()(children: F6Field*) = new F6VerticalField()(children: _*)
}

class F6HorizontalField()(children: (String, F6Field)*)
  extends StandardF6Field
    with F6FieldWithEnabled
    with F6FieldWithDependencies
    with F6FieldWithDisabled
    with F6FieldWithReadOnly {

  var currentlyEnabled = enabled()

  override def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    currentlyEnabled = enabled()
    if (!currentlyEnabled) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        val contents = children.map({
          case (clas, field) => <div class={clas}>{field.render()}</div>
        }).reduceOption[NodeSeq](_ ++ _).getOrElse(NodeSeq.Empty)
        <div class="row" id={aroundId}>{contents}</div>
      }
    }
  }

  override def reRender()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = {
    if (enabled() != currentlyEnabled) {
      Js.replace(aroundId, render())
    } else {
      children.map(_._2.reRender()).reduceOption[Js](_ & _).getOrElse(Js.void)
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F6Field, Boolean]): List[F6Field] =
    List(this).filter(_ => predicate.applyOrElse[F6Field, Boolean](this, _ => false)) :::
      children.toList.flatMap(_._2.fieldsMatching(predicate))

  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js =
    super.onEvent(event) & children.map(_._2.onEvent(event)).reduceOption(_ & _).getOrElse(Js.void)
}

object F6HorizontalField {
  def apply()(children: (String, F6Field)*) = new F6HorizontalField()(children: _*)
}

trait TextF6FieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[T](field: F6TextField[T])(label: Option[NodeSeq], inputElem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

trait TextareaF6FieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[T](field: F6TextareaField[T])(label: Option[NodeSeq], inputElem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

trait SelectF6FieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[T](field: F6SelectFieldBase[T])(label: Option[Elem], elem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem

  def renderOption[T](field: F6SelectFieldBase[T])(
    selected: Boolean,
    value: String,
    label: NodeSeq
  )(implicit hints: Seq[RenderHint]): Elem
}

//trait MultiSelectFieldRenderer {
//
//  def defaultRequiredFieldLabel: String
//
//  def render[T](field: F6MultiSelectField[T])(label: Option[Elem], elem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
//}
//
trait CheckboxF6FieldRenderer {

  def render(field: F6CheckboxField)(label: Option[Elem], elem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

//class F6CheckboxField(
//                       get: () => Boolean
//                       , set: Boolean => Js
//                       , label: Option[Elem] = None
//                       , name: Option[String] = None
//                       , tabindex: Option[Int] = None
//                       , val disabled: () => Boolean = () => false
//                       , val readOnly: () => Boolean = () => false
//                       , val enabled: () => Boolean = () => true
//                       , val deps: Set[FormField] = Set()
//                     )(implicit renderer: CheckboxFieldRenderer) extends StandardFormField with ValidatableField with StringSerializableField with FocusableFormField {
//
//  var currentValue: Boolean = get()
//
//  override def loadFromString(str: String): Seq[(ValidatableField, NodeSeq)] = {
//    str.toBooleanOption match {
//      case Some(value) =>
//        currentValue = value
//        set(currentValue)
//        Nil
//      case None =>
//        List((this, scala.xml.Text(s"Found not parse as a boolean: '$str'")))
//    }
//  }
//
//  override def saveToString(): Option[String] = Some(currentValue.toString).filter(_ != "")
//
//  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
//    case PerformSave => set(currentValue)
//    case _ => Js.void
//  })
//
//  override def errors(): Seq[(ValidatableField, NodeSeq)] = Nil
//
//  override def focusJs: Js = Js.focus(elemId)
//
//  def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
//    if (!enabled()) <div style="display:none;" id={aroundId}></div>
//    else {
//      withFieldRenderHints { implicit hints =>
//        renderer.render(this)(
//          label.map(lbl => <label for={elemId}>{lbl}</label>),
//          <input
//            type="checkbox"
//            value=""
//            id={elemId}
//            name={name.getOrElse(null)}
//            onchange={fsc.callback(Js.checkboxIsChecked(elemId), value => {
//              currentValue = value.toBoolean
//              form.onEvent(ChangedField(this)) & (if (hints.contains(ShowValidationsHint)) reRender() else Js.void)
//            }).cmd}
//            tabindex={tabindex.map(_ + "").getOrElse(null)}
//            checked={if (currentValue) "checked" else null}></input>,
//          errors().headOption.map(_._2)
//        )
//      }
//    }
//  }
//
//  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
//
//  def withLabel(label: String) = copy(label = Some(<span>{label}</span>))
//
//  def copy(
//            get: () => Boolean = get
//            , set: Boolean => Js = set
//            , label: Option[Elem] = label
//            , name: Option[String] = name
//            , tabindex: Option[Int] = tabindex
//            , disabled: () => Boolean = disabled
//            , readOnly: () => Boolean = readOnly
//            , enabled: () => Boolean = enabled
//            , deps: Set[FormField] = deps
//          ): F6CheckboxField = new F6CheckboxField(
//    get = get
//    , set = set
//    , label = label
//    , name = name
//    , tabindex = tabindex
//    , disabled = disabled
//    , readOnly = readOnly
//    , enabled = enabled
//    , deps = deps
//  )
//}
//
//class F6SelectField[T](
//                        all: () => Seq[T]
//                        , get: () => T
//                        , set: T => Js
//                        , toString: T => String = (v: T) => v.toString
//                        , toId: (T, Int) => String = (v: T, idx: Int) => idx.toString
//                        , val label: Option[NodeSeq] = None
//                        , name: Option[String] = None
//                        , noneSelected: Option[String] = None
//                        , val disabled: () => Boolean = () => false
//                        , val readOnly: () => Boolean = () => false
//                        , val enabled: () => Boolean = () => true
//                        , val deps: Set[FormField] = Set()
//                      )(implicit renderer: SelectFieldRenderer) extends StandardFormField with ValidatableField with StringSerializableField with FocusableFormField {
//
//  var currentlySelectedValue: T = get()
//
//  override def loadFromString(str: String): Seq[(ValidatableField, NodeSeq)] = {
//    all().zipWithIndex.find({
//      case (v, idx) => toId(v, idx) == str
//    }) match {
//      case Some((v, _)) =>
//        println(s"currentlySelectedValue = ${v}")
//        currentlySelectedValue = v
//        set(v)
//        Nil
//      case None =>
//        List((this, scala.xml.Text(s"Not found id: '$str'")))
//    }
//  }
//
//  override def saveToString(): Option[String] = Some(toId(currentlySelectedValue, all().indexOf(currentlySelectedValue))).filter(_ != "0")
//
//  def withLabel(label: String): F6SelectField[T] = copy(label = Some(scala.xml.Text(label)))
//
//  def copy(
//            all: () => Seq[T] = all
//            , get: () => T = get
//            , set: T => Js = set
//            , toString: T => String = toString
//            , label: Option[NodeSeq] = label
//            , name: Option[String] = name
//            , noneSelected: Option[String] = noneSelected
//            , enabled: () => Boolean = enabled
//            , deps: Set[FormField] = deps
//          )(
//            implicit renderer: SelectFieldRenderer
//          ): F6SelectField[T] = new F6SelectField[T](
//    all = all
//    , get = get
//    , set = set
//    , toString = toString
//    , label = label
//    , name = name
//    , noneSelected = noneSelected
//    , enabled = enabled
//    , deps = deps
//  )
//
//  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
//    case PerformSave => set(currentlySelectedValue)
//    case _ => Js.void
//  })
//
//  override def errors(): Seq[(ValidatableField, NodeSeq)] = Nil
//
//  override def focusJs: Js = Js.focus(elemId)
//
//  def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
//    val options = all()
//    val ids2Option: Map[String, T] = options.map(opt => fsc.session.nextID() -> opt).toMap
//    val option2Id: Map[T, String] = ids2Option.map(kv => kv._2 -> kv._1)
//    val optionsRendered = all().map(opt => {
//      <option selected={if (currentlySelectedValue == opt) "true" else null} value={option2Id(opt)}>{toString(opt)}</option>
//    })
//
//    val errorsAtRenderTime = errors()
//
//    if (!enabled()) <div style="display:none;" id={aroundId}></div>
//    else {
//      withFieldRenderHints { implicit hints =>
//        val onchangeJs = fsc.callback(Js.elementValueById(elemId), {
//          case id =>
//            currentlySelectedValue = ids2Option(id)
//            form.onEvent(ChangedField(this)) &
//              (if (hints.contains(ShowValidationsHint) || errorsAtRenderTime.nonEmpty || errors().nonEmpty) reRender() else Js.void)
//        }).cmd
//        renderer.render(this)(
//          label.map(label => <label for={elemId}>{label}</label>),
//          <select
//            name={name.getOrElse(null)}
//            onblur={onchangeJs}
//            onchange={onchangeJs}
//            id={elemId}
//          >{optionsRendered}</select>,
//          errorsAtRenderTime.headOption.map(_._2)
//        )
//      }
//    }
//  }
//
//  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
//}
//
//class F6MultiSelectField[T](
//                             all: () => Seq[T]
//                             , get: () => Set[T]
//                             , set: Set[T] => Js
//                             , toString: T => String = (v: T) => v.toString
//                             , toId: (T, Int) => String = (v: T, idx: Int) => idx.toString
//                             , val label: Option[NodeSeq] = None
//                             , name: Option[String] = None
//                             , noneSelected: Option[String] = None
//                             , val disabled: () => Boolean = () => false
//                             , val readOnly: () => Boolean = () => false
//                             , val enabled: () => Boolean = () => true
//                             , val deps: Set[FormField] = Set()
//                             , val size: Option[Int] = None
//                           )(implicit renderer: MultiSelectFieldRenderer) extends StandardFormField with ValidatableField with StringSerializableField {
//
//  var currentlySelectedValue: Set[T] = get()
//
//  override def loadFromString(str: String): Seq[(ValidatableField, NodeSeq)] = {
//    val selected: Set[String] = str.split(";").toSet
//
//    currentlySelectedValue = all().zipWithIndex.collect({
//      case (v, idx) if selected.contains(toId(v, idx)) => v
//    }).toSet
//    set(currentlySelectedValue)
//    Nil
//  }
//
//  override def saveToString(): Option[String] = Some(currentlySelectedValue.map(v => {
//    toId(v, all().indexOf(currentlySelectedValue))
//  }).mkString(";"))
//
//  def withLabel(label: String): F6MultiSelectField[T] = copy(label = Some(scala.xml.Text(label)))
//
//  def copy(
//            all: () => Seq[T] = all
//            , get: () => Set[T] = get
//            , set: Set[T] => Js = set
//            , toString: T => String = toString
//            , label: Option[NodeSeq] = label
//            , name: Option[String] = name
//            , noneSelected: Option[String] = noneSelected
//            , enabled: () => Boolean = enabled
//            , deps: Set[FormField] = deps
//            , size: Option[Int] = size
//          )(
//            implicit renderer: MultiSelectFieldRenderer
//          ): F6MultiSelectField[T] = new F6MultiSelectField[T](
//    all = all
//    , get = get
//    , set = set
//    , toString = toString
//    , label = label
//    , name = name
//    , noneSelected = noneSelected
//    , enabled = enabled
//    , deps = deps
//    , size = size
//  )
//
//  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
//    case PerformSave => set(currentlySelectedValue)
//    case _ => Js.void
//  })
//
//  override def errors(): Seq[(ValidatableField, NodeSeq)] = Nil
//
//  def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
//    val options = all()
//    val ids2Option: Map[String, T] = options.map(opt => fsc.session.nextID() -> opt).toMap
//    val option2Id: Map[T, String] = ids2Option.map(kv => kv._2 -> kv._1)
//    val optionsRendered = all().map(opt => {
//      <option selected={if (currentlySelectedValue.contains(opt)) "true" else null} value={option2Id(opt)}>{toString(opt)}</option>
//    })
//
//    if (!enabled()) <div style="display:none;" id={aroundId}></div>
//    else {
//      withFieldRenderHints { implicit hints =>
//        val onchangeJs = fsc.callback(Js.selectedValues(Js.elementById(elemId)), {
//          case ids =>
//            currentlySelectedValue = ids.split(",").toSet.map(id => ids2Option(id))
//            form.onEvent(ChangedField(this)) & (if (hints.contains(ShowValidationsHint)) reRender() else Js.void)
//        }).cmd
//        renderer.render(this)(
//          label.map(label => <label for={elemId}>{label}</label>),
//          <select
//            multiple="multiple"
//            name={name.getOrElse(null)}
//            onblur={onchangeJs}
//            onchange={onchangeJs}
//            id={elemId}
//            size={size.map(_ + "").getOrElse(null)}
//          >{optionsRendered}</select>,
//          errors().headOption.map(_._2)
//        )
//      }
//    }
//  }
//
//  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
//}
//
//object EnumField {
//
//  def NonNullable[T <: Enumeration](
//                                     enum: T
//                                     , get: () => T#Value
//                                     , set: T#Value => Js
//                                     , toString: T#Value => String = (v: T#Value) => v.toString
//                                     , label: Option[NodeSeq] = None
//                                     , name: Option[String] = None
//                                     , enabled: () => Boolean = () => true
//                                     , deps: Set[FormField] = Set()
//                                   )(implicit renderer: SelectFieldRenderer) = new F6SelectField[T#Value](
//    all = () => `enum`.values.toList,
//    get = get,
//    set = set,
//    toString = toString,
//    label = label,
//    name = name,
//    enabled = enabled,
//    deps = deps
//  )
//
//  def Multi[T <: Enumeration](
//                               enum: T
//                               , get: () => Set[T#Value]
//                               , set: Set[T#Value] => Js
//                               , toString: T#Value => String = (v: T#Value) => v.toString
//                               , label: Option[NodeSeq] = None
//                               , name: Option[String] = None
//                               , enabled: () => Boolean = () => true
//                               , deps: Set[FormField] = Set()
//                               , size: Option[Int] = None
//                             )(implicit renderer: MultiSelectFieldRenderer) = new F6MultiSelectField[T#Value](
//    all = () => `enum`.values.toList,
//    get = get,
//    set = set,
//    toString = toString,
//    label = label,
//    name = name,
//    enabled = enabled,
//    deps = deps,
//    size = size
//  )
//
//  def Nullable[T <: Enumeration](
//                                  enum: T
//                                  , get: () => Option[T#Value]
//                                  , set: Option[T#Value] => Js
//                                  , toString: Option[T#Value] => String = (v: Option[T#Value]) => v.map(_.toString).getOrElse("--")
//                                  , label: Option[NodeSeq] = None
//                                  , name: Option[String] = None
//                                  , required: () => Boolean = () => false
//                                  , enabled: () => Boolean = () => true
//                                  , deps: Set[FormField] = Set()
//                                )(implicit renderer: SelectFieldRenderer) = new F6SelectField[Option[T#Value]](
//    all = () => None :: `enum`.values.toList.map(Some(_)),
//    get = get,
//    set = set,
//    toString = toString,
//    label = label,
//    name = name,
//    enabled = enabled,
//    deps = deps
//  ) {
//    override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
//      (if (required() && currentlySelectedValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
//  }
//}
//
//object F6CodeField {
//
//  def cssImports: NodeSeq = {
//    <link href={"https://cdn.jsdelivr.net/npm/ace-builds@1.31.1/css/ace.min.css"} rel="stylesheet"></link>
//  }
//
//  def jsImports: NodeSeq = {
//    <script src={"https://cdn.jsdelivr.net/npm/ace-builds@1.31.1/src-min-noconflict/ace.min.js"}></script>
//    <script src={"https://cdn.jsdelivr.net/npm/ace-builds@1.31.1/src-min-noconflict/mode-html.js"}></script>
//    <script src={"https://cdn.jsdelivr.net/npm/ace-builds@1.31.1/src-min-noconflict/theme-textmate.js"}></script>
//  }
//}
//
//class F6CodeField(
//                   get: () => String
//                   , set: String => Js
//                   , label: Option[NodeSeq] = None
//                   , name: Option[String] = None
//                   , placeholder: Option[String] = None
//                   , tabindex: Option[Int] = None
//                   , maxlength: Option[Int] = None
//                   , required: () => Boolean = () => false
//                   , val disabled: () => Boolean = () => false
//                   , val readOnly: () => Boolean = () => false
//                   , val enabled: () => Boolean = () => true
//                   , val deps: Set[FormField] = Set(),
//                   saveEveryMillis: Int = 1000,
//                   height: String = "800px"
//                 )(
//                   implicit renderer: TextareaFieldRenderer
//                 ) extends StandardFormField with ValidatableField with StringSerializableField {
//
//  lazy val editorId = "__editor" + IdGen.id
//  lazy val timeoutId = editorId + "_TO"
//  lazy val savedId = editorId + "_saved"
//  lazy val savingId = editorId + "_saving"
//
//  var currentValue = get()
//
//  override def reRender()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = {
//    if (!hints.contains(OnSaveRerender)) super.reRender()
//    else Js.void
//  }
//
//  override def loadFromString(str: String): Seq[(ValidatableField, NodeSeq)] = {
//    currentValue = str
//    set(currentValue)
//    Nil
//  }
//
//  override def saveToString(): Option[String] = Some(currentValue).filter(_ != "")
//
//  def withLabel(label: String) = copy(label = Some(scala.xml.Text(label)))
//
//  def copy(
//            get: () => String = get
//            , set: String => Js = set
//            , label: Option[NodeSeq] = label
//            , name: Option[String] = name
//            , placeholder: Option[String] = placeholder
//            , tabindex: Option[Int] = tabindex
//            , maxlength: Option[Int] = maxlength
//            , required: () => Boolean = required
//            , enabled: () => Boolean = enabled
//            , deps: Set[FormField] = deps
//            , saveEveryMillis: Int = saveEveryMillis
//            , height: String = height
//          ): F6CodeField = {
//    new F6CodeField(
//      get = get
//      , set = set
//      , label = label
//      , name = name
//      , placeholder = placeholder
//      , tabindex = tabindex
//      , maxlength = maxlength
//      , required = required
//      , enabled = enabled
//      , deps = deps
//      , saveEveryMillis = saveEveryMillis
//      , height = height
//    )
//  }
//
//  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
//    case PerformSave => set(currentValue)
//    case _ => Js.void
//  })
//
//  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
//    (if (required() && currentValue.trim == "") Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
//
//  override def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
//    if (!enabled()) <div style="display:none;" id={aroundId}></div>
//    else {
//      withFieldRenderHints { implicit hints =>
//
//        val onChangeCallbackJS = fsc.callback(Js(s"window.$editorId.session.getValue()"), value => {
//          currentValue = value
//          form.onEvent(ChangedField(this)) & (if (hints.contains(ShowValidationsHint)) reRender() else Js.void)
//        })
//
//        val onChangeJS =
//          s"""function() {
//             |  $$('#$savedId').hide();
//             |  $$('#$savingId').show();
//             |  clearTimeout(window.$timeoutId);
//             |  window.$timeoutId = setTimeout(
//             |    function() {${onChangeCallbackJS.cmd}; $$('#$savedId').show(); $$('#$savingId').hide();},
//             |    $saveEveryMillis
//             |  );
//             |}
//             |""".stripMargin
//
//        <div id={aroundId}>
//          <p id={savedId} style="font-weight: bold; color: green;">Saved</p>
//          <p id={savingId} style="font-weight: bold; color: #999; display: none;">Saving...</p>
//          {
//          Unparsed(
//            s"""<style type="text/css" media="screen">
//               |  #${editorId} { width: 100%; height: 100%; }
//               |</style>
//               |""".stripMargin
//          )
//          }
//          <div style={s"width:100%;height: $height;display:inline-block;position:relative;"}>
//            <div id={editorId}>{currentValue}</div>
//          </div>
//
//          {
//            Unparsed(
//              s"""<script>window.$editorId = ace.edit(${Js.asJsStr(editorId).cmd});
//                 |window.$editorId.setTheme("ace/theme/textmate");
//                 |window.$editorId.session.setMode("ace/mode/html");
//                 |window.$editorId.session.on('change', ${onChangeJS});</script>""".stripMargin
//            )
//          }
//        </div>
//      }
//    }
//  }
//
//  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
//}
//
trait ButtonF6FieldRenderer {
  def render(field: F6SaveButtonField[_])(btn: Elem)(implicit hints: Seq[RenderHint]): Elem
}

class F6SaveButtonField[B <% Elem](
                                    btn: FSContext => B
                                    , val toInitialState: B => B = identity[B] _
                                    , val toChangedState: B => B = identity[B] _
                                    , val toErrorState: B => B = identity[B] _
                                  )(implicit renderer: ButtonF6FieldRenderer)
  extends StandardF6Field
    with F6FieldWithReadOnly
    with F6FieldWithDependencies
    with F6FieldWithDisabled
    with F6FieldWithEnabled {

  override def fieldsMatching(predicate: PartialFunction[F6Field, Boolean]): List[F6Field] = if (predicate.applyOrElse[F6Field, Boolean](this, _ => false)) List(this) else Nil

  val btnRenderer = Js.rerenderableP[(B => B, Form6)](_ => implicit fsc => {
    case (transformer, form) => (transformer(btn(fsc)): Elem).withId(elemId).addOnClick((Js.focus(elemId) & form.onSaveClientSide()).cmd)
  })

  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case AfterSave =>
      //btnRenderer.rerender((toInitialState, form)).printToConsoleBefore()
      Js.void
    case BeforeSave => Js.void
    case ErrorsOnSave =>
      //btnRenderer.rerender((toErrorState, form)).printToConsoleBefore()
      Js.void
    case ChangedField(_) =>
      //btnRenderer.rerender((toChangedState, form)).printToConsoleBefore()
      Js.void
    case PerformSave => Js.void
    case _ => Js.void
  })

  override def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        renderer.render(this)({
          if (hints.contains(FailedSaveStateHint)) btnRenderer.render((toErrorState, form))
          else btnRenderer.render((toInitialState, form))
        })
      }
    }
}
//
//trait FileUploadFieldRenderer {
//
//  def transformFormElem(field: F6FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem
//
//  def transforLabelElem(field: F6FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem
//
//  def transforSubmitButtonElem(field: F6FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem
//
//  def transforResetButtonElem(field: F6FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem
//
//  def transforFileInputElem(field: F6FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem
//}
//
//class F6FileUploadField(
//                         get: () => Option[(String, Array[Byte])]
//                         , set: Option[(String, Array[Byte])] => Js
//                         , submitBtn: Elem
//                         , renderPreview: FSContext => Option[(String, Array[Byte])] => Elem = _ => _ => <div></div>
//                         , resetBtn: Option[Elem] = None
//                         , label: Option[NodeSeq] = None
//                         , name: Option[String] = None
//                         , tabindex: Option[Int] = None
//
//                         , val disabled: () => Boolean = () => false
//                         , val readOnly: () => Boolean = () => false
//                         , val enabled: () => Boolean = () => true
//                         , val deps: Set[FormField] = Set()
//
//                         , val transformFormElem: Elem => Elem = identity[Elem]
//                         , val transforLabelElem: Elem => Elem = identity[Elem]
//                         , val transforSubmitButtonElem: Elem => Elem = identity[Elem]
//                         , val transforResetButtonElem: Elem => Elem = identity[Elem]
//                         , val transforFileInputElem: Elem => Elem = identity[Elem]
//                       )(implicit renderer: FileUploadFieldRenderer) extends StandardFormField with ValidatableField {
//
//  var currentValue: Option[(String, Array[Byte])] = get()
//
//  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
//    case PerformSave => set(currentValue)
//    case _ => Js.void
//  })
//
//  override def errors(): Seq[(ValidatableField, NodeSeq)] = Nil
//
//  def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
//    if (!enabled()) <div style="display:none;" id={aroundId}></div>
//    else {
//      withFieldRenderHints { implicit hints =>
//        val targetId = IdGen.id("targetFrame")
//        val inputId = IdGen.id("input")
//        val buttonId = IdGen.id("btn")
//        val resetButtonId = IdGen.id("reset-btn")
//
//        val previewRenderer = Js.rerenderable(rerenderer => implicit fsc => renderPreview(fsc)(currentValue))
//        val actionUrl = fsc.fileUploadActionUrl({
//          case Seq(uploadedFile: FSUploadedFile, _ *) =>
//            currentValue = Some((uploadedFile.submittedFileName, uploadedFile.content))
//            previewRenderer.rerender() &
//              form.onEvent(ChangedField(this)) &
//              (if (hints.contains(ShowValidationsHint)) reRender() else Js.void) &
//              Js.show(resetButtonId)
//        })
//        <form target={targetId} action={actionUrl} method="post" encoding="multipart/form-data" enctype="multipart/form-data" id={aroundId}>
//          <iframe id={targetId} name={targetId} src="about:blank" onload="eval(this.contentWindow.document.body.innerText)" style="width:0;height:0;border:0px solid #fff;"><html><body></body></html></iframe>
//          {
//          label.map(label => <label for={elemId}>{label}</label>).map(_.pipe(renderer.transforLabelElem(this)).pipe(transforLabelElem)).getOrElse(NodeSeq.Empty)
//          }
//          {
//          previewRenderer.render()
//          }
//          {
//            <input name="file" type="file" id={inputId} onchange={Js.show(buttonId).cmd}/>.pipe(renderer.transforFileInputElem(this)).pipe(transforFileInputElem)
//          }
//          {
//          submitBtn.pipe(renderer.transforSubmitButtonElem(this)).pipe(transforSubmitButtonElem).withId(buttonId).withStyle("display:none").withTypeSubmit()
//          }
//          {
//          resetBtn.map(renderer.transforResetButtonElem(this)).map(transforResetButtonElem).map(_.withId(resetButtonId).addOnClick(fsc.callback(() => {
//            currentValue = None
//            previewRenderer.rerender() &
//              form.onEvent(ChangedField(this)) &
//              (if (hints.contains(ShowValidationsHint)) reRender() else Js.void) &
//              Js.hide(resetButtonId)
//          }).cmd).withAttr("style")(cur => if (currentValue.isDefined) cur.getOrElse("") else cur.getOrElse("") + ";display:none;")).getOrElse(NodeSeq.Empty)
//          }
//        </form>.pipe(renderer.transformFormElem(this)).pipe(transformFormElem)
//      }
//    }
//  }
//
//  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
//
//  def withLabel(label: String) = copy(label = Some(<span>{label}</span>))
//
//  def copy(
//            get: () => Option[(String, Array[Byte])] = get
//            , set: Option[(String, Array[Byte])] => Js = set
//            , submitBtn: Elem = submitBtn
//            , renderPreview: FSContext => Option[(String, Array[Byte])] => Elem = renderPreview
//            , resetBtn: Option[Elem] = resetBtn
//            , label: Option[NodeSeq] = label
//            , name: Option[String] = name
//            , tabindex: Option[Int] = tabindex
//            , disabled: () => Boolean = disabled
//            , readOnly: () => Boolean = readOnly
//            , enabled: () => Boolean = enabled
//            , deps: Set[FormField] = deps
//            , transformFormElem: Elem => Elem = transformFormElem
//            , transforLabelElem: Elem => Elem = transforLabelElem
//            , transforSubmitButtonElem: Elem => Elem = transforSubmitButtonElem
//            , transforResetButtonElem: Elem => Elem = transforResetButtonElem
//            , transforFileInputElem: Elem => Elem = transforFileInputElem
//          ): F6FileUploadField = new F6FileUploadField(
//    get = get
//    , set = set
//    , submitBtn = submitBtn
//    , renderPreview = renderPreview
//    , resetBtn = resetBtn
//    , label = label
//    , name = name
//    , tabindex = tabindex
//    , disabled = disabled
//    , readOnly = readOnly
//    , enabled = enabled
//    , deps = deps
//    , transformFormElem = transformFormElem
//    , transforLabelElem = transforLabelElem
//    , transforSubmitButtonElem = transforSubmitButtonElem
//    , transforResetButtonElem = transforResetButtonElem
//    , transforFileInputElem = transforFileInputElem
//  )
//}
