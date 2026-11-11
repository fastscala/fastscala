package com.fastscala.testutils

import one.profiler.{AsyncProfiler, AsyncProfilerLoader}

import java.nio.file.{Files, Path, Paths}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Test copy of reconciliacao_app's `pt.reconciliacao.app.util.AsyncProf` (fs-components can't depend on the app, and
 * async-profiler is only a Test dependency here). Same behaviour, except the flame graph is converted synchronously,
 * so it's written before the test JVM can exit.
 *
 * Profiles the calling thread with async-profiler (event=wall) while `code` runs and, if that took longer than
 * `threshold` ms, writes to [[OutDir]] a JFR recording (with the timeline; open it in IntelliJ or JDK Mission Control)
 * and an HTML flame graph converted from it. One profiling session per JVM: calls that start while another is being
 * profiled just run `code` unprofiled.
 */
object AsyncProf {

  val OutDir: Path = Paths.get("/home/david/projs/async-profiler")

  private lazy val profiler: Option[AsyncProfiler] =
    try Some(AsyncProfilerLoader.load())
    catch {
      case t: Throwable =>
        println(s"[AsyncProf] async-profiler unavailable, not profiling: $t")
        None
    }

  private val busy = new AtomicBoolean(false)

  private val TimestampFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss.SSS")

  /**
   * @param threshold  keep the recording only if `code` took longer than this, in ms
   * @param intervalMs sampling interval, in ms
   */
  def profile[T](name: String, threshold: Long, intervalMs: Long = 1)(code: => T): T = profiler match {
    case Some(p) if busy.compareAndSet(false, true) =>
      try {
        val recording: Option[Path] =
          try {
            Files.createDirectories(OutDir)
            val file = Files.createTempFile(OutDir, "recording-", ".jfr")
            p.execute(s"start,event=wall,interval=${intervalMs}ms,filter,jfr,jfrsync=default,file=$file")
            p.addThread(Thread.currentThread())
            Some(file)
          } catch {
            case t: Throwable =>
              println(s"[AsyncProf] could not start profiling '$name': $t")
              None
          }
        val start = System.nanoTime()
        try code
        finally recording.foreach(stop(p, _, name, (System.nanoTime() - start) / 1000000, threshold))
      } finally busy.set(false)
    case _ => code
  }

  private def stop(p: AsyncProfiler, recording: Path, name: String, tookMs: Long, threshold: Long): Unit =
    try {
      p.execute("stop")
      if (tookMs > threshold) {
        // async-profiler's options are comma separated, so keep commas (and anything odd) out of the names
        val safeName = name.replaceAll("[^A-Za-z0-9._-]+", "_")
        val base = s"$safeName-${LocalDateTime.now().format(TimestampFormat)}-${tookMs}ms"
        val jfr = Files.move(recording, OutDir.resolve(base + ".jfr"))
        val html = OutDir.resolve(base + ".html")
        println(s"[AsyncProf] '$name' took ${tookMs}ms (> ${threshold}ms): $jfr")
        one.convert.Main.convert(jfr.toString, html.toString,
          new one.convert.Arguments("--wall", "-o", "html", "--title", s"${safeName}_${tookMs}ms"))
        println(s"[AsyncProf] '$name' flame graph: $html")
      } else Files.deleteIfExists(recording)
    } catch {
      case t: Throwable => println(s"[AsyncProf] could not stop profiling '$name': $t")
    }
}
