package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.Form7
import com.fastscala.xml.scala_xml.FSScalaXmlSupport

import scala.xml.{Elem, NodeSeq}

trait F7FieldWithOptions[T] extends F7DefaultField {
  var _options: () => Seq[T] = () => Nil

  def options() = _options()

  def options(v: Seq[T]): this.type = mutate {
    _options = () => v
  }

  def options(f: () => Seq[T]): this.type = mutate {
    _options = f
  }
}

trait F7FieldWithOptionsNsLabel[T] extends F7DefaultField {

  var _option2NodeSeq: T => NodeSeq = opt => FSScalaXmlSupport.fsXmlSupport.buildText(opt.toString)

  def option2NodeSeq(f: T => NodeSeq): this.type = mutate {
    _option2NodeSeq = f
  }

  def option2String(f: T => String): this.type = mutate {
    _option2NodeSeq = opt => FSScalaXmlSupport.fsXmlSupport.buildText(f(opt))
  }
}

trait F7FieldWithOptionIds[T] extends F7DefaultField {
  var _option2Id: (T, Seq[T]) => String = (opt, options) => "%X".formatted(options.indexOf(opt).toString)

  var _id2Option: (String, Seq[T]) => Option[T] = (id, options) => id.toIntOption.map(idx => options(idx))

  def option2Id(f: (T, Seq[T]) => String): this.type = mutate {
    _option2Id = f
  }

  def id2Option(f: (String, Seq[T]) => Option[T]): this.type = mutate {
    _id2Option = f
  }

  def optionIdsFromIdentityHashCode(): this.type = mutate {
    _option2Id = (opt, options) => "%X".format(math.abs(System.identityHashCode(opt)))
    _id2Option = (id, options) => options.find(opt => _option2Id(opt, options) == id)
  }
}

abstract class F7SelectFieldBase[T]()(implicit renderer: SelectF7FieldRenderer) extends StandardF7Field
  with F7FieldWithOptions[T]
  with F7FieldWithOptionIds[T]
  with ValidatableF7Field
  with StringSerializableF7Field
  with FocusableF7Field
  with F7FieldWithDisabled
  with F7FieldWithRequired
  with F7FieldWithReadOnly
  with F7FieldWithEnabled
  with F7FieldWithTabIndex
  with F7FieldWithName
  with F7FieldWithLabel
  with F7FieldWithAdditionalAttrs
  with F7FieldWithDependencies
  with F7FieldWithValue[T]
  with F7FieldWithOptionsNsLabel[T] {

  override def loadFromString(str: String): Seq[(ValidatableF7Field, NodeSeq)] = {
    val all = options()
    all.find({
      case opt => _option2Id(opt, all) == str
    }) match {
      case Some(v) =>
        currentValue = v
        _setter(v)
        Nil
      case None =>
        List((this, FSScalaXmlSupport.fsXmlSupport.buildText(s"Not found id: '$str'")))
    }
  }

  override def saveToString(): Option[String] = Some(_option2Id(currentValue, options())).filter(_ != "")

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case Submit => _setter(currentValue)
    case _ => Js.void
  })

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    val renderedOptions = options()
    val ids2Option: Map[String, T] = renderedOptions.map(opt => fsc.session.nextID() -> opt).toMap
    val option2Id: Map[T, String] = ids2Option.map(_.swap)
    val optionsRendered = renderedOptions.map(opt => {
      renderer.renderOption(this)(currentValue == opt, option2Id(opt), _option2NodeSeq(opt))
    })

    val errorsAtRenderTime = errors()

    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { hints =>
        val onchangeJs = fsc.callback(Js.elementValueById(elemId), {
          case id =>
            ids2Option.get(id).map(value => {
              currentValue = value
              form.onEvent(ChangedField(this)(hints))(form, fsc)
            }).getOrElse(Js.void) &
              (if (hints.contains(ShowValidationsHint) || errorsAtRenderTime.nonEmpty || errors().nonEmpty) reRender()(form, fsc, hints) else Js.void)
        }).cmd
        renderer.render(this)(
          label.map(label => <label for={elemId}>{label}</label>),
          processInputElem(<select
              name={name.getOrElse(null)}
              onblur={onchangeJs}
              onchange={onchangeJs}
              id={elemId}
            >{optionsRendered}</select>),
          errorsAtRenderTime.headOption.map(_._2)
        )(hints)
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}

