package com.fastscala.components.bootstrap5.helpers

trait BSColorsHelper[T] extends Any {

  protected def withStyle(style: String): T

  def withColorBodyColor = withStyle("color: var(--bs-body-color);")

  def withColorBodyColorRgb = withStyle("color: var(--bs-body-color-rgb);")

  def withColorBodyBg = withStyle("color: var(--bs-body-bg);")

  def withColorBodyBgRgb = withStyle("color: var(--bs-body-bg-rgb);")

  def withColorSecondaryColor = withStyle("color: var(--bs-secondary-color);")

  def withColorSecondaryColorRgb = withStyle("color: var(--bs-secondary-color-rgb);")

  def withColorSecondaryBg = withStyle("color: var(--bs-secondary-bg);")

  def withColorSecondaryBgRgb = withStyle("color: var(--bs-secondary-bg-rgb);")

  def withColorTertiaryColor = withStyle("color: var(--bs-tertiary-color);")

  def withColorTertiaryColorRgb = withStyle("color: var(--bs-tertiary-color-rgb);")

  def withColorTertiaryBg = withStyle("color: var(--bs-tertiary-bg);")

  def withColorTertiaryBgRgb = withStyle("color: var(--bs-tertiary-bg-rgb);")

  def withColorEmphasisColor = withStyle("color: var(--bs-emphasis-color);")

  def withColorEmphasisColorRgb = withStyle("color: var(--bs-emphasis-color-rgb);")

  def withColorBorderColor = withStyle("color: var(--bs-border-color);")

  def withColorBorderColorRgb = withStyle("color: var(--bs-border-color-rgb);")

  def withColorPrimary = withStyle("color: var(--bs-primary);")

  def withColorPrimaryRgb = withStyle("color: var(--bs-primary-rgb);")

  def withColorPrimaryBgSubtle = withStyle("color: var(--bs-primary-bg-subtle);")

  def withColorPrimaryBorderSubtle = withStyle("color: var(--bs-primary-border-subtle);")

  def withColorPrimaryTextEmphasis = withStyle("color: var(--bs-primary-text-emphasis);")

  def withColorSuccess = withStyle("color: var(--bs-success);")

  def withColorSuccessRgb = withStyle("color: var(--bs-success-rgb);")

  def withColorSuccessBgSubtle = withStyle("color: var(--bs-success-bg-subtle);")

  def withColorSuccessBorderSubtle = withStyle("color: var(--bs-success-border-subtle);")

  def withColorSuccessTextEmphasis = withStyle("color: var(--bs-success-text-emphasis);")

  def withColorDanger = withStyle("color: var(--bs-danger);")

  def withColorDangerRgb = withStyle("color: var(--bs-danger-rgb);")

  def withColorDangerBgSubtle = withStyle("color: var(--bs-danger-bg-subtle);")

  def withColorDangerBorderSubtle = withStyle("color: var(--bs-danger-border-subtle);")

  def withColorDangerTextEmphasis = withStyle("color: var(--bs-danger-text-emphasis);")

  def withColorWarning = withStyle("color: var(--bs-warning);")

  def withColorWarningRgb = withStyle("color: var(--bs-warning-rgb);")

  def withColorWarningBgSubtle = withStyle("color: var(--bs-warning-bg-subtle);")

  def withColorWarningBorderSubtle = withStyle("color: var(--bs-warning-border-subtle);")

  def withColorWarningTextEmphasis = withStyle("color: var(--bs-warning-text-emphasis);")

  def withColorInfo = withStyle("color: var(--bs-info);")

  def withColorInfoRgb = withStyle("color: var(--bs-info-rgb);")

  def withColorInfoBgSubtle = withStyle("color: var(--bs-info-bg-subtle);")

  def withColorInfoBorderSubtle = withStyle("color: var(--bs-info-border-subtle);")

  def withColorInfoTextEmphasis = withStyle("color: var(--bs-info-text-emphasis);")

  def withColorLight = withStyle("color: var(--bs-light);")

  def withColorLightRgb = withStyle("color: var(--bs-light-rgb);")

  def withColorLightBgSubtle = withStyle("color: var(--bs-light-bg-subtle);")

  def withColorLightBorderSubtle = withStyle("color: var(--bs-light-border-subtle);")

  def withColorLightTextEmphasis = withStyle("color: var(--bs-light-text-emphasis);")

  def withColorDark = withStyle("color: var(--bs-dark);")

  def withColorDarkRgb = withStyle("color: var(--bs-dark-rgb);")

  def withColorDarkBgSubtle = withStyle("color: var(--bs-dark-bg-subtle);")

  def withColorDarkBorderSubtle = withStyle("color: var(--bs-dark-border-subtle);")

  def withColorDarkTextEmphasis = withStyle("color: var(--bs-dark-text-emphasis);")
}
