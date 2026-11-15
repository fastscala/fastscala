package com.fastscala.scala_xml.xml;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

/**
 * Writes a long into a byte array, as 8 bytes (the lowest first), in one store. In Java, so the VarHandle is a
 * `static final` field: the JIT only inlines a VarHandle's access (into a plain 8-byte store) when it's a constant,
 * which a Scala object's field isn't.
 */
public final class LittleEndian {

  private static final VarHandle LONG = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);

  private LittleEndian() {
  }

  /** The 8 bytes of `value` (the lowest first) into `buf(p until p + 8)`. */
  public static void putLong(byte[] buf, int p, long value) {
    LONG.set(buf, p, value);
  }
}
