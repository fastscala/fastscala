package com.fastscala.components.form7

import com.fastscala.components.bootstrap5.form7.BSForm7Renderers
import com.fastscala.components.bootstrap5.form7.renderermodifiers.{CheckboxAlignment, CheckboxSide, CheckboxStyle}
import com.fastscala.components.form7.fields.color.F7ColorField
import com.fastscala.components.form7.fields.date.{F7LocalDateField, F7LocalDateOptField}
import com.fastscala.components.form7.fields.datetimelocal.F7LocalDateTimeOptField
import com.fastscala.components.form7.fields.number.*
import com.fastscala.components.form7.fields.select.F7SelectField
import com.fastscala.components.form7.fields.text.{F7StringField, F7StringOptField, F7TimeOfDayField}
import org.scalatest.flatspec.AnyFlatSpec

import java.time.{LocalDate, LocalDateTime}
import scala.xml.Text

/**
 * Form7's fields' values, without rendering: to and from the String in the input (with the prefix and suffix), loaded
 * and saved (as QueryStringSavedForm does), and validated.
 */
class F7FieldsValuesSpec extends AnyFlatSpec {

  private val renderers = new BSForm7Renderers()(using CheckboxAlignment.Vertical, CheckboxStyle.Checkbox, CheckboxSide.Normal) {
    override def defaultRequiredFieldLabel: String = "Required"
  }

  import renderers.*

  "F7StringField / F7StringOptField" should "be the String as it is ('' is None for the Opt one)" in {
    val f = new F7StringField()
    assert(f.fromString(" a b ") == Right(" a b "))
    assert(f.toString("x") == "x")
    val o = new F7StringOptField()
    assert(o.fromString("") == Right(None))
    assert(o.fromString("a") == Right(Some("a")))
    assert(o.toString(None) == "" && o.toString(Some("a")) == "a")
  }

  "F7IntField" should "parse ints, with or without its prefix and suffix (any case), and refuse the rest" in {
    val f = new F7IntField().prefix("€").suffix("Units")
    assert(f.toString(5) == "€ 5 Units")
    assert(f.fromString("€ 5 Units") == Right(5))
    assert(f.fromString(" 5 units ") == Right(5))
    assert(f.fromString("5") == Right(5))
    assert(f.fromString("-12") == Right(-12))
    assert(f.fromString("5.5").isLeft)
    assert(f.fromString("").isLeft)
    assert(new F7IntField().toString(7) == "7")
    assert(new F7IntField().defaultValue == 0)
  }

  "F7IntOptField" should "be None for blank, and the int otherwise" in {
    val f = new F7IntOptField().suffix("kg")
    assert(f.fromString("  ") == Right(None))
    assert(f.fromString("70 kg") == Right(Some(70)))
    assert(f.toString(Some(70)) == "70 kg")
    assert(f.toString(None) == "")
    assert(f.fromString("seventy").isLeft)
    assert(new F7IntOptField().toString(Some(1234567)) == "1234567")
  }

  "F7LongField / F7LongOptField" should "parse longs" in {
    assert(new F7LongField().fromString("9000000000") == Right(9000000000L))
    assert(new F7LongField().fromString("x").isLeft)
    assert(new F7LongOptField().fromString("") == Right(None))
    assert(new F7LongOptField().fromString("9000000000") == Right(Some(9000000000L)))
    assert(new F7LongOptField().toString(Some(9000000000L)) == "9000000000")
  }

  "F7DoubleField / F7DoubleOptField" should "parse doubles, and write them back as they parse" in {
    val f = new F7DoubleField().prefix("$")
    assert(f.fromString("$ 1.5") == Right(1.5))
    assert(f.fromString("1e3") == Right(1000.0))
    assert(f.fromString("1,5").isLeft)
    for (v <- Seq(0.1, -2.5, 1e10, 123456.789)) assert(f.fromString(f.toString(v)) == Right(v), v)
    val o = new F7DoubleOptField()
    assert(o.fromString("") == Right(None))
    assert(o.fromString(o.toString(Some(3.25))) == Right(Some(3.25)))
  }

  "F7TimeOfDayField" should "be minutes from midnight, as HH:mm" in {
    val f = new F7TimeOfDayField()
    assert(f.toString(Some(9 * 60 + 5)) == "09:05")
    assert(f.toString(None) == "")
    assert(f.fromString("09:05") == Right(Some(545)))
    assert(f.fromString("23:59") == Right(Some(23 * 60 + 59)))
    assert(f.fromString("") == Right(None))
    assert(f.fromString("25:00").isLeft)
    assert(f.fromString("noon").isLeft)
  }

  "F7LocalDateField" should "be yyyy-MM-dd, and refuse anything else" in {
    val f = new F7LocalDateField(LocalDate.of(2000, 1, 1))
    assert(f.defaultValue == LocalDate.of(2000, 1, 1))
    assert(f.toString(LocalDate.of(2026, 11, 5)) == "2026-11-05")
    assert(f.fromString("2026-11-05") == Right(LocalDate.of(2026, 11, 5)))
    assert(f.fromString("05/11/2026").isLeft)
  }

  "F7LocalDateOptField" should "be None for blank, and the date otherwise" in {
    val f = new F7LocalDateOptField()
    assert(f.fromString("") == Right(None))
    assert(f.fromString("2026-02-28") == Right(Some(LocalDate.of(2026, 2, 28))))
    assert(f.toString(Some(LocalDate.of(2026, 2, 28))) == "2026-02-28")
  }

