package com.fastscala.templates.bootstrap5.server

import com.fastscala.server.JettyServerHelper
import org.eclipse.jetty.server.Handler

import java.awt.Desktop
import java.net.URI

object JettyServer extends JettyServerHelper() {

  override def Port: Int = if (isLocal) 9064 else 80

  override def isLocal: Boolean = System.getProperty("user.name") == "david"

  override def appName: String = "fs_demo"

  override def buildMainHandler(): Handler = new RouterHandler()

  override def postStart(): Unit = {
    super.postStart()

    if (isLocal && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      val desktop = Desktop.getDesktop()
      desktop.browse(new URI(s"http://localhost:$Port"))
    }
  }
}
