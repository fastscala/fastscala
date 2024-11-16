package com.fastscala.demo.server

import com.fastscala.core.{FSFunc, FSPage, FSSystem}
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.server.JettyServerHelper
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import org.eclipse.jetty.server.Handler

import java.awt.Desktop
import java.net.URI

object JettyServer extends JettyServerHelper() {

  override def appName: String = "fs_demo"

  override def buildMainHandler(): Handler = new RoutingHandler()

  override def buildFSSystem(): FSSystem = new FSSystem(appName = appName) {
    override def transformCallbackResponse(resp: Js, fsFunc: FSFunc, page: FSPage): Js = super.transformCallbackResponse(resp, fsFunc, page) & JS.setContents("fs_num_page_callbacks", scala.xml.Text(page.callbacks.size.toString))
  }

  override def postStart(): Unit = {
    super.postStart()

    if (config.getBoolean("com.fastscala.demo.open-browser") && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      val desktop = Desktop.getDesktop()
      desktop.browse(new URI(s"http://localhost:$Port"))
    } else {
      println(s"Available at: http://localhost:$Port")
    }
  }

  override def postStop(): Unit = {
    super.postStop()
    cats.effect.unsafe.implicits.global.shutdown()
  }
}
