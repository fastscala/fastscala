package com.fastscala.routing.req

import com.fastscala.routing.method.Method
import org.eclipse.jetty.server.Request


abstract class UnapplyHelper1(methodName: String) {
  def unapplySeq(req: Request): Option[Seq[String]] =
    Some(req.getHttpURI.getPath.replaceAll("^/", "").split("/").toList.filter(_ != "")).filter(_ => req.getMethod == methodName)
}

object Req {

  def unapply(req: Request): Option[(Method, List[String], String, Boolean)] = {
    Method.fromString.unapply(req.getMethod).map(method => {
      val path = req.getHttpURI.getPath
      val ext = path.replaceAll(".*\\.(\\w+)$", "$1").toLowerCase
      (method,
        path.replaceAll("^/", "").replaceAll(s"\\.$ext$$", "").split("/").toList.filter(_ != ""),
        ext,
        path.endsWith("/"))
    })
  }
}

object Get extends UnapplyHelper1("GET")

object Head extends UnapplyHelper1("HEAD")

object Post extends UnapplyHelper1("POST")

object Put extends UnapplyHelper1("PUT")

object Delete extends UnapplyHelper1("DELETE")

object Connect extends UnapplyHelper1("CONNECT")

object Options extends UnapplyHelper1("OPTIONS")

object Trace extends UnapplyHelper1("TRACE")

object Patch extends UnapplyHelper1("PATCH")

