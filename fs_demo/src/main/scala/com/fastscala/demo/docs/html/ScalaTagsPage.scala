package com.fastscala.demo.docs.html

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.xml.scala_xml.JS
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem
import scalatags.Text
import scalatags.Text.TypedTag

import scala.xml.NodeSeq

class ScalaTagsPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "ScalaTags interoperability"

  override def renderExplanation()(implicit fsc: FSContext): NodeSeq = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    <p>
      Fastscala is not tied to {span.badge.text_bg_secondary.apply("scala-xml")}, but the libraries that are on top of do use it.
    </p>
    <p>
      If you want to use ScalaTags, you can use for example an implicit conversion to {span.badge.text_bg_secondary.apply("scala-xml")}:
      <b><pre>implicit def scalaTags2ScalaXml(frag: TypedTag[String]): NodeSeq = scala.xml.Unparsed(frag.render)</pre></b>
    </p>
  }

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Integrating ScalaTags using an implicit conversion") {
      // You can use an implicit conversion from ScalaTags to scala-xml:
      implicit def scalaTags2ScalaXml(frag: TypedTag[String]): NodeSeq =
        scala.xml.Unparsed(frag.render)

      BSBtn().BtnPrimary.lbl("Open Modal").ajax(implicit fsc =>
        BSModal5.verySimple("Simple modal", "Close")({ modal =>
          implicit fsc =>
            import scalatags.Text.all._
            div(
              p("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce id elit velit. Proin convallis ultrices nisi ac fermentum."),
              p("Nunc a lobortis arcu. Nullam cursus dapibus risus in pulvinar.")
            )
        })
      ).btn
    }
    closeSnippet()
  }
}
