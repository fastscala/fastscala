package com.fastscala.components.bootstrap5.form7

import com.fastscala.components.bootstrap5.form7.layout.F7BSFormInputGroup
import com.fastscala.components.bootstrap5.form7.renderermodifiers.{CheckboxAlignment, CheckboxSide, CheckboxStyle}
import com.fastscala.components.form7.fields.multiselect.F7MultiSelectField
import com.fastscala.components.form7.fields.select.F7SelectField
import com.fastscala.components.form7.fields.text.F7StringField
import com.fastscala.components.form7.fields.text.F7StringTextareaField
import com.fastscala.components.form7.mixins.InputSize
import com.fastscala.components.form7.mixins.F7FieldWithValidation
import com.fastscala.components.form7.mixins.mainelem.F7FieldWithMainElem
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.{Elem, Node}

class BSInputSizeSpec extends AnyFlatSpec {

  private val renderers = new BSForm7Renderers()(using CheckboxAlignment.Vertical, CheckboxStyle.Checkbox, CheckboxSide.Normal) {
    override def defaultRequiredFieldLabel: String = "Required"
  }

  import renderers.*

  private def allElems(n: Node): Seq[Elem] = n match {
    case e: Elem => e +: e.child.flatMap(allElems)
    case _ => Nil
  }

  private def classesOf(e: Elem): Set[String] = Option(e.attributes.asAttrMap.getOrElse("class", "")).map(_.trim.split("\\s+").filter(_.nonEmpty).toSet).get

  /** The classes the standard Bootstrap renderer gives to the main element (`mainElem`) of `field`. */
  private def mainElemClasses(field: F7FieldWithMainElem & F7FieldWithValidation, mainElem: Elem): Set[String] = {
    val rendered = textFieldRenderer.render(field)(mainElem, None, None, None, None)
    classesOf(allElems(rendered).find(_.label == mainElem.label).get)
  }

  "F7FieldWithInputSize" should "default to the default size" in {
    assert(new F7StringField().inputSize == InputSize.Default)
  }

  it should "be set by each of the helpers" in {
    assert(new F7StringField().inputSizeXs.inputSize == InputSize.ExtraSmall)
    assert(new F7StringField().inputSizeSm.inputSize == InputSize.Small)
    assert(new F7StringField().inputSizeLg.inputSizeDefault.inputSize == InputSize.Default)
    assert(new F7StringField().inputSizeLg.inputSize == InputSize.Large)
    assert(new F7StringField().inputSizeXl.inputSize == InputSize.ExtraLarge)
    var size = InputSize.Small
    val field = new F7StringField().inputSize(() => size)
    assert(field.inputSize == InputSize.Small)
    size = InputSize.Large
    assert(field.inputSize == InputSize.Large)
  }

  "The Bootstrap 5 renderers" should "map ExtraSmall and Small to sm, Large and ExtraLarge to lg, and Default to no size class" in {
    val expected = Map(
      InputSize.ExtraSmall -> Some("form-control-sm"),
      InputSize.Small -> Some("form-control-sm"),
      InputSize.Default -> None,
      InputSize.Large -> Some("form-control-lg"),
      InputSize.ExtraLarge -> Some("form-control-lg"),
    )
    InputSize.values.foreach { size =>
      val classes = mainElemClasses(new F7StringField().inputSize(size), <input type="text"/>)
      assert(classes.contains("form-control"), classes)
      assert(classes.filter(_.startsWith("form-control-")) == expected(size).toSet, s"$size: $classes")
    }
  }

  it should "size inputs, textareas, selects and multi-selects" in {
    assert(mainElemClasses(new F7StringField().inputSizeSm, <input type="text"/>).contains("form-control-sm"))
    assert(mainElemClasses(new F7StringTextareaField().inputSizeLg, <textarea></textarea>).contains("form-control-lg"))
    assert(mainElemClasses(new F7SelectField[String](() => Seq("a", "b")).inputSizeXs, <select></select>).contains("form-control-sm"))
    assert(mainElemClasses(new F7MultiSelectField[String]().inputSizeXl, <select></select>).contains("form-control-lg"))
  }

  it should "keep the multi-select's size attribute separate from its input size" in {
    val field = new F7MultiSelectField[String]().size(5).inputSizeLg
    assert(field.size() == Some(5))
    assert(field.inputSize == InputSize.Large)
  }

  it should "size input groups: sm for ExtraSmall and Small, lg for Large and ExtraLarge, nothing for Default" in {
    val expected = Map(
      InputSize.ExtraSmall -> Some("input-group-sm"),
      InputSize.Small -> Some("input-group-sm"),
      InputSize.Default -> None,
      InputSize.Large -> Some("input-group-lg"),
      InputSize.ExtraLarge -> Some("input-group-lg"),
    )
    InputSize.values.foreach { size =>
      val group = new F7BSFormInputGroup()(new F7StringField()).inputSize(size)
      assert(group.inputSize == size)
      val rendered = inputGroupFieldRenderer.render(group)(Nil, Seq(<input type="text"/>), Nil, group.inputSize, showErrors = false)
      val classes = classesOf(allElems(rendered).find(_.attributes.asAttrMap.get("id").contains(group.inputGroupId)).get)
      assert(classes.contains("input-group"), classes)
      assert(classes.filter(_.startsWith("input-group-")) == expected(size).toSet, s"$size: $classes")
    }
    assert(new F7BSFormInputGroup()(new F7StringField()).inputSizeXs.inputSize == InputSize.ExtraSmall)
  }
}
