package com.fastscala.test

import com.fastscala.core.FSSystem
import org.eclipse.jetty.http.{HttpCookie, HttpFields, HttpURI, HttpVersion, MimeTypes}
import org.eclipse.jetty.io.{Connection, Content}
import org.eclipse.jetty.server.{Components, ConnectionMetaData, Connector, Context, HttpConfiguration, HttpStream, Request, Session, TunnelSupport, Response => JettyServerResponse}
import org.eclipse.jetty.util.Callback
import org.eclipse.jetty.util.resource.Resource

import java.io.File
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.util
import java.util.concurrent.{CompletableFuture, TimeoutException}
import java.util.function
import java.util.function.{Consumer, Predicate, Supplier}

object Main {

  def main(args: Array[String]): Unit = {
    new FSSystem().handlerNoSession(new JettyServerResponse {
      override def getRequest: Request = ???

      override def getStatus: Int = ???

      override def setStatus(code: Int): Unit = ???

      override def getHeaders: HttpFields.Mutable = ???

      override def getTrailersSupplier: Supplier[HttpFields] = ???

      override def setTrailersSupplier(trailers: Supplier[HttpFields]): Unit = ???

      override def isCommitted: Boolean = ???

      override def hasLastWrite: Boolean = ???

      override def isCompletedSuccessfully: Boolean = ???

      override def reset(): Unit = ???

      override def writeInterim(status: Int, headers: HttpFields): CompletableFuture[Void] = ???

      override def write(last: Boolean, byteBuffer: ByteBuffer, callback: Callback): Unit = ???
    }, new Callback {

    })(new Request {
      override def getId: String = ???

      override def getComponents: Components = ???

      override def getConnectionMetaData: ConnectionMetaData = new ConnectionMetaData {
        override def getId: String = ???

        override def getHttpConfiguration: HttpConfiguration = new HttpConfiguration()

        override def getHttpVersion: HttpVersion = ???

        override def getProtocol: String = ???

        override def getConnection: Connection = ???

        override def getConnector: Connector = ???

        override def isPersistent: Boolean = ???

        override def getRemoteSocketAddress: SocketAddress = ???

        override def getLocalSocketAddress: SocketAddress = ???

        override def removeAttribute(name: String): AnyRef = ???

        override def setAttribute(name: String, attribute: Any): AnyRef = ???

        override def getAttribute(name: String): AnyRef = ???

        override def getAttributeNameSet: util.Set[String] = ???
      }

      override def getMethod: String = "GET"

      override def getHttpURI: HttpURI = HttpURI.from("http://localhost:9064/")

      override def getContext: Context = new Context {
        override def getContextPath: String = ???

        override def getClassLoader: ClassLoader = ???

        override def getBaseResource: Resource = ???

        override def getErrorHandler: Request.Handler = ???

        override def getVirtualHosts: util.List[String] = ???

        override def getMimeTypes: MimeTypes = ???

        override def execute(task: Runnable): Unit = ???

        override def run(task: Runnable): Unit = ???

        override def run(task: Runnable, request: Request): Unit = ???

        override def getTempDirectory: File = ???

        override def removeAttribute(name: String): AnyRef = ???

        override def setAttribute(name: String, attribute: Any): AnyRef = ???

        override def getAttribute(name: String): AnyRef = null

        override def getAttributeNameSet: util.Set[String] = ???

        override def decorate[T](o: T): T = ???

        override def destroy(o: Any): Unit = ???
      }

      override def getHeaders: HttpFields = ???

      override def demand(demandCallback: Runnable): Unit = ???

      override def getTrailers: HttpFields = ???

      override def getBeginNanoTime: Long = ???

      override def getHeadersNanoTime: Long = ???

      override def isSecure: Boolean = ???

      override def read(): Content.Chunk = ???

      override def consumeAvailable(): Boolean = ???

      override def addIdleTimeoutListener(onIdleTimeout: Predicate[TimeoutException]): Unit = ???

      override def addFailureListener(onFailure: Consumer[Throwable]): Unit = ???

      override def getTunnelSupport: TunnelSupport = ???

      override def addHttpStreamWrapper(wrapper: function.Function[HttpStream, HttpStream]): Unit = ???

      override def getSession(create: Boolean): Session = ???

      override def removeAttribute(name: String): AnyRef = ???

      override def setAttribute(name: String, attribute: Any): AnyRef = ???

      override def getAttribute(name: String): AnyRef = new util.ArrayList[HttpCookie]()

      override def getAttributeNameSet: util.Set[String] = ???

      override def fail(failure: Throwable): Unit = ???
    })
  }
}
