package com.fastscala.scala_xml.utils

import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers

import java.lang.invoke.{MethodHandle, MethodHandles, MethodType}
import java.lang.reflect.Modifier
import scala.annotation.static
import scala.xml.*


object ElemWithFastAttrsV3 {
  /** How many times the attributes of an [[ElemWithFastAttrsV3]] were computed; for tests. */
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
                   @net.bytebuddy.implementation.bind.annotation.FieldValue("attrs") attrs: Map[String, List[String]],
                   @net.bytebuddy.implementation.bind.annotation.FieldValue("cached") cached: MetaData
                 ): MetaData = {
      if (cached ne null) cached
      else if (attrs eq null) Null // called before the fields were set: don't cache
      else {
        // Benign race: concurrent callers may compute twice, but the result is the same.
        ElemWithFastAttrsV3.Computations.incrementAndGet()
        // In the Map's iteration order; each value is its pieces concatenated, oldest first.
        val computed = attrs.toList.foldRight(Null: MetaData)({
          case ((name, pieces), next) => if (pieces.isEmpty) next else new UnprefixedAttribute(name, joined(pieces), next)
        })
        ElemWithFastAttrsV3.SetCached.invokeExact(self, computed): Unit
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
    .defineField("attrs", classOf[Map[?, ?]], Modifier.PUBLIC)
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

  // MethodHandles instead of reflection; @static makes them static final fields, which the JIT treats as constants.
  // Each is adapted (asType) to the exact types used at its invokeExact call site.
  @static private val lookup = MethodHandles.lookup()

  /** (key: String, value: Seq[Node], next: MetaData) => new UnprefixedAttributeClass */
  @static val NewAttribute: MethodHandle = lookup
    .findConstructor(UnprefixedAttributeClass, MethodType.methodType(classOf[Unit], classOf[String], classOf[scala.collection.Seq[Node]], classOf[MetaData]))
    .asType(MethodType.methodType(classOf[UnprefixedAttribute], classOf[String], classOf[scala.collection.Seq[Node]], classOf[MetaData]))

  private def setter(name: String, owner: Class[?], tpe: Class[?]): MethodHandle =
    lookup.unreflectSetter(UnprefixedAttributeClass.getField(name)).asType(MethodType.methodType(Void.TYPE, owner, tpe))

  @static val SetAttrs: MethodHandle = setter("attrs", classOf[UnprefixedAttribute], classOf[Map[?, ?]])
  @static val SetCached: MethodHandle = setter("cached", classOf[AnyRef], classOf[MetaData])

  // ---------- transforms on the attrs Map ----------

  /**
   * A `String => String` transform as a list one: the pieces are concatenated, and the result is the only piece; a
   * null result removes the attribute (as Nil).
   */
  /** The value of `pieces` (stored newest first): concatenated oldest first. */
  def joined(pieces: List[String]): String =
    if (pieces.isEmpty) ""
    else if (pieces.tail.isEmpty) pieces.head
    else {
      val sb = new java.lang.StringBuilder()
      def appendOldestFirst(rest: List[String]): Unit = if (rest.nonEmpty) { appendOldestFirst(rest.tail); sb.append(rest.head) }
      appendOldestFirst(pieces)
      sb.toString
    }

  def fromStringTransform(transform: String => String): List[String] => List[String] = pieces => {
    val result = transform(joined(pieces))
    if (result eq null) Nil else result :: Nil
  }

  private[utils] def attrsOf(orig: Elem): Map[String, List[String]] =
    orig.attributes.iterator.map(md => md.key -> (textOfValue(md.value) :: Nil)).toMap

  /** Applies `transform` to attribute `name` (Nil if missing); an empty result removes it. */
  private[utils] def transformed(attrs: Map[String, List[String]], name: String, transform: List[String] => List[String]): Map[String, List[String]] = {
    val result = transform(attrs.getOrElse(name, Nil))
    if (result.isEmpty) attrs - name else attrs.updated(name, result)
  }

  private[utils] def transformedAll(attrs: Map[String, List[String]], transforms: Seq[(String, List[String] => List[String])]): Map[String, List[String]] =
    transforms.foldLeft(attrs)({ case (acc, (name, transform)) => transformed(acc, name, transform) })
}

/**
 * Like [[ElemWithFastAttrsV2]], but each attribute value is kept as a list of pieces, stored **newest first**, so
 * appending to a value (e.g. a class) is a prepend, `piece :: pieces`: no copying and no new, longer String each time.
 * The value is the pieces concatenated oldest first, with no separator ([[ElemWithFastAttrsV3.joined]]), built only when
 * the attributes are computed; [[XmlStream]] writes the pieces directly, without joining them.
 *
 * Transforms are either `List[String] => List[String]`, on the pieces newest first (a missing attribute is seen as
 * `Nil`, and returning `Nil` removes it), or, as a special case, `String => String` (the pieces are concatenated and
 * the result becomes the only piece; a missing attribute is seen as `""`, and returning `null` removes it, as with V2).
 * For a name repeated in one call, the result is undefined.
 *
 * The list constructors take an extra `(using DummyImplicit)` only so they don't clash, after erasure, with the
 * `String => String` ones; pass list transforms with explicit types, e.g. `(pieces: List[String]) => ...`.
 *
 * @param attrs printed in iteration order; each value's pieces newest first, never an empty list (a removed attribute
 *              is not in the Map)
 */
class ElemWithFastAttrsV3 private(
                                   val attrs: Map[String, List[String]],
                                   prefix: String,
                                   label: String,
                                   scope: NamespaceBinding,
                                   minimizeEmpty: Boolean,
                                   child: Node*
                                 ) extends Elem(prefix, label, Null, scope, minimizeEmpty, child *) {

  // ---------- List[String] => List[String] ----------

  def this(orig: Elem, name: String, transform: List[String] => List[String])(using DummyImplicit) = this(
    ElemWithFastAttrsV3.transformed(ElemWithFastAttrsV3.attrsOf(orig), name, transform),
    orig.prefix, orig.label, orig.scope, orig.minimizeEmpty, orig.child *
  )

  def this(orig: Elem, transforms: Seq[(String, List[String] => List[String])])(using DummyImplicit) = this(
    ElemWithFastAttrsV3.transformedAll(ElemWithFastAttrsV3.attrsOf(orig), transforms),
    orig.prefix, orig.label, orig.scope, orig.minimizeEmpty, orig.child *
  )

  def this(orig: ElemWithFastAttrsV3, name: String, transform: List[String] => List[String])(using DummyImplicit) = this(
    ElemWithFastAttrsV3.transformed(orig.attrs, name, transform),
    orig.prefix, orig.label, orig.scope, orig.minimizeEmpty, orig.child *
  )

  def this(orig: ElemWithFastAttrsV3, transforms: Seq[(String, List[String] => List[String])])(using DummyImplicit) = this(
    ElemWithFastAttrsV3.transformedAll(orig.attrs, transforms),
    orig.prefix, orig.label, orig.scope, orig.minimizeEmpty, orig.child *
  )

  // ---------- String => String: special cases of the above ----------

  def this(orig: Elem, name: String, transform: String => String) =
    this(orig, name, ElemWithFastAttrsV3.fromStringTransform(transform))(using DummyImplicit.dummyImplicit)

  def this(orig: Elem, transforms: Seq[(String, String => String)]) =
    this(orig, transforms.map(t => t._1 -> ElemWithFastAttrsV3.fromStringTransform(t._2)))(using DummyImplicit.dummyImplicit)

  def this(orig: ElemWithFastAttrsV3, name: String, transform: String => String) =
    this(orig, name, ElemWithFastAttrsV3.fromStringTransform(transform))(using DummyImplicit.dummyImplicit)

  def this(orig: ElemWithFastAttrsV3, transforms: Seq[(String, String => String)]) =
    this(orig, transforms.map(t => t._1 -> ElemWithFastAttrsV3.fromStringTransform(t._2)))(using DummyImplicit.dummyImplicit)

  override val attributes: MetaData = {
    val ins = ElemWithFastAttrsV3.NewAttribute.invokeExact("", null: scala.collection.Seq[Node], Null: MetaData): UnprefixedAttribute
    ElemWithFastAttrsV3.SetAttrs.invokeExact(ins, attrs): Unit
    ins
  }
}