class F7SelectOptField[T]()(implicit renderer: SelectF7FieldRenderer) extends F7SelectFieldBase[Option[T]] {
  override def defaultValue: Option[T] = None

  def optionsNonEmpty(v: Seq[T]): F7SelectOptField.this.type = options(None +: v.map(Some(_)))

  override def errors(): Seq[(ValidatableF7Field, NodeSeq)] = super.errors() ++
    (if (required() && currentValue.isEmpty) Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}

class F7SelectField[T](opts: () => Seq[T])(implicit renderer: SelectF7FieldRenderer) extends F7SelectFieldBase[T] with F7FieldWithValidations {
  options(opts)

  def this(opts: Seq[T])(implicit renderer: SelectF7FieldRenderer) = this(() => opts)

  override def defaultValue: T = options.head
}

abstract class F7MultiSelectFieldBase[T]()(implicit renderer: MultiSelectF7FieldRenderer) extends StandardF7Field
  with F7FieldWithOptions[T]
  with F7FieldWithOptionIds[T]
  with ValidatableF7Field
  with StringSerializableF7Field
  with FocusableF7Field
  with F7FieldWithDisabled
  with F7FieldWithRequired
  with F7FieldWithReadOnly
  with F7FieldWithEnabled
  with F7FieldWithTabIndex
  with F7FieldWithName
  with F7FieldWithSize
  with F7FieldWithLabel
  with F7FieldWithAdditionalAttrs
  with F7FieldWithDependencies
  with F7FieldWithValue[Set[T]]
  with F7FieldWithOptionsNsLabel[T] {

  override def loadFromString(str: String): Seq[(ValidatableF7Field, NodeSeq)] = {
    val all = options()
    val id2Option: Map[String, T] = all.map(opt => _option2Id(opt, all) -> opt).toMap
    val selected: Seq[T] = str.split(";").toList.flatMap(id => {
      id2Option.get(id)
    })
    currentValue = selected.toSet
    _setter(currentValue)
    Nil
  }

  override def saveToString(): Option[String] = Some(currentValue.map(opt => _option2Id(opt, options())).mkString(";"))

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case Submit => _setter(currentValue)
    case _ => Js.void
  })

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  override def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    val renderedOptions = options()
    val ids2Option: Map[String, T] = renderedOptions.map(opt => fsc.session.nextID() -> opt).toMap
    val option2Id: Map[T, String] = ids2Option.map(_.swap)
    val optionsRendered = renderedOptions.map(opt => {
      renderer.renderOption(this)(currentValue.contains(opt), option2Id(opt), _option2NodeSeq(opt))
    })

    val errorsAtRenderTime = errors()

    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { hints =>
        val onchangeJs = fsc.callback(Js.selectedValues(Js.elementById(elemId)), {
          case ids =>
            currentValue = ids.split(",").filter(_.trim != "").toSet[String].map(id => ids2Option(id))
            form.onEvent(ChangedField(this)(hints)) &
              (if (hints.contains(ShowValidationsHint) || errorsAtRenderTime.nonEmpty || errors().nonEmpty) reRender()(form, fsc, hints) & Js.focus(elemId) else Js.void)
        }).cmd
        renderer.render(this)(
          label.map(label => <label for={elemId}>{label}</label>),
          processInputElem(
            <select
              multiple="multiple"
              name={name.getOrElse(null)}
              onblur={onchangeJs}
              onchange={onchangeJs}
              id={elemId}
            >{optionsRendered}</select>),
          errorsAtRenderTime.headOption.map(_._2)
        )(hints)
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}

class F7MultiSelectField[T]()(implicit renderer: MultiSelectF7FieldRenderer) extends F7MultiSelectFieldBase[T] {
  override def defaultValue: Set[T] = Set()
}
