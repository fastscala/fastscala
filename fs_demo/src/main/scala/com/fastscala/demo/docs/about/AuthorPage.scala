package com.fastscala.demo.docs.about

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.LoggedInPage
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.NodeSeq

class AuthorPage extends LoggedInPage() {

  override def pageTitle: String = "FastScala Web Framework Author | David Antunes"

  override def renderPageContents()(implicit fsc: FSContext): NodeSeq = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    div.withStyle("background('#f8fafd'); border-style: solid; border-color: #b3c7de;").border_1.shadow_sm.py_2.px_3.apply {
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-3 mb-3">
        <h1 class="h3" style="color: #1b4d88;">FastScala Author</h1>
      </div> ++
        alert.alert_success.withRole("alert").d_flex.justify_content_between.align_items_center.mb_5.apply {
          div.apply("Interested in learning more about the FastScala framework? Register now for a free live demo/training here!:") ++
            BSBtn().BtnPrimary.lbl("Register for Free Training!").href("https://training.fastscala.com/").btnLink.ms_3
        } ++
        h2.apply("FastScala Author") ++
        row.apply {
          col_md_6.apply {
            <p>
              FastScala is developed by me, David Antunes. I started programming with Scala on 2012 and have been working both on startups as well as
                big companies developing in Scala.
            </p>
            <p>
              If you'd like to get in contact you can drop me an email to: {<pre>david at fastscala.com</pre>.d_inline}.
            </p>
          } ++
            col_md_6.text_center.apply {
              <img src="/static/images/david_antunes.png" class="bd-placeholder-img img-thumbnail" width="200px" height="200px"></img>
            }
        }
    }
  }
}
