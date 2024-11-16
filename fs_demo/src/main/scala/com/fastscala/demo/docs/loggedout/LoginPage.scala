package com.fastscala.demo.docs.loggedout

import com.fastscala.core.FSContext
import com.fastscala.demo.db.{CurrentUser, FakeDB}
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.printBeforeExec
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form7.fields.layout.F7VerticalField
import com.fastscala.templates.form7.fields.text.F7StringField
import com.fastscala.templates.form7.fields.{F7HtmlField, F7SubmitButtonField}
import com.fastscala.templates.form7.{DefaultForm7, F7Field}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

class LoginPage extends LoggedoutBasePage {

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers.*

  override def pageTitle: String = "FastScala Demo Login"

  override def renderBody()(implicit fsc: FSContext): Elem =
    super.renderBody().d_flex.align_content_center.py_4.withStyle("background-image: url('/static/images/pexels-pixabay-315938.jpg'); background-size: cover;background-position: center;")

  override def renderPageContents()(implicit fsc: FSContext): NodeSeq = {
    import com.fastscala.demo.docs.forms.DefaultFSDemoBSForm7Renderers.*
    val form = new DefaultForm7() {
      val usernameField = new F7StringField().placeholder("Username").name("username")
      val passwordField = new F7StringField().placeholder("Password").inputTypePassword.name("password")
      override val rootField: F7Field = F7VerticalField()(
        new F7HtmlField(<img src="/static/images/logo-wide.png"/>.mb_3.w_75.d_block.mx_auto),
        new F7HtmlField(<p>You can use user <b>admin</b> and password <b>admin</b> to login:</p>),
        usernameField,
        passwordField,
        new F7SubmitButtonField(implicit fsc => BSBtn().BtnOutlinePrimary.lbl("Login").sm.btn.d_block.w_100),
      )

      override def postSubmitForm()(implicit fsc: FSContext): Js = super.postSubmitForm() & {
        FakeDB.users.find(user => user.username == usernameField.getInternalValue() && user.checkPassword(passwordField.getInternalValue())) match {
          case Some(user) =>
            fsc.session.delete()
            val newSession = fsc.session.fsSystem.createSession()
            CurrentUser.update(user)(newSession)
            JS.setCookie(fsc.session.fsSystem.FSSessionIdCookieName, newSession.id, path = Some("/")).printBeforeExec & JS.redirectTo("/")
          case None => JS.alert("User or password invalid.")
        }
      }
    }
    <main class="form-signin" style="max-width: 330px;">
      {form.render()}
    </main>.w_100.m_auto.bg_white.rounded.shadow.shadow_lg.p_3
  }
}
