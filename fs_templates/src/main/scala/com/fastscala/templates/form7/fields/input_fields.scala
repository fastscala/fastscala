package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.Form7
import com.fastscala.utils.Lazy
import com.fastscala.xml.scala_xml.FSScalaXmlSupport
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import java.text.DecimalFormat
import java.time
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import scala.util.chaining.scalaUtilChainingOps
import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, NodeSeq}

trait F7FieldInputFieldMixin extends F7FieldWithValidations with F7DefaultField {

  def processInputElem(input: Elem): Elem = input
}

trait F7FieldWithValue[T] extends F7DefaultField {

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

trait F7FieldWithDisabled extends F7FieldInputFieldMixin {
  var _disabled: () => Boolean = () => false

  def disabled() = _disabled()

  def isDisabled: this.type = disabled(true)

  def isNotDisabled: this.type = disabled(false)

  def disabled(v: Boolean): this.type = mutate {
    _disabled = () => v
  }

  def disabled(f: () => Boolean): this.type = mutate({
    _disabled = f
  })

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    if (_disabled()) input.withAttr("disabled", "disabled") else input
  }
}

trait F7FieldWithRequired extends F7FieldInputFieldMixin {
  var _required: () => Boolean = () => false

  def required() = _required()

  def isRequired: this.type = required(true)

  def isNotRequired: this.type = required(false)

  def required(v: Boolean): this.type = mutate {
    _required = () => v
  }

  def required(f: () => Boolean): this.type = mutate {
    _required = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    if (_required()) input.withAttr("required", "true") else input
  }
}

trait F7FieldWithReadOnly extends F7FieldInputFieldMixin {
  var _readOnly: () => Boolean = () => false

  def readOnly() = _readOnly()

  def isReadOnly: this.type = readOnly(true)

  def isNotReadOnly: this.type = readOnly(false)

  def readOnly(v: Boolean): this.type = mutate {
    _readOnly = () => v
  }

  def readOnly(f: () => Boolean): this.type = mutate {
    _readOnly = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    if (_readOnly()) input.withAttr("readonly", "true") else input
  }
}

trait F7FieldWithEnabled extends F7FieldInputFieldMixin {
  var _enabled: () => Boolean = () => true

  def enabled(): Boolean = _enabled()

  def isEnabled: this.type = enabled(true)

  def isNotEnabled: this.type = enabled(false)

  def enabled(v: Boolean): this.type = mutate {
    _enabled = () => v
  }

  def enabled(f: () => Boolean): this.type = mutate {
    _enabled = f
  }
}

trait F7FieldWithTabIndex extends F7FieldInputFieldMixin {
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

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _tabIndex().map(tabIndex => input.withAttr("tabindex", tabIndex.toString)).getOrElse(input)
  }
}

trait F7FieldWithName extends F7FieldInputFieldMixin {
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

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _name().map(name => input.withAttr("name", name)).getOrElse(input)
  }
}

trait F7FieldWithSize extends F7FieldInputFieldMixin {
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

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _size().map(size => input.withAttr("size", size.toString)).getOrElse(input)
  }
}

trait F7FieldWithPlaceholder extends F7FieldInputFieldMixin {
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

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _placeholder().map(placeholder => input.withAttr("placeholder", placeholder)).getOrElse(input)
  }
}

trait F7FieldWithLabel extends F7FieldInputFieldMixin {
  var _label: () => Option[NodeSeq] = () => None

  def label() = _label()

  def label(v: Option[NodeSeq]): this.type = mutate {
    _label = () => v
  }

  def label(v: NodeSeq): this.type = mutate {
    _label = () => Some(v)
  }

  def label(v: String): this.type = mutate {
    _label = () => Some(FSScalaXmlSupport.fsXmlSupport.buildText(v))
  }

  def labelNodeSeqF(f: () => Option[NodeSeq]): this.type = mutate {
    _label = f
  }

  def labelStrF(f: () => String): this.type = mutate {
    _label = () => Some(<span>{f()}</span>)
  }

  def withLabel(label: String): this.type = mutate {
    _label = () => Some(<span>{label}</span>)
  }

  def withLabel(label: Elem): this.type = mutate {
    _label = () => Some(label)
  }
}

trait F7FieldWithMaxlength extends F7FieldInputFieldMixin {
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

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _maxlength().map(maxlength => input.withAttr("maxlength", maxlength.toString)).getOrElse(input)
  }
}

