package com.fastscala.scala_xml

import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromNodeSeq
import com.fastscala.scala_xml.js.JS
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers

import java.lang.reflect.Modifier
import java.util.regex.Pattern
import scala.collection.immutable.{AbstractSeq, LinearSeq, ListMap}
import scala.xml.*

class Interceptor {

  def textOfValue(value: Seq[Node]): Option[String] = value match {
    case null => None
    case Seq(Text(value)) => Some(value)
    case value => Some(value.text)
  }

  def updateMetaData(metaData: MetaData, remaining: Map[String, List[Option[String] => String]]): MetaData = metaData match {
    case Null =>
      // Create attributes that didn't exist yet, in the order they were first transformed (remaining is a ListMap):
      remaining.toList.foldRight(Null: MetaData)({
        case ((key, transforms), acc) =>
          new UnprefixedAttribute(key, transforms.reverse.foldLeft(Option.empty[String])((cur, next) => Some(next(cur))).get, acc)
      })
    case PrefixedAttribute((pre, key, value, next)) if remaining.contains(key) =>
      val attrValue: String = remaining(key).reverse.foldLeft(textOfValue(value))((cur, next) => Some(next(cur))).get
      new PrefixedAttribute(pre, key, attrValue, updateMetaData(next, remaining - key))
    case UnprefixedAttribute((key, value, next)) if remaining.contains(key) =>
      val attrValue: String = remaining(key).reverse.foldLeft(textOfValue(value))((cur, next) => Some(next(cur))).get
      new UnprefixedAttribute(key, attrValue, updateMetaData(next, remaining - key))
    case PrefixedAttribute((pre, key, value, next)) => new PrefixedAttribute(pre, key, value, updateMetaData(next, remaining))
    case UnprefixedAttribute((key, value, next)) => new UnprefixedAttribute(key, value, updateMetaData(next, remaining))
  }

  // A single Interceptor is shared by every generated instance (ByteBuddy keeps it in a static field),
  // so per-instance state, like the cache, must live in fields of the generated class, not here.
  def intercept(
                 @net.bytebuddy.implementation.bind.annotation.This self: AnyRef,
                 @net.bytebuddy.implementation.bind.annotation.FieldValue("orig") orig: Elem,
                 @net.bytebuddy.implementation.bind.annotation.FieldValue("pending") pending: Map[String, List[Option[String] => String]],
                 @net.bytebuddy.implementation.bind.annotation.FieldValue("cached") cached: MetaData
               ): MetaData = {
    if (cached ne null) cached
    else if ((orig eq null) || (pending eq null)) Null // called before the fields were set: don't cache
    else {
      // Benign race: concurrent callers may compute twice, but the result is the same.
      ElemWithPendingAttrTransforms.Computations.incrementAndGet()
      val computed = updateMetaData(orig.attributes, pending)
      ElemWithPendingAttrTransforms.FieldCached.set(self, computed)
      computed
    }
  }

  // The placeholder (key "", value null) heads the attribute list, with the computed attributes as its `next`.
  // scala-xml already skips null-valued attributes when iterating, but these two would still count/print it:

  /** MetaData.buildString appends ' ' before each attribute, even a null-valued one: print only the real ones. */
  def buildString(
                   @net.bytebuddy.implementation.bind.annotation.This self: MetaData,
                   @net.bytebuddy.implementation.bind.annotation.Argument(0) sb: StringBuilder
                 ): StringBuilder = self.next.buildString(sb)

  /** MetaData.length counts every attribute, even a null-valued one. */
  def length(
              @net.bytebuddy.implementation.bind.annotation.This self: MetaData,
              @net.bytebuddy.implementation.bind.annotation.Argument(0) i: Int
            ): Int = self.next.length(i)
}

object ElemWithPendingAttrTransforms {
  /** How many times the attributes of an [[ElemWithPendingAttrTransforms]] were computed; for tests. */
  private[scala_xml] val Computations = new java.util.concurrent.atomic.AtomicLong()

  private val interceptor = new Interceptor()

  val UnprefixedAttributeClass = new ByteBuddy()
    .subclass(classOf[UnprefixedAttribute])
    .defineField("pending", classOf[Map[String, List[Option[String] => String]]], Modifier.PUBLIC)
    .defineField("orig", classOf[Elem], Modifier.PUBLIC)
    .defineField("cached", classOf[MetaData], Modifier.PUBLIC)
    .method(ElementMatchers.named("next").and(ElementMatchers.takesArguments(0)))
    .intercept(MethodDelegation.withDefaultConfiguration().filter(ElementMatchers.named("intercept")).to(interceptor))
    .method(ElementMatchers.named("buildString").and(ElementMatchers.takesArguments(classOf[StringBuilder])))
    .intercept(MethodDelegation.withDefaultConfiguration().filter(ElementMatchers.named("buildString")).to(interceptor))
    .method(ElementMatchers.named("length").and(ElementMatchers.takesArguments(classOf[Int])))
    .intercept(MethodDelegation.withDefaultConfiguration().filter(ElementMatchers.named("length")).to(interceptor))
    .make()
    .load(getClass.getClassLoader)
    .getLoaded
  val FieldPending = ElemWithPendingAttrTransforms.UnprefixedAttributeClass.getField("pending")
  val FieldOrig = ElemWithPendingAttrTransforms.UnprefixedAttributeClass.getField("orig")
  val FieldCached = ElemWithPendingAttrTransforms.UnprefixedAttributeClass.getField("cached")
}

/**
 * @param orig the element the transforms apply to; never itself an [[ElemWithPendingAttrTransforms]] (see
 *             [[ScalaXmlElemUtils.attributeTransform]]), so a chain of transforms computes the attributes only once.
 */
class ElemWithPendingAttrTransforms(
                                     val orig: Elem,
                                     val pending: ListMap[String, List[Option[String] => String]]
                                   ) extends Elem(
  orig.prefix,
  orig.label,
  Null,
  orig.scope,
  orig.minimizeEmpty,
  orig.child *
) {

  override val attributes: MetaData = {
    val ins = ElemWithPendingAttrTransforms.UnprefixedAttributeClass
      .getDeclaredConstructor(classOf[String], classOf[scala.collection.Seq[Node]], classOf[MetaData])
      .newInstance("", null, Null)
    ElemWithPendingAttrTransforms.FieldOrig.set(ins, orig)
    ElemWithPendingAttrTransforms.FieldPending.set(ins, pending)
    ins
  }
}

trait ScalaXmlElemUtils extends Any {
  def elem: Elem

  def attributeTransform(name: String, transform: Option[String] => String): ElemWithPendingAttrTransforms = elem match {
    case elem: ElemWithPendingAttrTransforms =>
      // Wrap the original element, not `elem`: elem's transforms are already in elem.pending, and wrapping elem
      // would compute its attributes too and then apply its transforms a second time.
      new ElemWithPendingAttrTransforms(elem.orig, elem.pending.updated(name, transform :: elem.pending.getOrElse(name, Nil)))
    case elem => new ElemWithPendingAttrTransforms(elem, ListMap(name -> List(transform)))
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
  def withAttrs(attrs: (String, String)*): Elem = {
    val transforms = attrs.map({
      case (k, v) => k -> List(((_: Option[String]) => v))
    })
    elem match {
      case elem: ElemWithPendingAttrTransforms => new ElemWithPendingAttrTransforms(elem.orig, elem.pending ++ transforms)
      case elem => new ElemWithPendingAttrTransforms(elem, ListMap(transforms *))
    }
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
