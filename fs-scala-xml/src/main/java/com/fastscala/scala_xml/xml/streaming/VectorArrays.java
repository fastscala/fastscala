package com.fastscala.scala_xml.xml.streaming;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * The elements of a small Scala Vector (a {@code scala.collection.immutable.Vector1}: up to 32 elements, all in one
 * array), read in place: XmlJsStream reads a level's items from an array, and children built with {@code map} (on a
 * Range, a Seq...) are mostly such Vectors, which it would otherwise copy (through an iterator).
 *
 * <p>The array is Vector's {@code prefix1} ({@code private[immutable]} in Scala, public in the bytecode); it's only
 * read. If the Scala library doesn't have it (another version), {@link #arrayOfVector1} is always null, and the
 * Vectors are copied as any other IndexedSeq.
 *
 * <p>In Java, so the MethodHandle is a {@code static final} field: the JIT only inlines a handle that's a constant.
 */
public final class VectorArrays {

  private static final Class<?> VECTOR1 = classOrNull("scala.collection.immutable.Vector1");

  private static final MethodHandle PREFIX1 = prefix1OrNull();

  private VectorArrays() {
  }

  private static Class<?> classOrNull(String name) {
    try {
      return Class.forName(name);
    } catch (Throwable t) {
      return null;
    }
  }

  private static MethodHandle prefix1OrNull() {
    try {
      Class<?> vector = Class.forName("scala.collection.immutable.Vector");
      return MethodHandles.publicLookup()
          .findVirtual(vector, "prefix1", MethodType.methodType(Object[].class))
          .asType(MethodType.methodType(Object[].class, Object.class));
    } catch (Throwable t) {
      return null;
    }
  }

  /** The array of `seq`'s elements (exactly its length), if it's a Vector1; null otherwise. Not to be modified. */
  public static Object[] arrayOfVector1(Object seq) {
    if (PREFIX1 == null || seq.getClass() != VECTOR1) return null;
    try {
      return (Object[]) PREFIX1.invokeExact(seq);
    } catch (Throwable t) {
      return null;
    }
  }
}
