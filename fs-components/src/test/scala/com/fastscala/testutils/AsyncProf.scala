package com.fastscala.testutils

import one.profiler.AsyncProfilerLoader

import java.lang.reflect.InvocationTargetException

import java.nio.file.{Files, Path, Paths}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 * Profiles the calling thread with async-profiler (event=wall) while `code` runs and, if that took longer than
 * `threshold` ms, writes to [[OutDir]] a JFR recording (with the timeline; open it in IntelliJ or JDK Mission Control)
 * and an HTML flame graph converted from it. One profiling session per JVM: calls that start while another is being
 * profiled (sbt runs test suites in parallel) wait for it to finish.
 *
 * Tests run in sbt's JVM (they aren't forked), with a new classloader on every run, and a JVM can load the native
 * library only once: so the profiler loaded first is kept in the JVM-wide system properties, and later runs use it
 * through reflection (its class is from the classloader of the run that loaded it).
 */
object AsyncProf {

  val OutDir: Path = Paths.get("./async-profiler")

  private val ProfilerKey = "com.fastscala.testutils.AsyncProf.profiler"
  private val BusyKey = "com.fastscala.testutils.AsyncProf.busy"

  /** The JVM's profiler (an `AsyncProfiler`, maybe from another run's classloader), called through reflection. */
  private final class Profiler(instance: AnyRef) {
    private val executeMethod = instance.getClass.getMethod("execute", classOf[String])
    private val addThreadMethod = instance.getClass.getMethod("addThread", classOf[Thread])

    private def call(f: => AnyRef): AnyRef =
      try f
      catch {case e: InvocationTargetException if e.getCause != null => throw e.getCause}

    def execute(command: String): String = call(executeMethod.invoke(instance, command)).asInstanceOf[String]

    def addThread(thread: Thread): Unit = call(addThreadMethod.invoke(instance, thread))
  }

  /** The profiler, loading it if no run in this JVM has; `Left` with the reason if it can't be loaded. */
  private def profiler: Either[String, Profiler] = {
    val props = System.getProperties
    props.synchronized {
      Option(props.get(ProfilerKey)) match {
        case Some(instance) => Right(new Profiler(instance))
        case None =>
          try {
            val instance = AsyncProfilerLoader.load()
            props.put(ProfilerKey, instance)
            Right(new Profiler(instance))
          } catch {
            case t: Throwable => Left(t.toString)
          }
      }
    }
  }

  /** Whether a profiling session is running, for the whole JVM (shared by every run's classloader). */
  private def busy: AtomicBoolean = {
    val props = System.getProperties
    props.synchronized {
      props.computeIfAbsent(BusyKey, _ => new AtomicBoolean(false)).asInstanceOf[AtomicBoolean]
    }
  }

  private val TimestampFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss.SSS")

  /**
   * @param threshold  keep the recording only if `code` took longer than this, in ms
   * @param intervalMs sampling interval, in ms
   */
  def profile[T](name: String, threshold: Long, intervalMs: Long = 1)(code: => T): T =
    profileWithFiles(name, threshold, intervalMs)(code)._1

  /**
   * Like [[profile]], but also returns the files written: the JFR recording and the HTML flame graph, or none (if
   * async-profiler isn't available, couldn't start or stop, or `code` took less than
   * `threshold` ms; the reason is printed).
   */
  def profileWithFiles[T](name: String, threshold: Long, intervalMs: Long = 1)(code: => T): (T, Seq[Path]) = profiler match {
    case Right(p) =>
      acquire(name)
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
        var files: Seq[Path] = Nil
        val result =
          try code
          finally recording.foreach(r => files = stop(p, r, name, (System.nanoTime() - start) / 1000000, threshold))
        (result, files)
      } finally busy.set(false)
    case Left(reason) =>
      println(s"[AsyncProf] async-profiler unavailable, not profiling '$name': $reason")
      (code, Nil)
  }

  /** Waits until no other profiling session is running in the JVM (sbt runs test suites in parallel), and takes it. */
  private def acquire(name: String): Unit = {
    val b = busy
    if (!b.compareAndSet(false, true)) {
      println(s"[AsyncProf] another profiling session is running: '$name' waits for it to finish")
      while (!b.compareAndSet(false, true)) Thread.sleep(20)
    }
  }

  /** Stops profiling; returns the files written. */
  private def stop(p: Profiler, recording: Path, name: String, tookMs: Long, threshold: Long): Seq[Path] =
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
        Seq(jfr, html)
      } else {
        Files.deleteIfExists(recording)
        Nil
      }
    } catch {
      case t: Throwable =>
        println(s"[AsyncProf] could not stop profiling '$name': $t")
        Nil
    }
}
