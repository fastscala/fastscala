package com.fastscala.scala_xml

import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromNodeSeq
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.utils.ElemOptimized
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers

import java.lang.reflect.Modifier
import java.util.regex.Pattern
import scala.collection.immutable.{AbstractSeq, LinearSeq}
import scala.xml.*


/**
 * [[ScalaXmlElemUtilsV2]], but building [[ElemOptimized]] (attributes as a newest-first list of (name, value))
 * instead of [[ElemWithFastAttrsV2]]; kept for comparison (see XmlStreamTablePerfSpec in fs-components).
 */
trait ScalaXmlElemUtilsV1 extends Any {
  def elem: Elem

  /** Appends `value` to attribute `name` (creating it if missing). */
  private inline def attributeConcat(name: String, value: String): ElemOptimized =
    attributeTransform(name, _ + value)

  /** Sets attribute `name` to `value`; a null `value` removes it. */
  private inline def attributeSet(name: String, value: String): ElemOptimized =
    attributeTransform(name, _ => value)

  /** Removes attribute `name` (nothing happens if it's missing). */
  private inline def attributeRemove(name: String): ElemOptimized =
    attributeTransform(name, _ => null)

  /** `transform` gets the current value (`""` if missing); returning `null` removes the attribute. */
  private inline def attributeTransform(name: String, transform: String => String): ElemOptimized = elem match {
    case elem: ElemOptimized => new ElemOptimized(elem, name, transform)
    case elem => new ElemOptimized(elem, name, transform)
  }

  def addClass(`class`: String): Elem = if (`class` == "") elem else attributeConcat("class", " " + `class`)

  def withClass(`class`: String): Elem = attributeSet("class", `class`)

  def addClassIf(bool: Boolean, `class`: String): Elem = if (bool) addClass(`class`) else elem

  def removeClass(`class`: String): Elem = attributeTransform("class", _.replaceAll("(?i)" + Pattern.quote(`class`), ""))

  def addOnClick(js: String): Elem = attributeConcat("onclick", ";" + js)

  def addOnClick(js: Js): Elem = addOnClick(js.cmd)

  def withAttrIf(bool: Boolean, kv: (String, String)): Elem = if (bool) withAttr(kv) else elem

  def addStyle(style: String): Elem = if (style == "") elem else attributeConcat("style", ";" + style)

  def withStyle(style: String): Elem = attributeSet("style", style)

  def withFor(id: String): Elem = attributeSet("for", id)

  def withType(`type`: String): Elem = attributeSet("type", `type`)

  def withValue(value: String): Elem = attributeSet("value", value)

  def withTitle(title: String): Elem = attributeSet("title", title)

  def withName(name: String): Elem = attributeSet("name", name)

  def withTypeSubmit(): Elem = attributeSet("type", "submit")

  def withAttr(name: String)(value: String => String): Elem = attributeTransform(name, value)

  def withAttr(kv: (String, String)): Elem = attributeSet(kv._1, kv._2)

  /**
   * Sets each attribute, in a single pass over the attributes, creating one new [[Elem]]. Names should be unique
   * within one call: for a repeated name the resulting value is undefined. A later call overrides an earlier one
   * (`withAttrs("a" -> "1").withAttrs("a" -> "3")` gives `a="3"`). Attribute order is not preserved.
   */
  def withAttrs(attrs: (String, String)*): Elem = elem match {
    case elem if attrs.isEmpty => elem
    case elem: ElemOptimized => new ElemOptimized(elem, setAll(attrs))
    case elem => new ElemOptimized(elem, setAll(attrs))
  }

  /** As [[attributeSet]] for each pair: a null value removes it. */
  private def setAll(attrs: Seq[(String, String)]): Seq[(String, String => String)] =
    attrs.map({ case (name, value) => name -> ((_: String) => value) })

  def withRole(role: String): Elem = attributeSet("role", role)

  def withHref(href: String): Elem = attributeSet("href", href)

  def withRoleButton: Elem = withRole("button")

  def withRoleAlert: Elem = withRole("alert")

  def withId(id: String): Elem = attributeSet("id", id)

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

  // ---------- getters: read an ElemOptimized's attrs directly, instead of computing its MetaData ----------

  /** The (unescaped) value of attribute `name`; `plain` (the lookup on `elem.attributes`) if `elem` isn't fast. */
  private inline def fastAttr(name: String, inline plain: Option[String]): Option[String] = elem match {
    // attrs is newest first and may repeat a name: its first entry counts, and a null value there means removed
    case fast: ElemOptimized => fast.attrs.find(_._1 == name).flatMap(kv => Option(kv._2))
    case _ => plain
  }

  /** The attributes as (name, unescaped value), in `elem.attributes`' order; null if `elem` isn't fast. */
  private def fastAttrList: List[(String, String)] = elem match {
    case fast: ElemOptimized =>
      // The first entry per name, removed ones dropped; prepending gives the MetaData's order (reverse of attrs).
      var seen = Set.empty[String]
      var result: List[(String, String)] = Nil
      var rest = fast.attrs
      while (rest.nonEmpty) {
        val (name, value) = rest.head
        if (!seen.contains(name)) {
          seen += name
          if (value ne null) result = (name, value) :: result
        }
        rest = rest.tail
      }
      result
    case _ => null
  }

  // getAttrs and hasAttribute use the values as `toString` prints them, i.e. escaped (`a &amp; b`); the fast paths
  // escape the same way, so the result doesn't depend on the element's class.

  def getAttrs: List[(String, String)] = fastAttrList match {
    case null => elem.attributes.toList.map(a => a.key -> a.value.map(_.toString()).mkString(" "))
    case attrs => attrs.map(kv => kv._1 -> Utility.escape(kv._2))
  }

  def hasAttribute(name: String, value: String): Boolean = fastAttrList match {
    case null => elem.attributes.exists(a => a.key == name && a.value.map(_.toString()).mkString(" ") == value)
    case _ => fastAttr(name, None).exists(v => Utility.escape(v) == value)
  }

  // Attribute values are returned unescaped (`.text`): `title="a &amp; b"` gives "a & b".

  def attr(name: String): Option[String] = fastAttr(name, elem.attributes.find(a => a.key == name).map(_.value.text))

  def getClassAttr: String = fastAttr("class", elem.attributes.get("class").map(_.text)).getOrElse("")

  def getStyleAttr: String = fastAttr("style", elem.attributes.get("style").map(_.text)).getOrElse("")

  def getId: String = getIdOpt.get

  def getIdOpt: Option[String] = fastAttr("id", elem.attributes.get("id").map(_.text))
}

