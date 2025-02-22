package com.fastscala.server

import com.fastscala.core.FSSystem
import com.fastscala.utils.Jetty12StatisticsCollector
import com.fastscala.websockets.FSWebsocketJettyContextHandler
import com.typesafe.config.ConfigFactory
import io.prometheus.metrics.exporter.httpserver.HTTPServer
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics
import org.eclipse.jetty.http.CompressedContentFormat
import org.eclipse.jetty.server.*
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.server.handler.{ContextHandler, ResourceHandler, StatisticsHandler}
import org.eclipse.jetty.util.VirtualThreads
import org.eclipse.jetty.util.resource.{ResourceFactory, Resources}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.slf4j.LoggerFactory

abstract class JettyServerHelper() {

  val logger = LoggerFactory.getLogger(getClass.getName)

  val config = ConfigFactory.load()

  def appName: String

  def buildFSSystem(): FSSystem = new FSSystem(appName = appName)

  implicit lazy val fss: FSSystem = buildFSSystem()

  def buildMainHandler(): Handler

  def NThreads = 200

  def Port: Int = config.getInt("com.fastscala.server.helper.port")

  def isLocal: Boolean = config.getBoolean("com.fastscala.server.helper.is-local")

  def setupPrometheusJvmMetrics: Boolean = config.getBoolean("com.fastscala.server.helper.setup-prometheus-jvm-metrics")

  def prometheusHttpServerEnabled: Boolean = config.getBoolean("com.fastscala.server.helper.prometheus-http-server.enabled")

  def prometheusHttpServerPort: Int = config.getInt("com.fastscala.server.helper.prometheus-http-server.port")

  val threadPool = new QueuedThreadPool(NThreads)
  threadPool.setName("http_server")
  // enable virtual thread support on JDK21+

  def useVirtualThreads: Boolean = config.getBoolean("com.fastscala.core.virtual-threads")

  if (useVirtualThreads) {
    threadPool.setVirtualThreadsExecutor(VirtualThreads.getDefaultVirtualThreadsExecutor())
  }

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
          fss,
          mainHandler,
          wsHandler,
          resourceHandler,
        ) :::
        appendToHandlerList): _*
    ))

    val statHandler = new StatisticsHandler(gzipHandler)
    // register prometheus metrics
    new Jetty12StatisticsCollector(statHandler).register()

    if (setupPrometheusJvmMetrics) {
      JvmMetrics.builder().register()
    }

    if (prometheusHttpServerEnabled) {
      try {
        HTTPServer.builder()
          .port(prometheusHttpServerPort)
          .buildAndStart()
      } catch {
        case ex: java.net.BindException if ex.getMessage.contains("Address already in use") =>
          logger.error(s"Prometheus port ($prometheusHttpServerPort) already in use")
      }
    }

    server.setHandler(statHandler)

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
