package com.fastscala.utils

import com.fastscala.utils.Bytes.humanReadableBytes
import org.scalatest.flatspec.AnyFlatSpec

import java.util.Locale

class BytesSpec extends AnyFlatSpec {

  "humanReadableBytes" should "show plain bytes below one unit" in {
    assert(humanReadableBytes(0) == "0 B")
    assert(humanReadableBytes(512) == "512 B")
    assert(humanReadableBytes(1023) == "1023 B")
    assert(humanReadableBytes(999, si = true) == "999 B")
  }

  it should "use powers of 1024 with KiB/MiB/... by default" in {
    assert(humanReadableBytes(1024) == "1.0 KiB")
    assert(humanReadableBytes(1536) == "1.5 KiB")
    assert(humanReadableBytes(1_900_000) == "1.8 MiB")
    assert(humanReadableBytes(1L << 20) == "1.0 MiB")
    assert(humanReadableBytes(1L << 30) == "1.0 GiB")
    assert(humanReadableBytes(1L << 40) == "1.0 TiB")
    assert(humanReadableBytes(1L << 50) == "1.0 PiB")
    assert(humanReadableBytes(1L << 60) == "1.0 EiB")
  }

  it should "use powers of 1000 with KB/MB/... when si = true" in {
    assert(humanReadableBytes(1000, si = true) == "1.0 KB")
    assert(humanReadableBytes(1_900_000, si = true) == "1.9 MB")
    assert(humanReadableBytes(1_000_000_000L, si = true) == "1.0 GB")
  }

  it should "move to the next unit instead of showing a rounded-up 1024.0 / 1000.0" in {
    assert(humanReadableBytes((1L << 20) - 1) == "1.0 MiB")
    assert(humanReadableBytes(1_048_000) == "1023.4 KiB")
    assert(humanReadableBytes(999_960, si = true) == "1.0 MB")
    assert(humanReadableBytes(999_940, si = true) == "999.9 KB")
  }

  it should "handle the extremes of Long" in {
    assert(humanReadableBytes(Long.MaxValue) == "8.0 EiB")
    assert(humanReadableBytes(Long.MaxValue, si = true) == "9.2 EB")
    assert(humanReadableBytes(Long.MinValue) == "-8.0 EiB")
  }

  it should "keep the sign of negative values" in {
    assert(humanReadableBytes(-512) == "-512 B")
    assert(humanReadableBytes(-1536) == "-1.5 KiB")
  }

  it should "use '.' by default regardless of the JVM locale, or the given locale" in {
    val previous = Locale.getDefault
    try {
      Locale.setDefault(Locale.forLanguageTag("pt-PT"))
      assert(humanReadableBytes(1536) == "1.5 KiB")
      assert(humanReadableBytes(1536, locale = Locale.forLanguageTag("pt-PT")) == "1,5 KiB")
    } finally Locale.setDefault(previous)
  }
}
