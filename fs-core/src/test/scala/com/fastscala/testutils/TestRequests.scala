package com.fastscala.testutils

import org.eclipse.jetty.http.HttpURI
import org.eclipse.jetty.server.Request

import java.lang.reflect.{InvocationHandler, Method, Proxy}

/** Jetty Requests for tests that only look at the method and the URI: a proxy (any other call throws). */
object TestRequests {

  def request(method: String, uri: String): Request = {
    val httpUri = HttpURI.from(uri)
    val handler = new InvocationHandler {
      override def invoke(proxy: AnyRef, m: Method, args: Array[AnyRef]): AnyRef = m.getName match {
        case "getMethod" => method
        case "getHttpURI" => httpUri
        case "toString" => s"TestRequest($method $uri)"
        case "hashCode" => Int.box(System.identityHashCode(proxy))
        case "equals" => Boolean.box(proxy eq args(0))
        case other => throw new UnsupportedOperationException(s"TestRequest.$other")
      }
    }
    Proxy.newProxyInstance(getClass.getClassLoader, Array(classOf[Request]), handler).asInstanceOf[Request]
  }
}
