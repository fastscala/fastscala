package com.fastscala.templates.form6.fields

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6
import com.fastscala.utils.Lazy
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import java.text.DecimalFormat
import java.time
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import scala.util.chaining.scalaUtilChainingOps
import scala.util.{Failure, Success, Try}

trait F6FieldMixin[E <: FSXmlEnv] extends F6Field[E] {

  def mutate(code: => Unit): this.type = {
    code
    this
  }
}

trait F6FieldInputFieldMixin[E <: FSXmlEnv] extends F6FieldMixin[E] {

  def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = input
}

trait F6FieldWithValue[E <: FSXmlEnv, T] extends F6FieldMixin[E] {

  def defaultValue: T

  private lazy val currentValueHolder: Lazy[T] = Lazy(_getter())
  private lazy val internalValue: Lazy[T] = Lazy(defaultValue)

  def currentValue = currentValueHolder()

  def currentValue_=(v: T) = currentValueHolder() = v

  var _getter: () => T = () => internalValue()

  def getter(): () => T = _getter

  def get(): T = getter()()

  def getInternalValue(): T = internalValue()

  var _setter: T => Js = v => Js.void(() => {
    internalValue() = v
  })

  def setter(): T => Js = _setter

  def setInternalValue(value: T): this.type = mutate {
    internalValue() = value
  }

  def set(value: T): Js = setter()(value)

  def setter(setter: T => Js): this.type = mutate({
    _setter = setter
  })

  def getter(getter: () => T): this.type = mutate({
    _getter = getter
  })

  def rw(get: => T, set: T => Unit): this.type = mutate {
    _getter = () => get
    _setter = v => {
      set(v)
      Js.void
    }
  }
}

trait F6FieldWithDisabled[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
  var _disabled: () => Boolean = () => false

  def disabled() = _disabled()

  def disabled(v: Boolean): this.type = mutate {
    _disabled = () => v
  }

  def disabled(f: () => Boolean): this.type = mutate({
    _disabled = f
  })

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    if (_disabled()) input.withAttr("disabled", "disabled") else input
  }
}

trait F6FieldWithRequired[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
  var _required: () => Boolean = () => false

  def required() = _required()

  def required(v: Boolean): this.type = mutate {
    _required = () => v
  }

  def required(f: () => Boolean): this.type = mutate {
    _required = f
  }

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    if (_required()) input.withAttr("required", "true") else input
  }
}

trait F6FieldWithReadOnly[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
  var _readOnly: () => Boolean = () => false

  def readOnly() = _readOnly()

  def readOnly(v: Boolean): this.type = mutate {
    _readOnly = () => v
  }

  def readOnly(f: () => Boolean): this.type = mutate {
    _readOnly = f
  }

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    if (_readOnly()) input.withAttr("readonly", "true") else input
  }
}

trait F6FieldWithEnabled[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
  var _enabled: () => Boolean = () => true

  def enabled(): Boolean = _enabled()

  def enabled(v: Boolean): this.type = mutate {
    _enabled = () => v
  }

  def enabled(f: () => Boolean): this.type = mutate {
    _enabled = f
  }
}

trait F6FieldWithTabIndex[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
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

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    _tabIndex().map(tabIndex => input.withAttr("tabindex", tabIndex.toString)).getOrElse(input)
  }
}

trait F6FieldWithName[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
  var _name: () => Option[String] = () => None

  def name(): Option[String] = _name()

  def name(v: String): this.type = mutate {
    _name = () => Some(v)
  }

  def name(v: Option[String]): this.type = mutate {
    _name = () => v
  }

  def name(f: () => Option[String]): this.type = mutate {
    _name = f
  }

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    _name().map(name => input.withAttr("name", name)).getOrElse(input)
  }
}

trait F6FieldWithSize[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
  var _size: () => Option[Int] = () => None

  def size(): Option[Int] = _size()

  def size(v: Int): this.type = mutate {
    _size = () => Some(v)
  }

  def size(v: Option[Int]): this.type = mutate {
    _size = () => v
  }

  def size(f: () => Option[Int]): this.type = mutate {
    _size = f
  }

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    _size().map(size => input.withAttr("size", size.toString)).getOrElse(input)
  }
}

trait F6FieldWithPlaceholder[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
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

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    _placeholder().map(placeholder => input.withAttr("placeholder", placeholder)).getOrElse(input)
  }
}

