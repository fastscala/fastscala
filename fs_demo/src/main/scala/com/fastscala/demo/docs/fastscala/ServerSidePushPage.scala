package com.fastscala.demo.docs.fastscala

import cats.effect.IO
import com.fastscala.core.FSContext
import com.fastscala.demo.docs.SingleCodeExamplePage
import com.fastscala.js.Js
import com.fastscala.utils.IdGen
import com.fastscala.xml.scala_xml.JS

import scala.concurrent.duration.DurationInt
import scala.xml.NodeSeq

class ServerSidePushPage extends SingleCodeExamplePage() {
  override def pageTitle: String = "Server-Side push using Websockets"

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    // === code snippet ===
    import com.fastscala.templates.bootstrap5.helpers.BSHelpers._
    val id = IdGen.id
    val N = 30

    def factorial(n: BigInt): IO[BigInt] = for {
      rslt <- if (n == 0) IO.pure(BigInt(1)) else factorial(n - 1).map(_ * n)
      _ <- if (n < N) IO(fsc.sendToPage(JS.prepend2(id, div.apply(s"factorial($n) = $rslt").text_white_50))).flatMap(_ => IO.sleep(100.millis)) else IO.unit
    } yield rslt

    import cats.effect.unsafe.implicits.global
    factorial(N).unsafeRunAsync({
      case Right(value) => fsc.sendToPage(JS.prepend2(id, div.apply(s"Finished computation.").text_success_emphasis))
      case Left(_) => fsc.sendToPage(JS.prepend2(id, div.apply(s"Error!").text_danger))
    })

    h2.apply(s"Calculating the factorial($N)") ++
      <div id={id}></div>.m_2.bg_secondary.p_1
        .withStyle("font-family: courier; min-height: 100px;") ++
      JS.inScriptTag(fsc.page.initWebSocket())
    // === code snippet ===
  }
}
