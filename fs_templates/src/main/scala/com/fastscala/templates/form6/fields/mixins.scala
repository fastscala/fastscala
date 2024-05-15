package com.fastscala.templates.form6.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6
import com.fastscala.utils.ElemTransformers.RichElem
import org.joda.time.{DateTime, LocalDate}
import org.joda.time.format.DateTimeFormat

import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import scala.util.chaining.scalaUtilChainingOps
import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, NodeSeq}

trait F6FieldMixin extends FormField {

  def mutate(code: => Unit): this.type = {
    code
    this
  }
}

trait F6FieldInputFieldMixin extends F6FieldMixin {

  def process(input: Elem): Elem = input
}

trait F6FieldWithGettersAndSetters[V] extends F6FieldMixin {
  var getter: () => V
  var setter: V => Js

  def rw(get: => V, set: V => Unit) = {
    getter = () => get
    setter = v => {
      set(v)
      Js.void
    }
  }
}

trait F6FieldWithDisabled extends F6FieldInputFieldMixin {
  var _disabled: () => Boolean = () => false

  def disabled() = _disabled()

  def disabled(v: Boolean): this.type = mutate {
    _disabled = () => v
  }

  def disabled(f: () => Boolean): this.type = mutate({
    _disabled = f
  })

  override def process(input: Elem): Elem = if (_disabled()) input.withAttr("disabled", "disabled") else input
}

trait F6FieldWithRequired extends F6FieldInputFieldMixin {
  var _required: () => Boolean = () => false

  def required() = _required()

  def required(v: Boolean): this.type = mutate {
    _required = () => v
  }

  def required(f: () => Boolean): this.type = mutate {
    _required = f
  }

  override def process(input: Elem): Elem = if (_required()) input.withAttr("required", "true") else input
}

trait F6FieldWithReadOnly extends F6FieldInputFieldMixin {
  var _readOnly: () => Boolean = () => false

  def readOnly() = _readOnly()

  def readOnly(v: Boolean): this.type = mutate {
    _readOnly = () => v
  }

  def readOnly(f: () => Boolean): this.type = mutate {
    _readOnly = f
  }

  override def process(input: Elem): Elem = if (_readOnly()) input.withAttr("readonly", "true") else input
}

trait F6FieldWithEnabled extends F6FieldInputFieldMixin {
  var _enabled: () => Boolean = () => true

  def enabled(): Boolean = _enabled()

  def enabled(v: Boolean): this.type = mutate {
    _enabled = () => v
  }

  def enabled(f: () => Boolean): this.type = mutate {
    _enabled = f
  }
}

trait F6FieldWithTabIndex extends F6FieldInputFieldMixin {
  var _tabIndex: () => Option[Int] = () => None

  def tabIndex() = _tabIndex()

  def tabIndex(v: Option[Int]): this.type = mutate {
    _tabIndex = () => v
  }

  def tabIndex(v: Int): this.type = mutate {
    _tabIndex = () => Some(v)
  }

  def tabIndex(f: () => Option[Int]): this.type = mutate {
    _tabIndex = f
  }

  override def process(input: Elem): Elem = _tabIndex().map(tabIndex => {
    input.withAttr("tabindex", tabIndex.toString)
  }).getOrElse(input)
}

trait F6FieldWithName extends F6FieldInputFieldMixin {
  var _name: () => Option[String] = () => None

  def name() = _name()

  def name(v: String): this.type = mutate {
    _name = () => Some(v)
  }

  def name(v: Option[String]): this.type = mutate {
    _name = () => v
  }

  def name(f: () => Option[String]): this.type = mutate {
    _name = f
  }

  override def process(input: Elem): Elem = _name().map(name => {
    input.withAttr("name", name)
  }).getOrElse(input)
}

trait F6FieldWithPlaceholder extends F6FieldInputFieldMixin {
  var _placeholder: () => Option[String] = () => None

  def placeholder() = _placeholder()

  def placeholder(v: Option[String]): this.type = mutate {
    _placeholder = () => v
  }

  def placeholder(v: String): this.type = mutate {
    _placeholder = () => Some(v)
  }

  def placeholder(f: () => Option[String]): this.type = mutate {
    _placeholder = f
  }

  override def process(input: Elem): Elem = _placeholder().map(placeholder => {
    input.withAttr("placeholder", placeholder)
  }).getOrElse(input)
}

trait F6FieldWithLabel extends F6FieldInputFieldMixin {
  var _label: () => Option[NodeSeq] = () => None