trait F6FieldWithLabel[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
  var _label: () => Option[E#NodeSeq] = () => None

  def label() = _label()

  def label(v: Option[E#NodeSeq]): this.type = mutate {
    _label = () => v
  }

  def label(v: E#NodeSeq): this.type = mutate {
    _label = () => Some(v)
  }

  def label(v: String): this.type = mutate {
    _label = () => Some(fsXmlSupport.buildText(v))
  }

  def label(f: () => Option[E#NodeSeq]): this.type = mutate {
    _label = f
  }

  def withLabel(label: String): this.type = mutate {
    _label = () => Some(<span>{label}</span>.asFSXml())
  }
}

trait F6FieldWithMaxlength[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
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

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    _maxlength().map(maxlength => input.withAttr("maxlength", maxlength.toString)).getOrElse(input)
  }
}

trait F6FieldWithInputType[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
  def _inputTypeDefault: String = "text"

  var _inputType: () => String = () => _inputTypeDefault

  def inputType() = _inputType()

  def inputType(v: String): this.type = mutate {
    _inputType = () => v
  }

  def inputType(f: () => String): this.type = mutate {
    _inputType = f
  }

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    input.withAttr("type", _inputType())
  }
}

trait F6FieldWithAdditionalAttrs[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
  var _additionalAttrs: () => Seq[(String, String)] = () => Nil

  def additionalAttrs() = _additionalAttrs()

  def additionalAttrs(v: Seq[(String, String)]): this.type = mutate {
    _additionalAttrs = () => v
  }

  def additionalAttrs(f: () => Seq[(String, String)]): this.type = mutate {
    _additionalAttrs = f
  }

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    input.withAttrs(_additionalAttrs(): _*)
  }
}

trait F6FieldWithDependencies[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
  var _deps: () => Set[F6Field[_]] = () => Set()

  def deps() = _deps()

  def deps(v: Set[F6Field[_]]): this.type = mutate {
    _deps = () => v
  }

  def deps(f: () => Set[F6Field[_]]): this.type = mutate {
    _deps = f
  }
}

trait F6FieldWithPrefix[E <: FSXmlEnv] extends F6FieldMixin[E] {

  var _prefix: () => String = () => ""

  def prefix() = _prefix()

  def prefix(v: String): this.type = mutate {
    _prefix = () => v
  }

  def prefix(f: () => String): this.type = mutate {
    _prefix = f
  }
}

trait F6FieldWithSuffix[E <: FSXmlEnv] extends F6FieldMixin[E] {

  var _suffix: () => String = () => ""

  def suffix() = _suffix()

  def suffix(v: String): this.type = mutate {
    _suffix = () => v
  }

  def suffix(f: () => String): this.type = mutate {
    _suffix = f
  }
}

trait F6FieldWithMin[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
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

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    _min().map(min => input.withAttr("min", min)).getOrElse(input)
  }
}

trait F6FieldWithStep[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
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

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    _step().map(step => input.withAttr("step", step.toString)).getOrElse(input)
  }
}

trait F6FieldWithMax[E <: FSXmlEnv] extends F6FieldInputFieldMixin[E] {
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

  override def processInputElem(input: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]): E#Elem = super.processInputElem(input).pipe { input =>
    import com.fastscala.core.FSXmlUtils._
    _max().map(max => input.withAttr("max", max)).getOrElse(input)
  }
}

abstract class F6TextField[E <: FSXmlEnv, T]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: TextF6FieldRenderer[E]) extends StandardF6Field[E]
  with ValidatableField[E]
  with StringSerializableField[E]
  with FocusableF6Field[E]
  with F6FieldWithDisabled[E]
  with F6FieldWithRequired[E]
  with F6FieldWithReadOnly[E]
  with F6FieldWithEnabled[E]
  with F6FieldWithTabIndex[E]
  with F6FieldWithName[E]
  with F6FieldWithPlaceholder[E]
  with F6FieldWithLabel[E]
  with F6FieldWithMaxlength[E]
  with F6FieldWithInputType[E]
  with F6FieldWithAdditionalAttrs[E]
  with F6FieldWithDependencies[E]
  with F6FieldWithValue[E, T] {

  import com.fastscala.core.FSXmlUtils._

  def toString(value: T): String

  def fromString(str: String): Either[String, T]

  override def loadFromString(str: String): Seq[(ValidatableField[E], E#NodeSeq)] = {
    fromString(str) match {
      case Right(value) =>
        currentValue = value
        _setter(currentValue)
        Nil
      case Left(error) =>
        List((this, implicitly[FSXmlSupport[E]].buildText(s"Could not parse value '$str': $error")))
    }
  }

  override def saveToString(): Option[String] = Some(toString(currentValue)).filter(_ != "")

  override def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => _setter(currentValue)
    case _ => Js.void
  })

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem = {
    if (!enabled()) <div style="display:none;" id={aroundId}></div>.asFSXml()
    else {
      withFieldRenderHints { implicit hints =>
        renderer.render(this)(
          _label().map(lbl => <label for={elemId}>{lbl}</label>.asFSXml()),
          processInputElem(<input type={inputType}
                   id={elemId}
                   onblur={fsc.callback(Js.elementValueById(elemId), str => {
                     fromString(str).foreach(currentValue = _)
                     form.onEvent(ChangedField(this)) &
                       Js.evalIf(hints.contains(ShowValidationsHint))(reRender()) // TODO: is this wrong? (running on the client side, but should be server?)
                   }).cmd}
                   onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {${Js.evalIf(hints.contains(SaveOnEnterHint))(Js.blur(elemId) & form.onSaveClientSide())}}"}
                   value={toString(currentValue)}
            />.asFSXml()).withAttrs(finalAdditionalAttrs: _*),
          errors().headOption.map(_._2)
        )
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F6Field[E], Boolean]): List[F6Field[E]] = if (predicate.applyOrElse[F6Field[E], Boolean](this, _ => false)) List(this) else Nil
}

