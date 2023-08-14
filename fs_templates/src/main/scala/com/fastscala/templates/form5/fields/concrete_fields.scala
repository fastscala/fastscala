package com.fastscala.templates.form5.fields

import com.fastscala.core.{FSContext, FSUploadedFile}
import com.fastscala.js.Js
import com.fastscala.templates.form5.Form5
import com.fastscala.utils.ElemTransformers.RichElem
import com.fastscala.utils.IdGen
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

import java.util.regex.Pattern
import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq, Unparsed}

class RawHtmlField(
                    gen: => NodeSeq
                    , val enabled: () => Boolean = () => true
                    , val deps: Set[FormField] = Set()
                    , val disabled: () => Boolean = () => false
                    , val readOnly: () => Boolean = () => false
                  ) extends StandardFormField {
  override def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else <div id={aroundId}>{gen}</div>

  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
}

class SurroundWithHtmlField[T <: FormField](
                                             wrap: Elem => Elem
                                           )(
                                             field: T
                                             , val enabled: () => Boolean = () => true
                                             , val deps: Set[FormField] = Set()
                                             , val disabled: () => Boolean = () => false
                                             , val readOnly: () => Boolean = () => false
                                           ) extends StandardFormField {
  override def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else <div id={aroundId}>{wrap(field.render())}</div>


  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] =
    List(this).filter(_ => predicate.applyOrElse[FormField, Boolean](this, _ => false)) :::
      List(field).flatMap(_.fieldsMatching(predicate))

  override def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = field.onEvent(event)
}

class VerticalField(
                     val enabled: () => Boolean = () => true
                     , val deps: Set[FormField] = Set()
                     , val disabled: () => Boolean = () => false
                     , val readOnly: () => Boolean = () => false
                   )(children: FormField*) extends StandardFormField {

  var currentlyEnabled = enabled()

  override def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    currentlyEnabled = enabled()
    if (!currentlyEnabled) <div style="display:none;" id={aroundId}></div>
    else <div id={aroundId}>{children.map(_.render()).reduceOption[NodeSeq](_ ++ _).getOrElse(NodeSeq.Empty)}</div>
  }

  override def reRender()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = {
    if (enabled() != currentlyEnabled) {
      Js.replace(aroundId, render())
    } else {
      children.map(_.reRender()).reduceOption[Js](_ & _).getOrElse(Js.void)
    }
  }

  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] =
    List(this).filter(_ => predicate.applyOrElse[FormField, Boolean](this, _ => false)) :::
      children.toList.flatMap(_.fieldsMatching(predicate))

  override def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & children.map(_.onEvent(event)).reduceOption(_ & _).getOrElse(Js.void)
}

object VerticalField {
  def apply(
             enabled: () => Boolean = () => true
             , deps: Set[FormField] = Set()
           )(children: FormField*) = new VerticalField(enabled, deps)(children: _*)
}

class HorizontalField(
                       val enabled: () => Boolean = () => true
                       , val deps: Set[FormField] = Set()
                       , val disabled: () => Boolean = () => false
                       , val readOnly: () => Boolean = () => false
                     )(children: (String, FormField)*) extends StandardFormField {

  var currentlyEnabled = enabled()

  override def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
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

  override def reRender()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = {
    if (enabled() != currentlyEnabled) {
      Js.replace(aroundId, render())
    } else {
      children.map(_._2.reRender()).reduceOption[Js](_ & _).getOrElse(Js.void)
    }
  }

  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] =
    List(this).filter(_ => predicate.applyOrElse[FormField, Boolean](this, _ => false)) :::
      children.toList.flatMap(_._2.fieldsMatching(predicate))

  override def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js =
    super.onEvent(event) & children.map(_._2.onEvent(event)).reduceOption(_ & _).getOrElse(Js.void)
}

object HorizontalField {
  def apply(
             enabled: () => Boolean = () => true
             , deps: Set[FormField] = Set()
           )(children: (String, FormField)*) = new HorizontalField(enabled, deps)(children: _*)
}