  it should "refuse an invalid date (not throw)" in pendingUntilFixed {
    // BUG: LocalDate.parse's exception isn't caught: fromString throws instead of returning a Left.
    assert(new F7LocalDateOptField().fromString("2026-02-30").isLeft)
  }

  "F7LocalDateOptField.asString" should "read and write the date as a String, in the pattern given" in {
    var stored: Option[String] = Some("15/11/2026")
    val f = F7LocalDateOptField.asString(stored, stored = _, pattern = "dd/MM/yyyy")
    assert(f.get() == Some(LocalDate.of(2026, 11, 15)))
    f.set(Some(LocalDate.of(2027, 1, 2)))
    assert(stored == Some("02/01/2027"))
  }

  "F7LocalDateTimeOptField" should "be yyyy-MM-ddTHH:mm" in {
    val f = new F7LocalDateTimeOptField()
    assert(f.toString(Some(LocalDateTime.of(2026, 11, 15, 18, 0))) == "2026-11-15T18:00")
    assert(f.fromString("2026-11-15T18:00") == Right(Some(LocalDateTime.of(2026, 11, 15, 18, 0))))
    assert(f.fromString(" ") == Right(None))
  }

  it should "refuse an invalid date-time (not throw)" in pendingUntilFixed {
    // BUG: as F7LocalDateOptField's: LocalDateTime.parse's exception isn't caught.
    assert(new F7LocalDateTimeOptField().fromString("tomorrow").isLeft)
  }

  "F7ColorField" should "default to black, and be the String" in {
    val f = new F7ColorField()
    assert(f.defaultValue == "#000000")
    assert(f.toString(" #ff0000 ") == "#ff0000")
    assert(f.fromString("#00ff00") == Right("#00ff00"))
  }

  "loadFromString / saveToString" should "set the value (and the variable it's bound to), and give it back as a String" in {
    var age = 30
    val f = new F7IntField().rw(age, age = _)
    assert(f.saveToString().contains("30"))
    assert(f.loadFromString("41").isEmpty)
    assert(f.currentValue == 41 && age == 41)
    assert(f.saveToString().contains("41"))
  }

  it should "report what can't be parsed, keeping the value" in {
    val f = new F7IntField()
    f.loadFromString("12")
    val errors = f.loadFromString("twelve")
    assert(errors.size == 1 && (errors.head._1 eq f) && errors.head._2.text.contains("twelve"))
    assert(f.currentValue == 12)
  }

  it should "save nothing for an empty value" in {
    val f = new F7StringOptField()
    assert(f.saveToString().isEmpty)
    f.loadFromString("x")
    assert(f.saveToString().contains("x"))
  }

  "The internal value" should "be the default until set" in {
    val f = new F7StringField()
    assert(f.get() == "")
    f.set("v")
    assert(f.getInternalValue() == "v")
    val g = new F7IntField().setInternalValue(5)
    assert(g.get() == 5)
  }

  "getter / setter" should "replace where the value is read from and written to" in {
    var v = "a"
    val f = new F7StringField().getter(() => v).setter(nv => { v = nv; com.fastscala.js.Js.Void })
    assert(f.get() == "a" && f.currentValue == "a")
    f.set("b")
    assert(v == "b")
  }

  "validate" should "be the errors of the validations added (in order), each with its field" in {
    val f = new F7IntField()
      .addValidation(() => f2Error(f2 = false))
      .addValidation((field: F7IntField) => field.currentValue >= 0, (_: F7IntField) => Text("must be >= 0"))
      .addValidation((field: F7IntField) => if (field.currentValue > 100) Some(Text("too big")) else None)
    f.loadFromString("5")
    assert(f.validate().isEmpty)
    f.loadFromString("-1")
    assert(f.validate().map(_._2.text) == Seq("must be >= 0"))
    f.loadFromString("500")
    assert(f.validate().map(_._2.text) == Seq("too big"))
    assert(f.validate().forall(_._1 eq f))
  }

  private def f2Error(f2: Boolean) = if (f2) Some(Text("never")) else None

  "A required Opt field" should "be invalid without a value (with the renderer's label)" in {
    val f = new F7StringOptField().required(true)
    assert(f.validate().map(_._2.text) == Seq("Required"))
    f.loadFromString("x")
    assert(f.validate().isEmpty)
    val notRequired = new F7StringOptField()
    assert(notRequired.validate().isEmpty)
  }

  "F7SelectField" should "default to the first option, and save/load an option by its id" in {
    val f = new F7SelectField[String](Seq("red", "green", "blue"))
    assert(f.defaultValue == "red" && f.currentValue == "red")
    assert(f.saveToString().isEmpty) // the default isn't saved
    f.currentValue = "blue"
    val saved = f.saveToString()
    assert(saved.isDefined)
    val g = new F7SelectField[String](Seq("red", "green", "blue"))
    assert(g.loadFromString(saved.get).isEmpty)
    assert(g.currentValue == "blue")
  }

  it should "report an id that's not one of its options" in {
    val f = new F7SelectField[String](Seq("a"))
    val errors = f.loadFromString("no-such-id")
    assert(errors.size == 1 && errors.head._2.text.contains("no-such-id"))
    assert(f.currentValue == "a")
  }
}
