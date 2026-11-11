package com.fastscala.scala_xml.utils

import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromNodeSeq
import com.fastscala.scala_xml.js.JS
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers

import java.lang.invoke.{MethodHandle, MethodHandles, MethodType}
import java.lang.reflect.Modifier
import java.util.regex.Pattern
import scala.annotation.static
import scala.collection.immutable.{AbstractSeq, LinearSeq, ListMap}
import scala.xml.*


object ElemWithFastAttrsV2 {
  /** How many times the attributes of an [[ElemWithFastAttrsV2]] were computed; for tests. */
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
                   @net.bytebuddy.implementation.bind.annotation.FieldValue("attrs") attrs: Map[String, String],
                   @net.bytebuddy.implementation.bind.annotation.FieldValue("cached") cached: MetaData
                 ): MetaData = {
      if (cached ne null) cached
      else if (attrs eq null) Null // called before the fields were set: don't cache
      else {
        // Benign race: concurrent callers may compute twice, but the result is the same.
        ElemWithFastAttrsV2.Computations.incrementAndGet()
        // In the Map's iteration order, the same order XmlStream writes them in without computing this.
        val computed = attrs.toList.foldRight(Null: MetaData)({
          case ((name, value), next) => if (value eq null) next else new UnprefixedAttribute(name, value, next)
        })
        ElemWithFastAttrsV2.SetCached.invokeExact(self, computed): Unit
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
    .defineField("attrs", classOf[Map[String, String]], Modifier.PUBLIC)
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

  @static val SetAttrs: MethodHandle = setter("attrs", classOf[UnprefixedAttribute], classOf[Map[?, ?]])
  @static val SetCached: MethodHandle = setter("cached", classOf[AnyRef], classOf[MetaData])
}

/**
 * An [[Elem]] whose attributes are kept as a list of (name, value), only turned into [[MetaData]] (once, cached) when
 * read. Transforms are `String => String`: a missing attribute is seen as `""`, and returning `null` removes it.
 *
 * @param orig  the element whose attributes these replace; never itself an [[ElemWithFastAttrsV2]], so a chain of
 *              transforms doesn't nest wrappers and computes the attributes only once
 *
 * @param attrs printed in iteration order; a removed attribute is not in the Map
 */
class ElemWithFastAttrsV2 private(
                                   val attrs: Map[String, String],
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
        md.key -> transform(ElemWithFastAttrsV2.textOfValue(md.value))
      } else md.key -> ElemWithFastAttrsV2.textOfValue(md.value)
    }).toMap
    (if (found) updated else updated + (name -> transform(""))).filter(_._2 != null)
  },
    orig.prefix,
    orig.label,
    orig.scope,
    orig.minimizeEmpty,
    orig.child *
  )

  def this(orig: Elem, transforms: Seq[(String, String => String)]) = this({
    var found = Set[String]()
    val transformsByKey = transforms.toMap
    val updates = orig.attributes.iterator.map(md => {
      if (transformsByKey.contains(md.key)) {
        found += md.key
        md.key -> transformsByKey(md.key)(ElemWithFastAttrsV2.textOfValue(md.value))
      } else md.key -> ElemWithFastAttrsV2.textOfValue(md.value)
    }).toMap
    // transforms for attributes orig doesn't have yet
    val added = transformsByKey.removedAll(found).map(kv => kv._1 -> kv._2(""))
    (updates ++ added).filter(_._2 != null)
  },
    orig.prefix,
    orig.label,
    orig.scope,
    orig.minimizeEmpty,
    orig.child *
  )

  def this(orig: ElemWithFastAttrsV2, name: String, transform: String => String) = this(
    {
      val newAttr = (name -> orig.attrs.get(name).map(transform).getOrElse(transform("")))
      if (newAttr._2 != null) orig.attrs + newAttr else orig.attrs - name
    },
    orig.prefix,
    orig.label,
    orig.scope,
    orig.minimizeEmpty,
    orig.child *
  )

  def this(orig: ElemWithFastAttrsV2, transforms: Seq[(String, String => String)]) = this({
    val updates = transforms.map({
      case (name, transform) => (name -> orig.attrs.get(name).map(transform).getOrElse(transform("")))
    })
    orig.attrs.removedAll(updates.collect({ case (name, null) => name })) ++ updates.filter(_._2 != null)
  },
    orig.prefix,
    orig.label,
    orig.scope,
    orig.minimizeEmpty,
    orig.child *
  )

  override val attributes: MetaData = {
    val ins = ElemWithFastAttrsV2.NewAttribute.invokeExact("", null: scala.collection.Seq[Node], Null: MetaData): UnprefixedAttribute
    ElemWithFastAttrsV2.SetAttrs.invokeExact(ins, attrs): Unit
    ins
  }
}