trait F7FieldWithInputType extends F7FieldInputFieldMixin {
  def _inputTypeDefault: String = "text"

  var _inputType: () => String = () => _inputTypeDefault

  def inputType() = _inputType()

  def inputType(v: String): this.type = mutate {
    _inputType = () => v
  }

  def inputType(f: () => String): this.type = mutate {
    _inputType = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    input.withAttr("type", _inputType())
  }
}

trait F7FieldWithAdditionalAttrs extends F7FieldInputFieldMixin {
  var _additionalAttrs: () => Seq[(String, String)] = () => Nil

  def additionalAttrs() = _additionalAttrs()

  def additionalAttrs(v: Seq[(String, String)]): this.type = mutate {
    _additionalAttrs = () => v
  }

  def additionalAttrs(f: () => Seq[(String, String)]): this.type = mutate {
    _additionalAttrs = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    input.withAttrs(_additionalAttrs(): _*)
  }
}

trait F7FieldWithDependencies extends F7FieldInputFieldMixin {
  var _deps: () => Set[F7Field] = () => Set()

  def deps() = _deps()

  def deps(v: F7Field*): this.type = deps(v.toSet)

  def deps(v: Set[F7Field]): this.type = mutate {
    _deps = () => v
  }

  def deps(f: () => Set[F7Field]): this.type = mutate {
    _deps = f
  }
}

trait F7FieldWithPrefix extends F7DefaultField {

  var _prefix: () => String = () => ""

  def prefix() = _prefix()

  def prefix(v: String): this.type = mutate {
    _prefix = () => v
  }

  def prefix(f: () => String): this.type = mutate {
    _prefix = f
  }
}

trait F7FieldWithSuffix extends F7DefaultField {

  var _suffix: () => String = () => ""

  def suffix() = _suffix()

  def suffix(v: String): this.type = mutate {
    _suffix = () => v
  }

  def suffix(f: () => String): this.type = mutate {
    _suffix = f
  }
}

trait F7FieldWithMin extends F7FieldInputFieldMixin {
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

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _min().map(min => input.withAttr("min", min)).getOrElse(input)
  }
}

trait F7FieldWithStep extends F7FieldInputFieldMixin {
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

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _step().map(step => input.withAttr("step", step.toString)).getOrElse(input)
  }
}

trait F7FieldWithMax extends F7FieldInputFieldMixin {
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

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _max().map(max => input.withAttr("max", max)).getOrElse(input)
  }
}

abstract class F7TextField[T]()(implicit renderer: TextF7FieldRenderer) extends StandardF7Field
  with ValidatableF7Field
  with StringSerializableF7Field
  with FocusableF7Field
  with F7FieldWithDisabled
  with F7FieldWithRequired
  with F7FieldWithReadOnly
  with F7FieldWithEnabled
  with F7FieldWithTabIndex
  with F7FieldWithName
  with F7FieldWithPlaceholder
  with F7FieldWithLabel
  with F7FieldWithMaxlength
  with F7FieldWithInputType
  with F7FieldWithAdditionalAttrs
  with F7FieldWithDependencies
  with F7FieldWithValue[T] {

  def toString(value: T): String

  def fromString(str: String): Either[String, T]

  override def loadFromString(str: String): Seq[(ValidatableF7Field, NodeSeq)] = {
    fromString(str) match {
      case Right(value) =>
        currentValue = value
        _setter(currentValue)
        Nil
      case Left(error) =>
        List((this, FSScalaXmlSupport.fsXmlSupport.buildText(s"Could not parse value '$str': $error")))
    }
  }

  override def saveToString(): Option[String] = Some(toString(currentValue)).filter(_ != "")

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case Submit => _setter(currentValue)
    case _ => Js.void
  })

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        renderer.render(this)(
          _label().map(lbl => <label for={elemId}>{lbl}</label>),
          processInputElem(<input type={inputType}
                   id={elemId}
                   onblur={fsc.callback(Js.elementValueById(elemId), str => {
                     fromString(str).foreach(currentValue = _)
                     form.onEvent(ChangedField(this)) &
                       Js.evalIf(hints.contains(ShowValidationsHint))(reRender()) // TODO: is this wrong? (running on the client side, but should be server?)
                   }).cmd}
                   onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {${Js.evalIf(hints.contains(SaveOnEnterHint))(Js.blur(elemId) & form.onSaveClientSide())}}"}
                   value={toString(currentValue)}
            />).withAttrs(finalAdditionalAttrs: _*),
          errors().headOption.map(_._2)
        )
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}