trait TextFieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[T](field: TextField[T])(label: Option[NodeSeq], inputElem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

trait TextareaFieldRenderer {

  def defaultRequiredFieldLabel: String

  def render(field: TextAreaField)(label: Option[NodeSeq], inputElem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

trait SelectFieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[T](field: SelectField[T])(label: Option[Elem], elem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

trait MultiSelectFieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[T](field: MultiSelectField[T])(label: Option[Elem], elem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

trait CheckboxFieldRenderer {

  def render(field: CheckboxField)(label: Option[Elem], elem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

abstract class TextField[T](
                             getOpt: () => Option[T]
                             , setOpt: Option[T] => Js
                             , toString: Option[T] => String
                             , fromString: String => Either[String, Option[T]]
                             , label: Option[NodeSeq] = None
                             , name: Option[String] = None
                             , placeholder: Option[String] = None
                             , tabindex: Option[Int] = None
                             , maxlength: Option[Int] = None
                             , required: () => Boolean = () => false
                             , inputType: String = "text"
                             , val disabled: () => Boolean = () => false
                             , val readOnly: () => Boolean = () => false
                             , val enabled: () => Boolean = () => true
                             , val deps: Set[FormField] = Set()
                           )(implicit renderer: TextFieldRenderer) extends StandardFormField with ValidatableField with StringSerializableField with FocusableFormField {

  var currentValue: Option[T] = getOpt()

  override def loadFromString(str: String): Seq[(ValidatableField, NodeSeq)] = {
    fromString(str) match {
      case Right(value) =>
        currentValue = value
        setOpt(currentValue)
        Nil
      case Left(error) =>
        List((this, scala.xml.Text(s"Could not parse value '$str': $error")))
    }
  }

  override def saveToString(): Option[String] = Some(toString(currentValue)).filter(_ != "")

  override def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => setOpt(currentValue)
    case _ => Js.void
  })

  def additionalAttrs(): Seq[(String, String)] = Nil

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        renderer.render(this)(
          label.map(lbl => <label for={elemId}>{lbl}</label>),
            <input type={inputType}
                   name={name.getOrElse(null)}
                   class="form-control"
                   id={elemId}
                   onblur={fsc.callback(Js.elementValueById(elemId), str => {
                     fromString(str).foreach(currentValue = _)
                     form.onEvent(ChangedField(this)) &
                       Js.evalIf(hints.contains(ShowValidationsHint))(reRender()) // TODO: is this wrong? (running on the client side, but should be server?)
                   }).cmd}
                   onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {${Js.blur(elemId) & Js.evalIf(hints.contains(SaveOnEnterHint))(form.onSaveClientSide())}}"}
                   placeholder={placeholder.getOrElse(null)}
                   value={toString(currentValue)}
                   tabindex={tabindex.map(_ + "").getOrElse(null)}
                   maxlength={maxlength.map(_ + "").getOrElse(null)}
                   required={if (required()) "true" else null}/>.withAttrs(additionalAttrs(): _*),
          errors().headOption.map(_._2)
        )
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
}

class StringField(
                   get: () => String
                   , set: String => Js
                   , label: Option[NodeSeq] = None
                   , name: Option[String] = None
                   , placeholder: Option[String] = None
                   , tabindex: Option[Int] = None
                   , maxlength: Option[Int] = None
                   , required: () => Boolean = () => false
                   , inputType: String = "text"
                   , disabled: () => Boolean = () => false
                   , readOnly: () => Boolean = () => false
                   , enabled: () => Boolean = () => true
                   , deps: Set[FormField] = Set()
                 )(implicit renderer: TextFieldRenderer) extends TextField[String](
  getOpt = () => Some(get())
  , setOpt = strOpt => set(strOpt.getOrElse(""))
  , toString = _.getOrElse("")
  , fromString = str => Right(Some(str))
  , label = label
  , name = name
  , placeholder = placeholder
  , tabindex = tabindex
  , maxlength = maxlength
  , required = required
  , inputType = inputType
  , disabled = disabled
  , readOnly = readOnly
  , enabled = enabled
  , deps = deps
) {

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.getOrElse("").trim == "") Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())

  def withLabel(label: String) = copy(label = Some(<span>{label}</span>))

  def copy(
            get: () => String = get
            , set: String => Js = set
            , label: Option[NodeSeq] = label
            , name: Option[String] = name
            , placeholder: Option[String] = placeholder
            , tabindex: Option[Int] = tabindex
            , maxlength: Option[Int] = maxlength
            , required: () => Boolean = required
            , inputType: String = inputType
            , disabled: () => Boolean = disabled
            , readOnly: () => Boolean = readOnly
            , enabled: () => Boolean = enabled
            , deps: Set[FormField] = deps
          ): StringField = new StringField(
    get = get
    , set = set
    , label = label
    , name = name
    , placeholder = placeholder
    , tabindex = tabindex
    , maxlength = maxlength
    , required = required
    , inputType = inputType
    , disabled = disabled
    , readOnly = readOnly
    , enabled = enabled
    , deps = deps
  )
}

class DateOptField(
                    get: () => Option[LocalDate]
                    , set: Option[LocalDate] => Js
                    , label: Option[NodeSeq] = None
                    , name: Option[String] = None
                    , placeholder: Option[String] = None
                    , tabindex: Option[Int] = None
                    , maxlength: Option[Int] = None
                    , required: () => Boolean = () => false
                    , inputType: String = "date"
                    , disabled: () => Boolean = () => false
                    , readOnly: () => Boolean = () => false
                    , enabled: () => Boolean = () => true
                    , deps: Set[FormField] = Set()
                  )(implicit renderer: TextFieldRenderer) extends TextField[LocalDate](
  getOpt = () => get()
  , setOpt = optValue => set(optValue)
  , toString = _.map(_.toString("YYYY-MM-dd")).getOrElse("")
  , fromString = str => Right(Some(str).filter(_.trim != "").map(str => LocalDate.parse(str, DateTimeFormat.forPattern("YYYY-MM-dd"))))
  , label = label
  , name = name
  , placeholder = placeholder
  , tabindex = tabindex
  , maxlength = maxlength
  , required = required
  , inputType = inputType
  , disabled = disabled
  , readOnly = readOnly
  , enabled = enabled
  , deps = deps
) {

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())

  def withLabel(label: String) = copy(label = Some(<span>{label}</span>))

  def copy(
            get: () => Option[LocalDate] = get
            , set: Option[LocalDate] => Js = set
            , label: Option[NodeSeq] = label
            , name: Option[String] = name
            , placeholder: Option[String] = placeholder
            , tabindex: Option[Int] = tabindex
            , maxlength: Option[Int] = maxlength
            , required: () => Boolean = required
            , inputType: String = inputType
            , disabled: () => Boolean = disabled
            , readOnly: () => Boolean = readOnly
            , enabled: () => Boolean = enabled
            , deps: Set[FormField] = deps
          ): DateOptField = new DateOptField(
    get = get
    , set = set
    , label = label
    , name = name
    , placeholder = placeholder
    , tabindex = tabindex
    , maxlength = maxlength
    , required = required
    , inputType = inputType
    , disabled = disabled
    , readOnly = readOnly
    , enabled = enabled
    , deps = deps
  )
}

class DoubleOptField(
                      get: () => Option[Double]
                      , set: Option[Double] => Js
                      , label: Option[NodeSeq] = None
                      , name: Option[String] = None
                      , placeholder: Option[String] = None
                      , tabindex: Option[Int] = None
                      , maxlength: Option[Int] = None
                      , min: Option[Double] = None
                      , step: Option[Double] = None
                      , max: Option[Double] = None
                      , required: () => Boolean = () => false
                      , disabled: () => Boolean = () => false
                      , readOnly: () => Boolean = () => false
                      , enabled: () => Boolean = () => true
                      , deps: Set[FormField] = Set()
                      , prefix: String = ""
                      , suffix: String = ""
                    )(implicit renderer: TextFieldRenderer) extends TextField[Double](
  getOpt = () => get()
  , setOpt = doubleOpt => set(doubleOpt)
  , toString = _.map(value => prefix + " " + value.formatted("%.2f") + " " + suffix).map(_.trim).getOrElse("")
  , fromString = str => {
    if (str.trim == "") {
      Right(None)
    } else {
      str
        .toLowerCase
        .trim
        .replaceAll("^" + Pattern.quote(prefix.toLowerCase) + " *", "")
        .replaceAll(" *" + Pattern.quote(suffix.toLowerCase) + "$", "")
        .toDoubleOption match {
        case Some(value) => Right(Some(value))
        case None => Left(s"Not a double?: $str")
      }
    }
  }
  , label = label
  , name = name
  , placeholder = placeholder
  , tabindex = tabindex
  , maxlength = maxlength
  , required = required
  , inputType = "text"
  , disabled = disabled
  , readOnly = readOnly
  , enabled = enabled
  , deps = deps
) {

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())

  override def additionalAttrs(): Seq[(String, String)] = super.additionalAttrs() ++ List(
    "min" -> min.map(_.toString).getOrElse(null),
    "step" -> step.map(_.toString).getOrElse(null),
    "max" -> max.map(_.toString).getOrElse(null)
  )

  def withLabel(label: String) = copy(label = Some(scala.xml.Text(label)))

  def copy(
            get: () => Option[Double] = get
            , set: Option[Double] => Js = set
            , label: Option[NodeSeq] = label
            , name: Option[String] = name
            , placeholder: Option[String] = placeholder
            , tabindex: Option[Int] = tabindex
            , maxlength: Option[Int] = maxlength
            , required: () => Boolean = required
            , disabled: () => Boolean = disabled
            , readOnly: () => Boolean = readOnly
            , enabled: () => Boolean = enabled
            , deps: Set[FormField] = deps
            , prefix: String = prefix
            , suffix: String = suffix
          ) = new DoubleOptField(
    get = get
    , set = set
    , label = label
    , name = name
    , placeholder = placeholder
    , tabindex = tabindex
    , maxlength = maxlength
    , required = required
    , disabled = disabled
    , readOnly = readOnly
    , enabled = enabled
    , deps = deps
    , prefix = prefix
    , suffix = suffix
  )
}

class DoubleField(
                   get: () => Double
                   , set: Double => Js
                   , label: Option[NodeSeq] = None
                   , name: Option[String] = None
                   , placeholder: Option[String] = None
                   , tabindex: Option[Int] = None
                   , maxlength: Option[Int] = None
                   , min: Option[Double] = None
                   , step: Option[Double] = None
                   , max: Option[Double] = None
                   , required: () => Boolean = () => false
                   , disabled: () => Boolean = () => false
                   , readOnly: () => Boolean = () => false
                   , enabled: () => Boolean = () => true
                   , deps: Set[FormField] = Set()
                   , prefix: String = ""
                   , suffix: String = ""
                 )(implicit renderer: TextFieldRenderer) extends TextField[Double](
  getOpt = () => Some(get())
  , setOpt = doubleOpt => doubleOpt.map(double => set(double)).getOrElse(Js.void)
  , toString = _.map(value => prefix + " " + value.formatted("%.2f") + " " + suffix).map(_.trim).getOrElse("")
  , fromString = str => {
    str
      .toLowerCase
      .trim
      .replaceAll("^" + Pattern.quote(prefix.toLowerCase) + " *", "")
      .replaceAll(" *" + Pattern.quote(suffix.toLowerCase) + "$", "")
      .toDoubleOption match {
      case Some(value) => Right(Some(value))
      case None => Left(s"Not a double?: $str")
    }
  }
  , label = label
  , name = name
  , placeholder = placeholder
  , tabindex = tabindex
  , maxlength = maxlength
  , required = required
  , inputType = "number"
  , disabled = disabled
  , readOnly = readOnly
  , enabled = enabled
  , deps = deps
) {

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())

  def withLabel(label: String) = copy(label = Some(scala.xml.Text(label)))

  override def additionalAttrs(): Seq[(String, String)] = super.additionalAttrs() ++ Seq(
    min.map(min => "min" -> min.toString)
    , step.map(step => "step" -> step.toString)
    , max.map(max => "max" -> max.toString)
  ).flatten

  def copy(
            get: () => Double = get
            , set: Double => Js = set
            , label: Option[NodeSeq] = label
            , name: Option[String] = name
            , placeholder: Option[String] = placeholder
            , tabindex: Option[Int] = tabindex
            , maxlength: Option[Int] = maxlength
            , min: Option[Double] = min
            , step: Option[Double] = step
            , max: Option[Double] = max
            , required: () => Boolean = required
            , disabled: () => Boolean = disabled
            , readOnly: () => Boolean = readOnly
            , enabled: () => Boolean = enabled
            , deps: Set[FormField] = deps
            , prefix: String = prefix
            , suffix: String = suffix
          ) = new DoubleField(
    get = get
    , set = set
    , label = label
    , name = name
    , placeholder = placeholder
    , tabindex = tabindex
    , maxlength = maxlength
    , min = min
    , step = step
    , max = max
    , required = required
    , disabled = disabled
    , readOnly = readOnly
    , enabled = enabled
    , deps = deps
    , prefix = prefix
    , suffix = suffix
  )
}