  def label() = _label()

  def label(v: Option[NodeSeq]): this.type = mutate {
    _label = () => v
  }

  def label(v: NodeSeq): this.type = mutate {
    _label = () => Some(v)
  }

  def label(v: String): this.type = mutate {
    _label = () => Some(scala.xml.Text(v))
  }

  def label(f: () => Option[NodeSeq]): this.type = mutate {
    _label = f
  }

  def withLabel(label: String): this.type = mutate {
    _label = () => Some(<span>{label}</span>)
  }
}

trait F6FieldWithMaxlength extends F6FieldInputFieldMixin {
  var _maxlength: () => Option[Int] = () => None

  def maxlength() = _maxlength()

  def maxlength(v: Option[Int]): this.type = mutate {
    _maxlength = () => v
  }

  def maxlength(v: Int): this.type = mutate {
    _maxlength = () => Some(v)
  }

  def maxlength(f: () => Option[Int]): this.type = mutate {
    _maxlength = f
  }

  override def process(input: Elem): Elem = _maxlength().map(maxlength => {
    input.withAttr("maxlength", maxlength.toString)
  }).getOrElse(input)
}

trait F6FieldWithInputType extends F6FieldInputFieldMixin {
  def _inputTypeDefault: String = "text"

  var _inputType: () => String = () => _inputTypeDefault

  def inputType() = _inputType()

  def inputType(v: String): this.type = mutate {
    _inputType = () => v
  }

  def inputType(f: () => String): this.type = mutate {
    _inputType = f
  }

  override def process(input: Elem): Elem = input.withAttr("type", _inputType())
}

trait F6FieldWithAdditionalAttrs extends F6FieldInputFieldMixin {
  var _additionalAttrs: () => Seq[(String, String)] = () => Nil

  def additionalAttrs() = _additionalAttrs()

  def additionalAttrs(v: Seq[(String, String)]): this.type = mutate {
    _additionalAttrs = () => v
  }

  def additionalAttrs(f: () => Seq[(String, String)]): this.type = mutate {
    _additionalAttrs = f
  }

  override def process(input: Elem): Elem = input.withAttrs(_additionalAttrs(): _*)
}

trait F6FieldWithDependencies extends F6FieldInputFieldMixin {
  var _deps: () => Set[FormField] = () => Set()

  def deps() = _deps()

  def deps(v: Set[FormField]): this.type = mutate {
    _deps = () => v
  }

  def deps(f: () => Set[FormField]): this.type = mutate {
    _deps = f
  }
}

trait F6FieldWithPrefix extends F6FieldMixin {

  var _prefix: () => String = () => ""

  def prefix() = _prefix()

  def prefix(v: String): this.type = mutate {
    _prefix = () => v
  }

  def prefix(f: () => String): this.type = mutate {
    _prefix = f
  }
}

trait F6FieldWithSuffix extends F6FieldMixin {

  var _suffix: () => String = () => ""

  def suffix() = _suffix()

  def suffix(v: String): this.type = mutate {
    _suffix = () => v
  }

  def suffix(f: () => String): this.type = mutate {
    _suffix = f
  }
}

trait F6FieldWithMin extends F6FieldInputFieldMixin {
  var _min: () => Option[String] = () => None

  def min() = _min()

  def min(v: Option[String]): this.type = mutate {
    _min = () => v
  }

  def min(v: String): this.type = mutate {
    _min = () => Some(v)
  }

  def min(f: () => Option[String]): this.type = mutate {
    _min = f
  }

  override def process(input: Elem): Elem = _min().map(min => {
    input.withAttr("min", min.toString)
  }).getOrElse(input)
}

trait F6FieldWithStep extends F6FieldInputFieldMixin {
  var _step: () => Option[Int] = () => None

  def step(): Option[Int] = _step()

  def step(v: Option[Int]): this.type = mutate {
    _step = () => v
  }

  def step(v: Int): this.type = mutate {
    _step = () => Some(v)
  }

  def step(f: () => Option[Int]): this.type = mutate {
    _step = f
  }

  override def process(input: Elem): Elem = _step().map(step => {
    input.withAttr("step", step.toString)
  }).getOrElse(input)
}

trait F6FieldWithMax extends F6FieldInputFieldMixin {
  var _max: () => Option[String] = () => None

  def max() = _max()

  def max(v: Option[String]): this.type = mutate {
    _max = () => v
  }

