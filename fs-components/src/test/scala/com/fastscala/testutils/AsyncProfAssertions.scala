package com.fastscala.testutils

import org.scalatest.Assertions

import java.nio.file.{Files, Path}

/** For tests that profile with [[AsyncProf.profileWithFiles]]. */
trait AsyncProfAssertions { this: Assertions =>

  /** The profiler wrote the JFR recording and the flame graph (if not, the reason was printed with an `[AsyncProf]` prefix). */
  def assertProfileWritten(files: Seq[Path]): Unit = {
    assert(files.size == 2, s"No profile written to ${AsyncProf.OutDir}: see the [AsyncProf] message above")
    files.foreach(f => assert(Files.size(f) > 0, s"Empty: $f"))
  }
}
