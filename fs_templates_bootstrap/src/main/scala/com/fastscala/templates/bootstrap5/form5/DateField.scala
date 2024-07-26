package com.fastscala.templates.bootstrap5.form5

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.utils.ImmediateInputFields
import com.fastscala.templates.form5.Form5
import com.fastscala.templates.form5.fields._
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem
import org.joda.time.{DateTime, LocalDate}

import java.util.Locale

trait DateFieldOptRenderer {

  import com.fastscala.core.FSXmlUtils._

  def defaultRequiredFieldLabel: String

  def render[E <: FSXmlEnv : FSXmlSupport](field: DateFieldOpt[E])(
    labelOpt: Option[E#Elem],
    yearSelectElem: E#Elem,
    monthSelectElem: E#Elem,
    daySelectElem: E#Elem,
    error: Option[E#NodeSeq]
  )(implicit hints: Seq[RenderHint]): E#Elem = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    div.withId(field.aroundId).apply {
      val showErrors = hints.contains(ShowValidationsHint)
      labelOpt.map(_.form_label.withFor(field.elemId)).getOrElse(Empty) ++
        input_group.withId(field.elemId).withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr) {
          yearSelectElem.withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr) ++
            monthSelectElem.withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr) ++
            daySelectElem.withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
        } ++
        error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(Empty)
    }
  }
}

class DateFieldOpt[E <: FSXmlEnv : FSXmlSupport](
                                                  get: () => Option[LocalDate]
                                                  , set: Option[LocalDate] => Js
                                                  , label: Option[String] = None
                                                  , val required: () => Boolean = () => false
                                                  , val disabled: () => Boolean = () => false
                                                  , val readOnly: () => Boolean = () => false
                                                  , val enabled: () => Boolean = () => true
                                                  , val deps: Set[FormField[_]] = Set()
                                                )(implicit renderer: DateFieldOptRenderer) extends StandardFormField[E] with ValidatableField[E] {

  def withLabel(v: String) = copy(label = Some(v))

  def copy(
            get: () => Option[LocalDate] = get
            , set: Option[LocalDate] => Js = set
            , label: Option[String] = label
            , required: () => Boolean = required
            , enabled: () => Boolean = enabled
            , deps: Set[FormField[_]] = deps
          )(implicit renderer: DateFieldOptRenderer): DateFieldOpt[E] = new DateFieldOpt[E](
    get = get
    , set = set
    , label = label
    , required = required
    , enabled = enabled
    , deps = deps
  )

  var currentYear: Option[Int] = get().map(_.getYear)
  var currentMonth: Option[Int] = get().map(_.getMonthOfYear)
  var currentDay: Option[Int] = get().map(_.getDayOfMonth)

  override def onEvent(event: FormEvent)(implicit form: Form5[E], fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case PerformSave => set(for (year <- currentYear; month <- currentMonth; day <- currentDay) yield new LocalDate(year, month, day))
    case _ => Js.void
  })

  override def hasErrors_?(): Boolean = required() && (currentYear.isEmpty || currentMonth.isEmpty || currentDay.isEmpty)

  override def errors(): Seq[(ValidatableField[E], E#NodeSeq)] =
    if (required() && (currentYear.isEmpty || currentMonth.isEmpty || currentDay.isEmpty))
      Seq((this, implicitly[FSXmlSupport[E]].buildText(renderer.defaultRequiredFieldLabel))) else Seq()

  def render()(implicit form: Form5[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem = {
    withFieldRenderHints { implicit hints =>
      val dayField = Js.rerenderable[E](rerenderer => implicit fsc => {
        def maxDays: Int = (for {
          year <- currentYear
          month <- currentMonth
        } yield {
          println("maxDays: " + new DateTime().withYear(year).withMonthOfYear(month).dayOfMonth().getMaximumValue)
          new DateTime().withYear(year).withMonthOfYear(month).dayOfMonth().getMaximumValue
        }).getOrElse(31)

        println("1 to maxDays: " + (1 to maxDays).toList)
        ImmediateInputFields.select[E, Option[Int]](
          () => None :: (1 to maxDays).toList.map(Some(_)),
          () => currentDay,
          value => {
            currentDay = value
            form.onEvent(ChangedField(this)) & (if (hints.contains(ShowValidationsHint)) reRender() else Js.void)
          },
          toString = _.map(_.toString).getOrElse("--"),
          elemId = rerenderer.aroundId
        )
      })
      val monthField = ImmediateInputFields.select[E, Option[Int]](
        () => None :: (1 to 12).toList.map(Some(_)),
        () => currentMonth,
        value => {
          currentMonth = value
          dayField.rerender() & form.onEvent(ChangedField(this)) & (if (hints.contains(ShowValidationsHint)) reRender() else Js.void)
        },
        toString = idx => idx.map(idx => DateTime.now().withMonthOfYear(idx).toString("MMMM", Locale.forLanguageTag("pt-PT"))).getOrElse("--")
      )
      val yearField = ImmediateInputFields.select[E, Option[Int]](
        () => None :: (1900 to DateTime.now.getYear).toList.reverse.map(Some(_)),
        () => currentYear,
        value => {
          currentYear = value
          dayField.rerender() & form.onEvent(ChangedField(this)) & (if (hints.contains(ShowValidationsHint)) reRender() else Js.void)
        },
        toString = _.map(_.toString).getOrElse("--")
      )
      renderer.render(this)(label.map(txt => <label>{txt}</label>.asFSXml()), yearField, monthField, dayField.render(), errors().headOption.map(_._2))
    }
  }

  override def fieldsMatching(predicate: PartialFunction[FormField[E], Boolean]): List[FormField[E]] = if (predicate(this)) List(this) else Nil
}
