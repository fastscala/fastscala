package com.fastscala.utils

import com.fastscala.js.Js
import com.fastscala.utils
import com.fastscala.utils.NodeSeqUtils.MkNSFromNodeSeq

import scala.util.chaining.scalaUtilChainingOps
import scala.xml._

trait ElemTransformers {
  def elem: Elem

  def attributeTransform(attrName: String, transform: Option[String] => String): Elem = {

    def updateMetaData(
                        metaData: MetaData = Option(elem.attributes).getOrElse(Null),
                        found: Boolean = false
                      ): MetaData = metaData match {
      case Null if !found => new UnprefixedAttribute(attrName, transform(None), Null)
      case Null if found => Null
      case PrefixedAttribute((pre, key, value, next)) if key == attrName =>
        new PrefixedAttribute(pre, key, value match {
          case null => Seq(Text(transform(None)))
          case Seq(Text(value)) => Seq(Text(transform(Some(value))))
          case other => other
        }, updateMetaData(next, found = true))
      case UnprefixedAttribute((key, value, next)) if key == attrName =>
        new UnprefixedAttribute(key, value match {
          case null => Seq(Text(transform(None)))
          case Seq(Text(value)) => Seq(Text(transform(Some(value))))
          case other => other
        }, updateMetaData(next, found = true))
      case PrefixedAttribute((pre, key, value, next)) => new PrefixedAttribute(pre, key, value, updateMetaData(next, found))
      case UnprefixedAttribute((key, value, next)) => new UnprefixedAttribute(key, value, updateMetaData(next, found))
    }

    new Elem(elem.prefix, elem.label, updateMetaData(), elem.scope, elem.minimizeEmpty, elem.child: _*)
  }

  def addClass(`class`: String): Elem = attributeTransform("class", _.getOrElse("") + " " + `class`)

  def addOnClick(js: String): Elem = attributeTransform("onclick", _.getOrElse("") + ";" + js)

  def addOnClick(js: Js): Elem = addOnClick(js.cmd)

  def withClassIf(bool: Boolean, `class`: String): Elem = if (bool) addClass(`class`) else elem

  def withAttrIf(bool: Boolean, kv: (String, String)): Elem = if (bool) withAttr(kv) else elem

  def withStyle(style: String): Elem = attributeTransform("style", _.getOrElse("") + ";" + style)

  def withFor(`for`: String): Elem = attributeTransform("for", _ => `for`)

  def withType(`type`: String): Elem = attributeTransform("type", _ => `type`)

  def withValue(value: String): Elem = attributeTransform("value", _ => value)

  def withTitle(title: String): Elem = attributeTransform("title", _ => title)

  def withName(name: String): Elem = attributeTransform("name", _ => name)

  def withTypeSubmit(): Elem = attributeTransform("type", _ => "submit")

  def withAttr(name: String)(value: Option[String] => String): Elem = attributeTransform(name, value)

  def withAttr(kv: (String, String)): Elem = withAttr(kv._1)(_ => kv._2)

  def withAttrs(attrs: (String, String)*): Elem = attrs.foldLeft[Elem](elem)((acc, next) => new utils.ElemTransformers {
    override def elem: Elem = acc
  }.withAttr(next))

  def withRole(role: String): Elem = attributeTransform("role", _ => role)

  def withHref(href: String): Elem = attributeTransform("href", _ => href)

  def withRoleButton: Elem = withRole("button")

  def withId(id: String): Elem = attributeTransform("id", _ => id)

  def withIdIfNotSet(id: String): Elem = attributeTransform("id", existing => existing.getOrElse(id))

  def withPrependedToContents(value: NodeSeq): Elem = elem.copy(child = value ++ elem.child)

  def withAppendedToContents(value: NodeSeq): Elem = elem.copy(child = elem.child ++ value)

  def withContents(value: NodeSeq): Elem = apply(value)

  def apply(value: NodeSeq): Elem = elem.copy(child = value)

  def apply(values: NodeSeq*): Elem = elem.copy(child = values.mkNS)

  def apply(value: scala.xml.NodeBuffer): Elem = elem.copy(child = value)

  def apply(text: String): Elem = apply(scala.xml.Text(text))

  def showIf(bool: Boolean): NodeSeq = if (bool) elem else NodeSeq.Empty

  def getClassAttr: String = elem.attributes.get("class").map(_.map(_.toString()).mkString(" ")).getOrElse("")

  def getStyleAttr: String = elem.attributes.get("style").map(_.map(_.toString()).mkString(" ")).getOrElse("")

  def getId: Option[String] = elem.attributes.get("id").map(_.map(_.toString()).mkString(" "))
}

object ElemTransformers {

  implicit class RichElem(val elem: Elem) extends ElemTransformers

}