class F7StringField()(implicit renderer: TextF7FieldRenderer) extends F7TextField[String] {

  override def defaultValue: String = ""

  def toString(value: String): String = value

  def fromString(str: String): Either[String, String] = Right(str)

  override def errors(): Seq[(ValidatableF7Field, NodeSeq)] = super.errors() ++
    (if (_required() && currentValue.trim == "") Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}


class F7StringOptField()(implicit renderer: TextF7FieldRenderer) extends F7TextField[Option[String]] {

  override def defaultValue: Option[String] = None

  def toString(value: Option[String]): String = value.getOrElse("")

  def fromString(str: String): Either[String, Option[String]] = Right(Some(str).filter(_ != ""))

  override def errors(): Seq[(ValidatableF7Field, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

object F7LocalDateOptField {
  def apply(
             get: => Option[String]
             , set: Option[String] => Unit
             , pattern: String = "yyyy-MM-dd"
           )(implicit renderer: TextF7FieldRenderer): F7LocalDateOptField = new F7LocalDateOptField().rw(
    get.map(date => java.time.LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern))),
    dateOpt => set(dateOpt.map(_.format(DateTimeFormatter.ofPattern(pattern))))
  )
}

class F7LocalDateOptField()(implicit renderer: TextF7FieldRenderer) extends F7TextField[Option[java.time.LocalDate]] {
  override def _inputTypeDefault: String = "date"

  override def defaultValue: Option[time.LocalDate] = None

  def toString(value: Option[java.time.LocalDate]): String = value.map(_.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).getOrElse("")

  def fromString(str: String): Either[String, Option[java.time.LocalDate]] = Right(Some(str).filter(_.trim != "").map(str => java.time.LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"))))

  override def errors(): Seq[(ValidatableF7Field, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F7LocalDateField(dflt: time.LocalDate = time.LocalDate.now())(implicit renderer: TextF7FieldRenderer) extends F7TextField[java.time.LocalDate] {
  override def _inputTypeDefault: String = "date"

  override def defaultValue: time.LocalDate = dflt

  def toString(value: java.time.LocalDate): String = value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

  def fromString(str: String): Either[String, java.time.LocalDate] = scala.util.Try(java.time.LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"))).toEither.left.map(_ => "Invalid input")
}

class F7LocalDateTimeOptField()(implicit renderer: TextF7FieldRenderer) extends F7TextField[Option[java.time.LocalDateTime]] {
  override def _inputTypeDefault: String = "datetime-local"

  override def defaultValue: Option[LocalDateTime] = None

  def toString(value: Option[java.time.LocalDateTime]): String = value.map(_.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))).getOrElse("")

  def fromString(str: String): Either[String, Option[java.time.LocalDateTime]] = Right(Some(str).filter(_.trim != "").map(str => java.time.LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))))

  override def errors(): Seq[(ValidatableF7Field, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F7DoubleOptField()(implicit renderer: TextF7FieldRenderer)
  extends F7TextField[Option[Double]]
    with F7FieldWithPrefix
    with F7FieldWithSuffix
    with F7FieldWithMin
    with F7FieldWithStep
    with F7FieldWithMax {
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

  override def errors(): Seq[(ValidatableF7Field, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F7IntOptField()(implicit renderer: TextF7FieldRenderer)
  extends F7TextField[Option[Int]]
    with F7FieldWithPrefix
    with F7FieldWithSuffix
    with F7FieldWithMin
    with F7FieldWithStep
    with F7FieldWithMax {

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

  override def errors(): Seq[(ValidatableF7Field, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F7TimeOfDayField()(implicit renderer: TextF7FieldRenderer)
  extends F7TextField[Option[Int]]
    with F7FieldWithPrefix
    with F7FieldWithSuffix
    with F7FieldWithMin
    with F7FieldWithStep
    with F7FieldWithMax {

  override def defaultValue: Option[Int] = None

  override def errors(): Seq[(ValidatableF7Field, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())


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

class F7DoubleField()(implicit renderer: TextF7FieldRenderer)
  extends F7TextField[Double]
    with F7FieldWithPrefix
    with F7FieldWithSuffix
    with F7FieldWithMin
    with F7FieldWithStep
    with F7FieldWithMax {

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
