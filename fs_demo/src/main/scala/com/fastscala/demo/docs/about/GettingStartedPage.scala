package com.fastscala.demo.docs.about

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.{LoggedInPage, MultipleCodeExamples2Page}
import com.fastscala.demo.docs.forms.DefaultBSForm6Renderer
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form6.DefaultForm6
import com.fastscala.templates.form6.fields.{F6Field, F6SaveButtonField, F6StringField, F6VerticalField}
import io.circe.Decoder
import io.circe.generic.semiauto

import java.net.{HttpURLConnection, URL, URLEncoder}
import java.util.Date
import scala.io.Source
import scala.util.Try
import scala.xml.NodeSeq

class GettingStartedPage extends LoggedInPage() {

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  override def renderPageContents()(implicit fsc: FSContext): NodeSeq = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    div.withStyle("background('#f8fafd'); border-style: solid; border-color: #b3c7de;").border_1.shadow_sm.py_2.px_3.apply {
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-3 mb-3">
        <h1 class="h3" style="color: #1b4d88;">Getting Started</h1>
      </div>
    }
  }
}
