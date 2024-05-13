package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.demo.docs.data.{CountriesData, Country}
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.classes.BSHelpers.{div, text_center}
import com.fastscala.templates.bootstrap5.form5.BSFormRenderer
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form5.{Form5, FormRenderer}
import com.fastscala.templates.form5.fields.{F5HorizontalField, F5SaveButtonField, F5SelectField, F5StringField, F5VerticalField, FormField}

class FormInputTypesPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form Input Types"

  override def renderExamples()(implicit fsc: FSContext): Unit = {
    renderSnippet("Responsive") {
      <div></div>
    }
    closeSnippet()
  }
}
