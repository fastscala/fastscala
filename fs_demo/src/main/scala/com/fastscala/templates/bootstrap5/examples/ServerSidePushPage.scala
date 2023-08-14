package com.fastscala.templates.bootstrap5.examples

import cats.effect.IO
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.utils.IdGen

import scala.concurrent.duration.DurationInt
import scala.xml.NodeSeq

class ServerSidePushPage extends SingleCodeExamplePage("/com/fastscala/templates/bootstrap5/examples/ServerSidePushPage.scala") {

  override def pageTitle: String = "Server Side Push Example"

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    // === code snippet ===
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    val id = IdGen.id
    val N = 40
    new Thread() {

      def factorial(n: Int): IO[Int] = for {
        _ <- IO.sleep(100.millis)
        _ <- IO(fsc.sendToPage(Js.prepend2(id, div.apply(s"factorial($n)").text_white_50)))
        rslt <- if (n == 0) IO.pure(1) else factorial(n - 1).map(_ * n)
      } yield rslt

      override def run(): Unit = {
        import cats.effect.unsafe.implicits.global
        val rslt = factorial(N).unsafeRunSync()
        fsc.sendToPage(Js.prepend2(id, div.apply(s"factorial($N) = $rslt").text_white))
      }
    }.start()
    h2.apply(s"Calculating the factorial($N)") ++
      <div id={id}></div>.m_2.bg_secondary.p_1
        .withStyle("font-family: courier; min-height: 100px;") ++
      fsc.initWebSocket().inScriptTag
    // === code snippet ===
  }
}
