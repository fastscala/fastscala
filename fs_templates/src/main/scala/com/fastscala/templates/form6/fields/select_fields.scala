package com.fastscala.templates.form6.fields

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem

trait F6FieldWithOptions[E <: FSXmlEnv, T] extends F6FieldMixin[E] {
  var _options: () => Seq[T] = () => Nil

  def options() = _options()

  def options(v: Seq[T]): this.type = mutate {
    _options = () => v
  }

  def options(f: () => Seq[T]): this.type = mutate {
    _options = f
  }
}

trait F6FieldWithOptionsNsLabel[E <: FSXmlEnv, T] extends F6FieldMixin[E] {

  implicit def fsXmlSupport: FSXmlSupport[E]

  var _option2NodeSeq: T => E#NodeSeq = opt => fsXmlSupport.buildText(opt.toString)

  def option2NodeSeq(f: T => E#NodeSeq): this.type = mutate {
    _option2NodeSeq = f
  }

  def option2String(f: T => String): this.type = mutate {
    _option2NodeSeq = opt => fsXmlSupport.buildText(f(opt))
  }
}

trait F6FieldWithOptionIds[E <: FSXmlEnv, T] extends F6FieldMixin[E] {
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

abstract class F6SelectFieldBase[E <: FSXmlEnv, T]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: SelectF6FieldRenderer[E]) extends StandardF6Field[E]
  with F6FieldWithOptions[E, T]
  with F6FieldWithOptionIds[E, T]
  with ValidatableField[E]
  with StringSerializableField[E]
  with FocusableF6Field[E]
  with F6FieldWithDisabled[E]
  with F6FieldWithRequired[E]
  with F6FieldWithReadOnly[E]
  with F6FieldWithEnabled[E]
  with F6FieldWithTabIndex[E]
  with F6FieldWithName[E]
  with F6FieldWithLabel[E]
  with F6FieldWithAdditionalAttrs[E]
  with F6FieldWithDependencies[E]
  with F6FieldWithValue[E, T]
  with F6FieldWithOptionsNsLabel[E, T] {

  override def loadFromString(str: String): Seq[(ValidatableField[E], E#NodeSeq)] = {
    val all = options()
    all.find({
      case opt => _option2Id(opt, all) == str
    }) match {
      case Some(v) =>
        currentValue = v
        _setter(v)
        Nil
      case None =>
        List((this, fsXmlSupport.buildText(s"Not found id: '$str'")))
    }
  }

  override def saveToString(): Option[String] = Some(_option2Id(currentValue, options())).filter(_ != "")

  override def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => _setter(currentValue)
    case _ => Js.void
  })

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem = {
    val renderedOptions = options()
    val ids2Option: Map[String, T] = renderedOptions.map(opt => fsc.session.nextID() -> opt).toMap
    val option2Id: Map[T, String] = ids2Option.map(_.swap)
    val optionsRendered = renderedOptions.map(opt => {
      renderer.renderOption(this)(currentValue == opt, option2Id(opt), _option2NodeSeq(opt))
    })

    val errorsAtRenderTime = errors()

    if (!enabled()) <div style="display:none;" id={aroundId}></div>.asFSXml()
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
          label.map(label => <label for={elemId}>{label}</label>.asFSXml()),
          processInputElem(<select
              name={name.getOrElse(null)}
              onblur={onchangeJs}
              onchange={onchangeJs}
              id={elemId}
            >{optionsRendered}</select>.asFSXml()),
          errorsAtRenderTime.headOption.map(_._2)
        )(hints)
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F6Field[E], Boolean]): List[F6Field[E]] = if (predicate.applyOrElse[F6Field[E], Boolean](this, _ => false)) List(this) else Nil
}

class F6SelectOptField[E <: FSXmlEnv, T]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: SelectF6FieldRenderer[E]) extends F6SelectFieldBase[E, Option[T]] {
  override def defaultValue: Option[T] = None

  def optionsValid(v: Seq[T]): F6SelectOptField.this.type = options(None +: v.map(Some(_)))
}

class F6SelectField[E <: FSXmlEnv, T](options: Seq[T])(implicit fsXmlSupport: FSXmlSupport[E], renderer: SelectF6FieldRenderer[E]) extends F6SelectFieldBase[E, T] {
  options(options)

  override def defaultValue: T = options.head
}

abstract class F6MultiSelectFieldBase[E <: FSXmlEnv, T]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: MultiSelectF6FieldRenderer[E]) extends StandardF6Field[E]
  with F6FieldWithOptions[E, T]
  with F6FieldWithOptionIds[E, T]
  with ValidatableField[E]
  with StringSerializableField[E]
  with FocusableF6Field[E]
  with F6FieldWithDisabled[E]
  with F6FieldWithRequired[E]
  with F6FieldWithReadOnly[E]
  with F6FieldWithEnabled[E]
  with F6FieldWithTabIndex[E]
  with F6FieldWithName[E]
  with F6FieldWithSize[E]
  with F6FieldWithLabel[E]
  with F6FieldWithAdditionalAttrs[E]
  with F6FieldWithDependencies[E]
  with F6FieldWithValue[E, Set[T]]
  with F6FieldWithOptionsNsLabel[E, T] {

  override def loadFromString(str: String): Seq[(ValidatableField[E], E#NodeSeq)] = {
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

  override def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => _setter(currentValue)
    case _ => Js.void
  })

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  override def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem = {
    val renderedOptions = options()
    val ids2Option: Map[String, T] = renderedOptions.map(opt => fsc.session.nextID() -> opt).toMap
    val option2Id: Map[T, String] = ids2Option.map(_.swap)
    val optionsRendered = renderedOptions.map(opt => {
      renderer.renderOption(this)(currentValue == opt, option2Id(opt), _option2NodeSeq(opt))
    })

    val errorsAtRenderTime = errors()

    if (!enabled()) <div style="display:none;" id={aroundId}></div>.asFSXml()
    else {
      withFieldRenderHints { hints =>
        val onchangeJs = fsc.callback(Js.selectedValues(Js.elementById(elemId)), {
          case ids =>
            currentValue = ids.split(",").toSet.map(id => ids2Option(id))
            form.onEvent(ChangedField(this)(hints)) &
              (if (hints.contains(ShowValidationsHint) || errorsAtRenderTime.nonEmpty || errors().nonEmpty) reRender()(form, fsc, hints) else Js.void)
        }).cmd
        renderer.render(this)(
          label.map(label => <label for={elemId}>{label}</label>.asFSXml()),
          processInputElem(
            <select
              multiple="multiple"
              name={name.getOrElse(null)}
              onblur={onchangeJs}
              onchange={onchangeJs}
              id={elemId}
            >{optionsRendered}</select>.asFSXml()),
          errorsAtRenderTime.headOption.map(_._2)
        )(hints)
      }
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F6Field[E], Boolean]): List[F6Field[E]] = if (predicate.applyOrElse[F6Field[E], Boolean](this, _ => false)) List(this) else Nil
}

class F6MultiSelectField[E <: FSXmlEnv, T]()(implicit fsXmlSupport: FSXmlSupport[E], renderer: MultiSelectF6FieldRenderer[E]) extends F6MultiSelectFieldBase[E, T] {
  override def defaultValue: Set[T] = Set()
}