package com.fastscala.demo.server

import com.fastscala.core.{FSSession, FSSystem}
import com.fastscala.demo.db.{CurrentUser, FakeDB}
import com.fastscala.demo.docs.*
import com.fastscala.demo.docs.bootstrap.BootstrapModalPage
import com.fastscala.demo.docs.chartjs.SimpleChartjsPage
import com.fastscala.demo.docs.forms.BasicFormExamplePage
import com.fastscala.demo.docs.tables.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.inScriptTag
import com.fastscala.routing.req.Get
import com.fastscala.routing.resp.{Ok, Response}
import com.fastscala.routing.{FilterUtils, RoutingHandlerHelper}
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.routing.ScalaXmlRoutingHandlerHelper
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import org.eclipse.jetty.server.{Request, Response as JettyServerResponse}
import org.eclipse.jetty.util.Callback
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.{Collections, Date}
import scala.jdk.CollectionConverters.ListHasAsScala

class RoutingHandler(implicit fss: FSSystem) extends ScalaXmlRoutingHandlerHelper {

  val logger = LoggerFactory.getLogger(getClass.getName)


  override def handlerNoSession(response: JettyServerResponse, callback: Callback)(implicit req: Request): Option[Response] = Some(req).collect {
    case Get("loaderio-4370139ed4f90c60359531343155344a") =>
      Ok.plain("loaderio-4370139ed4f90c60359531343155344a")
    case Get(".well-known", "acme-challenge", code) =>
      val file = "/opt/certs/.well-known/acme-challenge/" + code
      logger.debug(s"Asked for file $file")
      val contents = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8)
      logger.debug(s"Returning contents $contents")
      Ok.plain(contents)
  }

  override def handlerInSession(response: JettyServerResponse, callback: Callback)(implicit req: Request, session: FSSession): Option[Response] = {
    if (req.getHttpURI.getPath == "/basic1") {
      Some(Ok.html(
        """<html>
          |<body>
          |<h1>Basic example 1</h1>
          |</body>
          |</html>
          |""".stripMargin
      ))
    } else if (req.getHttpURI.getPath == "/basic2") {
      Some(session.createPage(implicit fsc => {
        val callback = fsc.callback(() => {
          println("clicked!")
          JS.void
        })
        Ok.html(
          <html>
            <body>
              <h1>Basic example 2</h1>
              <button onclick={callback.cmd}>Click me!</button>
              <p>Will fail: missing JS in the page head.</p>
            </body>
           </html>.toString()
        )
      }))
    } else if (req.getHttpURI.getPath == "/basic3") {
      Some(session.createPage(implicit fsc => {
        val callback = fsc.callback(() => {
          println("clicked!")
          JS.void
        })
        Ok.html(
          <html>
            <head>{fsc.fsPageScript().inScriptTag}</head>
            <body>
              <h1>Basic example 3</h1>
              <button onclick={callback.cmd}>Click me!</button>
              <p>On click, calling: <pre>{callback.cmd}</pre></p>
            </body>
           </html>.toString()
        )
      }))
    } else if (req.getHttpURI.getPath == "/basic4") {
      Some(session.createPage(implicit fsc => {
        val callback = fsc.callback(JS("document.getElementById('myInput').value"), str => {
          println(s"input has value: '$str'")
          JS.alert(s"The server has received your input ('$str') at ${new Date().toGMTString}")
        })
        Ok.html(
          <html>
              <head>{fsc.fsPageScript().inScriptTag}</head>
              <body>
                <h1>Basic example 4</h1>
                <input type="text" id="myInput"></input>
                <button onclick={callback.cmd}>Click me!</button>
                <p>On click, calling: <pre>{callback.cmd}</pre></p>
              </body>
             </html>.toString()
        )
      }))
    } else FilterUtils.onlyHandleHtmlRequests {
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

      FSDemoMainMenu.serve().map(servePage(_)).orElse({
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
      })
      //        .orElse(
      //        Some(Redirect.temporaryRedirect("/demo/"))
      //      )
    }
  }
}
