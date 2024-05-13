package com.fastscala.demo.docs.about

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.{MultipleCodeExamples2Page, PageWithTopTitle}
import com.fastscala.js.Js
import io.circe.Decoder
import io.circe.generic.semiauto

import java.util.Date
import scala.xml.NodeSeq

class AboutPage extends PageWithTopTitle {

  override def pageTitle: String = "FastScala | About"

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  override def renderStandardPageContents()(implicit fsc: FSContext): NodeSeq = {
    fs_3.apply("What is the FastScala framework?") ++
      p.apply("")
  }
}