class CheckboxField(
                     get: () => Boolean
                     , set: Boolean => Js
                     , label: Option[Elem] = None
                     , name: Option[String] = None
                     , tabindex: Option[Int] = None
                     , val disabled: () => Boolean = () => false
                     , val readOnly: () => Boolean = () => false
                     , val enabled: () => Boolean = () => true
                     , val deps: Set[FormField] = Set()
                   )(implicit renderer: CheckboxFieldRenderer) extends StandardFormField with ValidatableField with StringSerializableField with FocusableFormField {

  var currentValue: Boolean = get()

  override def loadFromString(str: String): Seq[(ValidatableField, NodeSeq)] = {
    str.toBooleanOption match {
      case Some(value) =>
        currentValue = value
        set(currentValue)
        Nil
      case None =>
        List((this, scala.xml.Text(s"Found not parse as a boolean: '$str'")))
    }
  }

  override def saveToString(): Option[String] = Some(currentValue.toString).filter(_ != "")

  override def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => set(currentValue)
    case _ => Js.void
  })

  override def errors(): Seq[(ValidatableField, NodeSeq)] = Nil

  override def focusJs: Js = Js.focus(elemId)

  def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        renderer.render(this)(
          label.map(lbl => <label for={elemId}>{lbl}</label>),
          <input
            type="checkbox"
            value=""
            id={elemId}
            name={name.getOrElse(null)}
            onchange={fsc.callback(Js.checkboxIsChecked(elemId), value => {
              currentValue = value.toBoolean
              form.onEvent(ChangedField(this)) & (if (hints.contains(ShowValidationsHint)) reRender() else Js.void)
            }).cmd}
            tabindex={tabindex.map(_ + "").getOrElse(null)}
            checked={if (currentValue) "checked" else null}></input>,
          errors().headOption.map(_._2)
        )
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil

  def withLabel(label: String) = copy(label = Some(<span>{label}</span>))

  def copy(
            get: () => Boolean = get
            , set: Boolean => Js = set
            , label: Option[Elem] = label
            , name: Option[String] = name
            , tabindex: Option[Int] = tabindex
            , disabled: () => Boolean = disabled
            , readOnly: () => Boolean = readOnly
            , enabled: () => Boolean = enabled
            , deps: Set[FormField] = deps
          ): CheckboxField = new CheckboxField(
    get = get
    , set = set
    , label = label
    , name = name
    , tabindex = tabindex
    , disabled = disabled
    , readOnly = readOnly
    , enabled = enabled
    , deps = deps
  )
}

