package com.fastscala.components.bootstrap5.progress

import com.fastscala.components.bootstrap5.helpers.{AttrEnrichableImmutable, ClassEnrichableImmutable}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.Elem

case class BSProgress(
                       progressClasses: String = "progress",
                       progressBarClasses: String = "progress-bar",
                       additionalAttrs: Seq[(String, String)] = Nil
                     ) extends ClassEnrichableImmutable[BSProgress] with AttrEnrichableImmutable[BSProgress] {

  override def setAttribute(name: String, value: String): BSProgress = copy(additionalAttrs = additionalAttrs :+ (name -> value))

  override def addClass(clas: String): BSProgress = copy(progressClasses = progressClasses + " " + clas)

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def striped: BSProgress = copy(progressBarClasses = progressBarClasses + progress_bar_striped.getClassAttr)

  def animated: BSProgress = copy(progressBarClasses = progressBarClasses + progress_bar_animated.getClassAttr)

  def bgPrimary: BSProgress = copy(progressBarClasses = progressBarClasses + bg_primary.getClassAttr)

  def bgPrimarySubtle: BSProgress = copy(progressBarClasses = progressBarClasses + bg_primary_subtle.getClassAttr)

  def bgSecondary: BSProgress = copy(progressBarClasses = progressBarClasses + bg_secondary.getClassAttr)

  def bgSecondarySubtle: BSProgress = copy(progressBarClasses = progressBarClasses + bg_secondary_subtle.getClassAttr)

  def bgSuccess: BSProgress = copy(progressBarClasses = progressBarClasses + bg_success.getClassAttr)

  def bgSuccessSubtle: BSProgress = copy(progressBarClasses = progressBarClasses + bg_success_subtle.getClassAttr)

  def bgInfo: BSProgress = copy(progressBarClasses = progressBarClasses + bg_info.getClassAttr)

  def bgInfoSubtle: BSProgress = copy(progressBarClasses = progressBarClasses + bg_info_subtle.getClassAttr)

  def bgWarning: BSProgress = copy(progressBarClasses = progressBarClasses + bg_warning.getClassAttr)

  def bgWarningSubtle: BSProgress = copy(progressBarClasses = progressBarClasses + bg_warning_subtle.getClassAttr)

  def bgDanger: BSProgress = copy(progressBarClasses = progressBarClasses + bg_danger.getClassAttr)

  def bgDangerSubtle: BSProgress = copy(progressBarClasses = progressBarClasses + bg_danger_subtle.getClassAttr)

  def bgLight: BSProgress = copy(progressBarClasses = progressBarClasses + bg_light.getClassAttr)

  def bgLightSubtle: BSProgress = copy(progressBarClasses = progressBarClasses + bg_light_subtle.getClassAttr)

  def bgDark: BSProgress = copy(progressBarClasses = progressBarClasses + bg_dark.getClassAttr)

  def bgDarkSubtle: BSProgress = copy(progressBarClasses = progressBarClasses + bg_dark_subtle.getClassAttr)

  def render(percent: Double, label: String = ""): Elem =
    <div class={progressClasses} role="progressbar" aria-valuenow={percent.formatted("%.2f")} aria-valuemin="0" aria-valuemax="100">
      <div class={progressBarClasses} style={"width: " + percent.formatted("%.2f%%")}>{label}</div>
    </div>

  def renderXofY(x: Double, ofY: Double, label: String = ""): Elem = render(x / ofY * 100.0, label)
}
