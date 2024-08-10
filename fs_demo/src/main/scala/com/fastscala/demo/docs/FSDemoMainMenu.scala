package com.fastscala.demo.docs

import com.fastscala.demo.docs.about.{AboutPage, AuthorPage, GettingStartedPage}
import com.fastscala.demo.docs.bootstrap.{BootstrapButtonsPage, BootstrapImagesPage, BootstrapModalPage, BootstrapTypographyPage}
import com.fastscala.demo.docs.fastscala.{AnonymousPage, CallbacksPage, FileDownloadPage, FileUploadPage, RerenderablePage, ServerSidePushPage}
import com.fastscala.demo.docs.forms.FormInputTypesPage
import com.fastscala.demo.docs.html.{HtmlTagsPage, HtmlUtilsPage, ScalaTagsPage}
import com.fastscala.demo.docs.js.JsUtilsPage

object FSDemoMainMenu extends Menu(
  MenuSection("About FastScala")(
    new RoutingMenuItem()("About", () => new AboutPage())
    , new RoutingMenuItem("getting_started")("Getting Started", () => new GettingStartedPage())
    , new RoutingMenuItem("author")("Author", () => new AuthorPage())
  )
  , MenuSection("FastScala Basics")(
    new RoutingMenuItem("demo", "fastscala", "callbacks")("Callbacks", () => new CallbacksPage())
    , new RoutingMenuItem("demo", "fastscala", "rerenderable")("Rerenderable", () => new RerenderablePage())
    , new RoutingMenuItem("demo", "fastscala", "file_upload")("File Upload", () => new FileUploadPage())
    , new RoutingMenuItem("demo", "fastscala", "anon_page")("Anonymous Page", () => new AnonymousPage())
    , new RoutingMenuItem("demo", "fastscala", "file_download")("File Download", () => new FileDownloadPage())
    , new RoutingMenuItem("demo", "fastscala", "server_side_push")("Server Side Push", () => new ServerSidePushPage())
  ),
  MenuSection("HTML utils")(
    new RoutingMenuItem("demo", "html", "tags")("tags", () => new HtmlTagsPage())
    , new RoutingMenuItem("demo", "html", "utils")("utils", () => new HtmlUtilsPage())
    , new RoutingMenuItem("demo", "html", "scala-tags")("Integrating ScalaTags", () => new ScalaTagsPage())
  ),
  MenuSection("Js utils")(
    new RoutingMenuItem("demo", "js", "overview")("Overview", () => new JsUtilsPage()),
    SimpleMenuItem("BarChart", "/demo/chartjs/simple")
  ),
  MenuSection("Bootstrap utils")(
    new RoutingMenuItem("demo", "bootstrap", "buttons")("Buttons", () => new BootstrapButtonsPage())
    , new RoutingMenuItem("demo", "bootstrap", "typography")("Typography", () => new BootstrapTypographyPage())
    , new RoutingMenuItem("demo", "bootstrap", "images")("Images", () => new BootstrapImagesPage())
    , new RoutingMenuItem("demo", "bootstrap", "modal")("Modal", () => new BootstrapModalPage())
  ),
  MenuSection("Forms Lib")(
    SimpleMenuItem("Simple", "/demo/simple_form")
    , new RoutingMenuItem("demo", "forms", "input_types")("Input Types", () => new FormInputTypesPage())
  ),
  MenuSection("Table Lib")(
    SimpleMenuItem("Simple", "/demo/simple_tables")
    , SimpleMenuItem("Sortable", "/demo/sortable_tables")
    , SimpleMenuItem("Paginated", "/demo/paginated_tables")
    , SimpleMenuItem("Selectable Rows", "/demo/selectable_rows_tables")
    , SimpleMenuItem("Selectable Columns", "/demo/tables_sel_cols")
  ),
  //  MenuSection("chart.js integration")(
  //    SimpleMenuItem("Simple", "/demo/chartjs/simple")
  //  ),
)