class SelectField[T](
                      all: () => Seq[T]
                      , get: () => T
                      , set: T => Js
                      , toString: T => String = (v: T) => v.toString
                      , toId: (T, Int) => String = (v: T, idx: Int) => idx.toString
                      , val label: Option[NodeSeq] = None
                      , name: Option[String] = None
                      , noneSelected: Option[String] = None
                      , val disabled: () => Boolean = () => false
                      , val readOnly: () => Boolean = () => false
                      , val enabled: () => Boolean = () => true
                      , val deps: Set[FormField] = Set()
                    )(implicit renderer: SelectFieldRenderer) extends StandardFormField with ValidatableField with StringSerializableField with FocusableFormField {

  var currentlySelectedValue: T = get()

  override def loadFromString(str: String): Seq[(ValidatableField, NodeSeq)] = {
    all().zipWithIndex.find({
      case (v, idx) => toId(v, idx) == str
    }) match {
      case Some((v, _)) =>
        println(s"currentlySelectedValue = ${v}")
        currentlySelectedValue = v
        set(v)
        Nil
      case None =>
        List((this, scala.xml.Text(s"Not found id: '$str'")))
    }
  }

  override def saveToString(): Option[String] = Some(toId(currentlySelectedValue, all().indexOf(currentlySelectedValue))).filter(_ != "0")

  def withLabel(label: String): SelectField[T] = copy(label = Some(scala.xml.Text(label)))

  def copy(
            all: () => Seq[T] = all
            , get: () => T = get
            , set: T => Js = set
            , toString: T => String = toString
            , label: Option[NodeSeq] = label
            , name: Option[String] = name
            , noneSelected: Option[String] = noneSelected
            , enabled: () => Boolean = enabled
            , deps: Set[FormField] = deps
          )(
            implicit renderer: SelectFieldRenderer
          ): SelectField[T] = new SelectField[T](
    all = all
    , get = get
    , set = set
    , toString = toString
    , label = label
    , name = name
    , noneSelected = noneSelected
    , enabled = enabled
    , deps = deps
  )

  override def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => set(currentlySelectedValue)
    case _ => Js.void
  })

  override def errors(): Seq[(ValidatableField, NodeSeq)] = Nil

  override def focusJs: Js = Js.focus(elemId)

  def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    val options = all()
    val ids2Option: Map[String, T] = options.map(opt => fsc.session.nextID() -> opt).toMap
    val option2Id: Map[T, String] = ids2Option.map(kv => kv._2 -> kv._1)
    val optionsRendered = all().map(opt => {
      <option selected={if (currentlySelectedValue == opt) "true" else null} value={option2Id(opt)}>{toString(opt)}</option>
    })

    val errorsAtRenderTime = errors()

    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        val onchangeJs = fsc.callback(Js.elementValueById(elemId), {
          case id =>
            currentlySelectedValue = ids2Option(id)
            form.onEvent(ChangedField(this)) &
              (if (hints.contains(ShowValidationsHint) || errorsAtRenderTime.nonEmpty || errors().nonEmpty) reRender() else Js.void)
        }).cmd
        renderer.render(this)(
          label.map(label => <label for={elemId}>{label}</label>),
          <select
            name={name.getOrElse(null)}
            onblur={onchangeJs}
            onchange={onchangeJs}
            id={elemId}
          >{optionsRendered}</select>,
          errorsAtRenderTime.headOption.map(_._2)
        )
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
}

