package com.fastscala.templates.bootstrap5.helpers

trait BSDataHelper[T] extends Any {

  protected def setAttribute(name: String, value: String): T


  def dataBsAutoCloseAsFalse = setAttribute("data-bs-auto-close", "false")

  def dataBsAutoCloseAsInside = setAttribute("data-bs-auto-close", "inside")

  def dataBsAutoCloseAsOutside = setAttribute("data-bs-auto-close", "outside")

  def dataBsAutoCloseAsTrue = setAttribute("data-bs-auto-close", "true")

  def dataBsAutohideAsFalse = setAttribute("data-bs-autohide", "false")

  def dataBsBackdropAsFalse = setAttribute("data-bs-backdrop", "false")

  def dataBsBackdropAsStatic = setAttribute("data-bs-backdrop", "static")

  def dataBsCustomClassAsCustomPopover = setAttribute("data-bs-custom-class", "custom-popover")

  def dataBsCustomClassAsCustomTooltip = setAttribute("data-bs-custom-class", "custom-tooltip")

  def dataBsDismissAsAlert = setAttribute("data-bs-dismiss", "alert")

  def dataBsDismissAsModal = setAttribute("data-bs-dismiss", "modal")

  def dataBsDismissAsOffcanvas = setAttribute("data-bs-dismiss", "offcanvas")

  def dataBsDismissAsOffcanvasDark = setAttribute("data-bs-dismiss", "offcanvasDark")

  def dataBsDismissAsToast = setAttribute("data-bs-dismiss", "toast")

  def dataBsDisplayAsStatic = setAttribute("data-bs-display", "static")

  def dataBsHtmlAsTrue = setAttribute("data-bs-html", "true")

  def dataBsKeyboardAsFalse = setAttribute("data-bs-keyboard", "false")

  def dataBsPlacementAsBottom = setAttribute("data-bs-placement", "bottom")

  def dataBsPlacementAsLeft = setAttribute("data-bs-placement", "left")

  def dataBsPlacementAsRight = setAttribute("data-bs-placement", "right")

  def dataBsPlacementAsTop = setAttribute("data-bs-placement", "top")

  def dataBsReferenceAsParent = setAttribute("data-bs-reference", "parent")

  def dataBsRideAsCarousel = setAttribute("data-bs-ride", "carousel")

  def dataBsRideAsTrue = setAttribute("data-bs-ride", "true")

  def dataBsScrollAsTrue = setAttribute("data-bs-scroll", "true")

  def dataBsSmoothScrollAsTrue = setAttribute("data-bs-smooth-scroll", "true")

  def dataBsSpyAsScroll = setAttribute("data-bs-spy", "scroll")

  def dataBsThemeAsAuto = setAttribute("data-bs-theme", "auto")

  def dataBsThemeAsBlue = setAttribute("data-bs-theme", "blue")

  def dataBsThemeAsDark = setAttribute("data-bs-theme", "dark")

  def dataBsThemeAsLight = setAttribute("data-bs-theme", "light")

  def dataBsThemeValueAsAuto = setAttribute("data-bs-theme-value", "auto")

  def dataBsThemeValueAsDark = setAttribute("data-bs-theme-value", "dark")

  def dataBsThemeValueAsLight = setAttribute("data-bs-theme-value", "light")

  def dataBsToggleAsButton = setAttribute("data-bs-toggle", "button")

  def dataBsToggleAsCollapse = setAttribute("data-bs-toggle", "collapse")

  def dataBsToggleAsDropdown = setAttribute("data-bs-toggle", "dropdown")

  def dataBsToggleAsModal = setAttribute("data-bs-toggle", "modal")

  def dataBsToggleAsOffcanvas = setAttribute("data-bs-toggle", "offcanvas")

  def dataBsToggleAsPill = setAttribute("data-bs-toggle", "pill")

  def dataBsToggleAsPopover = setAttribute("data-bs-toggle", "popover")

  def dataBsToggleAsTab = setAttribute("data-bs-toggle", "tab")

  def dataBsToggleAsTooltip = setAttribute("data-bs-toggle", "tooltip")

  def dataBsTouchAsFalse = setAttribute("data-bs-touch", "false")

  def dataBsTriggerAsFocus = setAttribute("data-bs-trigger", "focus")
}
