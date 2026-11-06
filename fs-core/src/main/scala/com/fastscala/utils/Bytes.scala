package com.fastscala.utils

import java.util.Locale

object Bytes {

  private val Prefixes = "KMGTPE"

  /**
   * Formats a byte count for display, with one decimal place: 512 → "512 B", 1536 → "1.5 KiB".
   *
   * @param si     false (default): powers of 1024, with KiB/MiB/GiB/... units; true: powers of 1000, with KB/MB/GB/...
   * @param locale the locale for the decimal separator (default: [[Locale.ROOT]], i.e. always '.')
   */
  def humanReadableBytes(bytes: Long, si: Boolean = false, locale: Locale = Locale.ROOT): String = {
    val unit = if (si) 1000 else 1024
    val abs = if (bytes == Long.MinValue) Long.MaxValue else math.abs(bytes)
    if (abs < unit) s"$bytes B"
    else {
      // Divide step by step (not via log, which can be off by one at exact powers), and move to the next unit when
      // the value would round up to `unit` (e.g. 1048575 B is "1.0 MiB", not "1024.0 KiB").
      var value = abs.toDouble
      var exp = 0
      while (value >= unit - 0.05 && exp < Prefixes.length) {
        value /= unit
        exp += 1
      }
      val sign = if (bytes < 0) "-" else ""
      val prefix = Prefixes.charAt(exp - 1).toString + (if (si) "" else "i")
      sign + "%.1f".formatLocal(locale, value) + s" ${prefix}B"
    }
  }
}
