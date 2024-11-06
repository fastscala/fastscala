package com.fastscala.demo.docs.loggedout

import com.fastscala.core.FSContext
import com.fastscala.xml.scala_xml.{JS, ScalaXmlRenderableWithFSContext}
import com.typesafe.config.ConfigFactory

import scala.io.Source
import scala.util.Try
import scala.xml.{Elem, NodeSeq}

trait LoggedoutBasePage extends ScalaXmlRenderableWithFSContext {

  val config = ConfigFactory.load()

  def renderPageContents()(implicit fsc: FSContext): NodeSeq

  def append2Head(): NodeSeq = NodeSeq.Empty

  def append2Body(): NodeSeq = NodeSeq.Empty

  def pageTitle: String

  def openWSSessionAtStart: Boolean = false

  def renderBody()(implicit fsc: FSContext): Elem = {
    <body>
      {renderPageContents()}
      <script src="//code.jquery.com/jquery-2.2.4.min.js" integrity="sha256-BbhdlvQf/xTY9gja0Dq3HiwQF8LaCRTXxZKRutelT44=" crossorigin="anonymous"></script>
      <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
      <script src="//cdn.jsdelivr.net/npm/feather-icons@4.28.0/dist/feather.min.js" integrity="sha384-uO3SXW5IuS1ZpFPKugNNWqTZRRglnUJK6UAZ/gxOX80nxEkN9NcGZTftn6RzhGWE" crossorigin="anonymous"></script>
      {append2Body()}
      {Try(config.getString("com.fastscala.demo.pages.include_file_in_body")).map(Source.fromFile(_).getLines().mkString("\n")).map(scala.xml.Unparsed(_)).getOrElse(NodeSeq.Empty)}
    </body>
  }

  def render()(implicit fsc: FSContext): NodeSeq = {

    val style =
      """html,
        |body {
        |  height: 100%;
        |}
        |""".stripMargin

    <html>
      <head>
        <meta charset="utf-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1"/>
        <title>{pageTitle}</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous"/>
        <link href="//cdn.jsdelivr.net/npm/bootstrap-icons@1.10.2/font/bootstrap-icons.css" rel="stylesheet"/>
        {JS.inScriptTag(fsc.fsPageScript(openWSSessionAtStart))}
        {append2Head()}
        {Try(config.getString("com.fastscala.demo.pages.include_file_in_head")).map(Source.fromFile(_).getLines().mkString("\n")).map(scala.xml.Unparsed(_)).getOrElse(NodeSeq.Empty)}
        <style>{style}</style>
      </head>
      {renderBody()}
    </html>

  }
}
