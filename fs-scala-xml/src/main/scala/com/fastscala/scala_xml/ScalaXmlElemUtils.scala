package com.fastscala.scala_xml

import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromNodeSeq
import com.fastscala.scala_xml.js.JS

import java.util.regex.Pattern
import scala.xml.*

trait ScalaXmlElemUtils extends Any {
  def elem: Elem

  def attributeTransform(attrName: String, transform: Option[String] => String): Elem = {

    // The existing value as text (e.g. `Text("a"), EntityRef("amp")` gives "a&"), so values made of several nodes are
    // transformed too, instead of being left as they were.
    def newValue(value: Seq[Node]): Seq[Node] =
      Seq(Text(transform(if (value == null) None else Some(value.text))))

    def updateMetaData(metaData: MetaData = Option(elem.attributes).getOrElse(Null), found: Boolean = false): MetaData = metaData match {
      case Null if !found => new UnprefixedAttribute(attrName, transform(None), Null)
      case Null if found => Null
      case PrefixedAttribute((pre, key, value, next)) if key == attrName =>
        new PrefixedAttribute(
          pre,
          key,
          newValue(value),
          updateMetaData(next, found = true)
        )
      case UnprefixedAttribute((key, value, next)) if key == attrName =>
        new UnprefixedAttribute(
          key,
          newValue(value),
          updateMetaData(next, found = true)
        )
      case PrefixedAttribute((pre, key, value, next)) => new PrefixedAttribute(pre, key, value, updateMetaData(next, found))
      case UnprefixedAttribute((key, value, next)) => new UnprefixedAttribute(key, value, updateMetaData(next, found))
    }

    new Elem(elem.prefix, elem.label, updateMetaData(), elem.scope, elem.minimizeEmpty, elem.child *)
  }

  def addClass(`class`: String): Elem = if (`class` == "") elem else attributeTransform("class", _.getOrElse("") + " " + `class`)

  def withClass(`class`: String): Elem = attributeTransform("class", _ => `class`)

  def addClassIf(bool: Boolean, `class`: String): Elem = if (bool) addClass(`class`) else elem

  def removeClass(`class`: String): Elem = attributeTransform("class", _.getOrElse("").replaceAll("(?i)" + Pattern.quote(`class`), ""))

  def addOnClick(js: String): Elem = attributeTransform("onclick", _.getOrElse("") + ";" + js)

  def addOnClick(js: Js): Elem = addOnClick(js.cmd)

  def withAttrIf(bool: Boolean, kv: (String, String)): Elem = if (bool) withAttr(kv) else elem

  def addStyle(style: String): Elem = if (style == "") elem else attributeTransform("style", _.map(_ + ";").getOrElse("") + style)

  def withStyle(style: String): Elem = attributeTransform("style", _ => style)

  def withFor(id: String): Elem = attributeTransform("for", _ => id)

  def withType(`type`: String): Elem = attributeTransform("type", _ => `type`)

  def withValue(value: String): Elem = attributeTransform("value", _ => value)

  def withTitle(title: String): Elem = attributeTransform("title", _ => title)

  def withName(name: String): Elem = attributeTransform("name", _ => name)

  def withTypeSubmit(): Elem = attributeTransform("type", _ => "submit")

  def withAttr(name: String)(value: Option[String] => String): Elem = attributeTransform(name, value)

  def withAttr(kv: (String, String)): Elem = withAttr(kv._1)(_ => kv._2)

  /**
   * Same result as applying [[withAttr]] for each pair in order (the last value for a name wins; existing attributes
   * keep their position; new ones are appended in order of first appearance), but in a single pass over the
   * attributes, creating one new [[Elem]].
   */
  def withAttrs(attrs: (String, String)*): Elem = if (attrs.isEmpty) elem else {
    val values = collection.mutable.LinkedHashMap.empty[String, String]
    attrs.foreach((k, v) => values(k) = v)
    val found = collection.mutable.HashSet.empty[String]

    // Existing attributes, in order, with matching values replaced:
    val updated = collection.mutable.ArrayBuffer.empty[MetaData]
    var md: MetaData = Option(elem.attributes).getOrElse(Null)
    while (md ne Null) {
      updated += (values.get(md.key) match {
        case Some(newValue) =>
          found += md.key
          md match {
            case p: PrefixedAttribute => new PrefixedAttribute(p.pre, p.key, Seq(Text(newValue)), Null)
            case _ => new UnprefixedAttribute(md.key, Seq(Text(newValue)), Null)
          }
        case None => md
      })
      md = md.next
    }

    // Rebuild the chain back to front, appending the attributes that didn't exist yet:
    var result: MetaData = Null
    values.toSeq.reverseIterator.foreach((k, v) => if (!found(k)) result = new UnprefixedAttribute(k, v, result))
    updated.reverseIterator.foreach(a => result = a.copy(result))

    new Elem(elem.prefix, elem.label, result, elem.scope, elem.minimizeEmpty, elem.child *)
  }

  def withRole(role: String): Elem = attributeTransform("role", _ => role)

  def withHref(href: String): Elem = attributeTransform("href", _ => href)

  def withRoleButton: Elem = withRole("button")

  def withRoleAlert: Elem = withRole("alert")

  def withId(id: String): Elem = attributeTransform("id", _ => id)

  def withIdIfNotSet(id: String): Elem = attributeTransform("id", existing => existing.getOrElse(id))

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
