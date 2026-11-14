package com.fastscala.js

import com.fastscala.core.FSContext
import com.fastscala.utils.IdGen
import org.apache.commons.text.StringEscapeUtils
import org.eclipse.jetty.http.{HttpHeader, MimeTypes}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Response
import org.eclipse.jetty.util.{BufferUtil, Callback, IteratingCallback}
import org.owasp.encoder.Encode

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Date

class CompositeJs private(val children: Js*)(val separator: Js = Js.Void) extends Js {

  def this(children: Js*) = this(children *)(Js.Void)

  def withSeparator(sep: Js) = new CompositeJs(children *)(sep)

  override def cmd: String = children.map(_.cmd).mkString(separator.cmd)

  /** The children's parts, with the separator's between them: `cmd`, without joining it. */
  override def foreachPart(f: String => Unit): Unit = {
    var first = true
    children.foreach { child =>
      if (!first && (separator ne Js.Void)) separator.foreachPart(f)
      first = false
      child.foreachPart(f)
    }
  }

  /**
   * Writes the children (with separators between them) one at a time: each part's write starts only once the previous
   * one has completed, and `callback` completes exactly once — after the last write, or on the first failure.
   * Every part's `writeUtf8Enc` must complete the callback it is given exactly once.
   */
  override def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit = {
    val parts: Iterator[Js] =
      if (separator == Js.Void) children.iterator
      else children.iterator.zipWithIndex.flatMap((child, idx) => if (idx == 0) Iterator(child) else Iterator(separator, child))

    new IteratingCallback() {
      override def process(): IteratingCallback.Action =
        if (parts.hasNext) {
          parts.next().writeUtf8Enc(sink, this) // calls succeeded()/failed() on `this`, which re-enters process()
          IteratingCallback.Action.SCHEDULED
        } else IteratingCallback.Action.SUCCEEDED

      override def onCompleteSuccess(): Unit = callback.succeeded()

      override def onCompleteFailure(cause: Throwable): Unit = callback.failed(cause)
    }.iterate()
  }
}
