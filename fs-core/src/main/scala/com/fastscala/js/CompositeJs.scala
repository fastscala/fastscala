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

/**
 * Its children's code, one after the other. Nothing is put between them: a separator (e.g. `;`) is one of the children
 * (see [[CompositeJs.joined]]).
 */
class CompositeJs(val children: Js*) extends Js {

  override def cmd: String = children.map(_.cmd).mkString
}

object CompositeJs {

  /** `children`, with `separator` between them (as children too: built once, here). */
  def joined(children: collection.Seq[Js], separator: Js): CompositeJs = {
    val parts = Vector.newBuilder[Js]
    var first = true
    children.foreach { child =>
      if (!first) parts += separator
      first = false
      parts += child
    }
    new CompositeJs(parts.result() *)
  }
}