class MultiSelectField[T](
                           all: () => Seq[T]
                           , get: () => Set[T]
                           , set: Set[T] => Js
                           , toString: T => String = (v: T) => v.toString
                           , toId: (T, Int) => String = (v: T, idx: Int) => idx.toString
                           , val label: Option[NodeSeq] = None
                           , name: Option[String] = None
                           , noneSelected: Option[String] = None
                           , val disabled: () => Boolean = () => false
                           , val readOnly: () => Boolean = () => false
                           , val enabled: () => Boolean = () => true
                           , val deps: Set[FormField] = Set()
                           , val size: Option[Int] = None
                         )(implicit renderer: MultiSelectFieldRenderer) extends StandardFormField with ValidatableField with StringSerializableField {

  var currentlySelectedValue: Set[T] = get()

  override def loadFromString(str: String): Seq[(ValidatableField, NodeSeq)] = {
    val selected: Set[String] = str.split(";").toSet

    currentlySelectedValue = all().zipWithIndex.collect({
      case (v, idx) if selected.contains(toId(v, idx)) => v
    }).toSet
    set(currentlySelectedValue)
    Nil
  }

  override def saveToString(): Option[String] = Some(currentlySelectedValue.map(v => {
    toId(v, all().indexOf(currentlySelectedValue))
  }).mkString(";"))

  def withLabel(label: String): MultiSelectField[T] = copy(label = Some(scala.xml.Text(label)))

  def copy(
            all: () => Seq[T] = all
            , get: () => Set[T] = get
            , set: Set[T] => Js = set
            , toString: T => String = toString
            , label: Option[NodeSeq] = label
            , name: Option[String] = name
            , noneSelected: Option[String] = noneSelected
            , enabled: () => Boolean = enabled
            , deps: Set[FormField] = deps
            , size: Option[Int] = size
          )(
            implicit renderer: MultiSelectFieldRenderer
          ): MultiSelectField[T] = new MultiSelectField[T](
    all = all
    , get = get
    , set = set
    , toString = toString
    , label = label
    , name = name
    , noneSelected = noneSelected
    , enabled = enabled
    , deps = deps
    , size = size
  )

  override def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => set(currentlySelectedValue)
    case _ => Js.void
  })

  override def errors(): Seq[(ValidatableField, NodeSeq)] = Nil

  def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    val options = all()
    val ids2Option: Map[String, T] = options.map(opt => fsc.session.nextID() -> opt).toMap
    val option2Id: Map[T, String] = ids2Option.map(kv => kv._2 -> kv._1)
    val optionsRendered = all().map(opt => {
      <option selected={if (currentlySelectedValue.contains(opt)) "true" else null} value={option2Id(opt)}>{toString(opt)}</option>
    })

    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        val onchangeJs = fsc.callback(Js.selectedValues(Js.elementById(elemId)), {
          case ids =>
            currentlySelectedValue = ids.split(",").toSet.map(id => ids2Option(id))
            form.onEvent(ChangedField(this)) & (if (hints.contains(ShowValidationsHint)) reRender() else Js.void)
        }).cmd
        renderer.render(this)(
          label.map(label => <label for={elemId}>{label}</label>),
          <select
            multiple="multiple"
            name={name.getOrElse(null)}
            onblur={onchangeJs}
            onchange={onchangeJs}
            id={elemId}
            size={size.map(_ + "").getOrElse(null)}
          >{optionsRendered}</select>,
          errors().headOption.map(_._2)
        )
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
}

