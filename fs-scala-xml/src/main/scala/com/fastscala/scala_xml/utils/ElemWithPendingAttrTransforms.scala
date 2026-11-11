package com.fastscala.scala_xml.utils

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


object ElemWithPendingAttrTransforms {
  /** How many times the attributes of an [[ElemWithPendingAttrTransforms]] were computed; for tests. */
  private[scala_xml] val Computations = new java.util.concurrent.atomic.AtomicLong()

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