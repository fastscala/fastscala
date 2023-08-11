package com.fastscala.templates.bootstrap5.examples

import com.fastscala.code.FSContext
import com.fastscala.utils.IdGen

import scala.xml.NodeSeq

object MainMenu extends Menu(
  MenuSection("Tables")(
    MenuItem("Simple", "/simple_tables")
    , MenuItem("Sortable", "/sortable_tables")
    , MenuItem("Paginated", "/paginated_tables")
    , MenuItem("Selectable Rows", "/selectable_rows_tables")
    , MenuItem("Selectable Columns", "/tables_sel_cols")
  ),
  MenuSection("Forms")(
    MenuItem("Simple", "/simple_form")
  ),
  MenuSection("Modals")(
    MenuItem("Simple", "/simple_modal")
  ),
  MenuSection("chart.js")(
    MenuItem("Simple", "/chartjs/simple")
  ),
  MenuSection("Other")(
    MenuItem("File Upload", "/file_upload")
    , MenuItem("Anonymous Page", "/anon_page")
    , MenuItem("File Download", "/file_download")
    , MenuItem("Server Side Push", "/server_side_push")
  ),
)

case class Menu(sections: MenuSection*) {
  def render()(implicit fsc: FSContext): NodeSeq = {
    <div class="position-sticky p-3 sidebar-sticky">
      <ul class="list-unstyled ps-0">
        {sections.map(_.render())}
      </ul>
    </div>
  }
}

case class MenuSection(name: String)(items: MenuItem*) {
  def render()(implicit fsc: FSContext): NodeSeq = {
    val isOpen = items.exists(_.href == fsc.page.req.getRequestURI)
    val id = IdGen.id
    <li class="mb-1">
      <button class={"text-white btn bi btn-toggle d-inline-flex align-items-center rounded border-0" + (if (isOpen) "" else " collapsed")} data-bs-toggle="collapse" data-bs-target={s"#$id"} aria-expanded={isOpen.toString}>
        {name}
      </button>
      <div class={"collapse" + (if (isOpen) " show" else "")} id={id}>
        <ul class="btn-toggle-nav list-unstyled fw-normal pb-1 small">
          {items.map(_.render())}
        </ul>
      </div>
    </li>
  }
}

case class MenuItem(name: String, href: String) {
  def render()(implicit fsc: FSContext): NodeSeq = {
    <li><a href={href} class="text-white d-inline-flex text-decoration-none rounded">{name}</a></li>
  }
}