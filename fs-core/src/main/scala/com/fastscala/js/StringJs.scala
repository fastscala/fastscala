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

class StringJs(str: String) extends Js {
  override def cmd: String = str

  override def foreachPart(f: String => Unit): Unit = f(str)

  lazy val lengthUtf8Enc: Long = com.google.common.base.Utf8.encodedLength(str)

  override def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit = sink.write(false, ByteBuffer.wrap(str.getBytes("UTF-8")), callback)
}
