package com.fastscala.templates.form7.mixins

import com.fastscala.js.Js
import com.fastscala.templates.form7.F7Field


trait FocusableF7Field extends F7Field {

  def focusJs: Js
}
