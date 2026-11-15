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
      // The extension: the word chars after the last '.' of the last segment ("" if there are none).
      val lastDot = path.lastIndexOf('.')
      val hasExt = lastDot > path.lastIndexOf('/') && lastDot < path.length - 1 &&
        path.substring(lastDot + 1).forall(c => Character.isLetterOrDigit(c) || c == '_')
      val (withoutExt, ext) = if (hasExt) (path.substring(0, lastDot), path.substring(lastDot + 1).toLowerCase) else (path, "")
      (method,
        withoutExt.split("/").toList.filter(_ != ""),
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

