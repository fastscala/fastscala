package com.fastscala.demo.docs.fastscala

import cats.effect.IO
import com.fastscala.core.FSContext
import com.fastscala.demo.docs.SingleCodeExamplePage
import com.fastscala.js.Js
import com.fastscala.utils.IdGen
import com.fastscala.xml.scala_xml.JS
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.concurrent.duration.DurationInt
import scala.xml.NodeSeq

class ServerSidePushPage extends SingleCodeExamplePage() {

  override def pageTitle: String = "Server-Side push using Websockets"

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    // === code snippet ===
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    val id = IdGen.id
    val N = 40
    new Thread() {

      def factorial(n: Int): IO[Int] = for {
        _ <- IO.sleep(100.millis)
        _ <- IO(fsc.sendToPage(JS.prepend2(id, div.apply(s"factorial($n)").text_white_50)))
        rslt <- if (n == 0) IO.pure(1) else factorial(n - 1).map(_ * n)
      } yield rslt

      override def run(): Unit = {
        import cats.effect.unsafe.implicits.global
        val rslt = factorial(N).unsafeRunSync()
        fsc.sendToPage(JS.prepend2(id, div.apply(s"factorial($N) = $rslt").text_white))
      }
    }.start()
    h2.apply(s"Calculating the factorial($N)") ++
      <div id={id}></div>.m_2.bg_secondary.p_1
        .withStyle("font-family: courier; min-height: 100px;") ++
      JS.inScriptTag(fsc.initWebSocket())
    // === code snippet ===
  }
}
