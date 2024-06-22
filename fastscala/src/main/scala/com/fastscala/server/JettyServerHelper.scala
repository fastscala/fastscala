package com.fastscala.server

import com.fastscala.core.FSSystem
import com.fastscala.utils.FSOptimizedResourceHandler
import com.fastscala.websockets.FSWebsocketServletContextHandler
import com.typesafe.config.ConfigFactory
import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet
import org.eclipse.jetty.http.CompressedContentFormat
import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.server.handler.{ContextHandler, HandlerList, ResourceHandler}
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.util.resource.{EmptyResource, Resource}
import org.eclipse.jetty.util.thread.QueuedThreadPool

import java.io.{File, InputStream}
import java.net.URI
import java.nio.channels.ReadableByteChannel

abstract class JettyServerHelper() {

  val config = ConfigFactory.load()

  def appName: String

  implicit val fss: FSSystem = new FSSystem(appName = appName)

  def buildMainHandler(): Handler

  def NThreads = 200

  def Port: Int = config.getInt("com.fastscala.demo.server.port")

  def isLocal: Boolean = config.getBoolean("com.fastscala.demo.server.local")

  val threadPool = new QueuedThreadPool(NThreads)
  threadPool.setName("http_server")

  val server = new Server(threadPool)

  def preStart(): Unit = {}

  def postStart(): Unit = {}

  def postStop(): Unit = {}

  def resourceRoots: Seq[String] = Vector("/web")

  def prependToHandlerList: List[Handler] = Nil

  def appendToHandlerList: List[Handler] = Nil

  def main(args: Array[String]): Unit = {

    preStart()

    val connector = new ServerConnector(server)
    connector.setPort(Port)
    connector.setHost("0.0.0.0")
    connector.setAcceptQueueSize(128)
    server.addConnector(connector)

    val jettyStaticFilesHandler = new FSOptimizedResourceHandler(resourceRoots)
    val resourceService: ResourceService = new ResourceService()
    val precompressedFormats = Array(CompressedContentFormat.GZIP, CompressedContentFormat.BR, new CompressedContentFormat("bzip", ".bz"))
    val resourceHandler: ResourceHandler = new ResourceHandler(resourceService) {
      override def doStart(): Unit = {
        super.doStart()
        val cachedContentFactory = new CachedContentFactory(null, this, getMimeTypes, true, true, precompressedFormats)
        resourceService.setContentFactory(cachedContentFactory)
      }
    }
    val metricsHandler = new ServletContextHandler()
    metricsHandler.addServlet(new ServletHolder(new MetricsServlet()), "/")
    resourceHandler.setCacheControl("public, max-age=31536000")
    resourceHandler.setBaseResource(new Resource {
      override def isContainedIn(r: Resource): Boolean = ???

      override def close(): Unit = ???

      override def exists(): Boolean = ???

      override def isDirectory: Boolean = ???

      override def lastModified(): Long = ???

      override def length(): Long = ???

      override def getURI: URI = ???

      override def getFile: File = ???

      override def getName: String = ???

      override def getInputStream: InputStream = ???

      override def getReadableByteChannel: ReadableByteChannel = ???

      override def delete(): Boolean = ???

      override def renameTo(dest: Resource): Boolean = ???

      override def list(): Array[String] = ???

      override def addPath(path: String): Resource =
        resourceRoots.iterator.map(root => {
          Option(Resource.newClassPathResource(root + path)).filter(!_.isDirectory)
        }).find(_.isDefined).flatten.getOrElse(EmptyResource.INSTANCE)
    })
    resourceHandler.setDirAllowed(false)
    resourceHandler.setDirectoriesListed(false)
    val wsHandler = new FSWebsocketServletContextHandler(server)
    val gzipHandler = new GzipHandler() {
      override def isMimeTypeGzipable(mimetype: String): Boolean = true
    }
    gzipHandler.setInflateBufferSize(4096)
    gzipHandler.addIncludedMethods("GET", "POST", "PUT")

    val mainHandler = buildMainHandler()

    gzipHandler.setHandler(new HandlerList(
      (prependToHandlerList :::
        List(
          new ContextHandler("/metrics") {
            setHandler(metricsHandler)
          }
          , new ContextHandler("/static/optimized") {
            setHandler(new HandlerList(
              jettyStaticFilesHandler
            ))
          },
          fss
          , mainHandler
          , wsHandler
          , resourceHandler
        ) :::
        appendToHandlerList): _*
    ))
    server.setHandler(gzipHandler)

    server.start()
    println(s"Binded to port $Port")
    postStart()

    if (isLocal) {
      println("Local setup")
      Console.in.readLine()
      println("Stopping...")
      server.stop()
      println("Stopped the server")
      postStop()
      println("Post stop finished running")
    } else {
      while (true) Thread.sleep(10000)
    }
  }
}
