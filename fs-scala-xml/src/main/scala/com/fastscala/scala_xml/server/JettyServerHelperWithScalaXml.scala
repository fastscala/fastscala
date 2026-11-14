package com.fastscala.scala_xml.server

import com.fastscala.core.FSSystem
import com.fastscala.scala_xml.code.FSSystemWithScalaXmlImpl
import com.fastscala.server.JettyServerHelper

abstract class JettyServerHelperWithScalaXml extends JettyServerHelper {

  override def buildFSSystem(): FSSystem = new FSSystemWithScalaXmlImpl(appName = appName)

}