object EnumField {

  def NonNullable[T <: Enumeration](
                                     enum: T
                                     , get: () => T#Value
                                     , set: T#Value => Js
                                     , toString: T#Value => String = (v: T#Value) => v.toString
                                     , label: Option[NodeSeq] = None
                                     , name: Option[String] = None
                                     , enabled: () => Boolean = () => true
                                     , deps: Set[FormField] = Set()
                                   )(implicit renderer: SelectFieldRenderer) = new SelectField[T#Value](
    all = () => `enum`.values.toList,
    get = get,
    set = set,
    toString = toString,
    label = label,
    name = name,
    enabled = enabled,
    deps = deps
  )

  def Multi[T <: Enumeration](
                               enum: T
                               , get: () => Set[T#Value]
                               , set: Set[T#Value] => Js
                               , toString: T#Value => String = (v: T#Value) => v.toString
                               , label: Option[NodeSeq] = None
                               , name: Option[String] = None
                               , enabled: () => Boolean = () => true
                               , deps: Set[FormField] = Set()
                               , size: Option[Int] = None
                             )(implicit renderer: MultiSelectFieldRenderer) = new MultiSelectField[T#Value](
    all = () => `enum`.values.toList,
    get = get,
    set = set,
    toString = toString,
    label = label,
    name = name,
    enabled = enabled,
    deps = deps,
    size = size
  )

  def Nullable[T <: Enumeration](
                                  enum: T
                                  , get: () => Option[T#Value]
                                  , set: Option[T#Value] => Js
                                  , toString: Option[T#Value] => String = (v: Option[T#Value]) => v.map(_.toString).getOrElse("--")
                                  , label: Option[NodeSeq] = None
                                  , name: Option[String] = None
                                  , required: () => Boolean = () => false
                                  , enabled: () => Boolean = () => true
                                  , deps: Set[FormField] = Set()
                                )(implicit renderer: SelectFieldRenderer) = new SelectField[Option[T#Value]](
    all = () => None :: `enum`.values.toList.map(Some(_)),
    get = get,
    set = set,
    toString = toString,
    label = label,
    name = name,
    enabled = enabled,
    deps = deps
  ) {
    override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
      (if (required() && currentlySelectedValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
  }
}

class TextAreaField(
                     get: () => String
                     , set: String => Js
                     , label: Option[NodeSeq] = None
                     , name: Option[String] = None
                     , placeholder: Option[String] = None
                     , tabindex: Option[Int] = None
                     , maxlength: Option[Int] = None
                     , nRows: Int = 3
                     , additionalStyle: String = ""
                     , required: () => Boolean = () => false
                     , val disabled: () => Boolean = () => false
                     , val readOnly: () => Boolean = () => false
                     , val enabled: () => Boolean = () => true
                     , val deps: Set[FormField] = Set()
                   )(
                     implicit renderer: TextareaFieldRenderer
                   ) extends StandardFormField with ValidatableField with StringSerializableField with FocusableFormField {

  var currentValue = get()

  override def loadFromString(str: String): Seq[(ValidatableField, NodeSeq)] = {
    currentValue = str
    set(currentValue)
    Nil
  }

  override def saveToString(): Option[String] = Some(currentValue).filter(_ != "")

  def withLabel(label: String) = copy(label = Some(scala.xml.Text(label)))

  def copy(
            get: () => String = get
            , set: String => Js = set
            , label: Option[NodeSeq] = label
            , name: Option[String] = name
            , placeholder: Option[String] = placeholder
            , tabindex: Option[Int] = tabindex
            , maxlength: Option[Int] = maxlength
            , nRows: Int = nRows
            , additionalStyle: String = additionalStyle
            , required: () => Boolean = required
            , enabled: () => Boolean = enabled
            , deps: Set[FormField] = deps
          ): TextAreaField = {
    new TextAreaField(
      get = get
      , set = set
      , label = label
      , name = name
      , placeholder = placeholder
      , tabindex = tabindex
      , maxlength = maxlength
      , nRows = nRows
      , additionalStyle = additionalStyle
      , required = required
      , enabled = enabled
      , deps = deps
    )
  }

  override def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => set(currentValue)
    case _ => Js.void
  })


  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.trim == "") Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())

  override def focusJs: Js = Js.focus(elemId)

  override def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        val changedJs = fsc.callback(Js.elementValueById(elemId), value => {
          currentValue = value
          form.onEvent(ChangedField(this)) & (if (hints.contains(ShowValidationsHint)) reRender() else Js.void)
        }).cmd
        renderer.render(this)(
          label,
          <textarea type="text"
                    name={name.getOrElse(null)}
                    class="form-control"
                    style={additionalStyle}
                    id={elemId}
                    onblur={changedJs}
                    placeholder={placeholder.getOrElse(null)}
                    rows={nRows.toString}
                    tabindex={tabindex.map(_ + "").getOrElse(null)}
                    maxlength={maxlength.map(_ + "").getOrElse(null)}
                    required={if (required()) "true" else null}>{get()}</textarea>,
          errors().headOption.map(_._2)
        )
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
}

class CodeField(
                 get: () => String
                 , set: String => Js
                 , label: Option[NodeSeq] = None
                 , name: Option[String] = None
                 , placeholder: Option[String] = None
                 , tabindex: Option[Int] = None
                 , maxlength: Option[Int] = None
                 , required: () => Boolean = () => false
                 , val disabled: () => Boolean = () => false
                 , val readOnly: () => Boolean = () => false
                 , val enabled: () => Boolean = () => true
                 , val deps: Set[FormField] = Set(),
                 saveEveryMillis: Int = 1000,
                 height: String = "800px"
               )(
                 implicit renderer: TextareaFieldRenderer
               ) extends StandardFormField with ValidatableField with StringSerializableField {

  lazy val editorId = "__editor" + IdGen.id
  lazy val timeoutId = editorId + "_TO"
  lazy val savedId = editorId + "_saved"
  lazy val savingId = editorId + "_saving"

  var currentValue = get()

  override def reRender()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = {
    if (!hints.contains(OnSaveRerender)) super.reRender()
    else Js.void
  }

  override def loadFromString(str: String): Seq[(ValidatableField, NodeSeq)] = {
    currentValue = str
    set(currentValue)
    Nil
  }

  override def saveToString(): Option[String] = Some(currentValue).filter(_ != "")

  def withLabel(label: String) = copy(label = Some(scala.xml.Text(label)))

  def copy(
            get: () => String = get
            , set: String => Js = set
            , label: Option[NodeSeq] = label
            , name: Option[String] = name
            , placeholder: Option[String] = placeholder
            , tabindex: Option[Int] = tabindex
            , maxlength: Option[Int] = maxlength
            , required: () => Boolean = required
            , enabled: () => Boolean = enabled
            , deps: Set[FormField] = deps
            , saveEveryMillis: Int = saveEveryMillis
            , height: String = height
          ): CodeField = {
    new CodeField(
      get = get
      , set = set
      , label = label
      , name = name
      , placeholder = placeholder
      , tabindex = tabindex
      , maxlength = maxlength
      , required = required
      , enabled = enabled
      , deps = deps
      , saveEveryMillis = saveEveryMillis
      , height = height
    )
  }

  override def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => set(currentValue)
    case _ => Js.void
  })

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.trim == "") Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())