  def max(v: String): this.type = mutate {
    _max = () => Some(v)
  }

  def max(f: () => Option[String]): this.type = mutate {
    _max = f
  }

  override def process(input: Elem): Elem = _max().map(max => {
    input.withAttr("max", max.toString)
  }).getOrElse(input)
}

abstract class F6TextField[T](
                               getOpt: () => Option[T]
                               , setOpt: Option[T] => Js
                             )(implicit renderer: TextFieldRenderer) extends StandardFormField
  with ValidatableField
  with StringSerializableField
  with FocusableFormField
  with F6FieldWithDisabled
  with F6FieldWithRequired
  with F6FieldWithReadOnly
  with F6FieldWithEnabled
  with F6FieldWithTabIndex
  with F6FieldWithName
  with F6FieldWithPlaceholder
  with F6FieldWithLabel
  with F6FieldWithMaxlength
  with F6FieldWithInputType
  with F6FieldWithAdditionalAttrs
  with F6FieldWithDependencies {

  def toString(strOpt: Option[T]): String

  def fromString(str: String): Either[String, Option[T]]

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

  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => setOpt(currentValue)
    case _ => Js.void
  })

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  def render()(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        renderer.render(this)(
          _label().map(lbl => <label for={elemId}>{lbl}</label>),
            <input type={inputType}
                   class="form-control"
                   id={elemId}
                   onblur={fsc.callback(Js.elementValueById(elemId), str => {
                     fromString(str).foreach(currentValue = _)
                     form.onEvent(ChangedField(this)) &
                       Js.evalIf(hints.contains(ShowValidationsHint))(reRender()) // TODO: is this wrong? (running on the client side, but should be server?)
                   }).cmd}
                   onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {${Js.blur(elemId) & Js.evalIf(hints.contains(SaveOnEnterHint))(form.onSaveClientSide())}}"}
                   value={toString(currentValue)}
            />.withAttrs(finalAdditionalAttrs: _*),
          errors().headOption.map(_._2)
        )
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
}

class F6StringField(
                     get: () => String
                     , set: String => Js
                   )(implicit renderer: TextFieldRenderer) extends F6TextField[String](
  getOpt = () => Some(get())
  , setOpt = strOpt => set(strOpt.getOrElse(""))
) {

  def toString(strOpt: Option[String]): String = strOpt.getOrElse("")

  def fromString(str: String): Either[String, Option[String]] = Right(Some(str))

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (_required() && currentValue.getOrElse("").trim == "") Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
}


