package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.demo.docs.data.{CountriesData, Country}
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form6.{Form6, FormRenderer}
import com.fastscala.templates.form6.fields._

class FormInputTypesPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 6 Input Types"

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._
  import DefaultBSForm6Renderer._

  override def renderExamples()(implicit fsc: FSContext): Unit = {
    renderSnippet("Responsive") {
      var name = ""
      var email = ""
      var phone = ""

      div.border.p_2.rounded.apply {
        new Form6 {
          override def afterSave()(implicit fsc: FSContext): Js = {
            BSModal5.verySimple(
              "Creating User...",
              "Done"
            )(modal => implicit fsc => {
              <span><b>Name:</b> {name}</span><br/> ++
              <span><b>Email:</b> {email}</span><br/> ++
              <span><b>Phone:</b> {phone}</span>
            })
          }

          override lazy val rootField: FormField = F6VerticalField()(
            new F6StringField(() => name, str => {
              name = str;
              Js.void
            }).label("Name")
            , F6HorizontalField()(
              "col" -> new F6StringField(() => email, str => {
                email = str;
                Js.void
              }).label("Email")
              , "col" -> new F6StringField(() => phone, str => {
                phone = str;
                Js.void
              }).label("Phone")
            )
            , new F6SaveButtonField(implicit fsc => BSBtn.BtnPrimary.lbl("Create User").btn.d_block)
          )

          override def formRenderer: FormRenderer = formRenderer
        }.render()
      }
    }
    closeSnippet()
  }
}
