package com.fastscala.components.bootstrap5.modals

import scala.xml.Elem

object BSModal5Size extends Enumeration {
  val Small = Value("modal-sm")
  val Normal = Value("")
  val Large = Value("modal-lg")
  val ExtraLarge = Value("modal-xl")

  val FullScreen = Value("modal-fullscreen")
  val FullScreenBellowSm = Value("modal-fullscreen-sm-down")
  val FullScreenBellowMd = Value("modal-fullscreen-md-down")
  val FullScreenBellowLg = Value("modal-fullscreen-lg-down")
  val FullScreenBellowXl = Value("modal-fullscreen-xl-down")
  val FullScreenBellowXxl = Value("modal-fullscreen-xxl-down")
}
