package com.fastscala.scala_xml

import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromNodeSeq
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.utils.ElemWithFastAttrsV2
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers

import java.lang.reflect.Modifier
import java.util.regex.Pattern
import scala.collection.immutable.{AbstractSeq, LinearSeq}
import scala.xml.*


trait ScalaXmlElemUtils extends Any {
  def elem: Elem

  /** `transform` gets the current value (`""` if missing); returning `null` removes the attribute. */
  def attributeTransform(name: String, transform: String => String): ElemWithFastAttrsV2 = elem match {
    case elem: ElemWithFastAttrsV2 => new ElemWithFastAttrsV2(elem, name, transform)
    case elem => new ElemWithFastAttrsV2(elem, name, transform)
  }

  def addClass(`class`: String): Elem = if (`class` == "") elem else attributeTransform("class", _ + " " + `class`)

  def withClass(`class`: String): Elem = attributeTransform("class", _ => `class`)

  def addClassIf(bool: Boolean, `class`: String): Elem = if (bool) addClass(`class`) else elem

  def removeClass(`class`: String): Elem = attributeTransform("class", _.replaceAll("(?i)" + Pattern.quote(`class`), ""))

  def addOnClick(js: String): Elem = attributeTransform("onclick", _ + ";" + js)

  def addOnClick(js: Js): Elem = addOnClick(js.cmd)

  def withAttrIf(bool: Boolean, kv: (String, String)): Elem = if (bool) withAttr(kv) else elem

  def addStyle(style: String): Elem = if (style == "") elem else attributeTransform("style", _ + ";" + style)

  def withStyle(style: String): Elem = attributeTransform("style", _ => style)

  def withFor(id: String): Elem = attributeTransform("for", _ => id)

  def withType(`type`: String): Elem = attributeTransform("type", _ => `type`)

  def withValue(value: String): Elem = attributeTransform("value", _ => value)

  def withTitle(title: String): Elem = attributeTransform("title", _ => title)

  def withName(name: String): Elem = attributeTransform("name", _ => name)

  def withTypeSubmit(): Elem = attributeTransform("type", _ => "submit")

  def withAttr(name: String)(value: String => String): Elem = attributeTransform(name, value)

  def withAttr(kv: (String, String)): Elem = withAttr(kv._1)(_ => kv._2)

  /**
   * Sets each attribute, in a single pass over the attributes, creating one new [[Elem]]. Names should be unique
   * within one call: for a repeated name the resulting value is undefined. A later call overrides an earlier one
   * (`withAttrs("a" -> "1").withAttrs("a" -> "3")` gives `a="3"`). Attribute order is not preserved.
   */
  def withAttrs(attrs: (String, String)*): Elem = elem match {
    case elem if attrs.isEmpty => elem
    case elem: ElemWithFastAttrsV2 => new ElemWithFastAttrsV2(elem, attrs.map(kv => kv._1 -> ((_: String) => kv._2)))
    case elem => new ElemWithFastAttrsV2(elem, attrs.map(kv => kv._1 -> ((_: String) => kv._2)))
  }

  def withRole(role: String): Elem = attributeTransform("role", _ => role)

  def withHref(href: String): Elem = attributeTransform("href", _ => href)

  def withRoleButton: Elem = withRole("button")

  def withRoleAlert: Elem = withRole("alert")

  def withId(id: String): Elem = attributeTransform("id", _ => id)

  def withIdIfNotSet(id: String): Elem = attributeTransform("id", existing => if (existing == "") id else existing)

  def withPrependedToContents(value: NodeSeq): Elem = elem.copy(child = value ++ elem.child)

  def withAppendedToContents(value: NodeSeq): Elem = elem.copy(child = elem.child ++ value)

  def withContents(value: NodeSeq): Elem = apply(value)

  def withContents(value: Elem): Elem = apply(value)

  def apply(value: NodeSeq): Elem = elem.copy(child = value)

  def apply(values: NodeSeq*): Elem = elem.copy(child = values.mkNS)

  def apply(value: scala.xml.NodeBuffer): Elem = elem.copy(child = value)

  def apply(text: String): Elem = apply(scala.xml.Text(text))

  def showIf(bool: Boolean): NodeSeq = if (bool) elem else NodeSeq.Empty

  def getAttrs: List[(String, String)] = elem.attributes.toList.map(a => a.key -> a.value.map(_.toString()).mkString(" "))

  def hasAttribute(name: String, value: String): Boolean = elem.attributes.exists(a => a.key == name && a.value.map(_.toString()).mkString(" ") == value)

  // Attribute values are returned unescaped (`.text`): `title="a &amp; b"` gives "a & b".

  def attr(name: String): Option[String] = elem.attributes.find(a => a.key == name).map(_.value.text)

  def getClassAttr: String = elem.attributes.get("class").map(_.text).getOrElse("")

  def getStyleAttr: String = elem.attributes.get("style").map(_.text).getOrElse("")

  def getId: String = getIdOpt.get

  def getIdOpt: Option[String] = elem.attributes.get("id").map(_.text)
}

object ScalaXmlElemUtils {

  implicit class RichElem(val elem: Elem) extends AnyVal with ScalaXmlElemUtils

  def showIf(b: Boolean)(ns: => NodeSeq): NodeSeq = if (b) ns else NodeSeq.Empty
}
