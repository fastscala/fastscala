package com.fastscala.demo.docs.fastscala

import com.fastscala.core.FSContext
import com.fastscala.core.circe.CIrceSupport.FSContextWithCirceSupport
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import io.circe.Decoder
import io.circe.generic.semiauto

import java.util.Date

class CallbacksPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "FastScala Callbacks"

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Basic callback") {
      <button
        class="btn btn-primary d-block mx-auto"
        onclick={
          fsc.callback(() => {
            JS.alert(s"Current date/time on server: ${new Date().toString}")
          }).cmd}
      >Check time on server</button>
    }
    renderSnippet("Callback with args") {
      <button
        class="btn btn-primary d-block mx-auto"
        onclick={
          fsc.callback(JS("navigator.userAgent"), userAgent => {
            JS.alert(s"User's browser is: $userAgent")
          }).cmd
        }
      >Send browser details to server side</button>
    }
    renderSnippet("Callback with JSON arg with languages data") {
      <button
        class="btn btn-primary d-block mx-auto"
        onclick={
          fsc.callbackJSON(JS("navigator.languages"), json => {
            println(json.asString)
            val languages: Iterable[String] = json.asArray.map(_.flatMap(_.asString)).getOrElse(Seq())
            JS.alert(s"Supported languages on user's browser are: ${languages.mkString(", ")}")
          }).cmd}
      >Send browser details to server side as JSON object</button>
    }
    renderSnippet("Callback with JSON arg with languages data") {
      final case class NavigatorData(
                                      language: String,
                                      languages: List[String],
                                      platform: String,
                                      product: String,
                                      productSub: String,
                                      userAgent: String,
                                      vendor: String,
                                    )
      implicit val jsonDecoder: Decoder[NavigatorData] = semiauto.deriveDecoder[NavigatorData]
      <button
        class="btn btn-primary d-block mx-auto"
        onclick={
          fsc.callbackJSONDecoded[NavigatorData](JS("{language: navigator.language, languages: navigator.languages, platform: navigator.platform, product: navigator.product, productSub: navigator.productSub, userAgent: navigator.userAgent, vendor: navigator.vendor }"), navigatorData => {
            JS.alert(s"NavigatorData: ${navigatorData.toString}")
          }).cmd}
      >Send browser details to server side as a case class</button>
    }
    closeSnippet()
  }
}