class F6StringField[E <: FSXmlEnv]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: TextF6FieldRenderer[E]) extends F6TextField[E, String] {

  override def defaultValue: String = ""

  def toString(value: String): String = value

  def fromString(str: String): Either[String, String] = Right(str)

  override def errors(): Seq[(ValidatableField[E], E#NodeSeq)] = super.errors() ++
    (if (_required() && currentValue.trim == "") Seq((this, fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}


class F6StringOptField[E <: FSXmlEnv]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: TextF6FieldRenderer[E]) extends F6TextField[E, Option[String]] {

  override def defaultValue: Option[String] = None

  def toString(value: Option[String]): String = value.getOrElse("")

  def fromString(str: String): Either[String, Option[String]] = Right(Some(str).filter(_ != ""))

  override def errors(): Seq[(ValidatableField[E], E#NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6DateOptField[E <: FSXmlEnv]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: TextF6FieldRenderer[E]) extends F6TextField[E, Option[java.time.LocalDate]] {
  override def _inputTypeDefault: String = "date"

  override def defaultValue: Option[time.LocalDate] = None

  def toString(value: Option[java.time.LocalDate]): String = value.map(_.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).getOrElse("")

  def fromString(str: String): Either[String, Option[java.time.LocalDate]] = Right(Some(str).filter(_.trim != "").map(str => java.time.LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"))))

  override def errors(): Seq[(ValidatableField[E], E#NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6DateTimeOptField[E <: FSXmlEnv]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: TextF6FieldRenderer[E]) extends F6TextField[E, Option[java.time.LocalDateTime]] {
  override def _inputTypeDefault: String = "datetime-local"

  override def defaultValue: Option[LocalDateTime] = None

  def toString(value: Option[java.time.LocalDateTime]): String = value.map(_.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))).getOrElse("")

  def fromString(str: String): Either[String, Option[java.time.LocalDateTime]] = Right(Some(str).filter(_.trim != "").map(str => java.time.LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))))

  override def errors(): Seq[(ValidatableField[E], E#NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6DoubleOptField[E <: FSXmlEnv]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: TextF6FieldRenderer[E])
  extends F6TextField[E, Option[Double]]
    with F6FieldWithPrefix[E]
    with F6FieldWithSuffix[E]
    with F6FieldWithMin[E]
    with F6FieldWithStep[E]
    with F6FieldWithMax[E] {
  override def _inputTypeDefault: String = "number"

  override def defaultValue: Option[Double] = None

  def toString(value: Option[Double]): String = value.map(value => prefix + " " + new DecimalFormat("0.#").format(value) + " " + suffix).map(_.trim).getOrElse("")

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

  override def errors(): Seq[(ValidatableField[E], E#NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6IntOptField[E <: FSXmlEnv]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: TextF6FieldRenderer[E])
  extends F6TextField[E, Option[Int]]
    with F6FieldWithPrefix[E]
    with F6FieldWithSuffix[E]
    with F6FieldWithMin[E]
    with F6FieldWithStep[E]
    with F6FieldWithMax[E] {

  override def defaultValue: Option[Int] = None

  def toString(value: Option[Int]): String = value.map(value => prefix + " " + new DecimalFormat("0.#").format(value) + " " + suffix).map(_.trim).getOrElse("")

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

  override def errors(): Seq[(ValidatableField[E], E#NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F6TimeOfDayField[E <: FSXmlEnv]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: TextF6FieldRenderer[E])
  extends F6TextField[E, Option[Int]]
    with F6FieldWithPrefix[E]
    with F6FieldWithSuffix[E]
    with F6FieldWithMin[E]
    with F6FieldWithStep[E]
    with F6FieldWithMax[E] {

  override def defaultValue: Option[Int] = None

  override def errors(): Seq[(ValidatableField[E], E#NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())


  def toString(value: Option[Int]): String = value.map(value => DateTimeFormat.forPattern("HH:mm").print(new DateTime().withTime(value / 60, value % 60, 0, 0))).map(_.trim).getOrElse("")

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

class F6DoubleField[E <: FSXmlEnv]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: TextF6FieldRenderer[E])
  extends F6TextField[E, Double]
    with F6FieldWithPrefix[E]
    with F6FieldWithSuffix[E]
    with F6FieldWithMin[E]
    with F6FieldWithStep[E]
    with F6FieldWithMax[E] {

  override def defaultValue: Double = 0

  def toString(value: Double): String = (prefix + " " + value.formatted("%.2f") + " " + suffix).trim

  def fromString(str: String): Either[String, Double] = {
    str
      .toLowerCase
      .trim
      .replaceAll("^" + Pattern.quote(prefix.toLowerCase) + " *", "")
      .replaceAll(" *" + Pattern.quote(suffix.toLowerCase) + "$", "")
      .toDoubleOption match {
      case Some(value) => Right(value)
      case None => Left(s"Not a double?: $str")
    }
  }
}
