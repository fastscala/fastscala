package com.fastscala.components.bootstrap5.offcanvas

enum OffcanvasPosition(val clas: String) {
  
  case Start extends OffcanvasPosition("offcanvas-start")
  case End extends OffcanvasPosition("offcanvas-end")
  case Top extends OffcanvasPosition("offcanvas-top")
  case Bottom extends OffcanvasPosition("offcanvas-bottom")
}
