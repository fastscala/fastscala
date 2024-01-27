package com.fastscala.demo.examples.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.examples.SingleCodeExamplePage
import com.fastscala.demo.examples.data.{CountriesData, Country}
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.form5.BSFormRenderer
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form5.{Form5, FormRenderer}

import scala.xml.NodeSeq

class User1(
             var firstName: String,
             var lastName: String,
             var email: String,
             var phoneNumber: String,
             var securityLevel: Int,
             var countryOfResidence: Country
           )

class BasicFormExamplePage extends SingleCodeExamplePage("/com/fastscala/demo/examples/forms/BasicFormExamplePage.scala") {

  override def pageTitle: String = "Simple Form Example"

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    // === code snippet ===
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    import com.fastscala.templates.form5.fields._
    var editing = new User1(
      firstName = "",
      lastName = "",
      email = "",
      phoneNumber = "",
      securityLevel = 0,
      countryOfResidence = CountriesData.data(0)
    )
    val BSFormRenderer = new BSFormRenderer() {
      override def defaultRequiredFieldLabel: String = "Required Field"
    }
    import BSFormRenderer._
    div.border.p_2.rounded.apply {
      new Form5 {

        override def afterSave()(implicit fsc: FSContext): Js = {
          BSModal5.verySimple(
            "Created User",
            "Done"
          )(modal => implicit fsc => {
            <span><b>First Name:</b> {editing.firstName}</span><br/> ++
              <span><b>Last Name:</b> {editing.lastName}</span><br/> ++
              <span><b>Email:</b> {editing.email}</span><br/> ++
              <span><b>Phone:</b> {editing.phoneNumber}</span><br/> ++
              <span><b>Security Level:</b> {editing.securityLevel}</span><br/> ++
              <span><b>Country of Residence:</b> {editing.countryOfResidence.name.common}</span>
          })
        }

        override lazy val rootField: FormField = F5VerticalField()(
          F5HorizontalField()(
            "col" -> new F5StringField(() => editing.firstName, str => {editing.firstName = str; Js.void}).withLabel("First Name")
            , "col" -> new F5StringField(() => editing.lastName, str => {editing.lastName = str; Js.void}).withLabel("Last Name")
          )
          , new F5StringField(() => editing.email, str => {editing.email = str; Js.void}, inputType = "email").withLabel("Email")
          , new F5StringField(() => editing.phoneNumber, str => {editing.phoneNumber = str; Js.void}, inputType = "tel").withLabel("Phone Number")
          , new F5SelectField[Country](() => CountriesData.data.toList, () => editing.countryOfResidence, v => {editing.countryOfResidence = v; Js.void}, _.name.common).withLabel("Country of Residence")
          , new F5SaveButtonField(BSBtn.BtnPrimary.lbl("Create User").btn.d_block)
        )

        override def formRenderer: FormRenderer = formRenderer
      }.render()
    }
    // === code snippet ===
  }
}
