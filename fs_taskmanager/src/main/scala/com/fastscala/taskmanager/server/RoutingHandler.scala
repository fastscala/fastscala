package com.fastscala.taskmanager.server

import com.fastscala.core.{FSSession, FSSystem}
import com.fastscala.demo.db.{CurrentUser, FakeDB}
import com.fastscala.routing.req.Get
import com.fastscala.routing.resp.{Ok, Redirect, Response}
import com.fastscala.routing.{FilterUtils, RoutingHandlerHelper}
import com.fastscala.scala_xml.routing.ScalaXmlRoutingHandlerHelper
import com.fastscala.taskmanager.app.{FSTaskmanagerMainMenu, TasksPage}
import org.eclipse.jetty.server.{Request, Response as JettyServerResponse}
import org.eclipse.jetty.util.Callback
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.Collections
import scala.jdk.CollectionConverters.ListHasAsScala

class RoutingHandler(implicit fss: FSSystem) extends ScalaXmlRoutingHandlerHelper {

  val logger = LoggerFactory.getLogger(getClass.getName)

  override def handlerNoSession(response: JettyServerResponse, callback: Callback)(implicit req: Request): Option[Response] = Some(req).collect {
    case Get(".well-known", "acme-challenge", code) =>
      val file = "/opt/certs/.well-known/acme-challenge/" + code
      logger.debug(s"Asked for file $file")
      val contents = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8)
      logger.debug(s"Returning contents $contents")
      Ok.plain(contents)
  }

  override def handlerInSession(response: JettyServerResponse, callback: Callback)(implicit req: Request, session: FSSession): Option[Response] = {
    FilterUtils.onlyHandleHtmlRequests {
      if (CurrentUser().isEmpty) {
        val cookies = Option(Request.getCookies(req)).getOrElse(Collections.emptyList).asScala
        cookies.find(_.getName == "user_token").map(_.getValue).filter(_.trim != "").orElse(
          Option(Request.getParameters(req).getValues("user_token")).getOrElse(Collections.emptyList).asScala.headOption.filter(_.trim != "")
        ).foreach(token => {
          FakeDB.users.find(_.loginToken == token).foreach(user => {
            CurrentUser() = user
          })
        })
      }

      FSTaskmanagerMainMenu.serve().map(servePage(_)).orElse({
        Some(req).collect {
          case Get() | Get("tasks") => servePage(new TasksPage())
        }
      }).orElse(
        Some(Redirect.temporaryRedirect("/demo/"))
      )
    }
  }
}
