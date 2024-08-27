package com.fastscala.server

import com.fastscala.core.FSSystem
import com.fastscala.utils.FSOptimizedResourceHandler
import com.fastscala.websockets.FSWebsocketJettyContextHandler
import com.typesafe.config.ConfigFactory
import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet
import org.eclipse.jetty.http.CompressedContentFormat
import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.server.handler.{ContextHandler, ResourceHandler}
import org.eclipse.jetty.ee10.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.util.resource.{Resources, ResourceFactory, Resource}
import org.eclipse.jetty.util.thread.QueuedThreadPool

import java.io.{File, InputStream}
import java.net.URI
import java.nio.channels.ReadableByteChannel
import java.util.concurrent.Executors
import org.eclipse.jetty.util.VirtualThreads

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
  // enable virtual thread support on JDK21+
  threadPool.setVirtualThreadsExecutor(VirtualThreads.getDefaultVirtualThreadsExecutor())

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

    val metricsHandler = new ServletContextHandler()
    metricsHandler.addServlet(new ServletHolder(new MetricsServlet()), "/")

    val resourceHandler = new ResourceHandler()
    resourceHandler.setPrecompressedFormats(CompressedContentFormat.GZIP, CompressedContentFormat.BR, new CompressedContentFormat("bzip", ".bz"))
    resourceHandler.setEtags(true)
    resourceHandler.setCacheControl("public, max-age=31536000")
    resourceHandler.setDirAllowed(false)
    resourceHandler.setBaseResource(ResourceFactory.combine({
      val resourceFactory = ResourceFactory.of(resourceHandler)
      resourceRoots.map(resourceFactory.newClassLoaderResource(_))
                   .filter(Resources.isReadableDirectory(_))
    }: _*))

    val wsHandler = FSWebsocketJettyContextHandler(server, "/" + fss.FSPrefix)
    val gzipHandler = new GzipHandler() {
      override def isMimeTypeDeflatable(mimetype: String): Boolean = true
    }
    gzipHandler.setInflateBufferSize(4096)
    gzipHandler.addIncludedMethods("GET", "POST", "PUT")

    val mainHandler = buildMainHandler()

    gzipHandler.setHandler(new Handler.Sequence(
      (prependToHandlerList :::
        List(
          new ContextHandler(metricsHandler, "/metrics")
          , new ContextHandler(jettyStaticFilesHandler, "/static/optimized")
          , fss
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