  override def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>

        val onChangeCallbackJS = fsc.callback(Js(s"window.$editorId.session.getValue()"), value => {
          currentValue = value
          form.onEvent(ChangedField(this)) & (if (hints.contains(ShowValidationsHint)) reRender() else Js.void)
        })

        val onChangeJS =
          s"""function() {
             |  $$('#$savedId').hide();
             |  $$('#$savingId').show();
             |  clearTimeout(window.$timeoutId);
             |  window.$timeoutId = setTimeout(
             |    function() {${onChangeCallbackJS.cmd}; $$('#$savedId').show(); $$('#$savingId').hide();},
             |    $saveEveryMillis
             |  );
             |}
             |""".stripMargin

        <div id={aroundId}>
          <p id={savedId} style="font-weight: bold; color: green;">Saved</p>
          <p id={savingId} style="font-weight: bold; color: #999; display: none;">Saving...</p>
          {
          Unparsed(
            s"""<style type="text/css" media="screen">
               |  #${editorId} { width: 100%; height: 100%; }
               |</style>
               |""".stripMargin
          )
          }
          <div style={s"width:100%;height: $height;display:inline-block;position:relative;"}>
            <div id={editorId}>{currentValue}</div>
          </div>

          <script src="https://cdn.jsdelivr.net/npm/ace-builds@1.15.3/src-min-noconflict/ace.min.js" charset="utf-8"></script>
          <script src="https://cdn.jsdelivr.net/npm/ace-builds@1.15.3/src-min-noconflict/mode-html.js" charset="utf-8"></script>
          <script src="https://cdn.jsdelivr.net/npm/ace-builds@1.15.3/src-min-noconflict/theme-monokai.js" charset="utf-8"></script>
          {
            Unparsed(
              s"""<script>window.$editorId = ace.edit(${Js.asJsStr(editorId).cmd});
                 |window.$editorId.setTheme("ace/theme/monokai");
                 |window.$editorId.session.setMode("ace/mode/html");
                 |window.$editorId.session.on('change', ${onChangeJS});</script>""".stripMargin
            )
          }
        </div>
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
}

trait ButtonFieldRenderer {
  def render(field: SaveButtonField)(btn: Elem)(implicit hints: Seq[RenderHint]): Elem
}

class SaveButtonField(
                       btn: Elem
                       , val disabled: () => Boolean = () => false
                       , val enabled: () => Boolean = () => true
                       , val deps: Set[FormField] = Set()
                     )(implicit renderer: ButtonFieldRenderer) extends StandardFormField {

  def readOnly: () => Boolean = () => false

  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil

  override def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        renderer.render(this)(btn.withId(elemId).addOnClick((Js.focus(elemId) & form.onSaveClientSide()).cmd))
      }
    }
}

trait FileUploadFieldRenderer {

