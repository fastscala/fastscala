package com.fastscala.scala_xml.code

import com.fastscala.core.FSSystemImpl
import com.fastscala.stats.FSStats

class FSSystemWithScalaXmlImpl(appName: String = "app", stats: FSStats = new FSStats()) extends FSSystemImpl(appName, stats) with FSSystemWithScalaXml {

}
