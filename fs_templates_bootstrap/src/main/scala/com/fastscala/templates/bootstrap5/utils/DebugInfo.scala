package com.fastscala.templates.bootstrap5.utils

import com.fastscala.core.FSContext

object DebugInfo {

  def apply()(implicit fsc: FSContext) = {
    <span style="color: white" class="d-none d-md-block">
      #Sessions: {fsc.session.fsSystem.sessions.size},
      #Pages in session: {fsc.session.nPages()},
      #Funcs in page: {fsc.page.functions.size},
      Used Mem: {(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024}MB
    </span>
  }
}
