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


object JsHelper {

  val secureRandom = new SecureRandom()

  implicit class RichJs(val js: Js) extends AnyVal {

    def `_==`(other: Js): Js = Js(s"${js.cmd} == ${other.cmd}")

    def `_!=`(other: Js): Js = Js(s"${js.cmd} != ${other.cmd}")

    def `_===`(other: Js): Js = Js(s"${js.cmd} === ${other.cmd}")

    def `_!==`(other: Js): Js = Js(s"${js.cmd} !== ${other.cmd}")

    def `_>`(other: Js): Js = Js(s"${js.cmd} > ${other.cmd}")

    def `_>=`(other: Js): Js = Js(s"${js.cmd} >= ${other.cmd}")

    def `_<`(other: Js): Js = Js(s"${js.cmd} < ${other.cmd}")

    def `_<=`(other: Js): Js = Js(s"${js.cmd} <= ${other.cmd}")

    def `_&&`(other: Js): Js = Js(s"${js.cmd} && ${other.cmd}")

    def `_||`(other: Js): Js = Js(s"${js.cmd} || ${other.cmd}")

    def `_=`(other: Js): Js = Js(s"${js.cmd} = ${other.cmd};")

    def `_===null`: Js = Js(s"${js.cmd} === null")

    def asVar(code: Js => Js): Js = {
      val name = "v_" + "%04X".formatted(secureRandom.nextInt())
      Js(s"var $name = (${js.cmd}); " + code(Js(name)))
    }
  }

}
