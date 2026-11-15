package com.fastscala.utils

import org.scalatest.flatspec.AnyFlatSpec

import java.io.{ByteArrayOutputStream, PrintStream}

class UtilsSpec extends AnyFlatSpec {

  /** What `f` prints on Console.out, and its value. */
  private def printed[T](f: => T): (String, T) = {
    val bytes = new ByteArrayOutputStream()
    val v = Console.withOut(new PrintStream(bytes, true, "UTF-8"))(f)
    (bytes.toString("UTF-8"), v)
  }

  "time / timeNs" should "return the value and report how long it took" in {
    var ms = -1L
    assert(Utils.time { Thread.sleep(20); 42 }(ms = _) == 42)
    assert(ms >= 15, ms)
    var ns = -1L
    assert(Utils.timeNs("x")(ns = _) == "x")
    assert(ns >= 0)
  }

  "%?" should "return the block's value, and print its name and time" in {
    val (out, v) = printed(Utils.%?("work")(1 + 1))
    assert(v == 2)
    assert(out.contains("Starting 'work'...") && out.contains("ms]"), out)
  }

  it should "print the result, with a function for it" in {
    val (out, _) = printed(Utils.%?[Int]("sum", (r: Int) => s"r=$r")(40 + 2))
    assert(out.contains("[r=42]"), out)
  }

  it should "rethrow the block's exception" in {
    val (_, thrown) = printed(scala.util.Try(Utils.%?[Int]("boom")(throw new IllegalStateException("boom"))))
    assert(thrown.failed.get.isInstanceOf[IllegalStateException])
  }

  it should "indent nested blocks" in {
    val (out, _) = printed(Utils.%?("outer")(Utils.%?("inner")(1)))
    val indent = out.linesIterator.find(_.contains("Starting 'inner'")).get.takeWhile(_ == ' ').length
    assert(indent > 0, out)
  }

  "%??" should "profile each call of the function" in {
    val f = Utils.%??[Int, Int]("double")(_ * 2)
    val (out, v) = printed(f(21))
    assert(v == 42 && out.contains("Starting 'double'"), out)
  }

  "disableProfiling" should "run the block without printing, and re-enable profiling after it (even if it throws)" in {
    val (out, v) = printed(Utils.disableProfiling(Utils.%?("hidden")(7)))
    assert(v == 7 && out.isEmpty, out)
    assert(Utils.enabled)
    assert(scala.util.Try(Utils.disableProfiling(throw new RuntimeException("x"))).isFailure)
    assert(Utils.enabled)
  }

  "profile" should "return the code's value" in {
    assert(Utils.profile("p", threshold = 10_000, samplePeriod = 5)(41 + 1) == 42)
  }

  it should "print the stack traces sampled when the code took longer than the threshold" in {
    val (out, _) = printed {
      Utils.profile("slow", threshold = 0, samplePeriod = 5)(Thread.sleep(60))
      Thread.sleep(100) // the watchdog prints after the code, on its own thread
    }
    assert(out.contains("[slow - profile stack trace"), out)
  }
}
