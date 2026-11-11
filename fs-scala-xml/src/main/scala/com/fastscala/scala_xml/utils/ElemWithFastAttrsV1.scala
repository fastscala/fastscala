package com.fastscala.scala_xml.utils

import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtilsV2.RichElem
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromNodeSeq
import com.fastscala.scala_xml.js.JS
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers

import java.lang.invoke.{MethodHandle, MethodHandles, MethodType}
import java.lang.reflect.Modifier
import scala.annotation.static
import java.util.regex.Pattern
import scala.collection.immutable.{AbstractSeq, LinearSeq, ListMap}
import scala.xml.*


object ElemWithFastAttrsV1 {
  /** How many times the attributes of an [[ElemWithFastAttrsV1]] were computed; for tests. */
  @static private[scala_xml] val Computations = new java.util.concurrent.atomic.AtomicLong()

  inline def textOfValue(value: Seq[Node]): String = value match {
    case null => ""
    case Seq(Text(value)) => value
    case value => value.text
  }

  class Interceptor {

    // A single Interceptor is shared by every generated instance (ByteBuddy keeps it in a static field),
    // so per-instance state, like the cache, must live in fields of the generated class, not here.
    def intercept(
                   @net.bytebuddy.implementation.bind.annotation.This self: AnyRef,
                   @net.bytebuddy.implementation.bind.annotation.FieldValue("attrs") attrs: List[(String, String)],
                   @net.bytebuddy.implementation.bind.annotation.FieldValue("cached") cached: MetaData
                 ): MetaData = {
      if (cached ne null) cached
      else if (attrs eq null) Null // called before the fields were set: don't cache
      else {
        // Benign race: concurrent callers may compute twice, but the result is the same.
        ElemWithFastAttrsV1.Computations.incrementAndGet()
        val computed = attrs.foldLeft((Null: MetaData, Set[String]()))({
          case ((cur, seenAttrs), (name, value)) if seenAttrs.contains(name) => (cur, seenAttrs)
          case ((cur, seenAttrs), (name, null)) => (cur, seenAttrs + name)
          case ((cur, seenAttrs), (name, value)) => (new UnprefixedAttribute(name, value, cur), seenAttrs + name)
        })._1
        ElemWithFastAttrsV1.SetCached.invokeExact(self, computed): Unit
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

  @static private val interceptor = new Interceptor()

  @static val UnprefixedAttributeClass: Class[? <: UnprefixedAttribute] = new ByteBuddy()
    .subclass(classOf[UnprefixedAttribute])
    .defineField("attrs", classOf[List[(String, String)]], Modifier.PUBLIC)
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

  // MethodHandles instead of reflection (Constructor.newInstance / Field.set check access and look things up on every
  // call). @static makes them static final fields, which the JIT treats as constants and can inline through.
  // Each is adapted (asType) to the exact types used at its invokeExact call site.
  @static private val lookup = MethodHandles.lookup()

  /** (key: String, value: Seq[Node], next: MetaData) => new UnprefixedAttributeClass */
  @static val NewAttribute: MethodHandle = lookup
    .findConstructor(UnprefixedAttributeClass, MethodType.methodType(classOf[Unit], classOf[String], classOf[scala.collection.Seq[Node]], classOf[MetaData]))
    .asType(MethodType.methodType(classOf[UnprefixedAttribute], classOf[String], classOf[scala.collection.Seq[Node]], classOf[MetaData]))

  private def setter(name: String, owner: Class[?], tpe: Class[?]): MethodHandle =
    lookup.unreflectSetter(UnprefixedAttributeClass.getField(name)).asType(MethodType.methodType(Void.TYPE, owner, tpe))

  @static val SetAttrs: MethodHandle = setter("attrs", classOf[UnprefixedAttribute], classOf[List[?]])
  @static val SetCached: MethodHandle = setter("cached", classOf[AnyRef], classOf[MetaData])
}

/**
 * An [[Elem]] whose attributes are kept as a list of (name, value), only turned into [[MetaData]] (once, cached) when
 * read. Transforms are `String => String`: a missing attribute is seen as `""`, and returning `null` removes it.
 *
 * @param orig  the element whose attributes these replace; never itself an [[ElemWithFastAttrsV1]], so a chain of
 *              transforms doesn't nest wrappers and computes the attributes only once
 * @param attrs in reverse print order, names unique; a null value is a removed attribute
 */
class ElemWithFastAttrsV1 private(
                                   val attrs: List[(String, String)],
                                   prefix: String,
                                   label: String,
                                   scope: NamespaceBinding,
                                   minimizeEmpty: Boolean,
                                   child: Node*
                                 ) extends Elem(prefix, label, Null, scope, minimizeEmpty, child *) {

  def this(orig: Elem, name: String, transform: String => String) = this({
    var found = false
    val updated = orig.attributes.iterator.map(md => {
      if (md.key == name) {
        found = true
        md.key -> transform(ElemWithFastAttrsV1.textOfValue(md.value))
      } else md.key -> ElemWithFastAttrsV1.textOfValue(md.value)
    }).toList
    if (found) updated else (name -> transform("")) :: updated
  },
    orig.prefix,
    orig.label,
    orig.scope,
    orig.minimizeEmpty,
    orig.child *
  )

  def this(orig: Elem, transforms: Seq[(String, String => String)]) = this(({
    var found = Set[String]()
    val transformsByKey = transforms.toMap
    val updated = orig.attributes.iterator.map(md => {
      if (transformsByKey.contains(md.key)) {
        found += md.key
        md.key -> transformsByKey(md.key)(ElemWithFastAttrsV1.textOfValue(md.value))
      } else md.key -> ElemWithFastAttrsV1.textOfValue(md.value)
    }).toList
    if (found.size == transforms.size) updated
    else transformsByKey.removedAll(found).toList.map(kv => kv._1 -> kv._2("")) ::: updated // last one per name wins
  }: List[(String, String)]),
    orig.prefix,
    orig.label,
    orig.scope,
    orig.minimizeEmpty,
    orig.child *
  )

  def this(orig: ElemWithFastAttrsV1, name: String, transform: String => String) = this(orig.attrs match {
    case (`name`, value) :: rest =>
      val updated = transform(value)
      if (updated ne null) (name, transform(value)) :: rest
      else rest
    case Nil =>
      val value = transform("")
      if (value ne null) List((name, transform("")))
      else Nil
    case attrs =>
      val found = attrs.find(_._1 == name)
      val updated = transform(found.getOrElse(("", ""))._2)
      if (updated ne null) {
        if (found.isDefined) (name -> updated) :: attrs.filter(_._1 != name)
        else (name -> updated) :: attrs
      } else {
        if (found.isDefined) attrs.filter(_._1 != name)
        else attrs
      }
  },
    orig.prefix,
    orig.label,
    orig.scope,
    orig.minimizeEmpty,
    orig.child *
  )

  def this(orig: ElemWithFastAttrsV1, transforms: Seq[(String, String => String)]) = this(
    {
      var found = Set[String]()
      val transformsByKey = transforms.toMap
      val updated = orig.attrs.collect({
        case (key, value) if transformsByKey.contains(key) && !found.contains(key) =>
          found += key
          key -> transformsByKey(key)(value)
      }) ::: orig.attrs
      transformsByKey.removedAll(found).toList.map(kv => kv._1 -> kv._2.apply("")) ::: updated
    },
    orig.prefix,
    orig.label,
    orig.scope,
    orig.minimizeEmpty,
    orig.child *
  )

  override val attributes: MetaData = {
    val ins = ElemWithFastAttrsV1.NewAttribute.invokeExact("", null: scala.collection.Seq[Node], Null: MetaData): UnprefixedAttribute
    ElemWithFastAttrsV1.SetAttrs.invokeExact(ins, attrs): Unit
    ins
  }
}