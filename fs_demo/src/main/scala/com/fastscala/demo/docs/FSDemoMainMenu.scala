package com.fastscala.demo.docs

import com.fastscala.demo.docs.about.AboutPage
import com.fastscala.demo.docs.bootstrap.{BootstrapImagesPage, BootstrapTypographyPage}
import com.fastscala.demo.docs.fastscala.{CallbacksPage, RerenderablePage}
import com.fastscala.demo.docs.forms.FormInputTypesPage

object FSDemoMainMenu extends Menu(
  MenuSection("About FastScala")(
    new RoutingMenuItem()("About", () => new AboutPage())
  )
  , MenuSection("FastScala Basics")(
    new RoutingMenuItem("demo", "fastscala", "callbacks")("Callbacks", () => new CallbacksPage())
    , new RoutingMenuItem("demo", "fastscala", "rerenderable")("Rerenderable", () => new RerenderablePage())
  ),
  MenuSection("Bootstrap")(
    SimpleMenuItem("Basics", "/demo/bootstrap")
    , SimpleMenuItem("Buttons", "/demo/bootstrap/buttons")
    , new RoutingMenuItem("demo", "bootstrap", "typography")("Typography", () => new BootstrapTypographyPage())
    , new RoutingMenuItem("demo", "bootstrap", "images")("Images", () => new BootstrapImagesPage())
  ),
  MenuSection("Tables")(
    SimpleMenuItem("Simple", "/demo/simple_tables")
    , SimpleMenuItem("Sortable", "/demo/sortable_tables")
    , SimpleMenuItem("Paginated", "/demo/paginated_tables")
    , SimpleMenuItem("Selectable Rows", "/demo/selectable_rows_tables")
    , SimpleMenuItem("Selectable Columns", "/demo/tables_sel_cols")
  ),
  MenuSection("Forms")(
    SimpleMenuItem("Simple", "/demo/simple_form")
    , new RoutingMenuItem("demo", "simple_form")("Input Types", () => new FormInputTypesPage())
  ),
  MenuSection("Modals")(
    SimpleMenuItem("Simple", "/demo/simple_modal")
  ),
  MenuSection("chart.js")(
    SimpleMenuItem("Simple", "/demo/chartjs/simple")
  ),
  MenuSection("Other")(
    SimpleMenuItem("File Upload", "/demo/file_upload")
    , SimpleMenuItem("Anonymous Page", "/demo/anon_page")
    , SimpleMenuItem("File Download", "/demo/file_download")
    , SimpleMenuItem("Server Side Push", "/demo/server_side_push")
  ),
)
