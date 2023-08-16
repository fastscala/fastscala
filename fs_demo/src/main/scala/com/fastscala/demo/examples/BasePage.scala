package com.fastscala.demo.examples

import com.fastscala.core.FSContext
import com.fastscala.utils.RenderableWithFSContext
import org.apache.commons.io.IOUtils

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.util.matching.Regex
import scala.xml.NodeSeq


trait BasePage extends RenderableWithFSContext {

  def renderSideMenu()(implicit fsc: FSContext): NodeSeq = MainMenu.render()

  def renderPageContents()(implicit fsc: FSContext): NodeSeq

  def append2Head(): NodeSeq = NodeSeq.Empty

  def append2Body(): NodeSeq = NodeSeq.Empty

  override def render()(implicit fsc: FSContext): NodeSeq = {
    <html>
      <head>
        <meta charset="utf-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1"/>
        <meta name="description" content="FastScala Demo"/>
        <meta name="author" content={"David Antunes <david@fastscala.com>"}/>
        <title>FastScala Bootstrap Template Example</title>
        <link href="/static/assets/dist/css/bootstrap.min.css" rel="stylesheet"/>
        <link href="//cdn.jsdelivr.net/npm/bootstrap-icons@1.10.2/font/bootstrap-icons.css" rel="stylesheet"/>
        <link href="/static/custom_base_page.css" rel="stylesheet"/>
        {fsc.fsPageScript().inScriptTag}
        {append2Head()}
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

                        <div class="text-end justify-content-end">
                            <a href="https://github.com/fastscala/fastscala" class="btn btn-warning">GitHub</a>
                        </div>
                    </div>
                </header>

                <div class="p-3">
                  {renderPageContents()}
                </div>
            </div>
        </main>

        <script src="//code.jquery.com/jquery-2.2.4.min.js" integrity="sha256-BbhdlvQf/xTY9gja0Dq3HiwQF8LaCRTXxZKRutelT44=" crossorigin="anonymous"></script>
        <script src="/static/assets/dist/js/bootstrap.bundle.min.js"></script>
        <script src="//cdn.jsdelivr.net/npm/feather-icons@4.28.0/dist/feather.min.js" integrity="sha384-uO3SXW5IuS1ZpFPKugNNWqTZRRglnUJK6UAZ/gxOX80nxEkN9NcGZTftn6RzhGWE" crossorigin="anonymous"></script>
        {append2Body()}
      </body>
    </html>

  }
}