  def transformFormElem(field: FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem

  def transforLabelElem(field: FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem

  def transforSubmitButtonElem(field: FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem

  def transforResetButtonElem(field: FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem

  def transforFileInputElem(field: FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem
}

class FileUploadField(
                       get: () => Option[(String, Array[Byte])]
                       , set: Option[(String, Array[Byte])] => Js
                       , submitBtn: Elem
                       , renderPreview: FSContext => Option[(String, Array[Byte])] => Elem
                       , resetBtn: Option[Elem] = None
                       , label: Option[NodeSeq] = None
                       , name: Option[String] = None
                       , tabindex: Option[Int] = None

                       , val disabled: () => Boolean = () => false
                       , val readOnly: () => Boolean = () => false
                       , val enabled: () => Boolean = () => true
                       , val deps: Set[FormField] = Set()

                       , val transformFormElem: Elem => Elem = identity[Elem]
                       , val transforLabelElem: Elem => Elem = identity[Elem]
                       , val transforSubmitButtonElem: Elem => Elem = identity[Elem]
                       , val transforResetButtonElem: Elem => Elem = identity[Elem]
                       , val transforFileInputElem: Elem => Elem = identity[Elem]
                     )(implicit renderer: FileUploadFieldRenderer) extends StandardFormField with ValidatableField {

  var currentValue: Option[(String, Array[Byte])] = get()

  override def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => set(currentValue)
    case _ => Js.void
  })

  override def errors(): Seq[(ValidatableField, NodeSeq)] = Nil

  def render()(implicit form: Form5, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        val targetId = IdGen.id("targetFrame")
        val inputId = IdGen.id("input")
        val buttonId = IdGen.id("btn")
        val resetButtonId = IdGen.id("reset-btn")

        val previewRenderer = Js.rerenderable(rerenderer => implicit fsc => renderPreview(fsc)(currentValue))
        val actionUrl = fsc.fileUploadActionUrl({
          case Seq(uploadedFile: FSUploadedFile, _ *) =>
            currentValue = Some((uploadedFile.submittedFileName, uploadedFile.content))
            previewRenderer.rerender() &
              form.onEvent(ChangedField(this)) &
              (if (hints.contains(ShowValidationsHint)) reRender() else Js.void) &
              Js.show(resetButtonId)
        })
        <form target={targetId} action={actionUrl} method="post" encoding="multipart/form-data" enctype="multipart/form-data" id={aroundId}>
          <iframe id={targetId} name={targetId} src="about:blank" onload="eval(this.contentWindow.document.body.innerText)" style="width:0;height:0;border:0px solid #fff;"><html><body></body></html></iframe>
          {
          label.map(label => <label for={elemId}>{label}</label>).map(_.pipe(renderer.transforLabelElem(this)).pipe(transforLabelElem)).getOrElse(NodeSeq.Empty)
          }
          {
          previewRenderer.render()
          }
          {
            <input name="file" type="file" id={inputId} onchange={Js.show(buttonId).cmd}/>.pipe(renderer.transforFileInputElem(this)).pipe(transforFileInputElem)
          }
          {
          submitBtn.pipe(renderer.transforSubmitButtonElem(this)).pipe(transforSubmitButtonElem).withId(buttonId).withStyle("display:none").withTypeSubmit()
          }
          {
          resetBtn.map(renderer.transforResetButtonElem(this)).map(transforResetButtonElem).map(_.withId(resetButtonId).addOnClick(fsc.callback(() => {
            currentValue = None
            previewRenderer.rerender() &
              form.onEvent(ChangedField(this)) &
              (if (hints.contains(ShowValidationsHint)) reRender() else Js.void) &
              Js.hide(resetButtonId)
          }).cmd).withAttr("style")(cur => if (currentValue.isDefined) cur.getOrElse("") else cur.getOrElse("") + ";display:none;")).getOrElse(NodeSeq.Empty)
          }
        </form>.pipe(renderer.transformFormElem(this)).pipe(transformFormElem)
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil

  def withLabel(label: String) = copy(label = Some(<span>{label}</span>))

  def copy(
            get: () => Option[(String, Array[Byte])] = get
            , set: Option[(String, Array[Byte])] => Js = set
            , submitBtn: Elem = submitBtn
            , renderPreview: FSContext => Option[(String, Array[Byte])] => Elem = renderPreview
            , resetBtn: Option[Elem] = resetBtn
            , label: Option[NodeSeq] = label
            , name: Option[String] = name
            , tabindex: Option[Int] = tabindex
            , disabled: () => Boolean = disabled
            , readOnly: () => Boolean = readOnly
            , enabled: () => Boolean = enabled
            , deps: Set[FormField] = deps
            , transformFormElem: Elem => Elem = transformFormElem
            , transforLabelElem: Elem => Elem = transforLabelElem
            , transforSubmitButtonElem: Elem => Elem = transforSubmitButtonElem
            , transforResetButtonElem: Elem => Elem = transforResetButtonElem
            , transforFileInputElem: Elem => Elem = transforFileInputElem
          ): FileUploadField = new FileUploadField(
    get = get
    , set = set
    , submitBtn = submitBtn
    , renderPreview = renderPreview
    , resetBtn = resetBtn
    , label = label
    , name = name
    , tabindex = tabindex
    , disabled = disabled
    , readOnly = readOnly
    , enabled = enabled
    , deps = deps
    , transformFormElem = transformFormElem
    , transforLabelElem = transforLabelElem
    , transforSubmitButtonElem = transforSubmitButtonElem
    , transforResetButtonElem = transforResetButtonElem
    , transforFileInputElem = transforFileInputElem
  )
}