class F6StringOptField(
                        get: () => Option[String]
                        , set: Option[String] => Js
                      )(implicit renderer: TextFieldRenderer) extends F6TextField[String](
  getOpt = () => get()
  , setOpt = strOpt => set(strOpt.filter(_ != ""))
) {

  def toString(strOpt: Option[String]): String = strOpt.getOrElse("")

  def fromString(str: String): Either[String, Option[String]] = Right(Some(str))

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6JodaDateOptField(
                          get: () => Option[LocalDate]
                          , set: Option[LocalDate] => Js
                        )(implicit renderer: TextFieldRenderer) extends F6TextField[LocalDate](
  getOpt = () => get()
  , setOpt = optValue => set(optValue)
) {

  def toString(strOpt: Option[LocalDate]): String = strOpt.map(_.toString("YYYY-MM-dd")).getOrElse("")

  def fromString(str: String): Either[String, Option[LocalDate]] = Right(Some(str).filter(_.trim != "").map(str => LocalDate.parse(str, DateTimeFormat.forPattern("YYYY-MM-dd"))))

  override def _inputTypeDefault: String = "date"

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6DateOptField(
                      get: () => Option[java.time.LocalDate]
                      , set: Option[java.time.LocalDate] => Js
                    )(implicit renderer: TextFieldRenderer) extends F6TextField[java.time.LocalDate](
  getOpt = () => get()
  , setOpt = optValue => set(optValue)
) {
  override def _inputTypeDefault: String = "date"

  def toString(strOpt: Option[java.time.LocalDate]): String = strOpt.map(_.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).getOrElse("")

  def fromString(str: String): Either[String, Option[java.time.LocalDate]] = Right(Some(str).filter(_.trim != "").map(str => java.time.LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"))))

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6DateTimeOptField(
                          get: () => Option[java.time.LocalDateTime]
                          , set: Option[java.time.LocalDateTime] => Js
                        )(implicit renderer: TextFieldRenderer) extends F6TextField[java.time.LocalDateTime](
  getOpt = () => get()
  , setOpt = optValue => set(optValue)
) {
  override def _inputTypeDefault: String = "datetime-local"

  def toString(strOpt: Option[java.time.LocalDateTime]): String = strOpt.map(_.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))).getOrElse("")

  def fromString(str: String): Either[String, Option[java.time.LocalDateTime]] = Right(Some(str).filter(_.trim != "").map(str => java.time.LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))))

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6DoubleOptField(
                        get: () => Option[Double]
                        , set: Option[Double] => Js
                      )(implicit renderer: TextFieldRenderer) extends F6TextField[Double](
  getOpt = () => get()
  , setOpt = doubleOpt => set(doubleOpt)
) with F6FieldWithPrefix
  with F6FieldWithSuffix
  with F6FieldWithMin
  with F6FieldWithStep
  with F6FieldWithMax {
  override def _inputTypeDefault: String = "number"

  def toString(strOpt: Option[Double]): String = strOpt.map(value => prefix + " " + new DecimalFormat("0.#").format(value) + " " + suffix).map(_.trim).getOrElse("")

  def fromString(str: String): Either[String, Option[Double]] = {
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

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6IntOptField(
                     get: () => Option[Int]
                     , set: Option[Int] => Js
                   )(implicit renderer: TextFieldRenderer) extends F6TextField[Int](
  getOpt = () => get()
  , setOpt = intOpt => set(intOpt)
) with F6FieldWithPrefix
  with F6FieldWithSuffix
  with F6FieldWithMin
  with F6FieldWithStep
  with F6FieldWithMax {

  def toString(strOpt: Option[Int]): String = strOpt.map(value => prefix + " " + new DecimalFormat("0.#").format(value) + " " + suffix).map(_.trim).getOrElse("")

  def fromString(str: String): Either[String, Option[Int]] = {
    if (str.trim == "") {
      Right(None)
    } else {
      str
        .toLowerCase
        .trim
        .replaceAll("^" + Pattern.quote(prefix.toLowerCase) + " *", "")
        .replaceAll(" *" + Pattern.quote(suffix.toLowerCase) + "$", "")
        .toIntOption match {
        case Some(value) => Right(Some(value))
        case None => Left(s"Not an int?: $str")
      }
    }
  }

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6TimeOfDayField(
                        get: () => Option[Int]
                        , set: Option[Int] => Js
                      )(implicit renderer: TextFieldRenderer) extends F6TextField[Int](
  getOpt = () => get()
  , setOpt = doubleOpt => set(doubleOpt)
) with F6FieldWithPrefix
  with F6FieldWithSuffix
  with F6FieldWithMin
  with F6FieldWithStep
  with F6FieldWithMax {

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())


  def toString(strOpt: Option[Int]): String = strOpt.map(value => DateTimeFormat.forPattern("HH:mm").print(new DateTime().withTime(value / 60, value % 60, 0, 0))).map(_.trim).getOrElse("")

  def fromString(str: String): Either[String, Option[Int]] = {
    if (str.trim == "") {
      Right(None)
    } else {
      str
        .toLowerCase
        .trim
        .replaceAll("^" + Pattern.quote(prefix.toLowerCase) + " *", "")
        .replaceAll(" *" + Pattern.quote(suffix.toLowerCase) + "$", "")
        .pipe(txt => {
          Try(DateTimeFormat.forPattern("HH:mm").parseLocalTime(txt)) match {
            case Failure(exception) => Left(s"Not a time?: $txt")
            case Success(parsed) => Right(Some(parsed.getHourOfDay * 60 + parsed.getMinuteOfHour))
          }
        })
    }
  }
}

class F6DoubleField(
                     get: () => Double
                     , set: Double => Js
                   )(implicit renderer: TextFieldRenderer) extends F6TextField[Double](
  getOpt = () => Some(get())
  , setOpt = doubleOpt => doubleOpt.map(double => set(double)).getOrElse(Js.void)
) with F6FieldWithPrefix
  with F6FieldWithSuffix
  with F6FieldWithMin
  with F6FieldWithStep
  with F6FieldWithMax {

  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())

  def toString(strOpt: Option[Double]): String = strOpt.map(value => prefix + " " + value.formatted("%.2f") + " " + suffix).map(_.trim).getOrElse("")

  def fromString(str: String): Either[String, Option[Double]] = {
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
