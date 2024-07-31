package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.SingleCodeExamplePage
import com.fastscala.demo.docs.data.{CountriesData, Country}
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.form6.BSForm6Renderer
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form6._
import com.fastscala.templates.form6.fields._
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.NodeSeq

class User1(
             var firstName: String,
             var lastName: String,
             var email: String,
             var phoneNumber: String,
             var securityLevel: Int,
             var countryOfResidence: Country
           )

class BasicFormExamplePage extends SingleCodeExamplePage() {

  override def pageTitle: String = "Simple Form Example"

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    // === code snippet ===
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    var editing = new User1(
      firstName = "",
      lastName = "",
      email = "",
      phoneNumber = "",
      securityLevel = 0,
      countryOfResidence = CountriesData.data(0)
    )
    val BSFormRenderer = new BSForm6Renderer {
      override def defaultRequiredFieldLabel: String = "Required Field"
    }
    import BSFormRenderer._
    div.border.p_2.rounded.apply {
      new Form6 {
        override def postSave()(implicit fsc: FSContext): Js = {
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

        override lazy val rootField: F6Field = F6VerticalField()(
          F6HorizontalRowField()(
            "col" -> new F6StringField().label("First Name").rw(editing.firstName, editing.firstName = _)
            , "col" -> new F6StringField().label("Last Name").rw(editing.lastName, editing.lastName = _)
          )
          , new F6StringField().label("Email").rw(editing.email, editing.email = _).inputType("email")
          , new F6StringField().label("Phone Number").rw(editing.phoneNumber, editing.phoneNumber = _).inputType("tel")
          , new F6StringField().label("Email").rw(editing.email, editing.email = _).inputType("email")
          , new F6SelectField[Country](CountriesData.data.toList).label("Country of Residence").rw(editing.countryOfResidence, editing.countryOfResidence = _).option2String(_.name.common)
          , new F6SaveButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Create User").btn.d_block)
        )

        override def formRenderer: F6FormRenderer = formRenderer
      }.render()
    }
    // === code snippet ===
  }
}
