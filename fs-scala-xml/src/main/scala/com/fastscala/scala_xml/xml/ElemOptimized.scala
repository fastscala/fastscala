package com.fastscala.scala_xml.xml

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers

import java.lang.invoke.{MethodHandle, MethodHandles, MethodType}
import java.lang.reflect.Modifier
import java.util.regex.Pattern
import scala.annotation.{static, tailrec}
import scala.collection.immutable.{AbstractSeq, LinearSeq, ListMap}
import scala.xml.*


object ElemOptimized {

  /**
   * An attribute's value: a String, or a [[Js]] (e.g. an `onclick`), kept as it is, so it isn't turned into one String
   * until it's needed (see [[textOf]]), and [[XmlJsStream]] can write it part by part.
   */
  type AttrValue = String | Js

  /** The value as text (a Js's `cmd`); null stays null. */
  inline def textOf(value: AttrValue): String = value match {
    case js: Js => js.cmd
    case s: String => s
    case null => null
  }

  /** A transform of the text of a value (a Js becomes its `cmd`), as a transform of values. */
  private[xml] def onText(transform: String => String): AttrValue => AttrValue = value => transform(textOf(value))

  /** How many times the attributes of an [[ElemOptimized]] were computed; for tests. */
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
                   @net.bytebuddy.implementation.bind.annotation.FieldValue("attrs") attrs: List[(String, AttrValue)],
                   @net.bytebuddy.implementation.bind.annotation.FieldValue("cached") cached: MetaData
                 ): MetaData = {
      if (cached ne null) cached
      else if (attrs eq null) Null // called before the fields were set: don't cache
      else {
        // Benign race: concurrent callers may compute twice, but the result is the same.
        ElemOptimized.Computations.incrementAndGet()
        val computed = attrs.foldLeft((Null: MetaData, Set[String]()))({
          case ((cur, seenAttrs), (name, value)) if seenAttrs.contains(name) => (cur, seenAttrs)
          case ((cur, seenAttrs), (name, null)) => (cur, seenAttrs + name)
          case ((cur, seenAttrs), (name, value)) => (new UnprefixedAttribute(name, textOf(value), cur), seenAttrs + name)
        })._1
        ElemOptimized.SetCached.invokeExact(self, computed): Unit
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

  @static private val UnprefixedAttributeClass: Class[? <: UnprefixedAttribute] = new ByteBuddy()
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
  @static private val NewAttribute: MethodHandle = lookup
    .findConstructor(UnprefixedAttributeClass, MethodType.methodType(classOf[Unit], classOf[String], classOf[scala.collection.Seq[Node]], classOf[MetaData]))
    .asType(MethodType.methodType(classOf[UnprefixedAttribute], classOf[String], classOf[scala.collection.Seq[Node]], classOf[MetaData]))

  private def setter(name: String, owner: Class[?], tpe: Class[?]): MethodHandle =
    lookup.unreflectSetter(UnprefixedAttributeClass.getField(name)).asType(MethodType.methodType(Void.TYPE, owner, tpe))

  @static private val SetAttrs: MethodHandle = setter("attrs", classOf[UnprefixedAttribute], classOf[List[?]])
  @static private val SetCached: MethodHandle = setter("cached", classOf[AnyRef], classOf[MetaData])

  def apply(label: String, attrs: List[(String, AttrValue)], minimizeEmpty: Boolean = false)(child: Node*): ElemOptimized = new ElemOptimized(
    attrs = attrs, label = label, minimizeEmpty = minimizeEmpty, child = child *
  )

  def apply(label: String, attrs: (String, AttrValue)*)(child: Node*): ElemOptimized = new ElemOptimized(
    attrs = attrs.toList, label = label, minimizeEmpty = false, child = child *
  )

  def transform(orig: Elem, name: String, transform: String => String) =
    new ElemOptimized(orig, name, ElemOptimized.onText(transform))

  def transform(orig: Elem, transforms: Seq[(String, String => String)]) =
    new ElemOptimized(orig, transforms.map(t => t._1 -> ElemOptimized.onText(t._2)))

  def transform(orig: ElemOptimized, name: String, transform: String => String) =
    new ElemOptimized(orig, name, ElemOptimized.onText(transform))

  def transform(orig: ElemOptimized, transforms: Seq[(String, String => String)]) =
    new ElemOptimized(orig, transforms.map(t => t._1 -> ElemOptimized.onText(t._2)))

  def transformOptimized(orig: Elem, name: String, transform: AttrValue => AttrValue) = new ElemOptimized(orig, name, transform)

  def transformOptimized(orig: Elem, transforms: Seq[(String, AttrValue => AttrValue)]) = new ElemOptimized(orig, transforms)

  def transformOptimized(orig: ElemOptimized, name: String, transform: AttrValue => AttrValue) = new ElemOptimized(orig, name, transform)

  def transformOptimized(orig: ElemOptimized, transforms: Seq[(String, AttrValue => AttrValue)]) = new ElemOptimized(orig, transforms)

}

/**
 * An [[Elem]] whose attributes are kept as a list of (name, value), only turned into [[MetaData]] (once, cached) when
 * read. A value is a String or a [[Js]] (see [[ElemOptimized.AttrValue]]): a Js is kept as it is, until its text is
 * needed. Transforms get the current value (`""` if the attribute is missing), and returning `null` removes it (in
 * every constructor: the attribute is dropped from `attrs`, never stored with a null value). Those that are
 * `String => String` get the value's text (a Js's `cmd`); those that are `AttrValue => AttrValue` (with a
 * `DummyImplicit`, so they're told apart) get the value as it is, so they can keep a Js.
 *
 * @param orig  the element whose attributes these replace; never itself an [[ElemOptimized]], so a chain of
 *              transforms doesn't nest wrappers and computes the attributes only once
 *
 * @param attrs in reverse print order; names unique, values never null (a removed attribute is not in the list)
 */
class ElemOptimized private(
                             val attrs: List[(String, ElemOptimized.AttrValue)],
                             prefix: String = null,
                             label: String,
                             scope: NamespaceBinding = TopScope,
                             minimizeEmpty: Boolean = false,
                             child: Node*
                           ) extends Elem(prefix, label, Null, scope, minimizeEmpty, child *) {

  private def this(orig: Elem, name: String, transform: ElemOptimized.AttrValue => ElemOptimized.AttrValue) = this({
    @tailrec def aux(
                      remaining: Iterator[(String, ElemOptimized.AttrValue)] = orig.attributes.iterator.map(md => md.key -> ElemOptimized.textOfValue(md.value)),
                      rslt: List[(String, ElemOptimized.AttrValue)] = Nil,
                    ): List[(String, ElemOptimized.AttrValue)] = if (remaining.hasNext) remaining.next() match {
      case (`name`, value) =>
        val updated = transform(value)
        if (updated eq null) remaining ++: rslt else (name -> updated) :: (remaining ++: rslt)
      case entry => aux(remaining, entry :: rslt)
    } else {
      val updated = transform("")
      if (updated eq null) rslt else (name -> updated) :: rslt
    }

    aux()
  },
    orig.prefix,
    orig.label,
    orig.scope,
    orig.minimizeEmpty,
    orig.child *
  )

  private def this(orig: Elem, transforms: Seq[(String, ElemOptimized.AttrValue => ElemOptimized.AttrValue)]) = this({
    @tailrec def aux(
                      remaining: Iterator[(String, ElemOptimized.AttrValue)] = orig.attributes.iterator.map(md => md.key -> ElemOptimized.textOfValue(md.value)),
                      transforms: Map[String, ElemOptimized.AttrValue => ElemOptimized.AttrValue] = transforms.toMap,
                      rslt: List[(String, ElemOptimized.AttrValue)] = Nil,
                    ): List[(String, ElemOptimized.AttrValue)] = if (remaining.hasNext) remaining.next() match {
      case (name, value) if transforms.contains(name) =>
        val updated = transforms(name)(value)
        if (transforms.size == 1) {
          if (updated eq null) remaining ++: rslt else (name -> updated) :: (remaining ++: rslt)
        } else {
          if (updated eq null) aux(remaining, transforms.removed(name), rslt) else aux(remaining, transforms.removed(name), (name -> updated) :: rslt)
        }
      case entry => aux(remaining, transforms, entry :: rslt)
    } else transforms.iterator.foldLeft(rslt)({
      case (rslt, (name, transform)) =>
        val updated = transform("")
        if (updated eq null) rslt else (name -> updated) :: rslt
    })

    aux()
  },
    orig.prefix,
    orig.label,
    orig.scope,
    orig.minimizeEmpty,
    orig.child *
  )

  private def this(orig: ElemOptimized, name: String, transform: ElemOptimized.AttrValue => ElemOptimized.AttrValue) = this({
    @tailrec def aux(
                      remaining: List[(String, ElemOptimized.AttrValue)] = orig.attrs,
                      rslt: List[(String, ElemOptimized.AttrValue)] = Nil,
                    ): List[(String, ElemOptimized.AttrValue)] = remaining match {
      case Nil =>
        val updated = transform("")
        if (updated eq null) rslt else (name -> updated) :: rslt
      case (`name`, value) :: rest =>
        val updated = transform(value)
        if (updated eq null) rest ::: rslt else (name -> updated) :: rest ::: rslt
      case entry :: rest => aux(rest, entry :: rslt)
    }

    aux()
  },
    orig.prefix,
    orig.label,
    orig.scope,
    orig.minimizeEmpty,
    orig.child *
  )

  private def this(orig: ElemOptimized, transforms: Seq[(String, ElemOptimized.AttrValue => ElemOptimized.AttrValue)]) = this({
    @tailrec def aux(
                      remaining: List[(String, ElemOptimized.AttrValue)] = orig.attrs,
                      transforms: Map[String, ElemOptimized.AttrValue => ElemOptimized.AttrValue] = transforms.toMap,
                      rslt: List[(String, ElemOptimized.AttrValue)] = Nil,
                    ): List[(String, ElemOptimized.AttrValue)] = remaining match {
      case Nil =>
        transforms.iterator.foldLeft(rslt)({
          case (rslt, (name, transform)) =>
            val updated = transform("")
            if (updated eq null) rslt else (name -> updated) :: rslt
        })
      case (name, value) :: rest if transforms.contains(name) =>
        val updated = transforms(name)(value)
        if (transforms.size == 1) {
          if (updated eq null) rest ::: rslt else (name -> updated) :: rest ::: rslt
        } else {
          if (updated eq null) aux(rest, transforms.removed(name), rslt) else aux(rest, transforms.removed(name), (name -> updated) :: rslt)
        }
      case entry :: rest => aux(rest, transforms, entry :: rslt)
    }

    aux()
  },
    orig.prefix,
    orig.label,
    orig.scope,
    orig.minimizeEmpty,
    orig.child *
  )

  override val attributes: MetaData = {
    val ins = ElemOptimized.NewAttribute.invokeExact("", null: scala.collection.Seq[Node], Null: MetaData): UnprefixedAttribute
    ElemOptimized.SetAttrs.invokeExact(ins, attrs): Unit
    ins
  }
}