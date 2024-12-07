package com.fastscala.taskmanager.app

import com.fastscala.core.FSContext
import com.fastscala.scala_xml.js.inScriptTag
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.utils.RenderableWithFSContext
import com.fastscala.taskmanager.db.DB
import com.fastscala.components.bootstrap5.helpers.BSHelpers
import com.fastscala.components.bootstrap5.toast.BSToast2
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.typesafe.config.ConfigFactory

import scala.io.Source
import scala.util.Try
import scala.xml.NodeSeq

trait BasePage extends RenderableWithFSContext {

  val config = ConfigFactory.load()

  import BSHelpers.*

  def navBarTopRight()(implicit fsc: FSContext): NodeSeq =
    BSBtn().BtnSecondary.lbl("Save").ajax(implicit fsc => {
      DB.save()
      BSToast2.VerySimple(<label>Saved</label>)(<p>Saved DB data to disk.</p>).installAndShow()
    }).btn ++
      BSBtn().BtnSecondary.lbl("Load").ajax(implicit fsc => {
        DB.load()
        BSToast2.VerySimple(<label>Loaded</label>)(<p>Loaded DB data from disk.</p>).installAndShow()
      }).btn.ms_2

  def renderSideMenu()(implicit fsc: FSContext): NodeSeq = {
    FSTaskmanagerMainMenu.render()
  }

  def renderPageContents()(implicit fsc: FSContext): NodeSeq

  def append2Head(): NodeSeq = NodeSeq.Empty

  def append2Body(): NodeSeq = NodeSeq.Empty

  def pageTitle: String

  def openWSSessionAtStart: Boolean = false

  def render()(implicit fsc: FSContext): NodeSeq = {
    import BSHelpers.*

    <html>
      <head>
        <meta charset="utf-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1"/>
        <meta name="author" content={"David Antunes <david@fastscala.com>"}/>
        <title>{pageTitle}</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous"/>
        <link href="//cdn.jsdelivr.net/npm/bootstrap-icons@1.10.2/font/bootstrap-icons.css" rel="stylesheet"/>
        <link href="/static/custom_base_page.css" rel="stylesheet"/>
        {JS.inScriptTag(fsc.fsPageScript(openWSSessionAtStart))}
        {append2Head()}
        {Try(config.getString("com.fastscala.demo.pages.include_file_in_head")).map(Source.fromFile(_).getLines().mkString("\n")).map(scala.xml.Unparsed(_)).getOrElse(NodeSeq.Empty)}
      </head>
      <body>

        <main>
            <aside class="main-sidebar min-vh-100 h-100 offcanvas-lg offcanvas-start" id="main-sidebar">
                <div class="d-flex flex-nowrap min-vh-100 h-100">
                    <div class="d-flex flex-column flex-shrink-0 p-3 text-bg-dark" style="width: 280px;">
                        <div class="position-relative">
                          <a href="/" class="p-3 d-flex align-items-center mb-3 mb-md-0 me-md-auto text-white text-decoration-none">
                            <img style="width: 200px;" src="/static/images/fastscala_logo.svg"></img>
                          </a>
                          <button type="button" class="btn-close btn-close-white d-lg-none text-white position-absolute end-0 top-0" data-bs-dismiss="offcanvas" aria-label="Close" data-bs-target="#main-sidebar"></button>
                        </div>
                        {renderSideMenu()}
                    </div>
                </div>
            </aside>

            <div class="main-content min-vh-100">
                <header class="p-3 text-bg-dark">
                    <div class="d-flex flex-row align-items-center justify-content-between">
                        <div>
                            <button class="d-lg-none navbar-toggler p-2" type="button" data-bs-toggle="offcanvas"
                                    data-bs-target="#main-sidebar" aria-controls="main-sidebar"
                                    aria-label="Toggle docs navigation">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" class="bi" fill="currentColor"
                                     viewBox="0 0 16 16">
                                    <path fill-rule="evenodd"
                                          d="M2.5 11.5A.5.5 0 0 1 3 11h10a.5.5 0 0 1 0 1H3a.5.5 0 0 1-.5-.5zm0-4A.5.5 0 0 1 3 7h10a.5.5 0 0 1 0 1H3a.5.5 0 0 1-.5-.5zm0-4A.5.5 0 0 1 3 3h10a.5.5 0 0 1 0 1H3a.5.5 0 0 1-.5-.5z"></path>
                                </svg>

                                <span class="d-none fs-6 pe-1">Browse</span>
                            </button>
                        </div>

                        {div.d_flex.apply(navBarTopRight())}
                    </div>
                </header>
                <header class="py-1 px-2 text-bg-secondary">#Callbacks: <span id="fs_num_page_callbacks">--</span></header>

                <div class="p-3">
                  {renderPageContents()}
                </div>
            </div>
        </main>

        <script src="//code.jquery.com/jquery-2.2.4.min.js" integrity="sha256-BbhdlvQf/xTY9gja0Dq3HiwQF8LaCRTXxZKRutelT44=" crossorigin="anonymous"></script>
        <!--script src="/static/assets/dist/js/bootstrap.bundle.min.js"></script-->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
        <script src="//cdn.jsdelivr.net/npm/feather-icons@4.28.0/dist/feather.min.js" integrity="sha384-uO3SXW5IuS1ZpFPKugNNWqTZRRglnUJK6UAZ/gxOX80nxEkN9NcGZTftn6RzhGWE" crossorigin="anonymous"></script>
        {append2Body()}
        {Try(config.getString("com.fastscala.demo.pages.include_file_in_body")).map(Source.fromFile(_).getLines().mkString("\n")).map(scala.xml.Unparsed(_)).getOrElse(NodeSeq.Empty)}
        {JS.setContents("fs_num_page_callbacks", scala.xml.Text(fsc.page.callbacks.size.toString)).inScriptTag}
      </body>
    </html>

  }
}
