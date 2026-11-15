package com.fastscala.js

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

/**
 * Writes a [[Js]]'s code (UTF-8) into a [[Content.Sink]] (e.g. a response), without blocking: completes `callback` once,
 * after the last write (or fails it). The content is ended by the last write only if `endContent`.
 *
 * [[JsWriter.Cmd]] (fs-core) writes the Js's `cmd`; fs-scala-xml's `XmlJsStream` writes it part by part, through its
 * own buffer (with the XML in it), without building it.
 */
trait JsWriter {
  def writeJs(js: Js, sink: Content.Sink, callback: Callback, endContent: Boolean): Unit
}

object JsWriter {

  /** The Js's `cmd` (built, as one String; a null one is `null`), in one write. */
  object Cmd extends JsWriter {
    override def writeJs(js: Js, sink: Content.Sink, callback: Callback, endContent: Boolean): Unit =
      try {
        sink.write(endContent, ByteBuffer.wrap(String.valueOf(js.cmd: AnyRef).getBytes(UTF_8)), callback)
      } catch {
        case t: Throwable => callback.failed(t)
      }
  }
}
