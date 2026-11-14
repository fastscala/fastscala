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

class EncJs(js: Js, inString: Boolean = false) extends Js {
  override def cmd: String = {
    if (inString) '"' + Encode.forJavaScript(js.cmd) + '"'
    else Encode.forJavaScript(js.cmd)
  }

  override def foreachPart(f: String => Unit): Unit = f(cmd)

  override def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit = sink.write(false, ByteBuffer.wrap(cmd.getBytes("UTF-8")), callback)
}
