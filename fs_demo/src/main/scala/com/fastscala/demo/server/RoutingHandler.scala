package com.fastscala.demo.server

import com.fastscala.core.{FSSession, FSSystem}
import com.fastscala.demo.db.{CurrentUser, FakeDB}
import com.fastscala.demo.docs._
import com.fastscala.demo.docs.bootstrap.BootstrapModalPage
import com.fastscala.demo.docs.chartjs.SimpleChartjsPage
import com.fastscala.demo.docs.forms.BasicFormExamplePage
import com.fastscala.demo.docs.tables._
import com.fastscala.server.{Ok, Redirect, Response, RoutingHandlerHelper}
import com.fastscala.xml.scala_xml.FSScalaXmlEnv
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.fsXmlSupport
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

class RoutingHandler()(implicit fss: FSSystem) extends RoutingHandlerHelper {

  val logger = LoggerFactory.getLogger(getClass.getName)

  import com.fastscala.server.RoutingHandlerHelper._

  override def handlerNoSession(implicit req: HttpServletRequest): Option[Response] = Some(req).collect {
    case Get("loaderio-4370139ed4f90c60359531343155344a") =>
      Ok.plain("loaderio-4370139ed4f90c60359531343155344a")
    case Get(".well-known", "acme-challenge", code) =>
      val file = "/opt/certs/.well-known/acme-challenge/" + code
      logger.debug(s"Asked for file $file")
      val contents = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8)
      logger.debug(s"Returning contents $contents")
      Ok.plain(contents)
  }

  override def handlerInSession(implicit req: HttpServletRequest, session: FSSession): Option[Response] = {
    onlyHandleHtmlRequests {
      if (CurrentUser().isEmpty) {
        Option(req.getCookies).getOrElse(Array()).find(_.getName == "user_token").map(_.getValue).filter(_.trim != "").orElse(
          Option(req.getParameterValues("user_token")).getOrElse(Array[String]()).headOption.filter(_.trim != "")
        ).foreach(token => {
          FakeDB.users.find(_.loginToken == token).foreach(user => {
            CurrentUser() = user
          })
        })
      }

      FSDemoMainMenu.serve().map(servePage[FSScalaXmlEnv.type](_)).orElse({
        Some(req).collect {
          case Get("demo") => servePage(new SimpleTableExamplePage())
          case Get("demo", "simple_tables") => servePage(new SimpleTableExamplePage())
          case Get("demo", "sortable_tables") => servePage(new SortableTableExamplePage())
          case Get("demo", "paginated_tables") => servePage(new PaginatedTableExamplePage())
          case Get("demo", "selectable_rows_tables") => servePage(new SelectableRowsTableExamplePage())
          case Get("demo", "tables_sel_cols") => servePage(new SelectableColsTableExamplePage())
          case Get("demo", "simple_form") => servePage(new BasicFormExamplePage())
          case Get("demo", "simple_modal") => servePage(new BootstrapModalPage())

          case Get("demo", "chartjs", "simple") => servePage(new SimpleChartjsPage())
        }
      }).orElse(
        Some(Redirect.temporaryRedirect("/demo/"))
      )
    }
  }
}
