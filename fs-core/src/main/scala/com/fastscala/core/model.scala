package com.fastscala.core

import com.fastscala.core.FSContext.logger
import com.fastscala.js.{Js, JsUtils}
import com.fastscala.routing.RoutingHandlerNoSessionHelper
import com.fastscala.routing.req.{Get, Post}
import com.fastscala.routing.resp.*
import com.fastscala.stats.{FSStats, StatEvent}
import com.fastscala.utils.{IdGen, Missing}
import com.typesafe.config.ConfigFactory
import it.unimi.dsi.util.XoRoShiRo128PlusRandom
import org.eclipse.jetty.http.*
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.{Request, Response as JettyServerResponse}
import org.eclipse.jetty.util.Callback
import org.eclipse.jetty.websocket.api.Callback.Completable
import org.eclipse.jetty.websocket.api.Session
import org.slf4j.LoggerFactory

import java.net.URLEncoder
import java.nio.file.{Files, Path}
import java.util.Collections
import scala.jdk.CollectionConverters.{IterableHasAsScala, MapHasAsScala}
import scala.util.chaining.scalaUtilChainingOps

object JS extends JsUtils

class FSFunc(val id: String, val func: String => Js, var keepAliveAt: Long = System.currentTimeMillis(), val debugLbl: Option[String] = None)(implicit val fsc: FSContext) {

  def fullPath = fsc.fullPath + " => " + debugLbl.getOrElse("anon func")

  def allKeepAlivesIterable: Iterable[Long] = Iterable(keepAliveAt)

  def keepAlive(): Unit = {
    keepAliveAt = System.currentTimeMillis()
    fsc.keepAlive()
  }
}

class FSFileUpload(val id: String, val func: Seq[FSUploadedFile] => Js, var keepAliveAt: Long = System.currentTimeMillis())(implicit val fsc: FSContext) {

  def allKeepAlivesIterable: Iterable[Long] = Iterable(keepAliveAt)

  def keepAlive(): Unit = {
    keepAliveAt = System.currentTimeMillis()
    fsc.keepAlive()
  }
}

class FSFileDownload(val id: String, val contentType: String, val func: () => Array[Byte], var keepAliveAt: Long = System.currentTimeMillis(), val debugLbl: Option[String] = None)(implicit
  val fsc: FSContext
) {

  def allKeepAlivesIterable: Iterable[Long] = Iterable(keepAliveAt)

  def keepAlive(): Unit = {
    keepAliveAt = System.currentTimeMillis()
    fsc.keepAlive()
  }
}

class FSUploadedFile(val name: String, val submittedFileName: String, val contentType: String, val content: Array[Byte])

object FSUploadedFile {
  def unapply(file: FSUploadedFile): Option[(String, String, String, Array[Byte])] = Some((file.name, file.submittedFileName, file.contentType, file.content))
}
