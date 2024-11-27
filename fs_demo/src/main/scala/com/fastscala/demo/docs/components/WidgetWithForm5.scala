package com.fastscala.demo.docs.components

import com.fastscala.core.FSContext
import com.fastscala.components.form5.{Form5, FormRenderer}

import scala.xml.NodeSeq

abstract class WidgetWithForm5(
                                val widgetTitle: String
                              )(implicit val formRenderer: FormRenderer) extends Widget with Form5 {

  override def widgetContents()(implicit fsc: FSContext): NodeSeq = render()

}
