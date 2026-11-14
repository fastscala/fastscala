package com.fastscala.js

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.{BufferUtil, Callback}

/**
 * Writes a [[Js]]'s code (UTF-8) into a [[Content.Sink]] (e.g. a response), without blocking: completes `callback` once,
 * after the last write (or fails it). The content is ended by the last write only if `endContent`.
 *
 * [[JsWriter.Recursive]] (fs-core) has each Js write itself; fs-scala-xml's `XmlJsStream` writes them iteratively,
 * through its own buffer (with the XML in them).
 */
trait JsWriter {
  def writeJs(js: Js, sink: Content.Sink, callback: Callback, endContent: Boolean): Unit
}

object JsWriter {

  /** Each Js writes itself ([[Js.writeUtf8Enc]], recursively); the content is ended with an extra, empty, last write. */
  object Recursive extends JsWriter {
    override def writeJs(js: Js, sink: Content.Sink, callback: Callback, endContent: Boolean): Unit =
      if (!endContent) js.writeUtf8Enc(sink, callback)
      else js.writeUtf8Enc(sink, Callback.from(
        () => sink.write(true, BufferUtil.EMPTY_BUFFER, callback),
        (t: Throwable) => callback.failed(t)
      ))
  }
}
