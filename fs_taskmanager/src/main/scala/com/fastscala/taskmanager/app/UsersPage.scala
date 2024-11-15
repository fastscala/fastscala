package com.fastscala.taskmanager.app

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.components.Widget
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.taskmanager.db.{DB, User}
import com.fastscala.templates.bootstrap5.form7.layout.F7BSFormInputGroup
import com.fastscala.templates.bootstrap5.modals.BSModal5WithForm7Base
import com.fastscala.templates.bootstrap5.tables.*
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form7.F7Field
import com.fastscala.templates.form7.fields.layout.F7VerticalField
import com.fastscala.templates.form7.fields.text.F7StringField
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

class UsersPage extends BasePage() {

  override def pageTitle: String = "Users"

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers.*

  override def renderPageContents()(implicit fsc: FSContext): NodeSeq = {
    new Widget {
      override def widgetTitle: String = "Users"

      val table = new Table5Base
        with Table5BaseBootrapSupport
        with Table5StandardColumns
        with Table5SeqSortableDataSource
        with Table5SelectableRowsWithActions
        with Table5Paginated {

        override type R = User

        override def defaultPageSize = 10

        val ColFirstName = ColStr("First Name", _.firstName)
        val ColLastName = ColStr("Last Name", _.lastName)
        val ColEmail = ColStr("Email", _.email)

        override def columns(): List[C] = List(
          ColFirstName
          , ColLastName
          , ColEmail
          , DefaultColActions
          , ColSelectRow
        )

        override def rowsSorter: PartialFunction[Table5StandardColumn[R], Seq[R] => Seq[R]] = {
          case ColFirstName => _.sortBy(_.firstName)
          case ColLastName => _.sortBy(_.lastName)
          case ColEmail => _.sortBy(_.email)
        }

        override def seqRowsSource: Seq[R] = DB().users.toSeq

        override def actionsBtnToIncludeInTopDropdown: BSBtn = super.actionsBtnToIncludeInTopDropdown.sm.mx_2
      }

      override def widgetTopRight()(implicit fsc: FSContext): NodeSeq = super.widgetTopRight() ++
        BSBtn().BtnSuccess.sm.lbl("Create User").ajax(implicit fsc => {
          import com.fastscala.demo.docs.forms.DefaultFSDemoBSForm7Renderers.*
          new BSModal5WithForm7Base("New User") {
            val user = new User("", "", "")
            override val rootField: F7Field = F7VerticalField()(
              new F7BSFormInputGroup()(
                new F7StringField().placeholder("First Name").rw(user.firstName, user.firstName = _),
                new F7StringField().placeholder("Last Name").rw(user.lastName, user.lastName = _),
              ),
              new F7StringField().label("Email").rw(user.email, user.email = _).inputTypeEmail,
            )

            override def postSubmitForm()(implicit fsc: FSContext): Js = super.postSubmitForm() & {
              DB().users += user
              table.rerenderTable() &
                hideAndRemoveAndDeleteContext()
            }
          }.installAndShow()
        }).btn ++ table.actionsDropdownBtnRenderer.render()

      override def widgetContents()(implicit fsc: FSContext): NodeSeq = table.render()

      override def transformWidgetCardBody(elem: Elem): Elem = elem.p_0
    }.renderWidget()
  }
}
