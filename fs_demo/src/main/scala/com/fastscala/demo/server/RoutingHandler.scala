package com.fastscala.demo.server

import com.fastscala.core.{FSSession, FSSystem}
import com.fastscala.demo.examples.{AnonymousPage, FileDownloadPage, FileUploadPage, MainMenu, ServerSidePushPage, SimpleModalPage}
import com.fastscala.demo.examples.bootstrap.{BootstrapButtonsPage, BootstrapExamplePage}
import com.fastscala.demo.examples.chartjs.SimpleChartjsPage
import com.fastscala.demo.examples.forms.BasicFormExamplePage
import com.fastscala.demo.examples.tables.{PaginatedTableExamplePage, SelectableColsTableExamplePage, SelectableRowsTableExamplePage, SimpleTableExamplePage, SortableTableExamplePage}
import com.fastscala.server.{RoutingHandlerHelper, Ok, Response}
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
    MainMenu.serve().map(servePage(_)).orElse {
      Some(req).collect {
        case Get() => servePage(new SimpleTableExamplePage())
        case Get("bootstrap") => servePage(new BootstrapExamplePage())
        case Get("bootstrap", "buttons") => servePage(new BootstrapButtonsPage())
        case Get("simple_tables") => servePage(new SimpleTableExamplePage())
        case Get("sortable_tables") => servePage(new SortableTableExamplePage())
        case Get("paginated_tables") => servePage(new PaginatedTableExamplePage())
        case Get("selectable_rows_tables") => servePage(new SelectableRowsTableExamplePage())
        case Get("tables_sel_cols") => servePage(new SelectableColsTableExamplePage())
        case Get("simple_form") => servePage(new BasicFormExamplePage())
        case Get("simple_modal") => servePage(new SimpleModalPage())
        case Get("file_upload") => servePage(new FileUploadPage())
        case Get("anon_page") => servePage(new AnonymousPage())
        case Get("file_download") => servePage(new FileDownloadPage())
        case Get("server_side_push") => servePage(new ServerSidePushPage())

        case Get("chartjs", "simple") => servePage(new SimpleChartjsPage())
      }
    }
  }
}
