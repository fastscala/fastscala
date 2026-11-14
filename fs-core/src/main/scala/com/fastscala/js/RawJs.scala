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

class RawJs(str: String) extends Js {
  override def cmd: String = str

  override def foreachPart(f: String => Unit): Unit = f(str)

  override def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit = sink.write(false, ByteBuffer.wrap(str.getBytes("UTF-8")), callback)
}

/**
 * `js"..."`: JavaScript as it's written. Raw (as Scala's `raw"..."`): backslashes are kept (`\n` stays a backslash and an
 * `n`, as JavaScript string literals need), and interpolated values are inserted as they are, not escaped or quoted:
 * a [[Js]] as a part of the result (so it's written part by part, its `cmd` never built: see [[Js.foreachPart]]),
 * anything else as its `toString`. With no Js values, the result is a [[RawJs]]; with some, a [[CompositeJs]] of the
 * literal text (as RawJs) and them.
 *
 * Use with `import com.fastscala.js.js` (or `com.fastscala.js.*`).
 */
extension (sc: StringContext) def js(args: Any*): Js =
  if (!args.exists(_.isInstanceOf[Js])) Js(StringContext.standardInterpolator(identity, args, sc.parts))
  else {
    val parts = Seq.newBuilder[Js]
    val text = new java.lang.StringBuilder // literal text (and non-Js values) since the last Js value

    def flushText(): Unit = if (text.length > 0) {
      parts += Js(text.toString)
      text.setLength(0)
    }

    val literals = sc.parts.iterator
    text.append(literals.next())
    args.foreach { arg =>
      arg match {
        case js: Js =>
          flushText()
          if (js ne Js.Void) parts += js
        case other => text.append(String.valueOf(other))
      }
      text.append(literals.next())
    }
    flushText()
    Js(parts.result() *)
  }
