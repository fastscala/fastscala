package com.fastscala.js

import com.fastscala.utils.Utf8ToWriterSink
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.owasp.encoder.{EncodedWriter, Encode, Encoders}

import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * `js`, escaped for a JavaScript string literal (with OWASP's `Encode.forJavaScript`); with `inString`, quoted too: a
 * string literal whose value is `js`'s code (e.g. `new EncJs(new NodeSeqJs(ns), inString = true)`, HTML for innerHTML).
 *
 * Written as it's escaped: `js` writes its UTF-8 bytes into a [[Utf8ToWriterSink]], which decodes them into an OWASP
 * [[EncodedWriter]] (JavaScript) on an OutputStreamWriter (UTF-8) on the sink. So `js`'s code is never one String
 * (only [[cmd]] builds it). The sink's writes block until each is taken.
 */
class EncJs(val js: Js, val inString: Boolean = false) extends Js {

  override def cmd: String = {
    if (inString) '"' + Encode.forJavaScript(js.cmd) + '"'
    else Encode.forJavaScript(js.cmd)
  }

  override def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit = {
    // Not closed: closing the sink's OutputStream would write last=true and end the content (this is a part of it).
    val utf8 = new OutputStreamWriter(Content.Sink.asOutputStream(sink), StandardCharsets.UTF_8)
    val encoded = new EncodedWriter(utf8, Encoders.JAVASCRIPT)
    val decoded = new Utf8ToWriterSink(encoded)
    try {
      if (inString) utf8.write('"')
      js.writeUtf8Enc(decoded, Callback.from(
        () =>
          try {
            decoded.finish() // (flushes the encoded writer, so its chars are in utf8 before the closing quote)
            if (inString) utf8.write('"')
            utf8.flush()
            callback.succeeded()
          } catch {
            case t: Throwable => callback.failed(t)
          },
        (t: Throwable) => callback.failed(t)
      ))
    } catch {
      case t: Throwable => callback.failed(t)
    }
  }
}
