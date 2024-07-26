package com.fastscala.templates.bootstrap5.classes

import com.fastscala.core.{FSXmlEnv, FSXmlSupport}

trait BSClassesHelper {

  def withClass[E <: FSXmlEnv : FSXmlSupport](clas: String): E#Elem

  def accordion[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("accordion")

  def accordion_body[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("accordion-body")

  def accordion_button[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("accordion-button")

  def accordion_flush[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("accordion-flush")

  def accordion_header[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("accordion-header")

  def accordion_item[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("accordion-item")

  def active[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("active")

  def alert[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("alert")

  def alert_danger[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("alert-danger")

  def alert_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("alert-dark")

  def alert_dismissible[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("alert-dismissible")

  def alert_heading[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("alert-heading")

  def alert_info[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("alert-info")

  def alert_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("alert-light")

  def alert_link[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("alert-link")

  def alert_primary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("alert-primary")

  def alert_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("alert-secondary")

  def alert_success[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("alert-success")

  def alert_warning[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("alert-warning")

  def align_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-baseline")

  def align_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-bottom")

  def align_content_around[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-around")

  def align_content_between[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-between")

  def align_content_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-center")

  def align_content_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-end")

  def align_content_lg_around[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-lg-around")

  def align_content_lg_between[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-lg-between")

  def align_content_lg_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-lg-center")

  def align_content_lg_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-lg-end")

  def align_content_lg_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-lg-start")

  def align_content_lg_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-lg-stretch")

  def align_content_md_around[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-md-around")

  def align_content_md_between[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-md-between")

  def align_content_md_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-md-center")

  def align_content_md_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-md-end")

  def align_content_md_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-md-start")

  def align_content_md_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-md-stretch")

  def align_content_sm_around[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-sm-around")

  def align_content_sm_between[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-sm-between")

  def align_content_sm_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-sm-center")

  def align_content_sm_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-sm-end")

  def align_content_sm_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-sm-start")

  def align_content_sm_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-sm-stretch")

  def align_content_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-start")

  def align_content_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-stretch")

  def align_content_xl_around[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-xl-around")

  def align_content_xl_between[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-xl-between")

  def align_content_xl_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-xl-center")

  def align_content_xl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-xl-end")

  def align_content_xl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-xl-start")

  def align_content_xl_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-xl-stretch")

  def align_content_xxl_around[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-xxl-around")

  def align_content_xxl_between[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-xxl-between")

  def align_content_xxl_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-xxl-center")

  def align_content_xxl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-xxl-end")

  def align_content_xxl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-xxl-start")

  def align_content_xxl_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-content-xxl-stretch")

  def align_items_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-baseline")

  def align_items_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-center")

  def align_items_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-end")

  def align_items_lg_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-lg-baseline")

  def align_items_lg_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-lg-center")

  def align_items_lg_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-lg-end")

  def align_items_lg_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-lg-start")

  def align_items_lg_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-lg-stretch")

  def align_items_md_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-md-baseline")

  def align_items_md_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-md-center")

  def align_items_md_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-md-end")

  def align_items_md_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-md-start")

  def align_items_md_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-md-stretch")

  def align_items_sm_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-sm-baseline")

  def align_items_sm_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-sm-center")

  def align_items_sm_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-sm-end")

  def align_items_sm_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-sm-start")

  def align_items_sm_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-sm-stretch")

  def align_items_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-start")

  def align_items_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-stretch")

  def align_items_xl_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-xl-baseline")

  def align_items_xl_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-xl-center")

  def align_items_xl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-xl-end")

  def align_items_xl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-xl-start")

  def align_items_xl_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-xl-stretch")

  def align_items_xxl_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-xxl-baseline")

  def align_items_xxl_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-xxl-center")

  def align_items_xxl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-xxl-end")

  def align_items_xxl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-xxl-start")

  def align_items_xxl_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-items-xxl-stretch")

  def align_middle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-middle")

  def align_self_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-auto")

  def align_self_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-baseline")

  def align_self_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-center")

  def align_self_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-end")

  def align_self_lg_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-lg-auto")

  def align_self_lg_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-lg-baseline")

  def align_self_lg_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-lg-center")

  def align_self_lg_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-lg-end")

  def align_self_lg_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-lg-start")

  def align_self_lg_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-lg-stretch")

  def align_self_md_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-md-auto")

  def align_self_md_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-md-baseline")

  def align_self_md_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-md-center")

  def align_self_md_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-md-end")

  def align_self_md_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-md-start")

  def align_self_md_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-md-stretch")

  def align_self_sm_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-sm-auto")

  def align_self_sm_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-sm-baseline")

  def align_self_sm_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-sm-center")

  def align_self_sm_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-sm-end")

  def align_self_sm_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-sm-start")

  def align_self_sm_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-sm-stretch")

  def align_self_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-start")

  def align_self_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-stretch")

  def align_self_xl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-xl-auto")

  def align_self_xl_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-xl-baseline")

  def align_self_xl_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-xl-center")

  def align_self_xl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-xl-end")

  def align_self_xl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-xl-start")

  def align_self_xl_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-xl-stretch")

  def align_self_xxl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-xxl-auto")

  def align_self_xxl_baseline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-xxl-baseline")

  def align_self_xxl_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-xxl-center")

  def align_self_xxl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-xxl-end")

  def align_self_xxl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-xxl-start")

  def align_self_xxl_stretch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-self-xxl-stretch")

  def align_text_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-text-bottom")

  def align_text_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-text-top")

  def align_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("align-top")

  def badge[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("badge")

  def bg_black[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-black")

  def bg_body[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-body")

  def bg_body_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-body-secondary")

  def bg_body_tertiary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-body-tertiary")

  def bg_danger[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-danger")

  def bg_danger_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-danger-subtle")

  def bg_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-dark")

  def bg_dark_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-dark-subtle")

  def bg_gradient[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-gradient")

  def bg_info[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-info")

  def bg_info_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-info-subtle")

  def bg_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-light")

  def bg_light_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-light-subtle")

  def bg_opacity_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-opacity-10")

  def bg_opacity_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-opacity-100")

  def bg_opacity_25[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-opacity-25")

  def bg_opacity_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-opacity-50")

  def bg_opacity_75[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-opacity-75")

  def bg_primary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-primary")

  def bg_primary_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-primary-subtle")

  def bg_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-secondary")

  def bg_secondary_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-secondary-subtle")

  def bg_success[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-success")

  def bg_success_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-success-subtle")

  def bg_transparent[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-transparent")

  def bg_warning[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-warning")

  def bg_warning_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-warning-subtle")

  def bg_white[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bg-white")

  def blockquote[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("blockquote")

  def blockquote_footer[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("blockquote-footer")

  def border[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border")

  def border_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-0")

  def border_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-1")

  def border_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-2")

  def border_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-3")

  def border_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-4")

  def border_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-5")

  def border_black[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-black")

  def border_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-bottom")

  def border_bottom_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-bottom-0")

  def border_danger[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-danger")

  def border_danger_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-danger-subtle")

  def border_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-dark")

  def border_dark_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-dark-subtle")

  def border_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-end")

  def border_end_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-end-0")

  def border_info[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-info")

  def border_info_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-info-subtle")

  def border_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-light")

  def border_light_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-light-subtle")

  def border_opacity_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-opacity-10")

  def border_opacity_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-opacity-100")

  def border_opacity_25[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-opacity-25")

  def border_opacity_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-opacity-50")

  def border_opacity_75[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-opacity-75")

  def border_primary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-primary")

  def border_primary_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-primary-subtle")

  def border_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-secondary")

  def border_secondary_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-secondary-subtle")

  def border_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-start")

  def border_start_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-start-0")

  def border_success[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-success")

  def border_success_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-success-subtle")

  def border_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-top")

  def border_top_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-top-0")

  def border_warning[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-warning")

  def border_warning_subtle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-warning-subtle")

  def border_white[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("border-white")

  def bottom_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bottom-0")

  def bottom_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bottom-100")

  def bottom_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bottom-50")

  def breadcrumb[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("breadcrumb")

  def breadcrumb_item[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("breadcrumb-item")

  def bs_popover_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bs-popover-auto")

  def bs_popover_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bs-popover-bottom")

  def bs_popover_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bs-popover-end")

  def bs_popover_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bs-popover-start")

  def bs_popover_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bs-popover-top")

  def bs_tooltip_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bs-tooltip-auto")

  def bs_tooltip_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bs-tooltip-bottom")

  def bs_tooltip_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bs-tooltip-end")

  def bs_tooltip_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bs-tooltip-start")

  def bs_tooltip_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("bs-tooltip-top")

  def btn[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn")

  def btn_check[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-check")

  def btn_close[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-close")

  def btn_close_white[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-close-white")

  def btn_danger[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-danger")

  def btn_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-dark")

  def btn_group[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-group")

  def btn_group_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-group-lg")

  def btn_group_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-group-sm")

  def btn_group_vertical[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-group-vertical")

  def btn_info[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-info")

  def btn_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-lg")

  def btn_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-light")

  def btn_link[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-link")

  def btn_outline_danger[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-outline-danger")

  def btn_outline_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-outline-dark")

  def btn_outline_info[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-outline-info")

  def btn_outline_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-outline-light")

  def btn_outline_primary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-outline-primary")

  def btn_outline_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-outline-secondary")

  def btn_outline_success[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-outline-success")

  def btn_outline_warning[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-outline-warning")

  def btn_primary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-primary")

  def btn_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-secondary")

  def btn_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-sm")

  def btn_success[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-success")

  def btn_toolbar[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-toolbar")

  def btn_warning[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("btn-warning")

  def caption_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("caption-top")

  def card[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card")

  def card_body[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-body")

  def card_footer[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-footer")

  def card_group[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-group")

  def card_header[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-header")

  def card_header_pills[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-header-pills")

  def card_header_tabs[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-header-tabs")

  def card_img[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-img")

  def card_img_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-img-bottom")

  def card_img_overlay[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-img-overlay")

  def card_img_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-img-top")

  def card_link[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-link")

  def card_subtitle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-subtitle")

  def card_text[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-text")

  def card_title[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("card-title")

  def carousel[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel")

  def carousel_caption[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel-caption")

  def carousel_control_next[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel-control-next")

  def carousel_control_next_icon[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel-control-next-icon")

  def carousel_control_prev[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel-control-prev")

  def carousel_control_prev_icon[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel-control-prev-icon")

  def carousel_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel-dark")

  def carousel_fade[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel-fade")

  def carousel_indicators[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel-indicators")

  def carousel_inner[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel-inner")

  def carousel_item[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel-item")

  def carousel_item_next[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel-item-next")

  def carousel_item_prev[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("carousel-item-prev")

  def clearfix[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("clearfix")

  def col[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col")

  def col_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-1")

  def col_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-10")

  def col_11[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-11")

  def col_12[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-12")

  def col_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-2")

  def col_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-3")

  def col_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-4")

  def col_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-5")

  def col_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-6")

  def col_7[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-7")

  def col_8[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-8")

  def col_9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-9")

  def col_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-auto")

  def col_form_label[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-form-label")

  def col_form_label_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-form-label-lg")

  def col_form_label_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-form-label-sm")

  def collapse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("collapse")

  def collapsing[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("collapsing")

  def col_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg")

  def col_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-1")

  def col_lg_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-10")

  def col_lg_11[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-11")

  def col_lg_12[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-12")

  def col_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-2")

  def col_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-3")

  def col_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-4")

  def col_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-5")

  def col_lg_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-6")

  def col_lg_7[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-7")

  def col_lg_8[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-8")

  def col_lg_9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-9")

  def col_lg_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-lg-auto")

  def col_md[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md")

  def col_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-1")

  def col_md_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-10")

  def col_md_11[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-11")

  def col_md_12[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-12")

  def col_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-2")

  def col_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-3")

  def col_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-4")

  def col_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-5")

  def col_md_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-6")

  def col_md_7[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-7")

  def col_md_8[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-8")

  def col_md_9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-9")

  def col_md_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-md-auto")

  def col_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm")

  def col_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-1")

  def col_sm_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-10")

  def col_sm_11[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-11")

  def col_sm_12[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-12")

  def col_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-2")

  def col_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-3")

  def col_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-4")

  def col_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-5")

  def col_sm_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-6")

  def col_sm_7[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-7")

  def col_sm_8[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-8")

  def col_sm_9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-9")

  def col_sm_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-sm-auto")

  def column_gap_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-0")

  def column_gap_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-1")

  def column_gap_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-2")

  def column_gap_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-3")

  def column_gap_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-4")

  def column_gap_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-5")

  def column_gap_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-lg-0")

  def column_gap_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-lg-1")

  def column_gap_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-lg-2")

  def column_gap_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-lg-3")

  def column_gap_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-lg-4")

  def column_gap_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-lg-5")

  def column_gap_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-md-0")

  def column_gap_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-md-1")

  def column_gap_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-md-2")

  def column_gap_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-md-3")

  def column_gap_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-md-4")

  def column_gap_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-md-5")

  def column_gap_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-sm-0")

  def column_gap_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-sm-1")

  def column_gap_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-sm-2")

  def column_gap_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-sm-3")

  def column_gap_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-sm-4")

  def column_gap_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-sm-5")

  def column_gap_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-xl-0")

  def column_gap_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-xl-1")

  def column_gap_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-xl-2")

  def column_gap_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-xl-3")

  def column_gap_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-xl-4")

  def column_gap_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-xl-5")

  def column_gap_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-xxl-0")

  def column_gap_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-xxl-1")

  def column_gap_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-xxl-2")

  def column_gap_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-xxl-3")

  def column_gap_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-xxl-4")

  def column_gap_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("column-gap-xxl-5")

  def col_xl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl")

  def col_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-1")

  def col_xl_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-10")

  def col_xl_11[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-11")

  def col_xl_12[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-12")

  def col_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-2")

  def col_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-3")

  def col_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-4")

  def col_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-5")

  def col_xl_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-6")

  def col_xl_7[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-7")

  def col_xl_8[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-8")

  def col_xl_9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-9")

  def col_xl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xl-auto")

  def col_xxl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl")

  def col_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-1")

  def col_xxl_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-10")

  def col_xxl_11[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-11")

  def col_xxl_12[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-12")

  def col_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-2")

  def col_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-3")

  def col_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-4")

  def col_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-5")

  def col_xxl_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-6")

  def col_xxl_7[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-7")

  def col_xxl_8[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-8")

  def col_xxl_9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-9")

  def col_xxl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("col-xxl-auto")

  def container[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("container")

  def container_fluid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("container-fluid")

  def container_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("container-lg")

  def container_md[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("container-md")

  def container_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("container-sm")

  def container_xl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("container-xl")

  def container_xxl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("container-xxl")

  def d_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-block")

  def d_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-flex")

  def d_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-grid")

  def d_inline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-inline")

  def d_inline_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-inline-block")

  def d_inline_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-inline-flex")

  def d_inline_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-inline-grid")

  def disabled[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("disabled")

  def display_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("display-1")

  def display_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("display-2")

  def display_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("display-3")

  def display_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("display-4")

  def display_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("display-5")

  def display_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("display-6")

  def d_lg_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-lg-block")

  def d_lg_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-lg-flex")

  def d_lg_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-lg-grid")

  def d_lg_inline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-lg-inline")

  def d_lg_inline_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-lg-inline-block")

  def d_lg_inline_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-lg-inline-flex")

  def d_lg_inline_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-lg-inline-grid")

  def d_lg_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-lg-none")

  def d_lg_table[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-lg-table")

  def d_lg_table_cell[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-lg-table-cell")

  def d_lg_table_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-lg-table-row")

  def d_md_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-md-block")

  def d_md_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-md-flex")

  def d_md_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-md-grid")

  def d_md_inline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-md-inline")

  def d_md_inline_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-md-inline-block")

  def d_md_inline_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-md-inline-flex")

  def d_md_inline_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-md-inline-grid")

  def d_md_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-md-none")

  def d_md_table[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-md-table")

  def d_md_table_cell[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-md-table-cell")

  def d_md_table_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-md-table-row")

  def d_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-none")

  def d_print_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-print-block")

  def d_print_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-print-flex")

  def d_print_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-print-grid")

  def d_print_inline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-print-inline")

  def d_print_inline_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-print-inline-block")

  def d_print_inline_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-print-inline-flex")

  def d_print_inline_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-print-inline-grid")

  def d_print_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-print-none")

  def d_print_table[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-print-table")

  def d_print_table_cell[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-print-table-cell")

  def d_print_table_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-print-table-row")

  def dropdown[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown")

  def dropdown_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-center")

  def dropdown_divider[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-divider")

  def dropdown_header[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-header")

  def dropdown_item[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-item")

  def dropdown_item_text[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-item-text")

  def dropdown_menu[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu")

  def dropdown_menu_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-dark")

  def dropdown_menu_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-end")

  def dropdown_menu_lg_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-lg-end")

  def dropdown_menu_lg_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-lg-start")

  def dropdown_menu_md_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-md-end")

  def dropdown_menu_md_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-md-start")

  def dropdown_menu_sm_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-sm-end")

  def dropdown_menu_sm_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-sm-start")

  def dropdown_menu_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-start")

  def dropdown_menu_xl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-xl-end")

  def dropdown_menu_xl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-xl-start")

  def dropdown_menu_xxl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-xxl-end")

  def dropdown_menu_xxl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-menu-xxl-start")

  def dropdown_toggle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-toggle")

  def dropdown_toggle_split[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropdown-toggle-split")

  def dropend[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropend")

  def dropstart[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropstart")

  def dropup[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropup")

  def dropup_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("dropup-center")

  def d_sm_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-sm-block")

  def d_sm_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-sm-flex")

  def d_sm_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-sm-grid")

  def d_sm_inline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-sm-inline")

  def d_sm_inline_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-sm-inline-block")

  def d_sm_inline_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-sm-inline-flex")

  def d_sm_inline_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-sm-inline-grid")

  def d_sm_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-sm-none")

  def d_sm_table[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-sm-table")

  def d_sm_table_cell[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-sm-table-cell")

  def d_sm_table_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-sm-table-row")

  def d_table[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-table")

  def d_table_cell[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-table-cell")

  def d_table_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-table-row")

  def d_xl_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xl-block")

  def d_xl_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xl-flex")

  def d_xl_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xl-grid")

  def d_xl_inline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xl-inline")

  def d_xl_inline_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xl-inline-block")

  def d_xl_inline_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xl-inline-flex")

  def d_xl_inline_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xl-inline-grid")

  def d_xl_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xl-none")

  def d_xl_table[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xl-table")

  def d_xl_table_cell[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xl-table-cell")

  def d_xl_table_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xl-table-row")

  def d_xxl_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xxl-block")

  def d_xxl_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xxl-flex")

  def d_xxl_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xxl-grid")

  def d_xxl_inline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xxl-inline")

  def d_xxl_inline_block[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xxl-inline-block")

  def d_xxl_inline_flex[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xxl-inline-flex")

  def d_xxl_inline_grid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xxl-inline-grid")

  def d_xxl_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xxl-none")

  def d_xxl_table[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xxl-table")

  def d_xxl_table_cell[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xxl-table-cell")

  def d_xxl_table_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("d-xxl-table-row")

  def end_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("end-0")

  def end_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("end-100")

  def end_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("end-50")

  def fade[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fade")

  def figure[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("figure")

  def figure_caption[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("figure-caption")

  def figure_img[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("figure-img")

  def fixed_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fixed-bottom")

  def fixed_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fixed-top")

  def flex_column[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-column")

  def flex_column_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-column-reverse")

  def flex_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-fill")

  def flex_grow_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-grow-0")

  def flex_grow_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-grow-1")

  def flex_lg_column[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-lg-column")

  def flex_lg_column_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-lg-column-reverse")

  def flex_lg_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-lg-fill")

  def flex_lg_grow_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-lg-grow-0")

  def flex_lg_grow_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-lg-grow-1")

  def flex_lg_nowrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-lg-nowrap")

  def flex_lg_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-lg-row")

  def flex_lg_row_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-lg-row-reverse")

  def flex_lg_shrink_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-lg-shrink-0")

  def flex_lg_shrink_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-lg-shrink-1")

  def flex_lg_wrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-lg-wrap")

  def flex_lg_wrap_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-lg-wrap-reverse")

  def flex_md_column[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-md-column")

  def flex_md_column_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-md-column-reverse")

  def flex_md_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-md-fill")

  def flex_md_grow_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-md-grow-0")

  def flex_md_grow_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-md-grow-1")

  def flex_md_nowrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-md-nowrap")

  def flex_md_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-md-row")

  def flex_md_row_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-md-row-reverse")

  def flex_md_shrink_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-md-shrink-0")

  def flex_md_shrink_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-md-shrink-1")

  def flex_md_wrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-md-wrap")

  def flex_md_wrap_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-md-wrap-reverse")

  def flex_nowrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-nowrap")

  def flex_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-row")

  def flex_row_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-row-reverse")

  def flex_shrink_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-shrink-0")

  def flex_shrink_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-shrink-1")

  def flex_sm_column[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-sm-column")

  def flex_sm_column_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-sm-column-reverse")

  def flex_sm_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-sm-fill")

  def flex_sm_grow_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-sm-grow-0")

  def flex_sm_grow_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-sm-grow-1")

  def flex_sm_nowrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-sm-nowrap")

  def flex_sm_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-sm-row")

  def flex_sm_row_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-sm-row-reverse")

  def flex_sm_shrink_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-sm-shrink-0")

  def flex_sm_shrink_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-sm-shrink-1")

  def flex_sm_wrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-sm-wrap")

  def flex_sm_wrap_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-sm-wrap-reverse")

  def flex_wrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-wrap")

  def flex_wrap_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-wrap-reverse")

  def flex_xl_column[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xl-column")

  def flex_xl_column_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xl-column-reverse")

  def flex_xl_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xl-fill")

  def flex_xl_grow_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xl-grow-0")

  def flex_xl_grow_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xl-grow-1")

  def flex_xl_nowrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xl-nowrap")

  def flex_xl_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xl-row")

  def flex_xl_row_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xl-row-reverse")

  def flex_xl_shrink_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xl-shrink-0")

  def flex_xl_shrink_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xl-shrink-1")

  def flex_xl_wrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xl-wrap")

  def flex_xl_wrap_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xl-wrap-reverse")

  def flex_xxl_column[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xxl-column")

  def flex_xxl_column_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xxl-column-reverse")

  def flex_xxl_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xxl-fill")

  def flex_xxl_grow_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xxl-grow-0")

  def flex_xxl_grow_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xxl-grow-1")

  def flex_xxl_nowrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xxl-nowrap")

  def flex_xxl_row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xxl-row")

  def flex_xxl_row_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xxl-row-reverse")

  def flex_xxl_shrink_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xxl-shrink-0")

  def flex_xxl_shrink_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xxl-shrink-1")

  def flex_xxl_wrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xxl-wrap")

  def flex_xxl_wrap_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("flex-xxl-wrap-reverse")

  def float_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-end")

  def float_lg_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-lg-end")

  def float_lg_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-lg-none")

  def float_lg_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-lg-start")

  def float_md_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-md-end")

  def float_md_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-md-none")

  def float_md_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-md-start")

  def float_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-none")

  def float_sm_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-sm-end")

  def float_sm_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-sm-none")

  def float_sm_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-sm-start")

  def float_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-start")

  def float_xl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-xl-end")

  def float_xl_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-xl-none")

  def float_xl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-xl-start")

  def float_xxl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-xxl-end")

  def float_xxl_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-xxl-none")

  def float_xxl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("float-xxl-start")

  def focus_ring[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("focus-ring")

  def focus_ring_danger[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("focus-ring-danger")

  def focus_ring_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("focus-ring-dark")

  def focus_ring_info[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("focus-ring-info")

  def focus_ring_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("focus-ring-light")

  def focus_ring_primary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("focus-ring-primary")

  def focus_ring_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("focus-ring-secondary")

  def focus_ring_success[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("focus-ring-success")

  def focus_ring_warning[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("focus-ring-warning")

  def font_monospace[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("font-monospace")

  def form_check[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-check")

  def form_check_inline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-check-inline")

  def form_check_input[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-check-input")

  def form_check_reverse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-check-reverse")

  def form_control[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-control")

  def form_control_color[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-control-color")

  def form_control_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-control-lg")

  def form_control_plaintext[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-control-plaintext")

  def form_control_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-control-sm")

  def form_floating[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-floating")

  def form_label[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-label")

  def form_range[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-range")

  def form_select[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-select")

  def form_select_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-select-lg")

  def form_select_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-select-sm")

  def form_switch[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-switch")

  def form_text[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-text")

  def fs_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fs-1")

  def fs_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fs-2")

  def fs_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fs-3")

  def fs_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fs-4")

  def fs_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fs-5")

  def fs_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fs-6")

  def fst_italic[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fst-italic")

  def fst_normal[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fst-normal")

  def fw_bold[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fw-bold")

  def fw_bolder[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fw-bolder")

  def fw_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fw-light")

  def fw_lighter[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fw-lighter")

  def fw_medium[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fw-medium")

  def fw_normal[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fw-normal")

  def fw_semibold[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("fw-semibold")

  def g_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-0")

  def g_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-1")

  def g_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-2")

  def g_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-3")

  def g_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-4")

  def g_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-5")

  def gap_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-0")

  def gap_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-1")

  def gap_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-2")

  def gap_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-3")

  def gap_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-4")

  def gap_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-5")

  def gap_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-lg-0")

  def gap_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-lg-1")

  def gap_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-lg-2")

  def gap_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-lg-3")

  def gap_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-lg-4")

  def gap_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-lg-5")

  def gap_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-md-0")

  def gap_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-md-1")

  def gap_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-md-2")

  def gap_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-md-3")

  def gap_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-md-4")

  def gap_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-md-5")

  def gap_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-sm-0")

  def gap_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-sm-1")

  def gap_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-sm-2")

  def gap_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-sm-3")

  def gap_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-sm-4")

  def gap_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-sm-5")

  def gap_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-xl-0")

  def gap_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-xl-1")

  def gap_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-xl-2")

  def gap_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-xl-3")

  def gap_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-xl-4")

  def gap_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-xl-5")

  def gap_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-xxl-0")

  def gap_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-xxl-1")

  def gap_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-xxl-2")

  def gap_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-xxl-3")

  def gap_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-xxl-4")

  def gap_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gap-xxl-5")

  def g_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-lg-0")

  def g_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-lg-1")

  def g_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-lg-2")

  def g_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-lg-3")

  def g_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-lg-4")

  def g_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-lg-5")

  def g_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-md-0")

  def g_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-md-1")

  def g_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-md-2")

  def g_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-md-3")

  def g_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-md-4")

  def g_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-md-5")

  def g_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-sm-0")

  def g_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-sm-1")

  def g_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-sm-2")

  def g_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-sm-3")

  def g_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-sm-4")

  def g_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-sm-5")

  def gx_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-0")

  def gx_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-1")

  def gx_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-2")

  def gx_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-3")

  def gx_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-4")

  def gx_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-5")

  def g_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-xl-0")

  def g_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-xl-1")

  def g_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-xl-2")

  def g_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-xl-3")

  def g_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-xl-4")

  def g_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-xl-5")

  def gx_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-lg-0")

  def gx_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-lg-1")

  def gx_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-lg-2")

  def gx_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-lg-3")

  def gx_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-lg-4")

  def gx_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-lg-5")

  def gx_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-md-0")

  def gx_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-md-1")

  def gx_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-md-2")

  def gx_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-md-3")

  def gx_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-md-4")

  def gx_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-md-5")

  def gx_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-sm-0")

  def gx_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-sm-1")

  def gx_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-sm-2")

  def gx_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-sm-3")

  def gx_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-sm-4")

  def gx_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-sm-5")

  def g_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-xxl-0")

  def gx_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-xl-0")

  def g_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-xxl-1")

  def gx_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-xl-1")

  def g_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-xxl-2")

  def gx_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-xl-2")

  def g_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-xxl-3")

  def gx_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-xl-3")

  def g_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-xxl-4")

  def gx_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-xl-4")

  def g_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("g-xxl-5")

  def gx_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-xl-5")

  def gx_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-xxl-0")

  def gx_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-xxl-1")

  def gx_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-xxl-2")

  def gx_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-xxl-3")

  def gx_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-xxl-4")

  def gx_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gx-xxl-5")

  def gy_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-0")

  def gy_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-1")

  def gy_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-2")

  def gy_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-3")

  def gy_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-4")

  def gy_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-5")

  def gy_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-lg-0")

  def gy_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-lg-1")

  def gy_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-lg-2")

  def gy_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-lg-3")

  def gy_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-lg-4")

  def gy_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-lg-5")

  def gy_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-md-0")

  def gy_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-md-1")

  def gy_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-md-2")

  def gy_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-md-3")

  def gy_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-md-4")

  def gy_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-md-5")

  def gy_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-sm-0")

  def gy_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-sm-1")

  def gy_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-sm-2")

  def gy_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-sm-3")

  def gy_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-sm-4")

  def gy_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-sm-5")

  def gy_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-xl-0")

  def gy_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-xl-1")

  def gy_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-xl-2")

  def gy_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-xl-3")

  def gy_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-xl-4")

  def gy_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-xl-5")

  def gy_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-xxl-0")

  def gy_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-xxl-1")

  def gy_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-xxl-2")

  def gy_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-xxl-3")

  def gy_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-xxl-4")

  def gy_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("gy-xxl-5")

  def h_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("h-100")

  def h_25[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("h-25")

  def h_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("h-50")

  def h_75[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("h-75")

  def h_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("h-auto")

  def hstack[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("hstack")

  def icon_link[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("icon-link")

  def icon_link_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("icon-link-hover")

  def img_fluid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("img-fluid")

  def img_thumbnail[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("img-thumbnail")

  def initialism[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("initialism")

  def input_group[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("input-group")

  def input_group_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("input-group-lg")

  def input_group_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("input-group-sm")

  def input_group_text[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("input-group-text")

  def invalid_feedback[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("invalid-feedback")

  def invalid_tooltip[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("invalid-tooltip")

  def invisible[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("invisible")

  def is_invalid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("is-invalid")

  def is_valid[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("is-valid")

  def justify_content_around[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-around")

  def justify_content_between[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-between")

  def justify_content_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-center")

  def justify_content_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-end")

  def justify_content_evenly[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-evenly")

  def justify_content_lg_around[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-lg-around")

  def justify_content_lg_between[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-lg-between")

  def justify_content_lg_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-lg-center")

  def justify_content_lg_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-lg-end")

  def justify_content_lg_evenly[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-lg-evenly")

  def justify_content_lg_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-lg-start")

  def justify_content_md_around[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-md-around")

  def justify_content_md_between[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-md-between")

  def justify_content_md_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-md-center")

  def justify_content_md_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-md-end")

  def justify_content_md_evenly[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-md-evenly")

  def justify_content_md_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-md-start")

  def justify_content_sm_around[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-sm-around")

  def justify_content_sm_between[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-sm-between")

  def justify_content_sm_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-sm-center")

  def justify_content_sm_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-sm-end")

  def justify_content_sm_evenly[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-sm-evenly")

  def justify_content_sm_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-sm-start")

  def justify_content_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-start")

  def justify_content_xl_around[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-xl-around")

  def justify_content_xl_between[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-xl-between")

  def justify_content_xl_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-xl-center")

  def justify_content_xl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-xl-end")

  def justify_content_xl_evenly[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-xl-evenly")

  def justify_content_xl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-xl-start")

  def justify_content_xxl_around[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-xxl-around")

  def justify_content_xxl_between[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-xxl-between")

  def justify_content_xxl_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-xxl-center")

  def justify_content_xxl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-xxl-end")

  def justify_content_xxl_evenly[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-xxl-evenly")

  def justify_content_xxl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("justify-content-xxl-start")

  def lead[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("lead")

  def lh_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("lh-1")

  def lh_base[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("lh-base")

  def lh_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("lh-lg")

  def lh_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("lh-sm")

  def link_body_emphasis[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-body-emphasis")

  def link_danger[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-danger")

  def link_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-dark")

  def link_info[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-info")

  def link_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-light")

  def link_offset_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-offset-1")

  def link_offset_1_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-offset-1-hover")

  def link_offset_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-offset-2")

  def link_offset_2_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-offset-2-hover")

  def link_offset_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-offset-3")

  def link_offset_3_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-offset-3-hover")

  def link_opacity_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-opacity-10")

  def link_opacity_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-opacity-100")

  def link_opacity_100_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-opacity-100-hover")

  def link_opacity_10_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-opacity-10-hover")

  def link_opacity_25[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-opacity-25")

  def link_opacity_25_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-opacity-25-hover")

  def link_opacity_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-opacity-50")

  def link_opacity_50_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-opacity-50-hover")

  def link_opacity_75[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-opacity-75")

  def link_opacity_75_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-opacity-75-hover")

  def link_primary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-primary")

  def link_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-secondary")

  def link_success[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-success")

  def link_underline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline")

  def link_underline_danger[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-danger")

  def link_underline_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-dark")

  def link_underline_info[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-info")

  def link_underline_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-light")

  def link_underline_opacity_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-opacity-0")

  def link_underline_opacity_0_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-opacity-0-hover")

  def link_underline_opacity_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-opacity-10")

  def link_underline_opacity_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-opacity-100")

  def link_underline_opacity_100_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-opacity-100-hover")

  def link_underline_opacity_10_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-opacity-10-hover")

  def link_underline_opacity_25[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-opacity-25")

  def link_underline_opacity_25_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-opacity-25-hover")

  def link_underline_opacity_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-opacity-50")

  def link_underline_opacity_50_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-opacity-50-hover")

  def link_underline_opacity_75[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-opacity-75")

  def link_underline_opacity_75_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-opacity-75-hover")

  def link_underline_primary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-primary")

  def link_underline_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-secondary")

  def link_underline_success[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-success")

  def link_underline_warning[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-underline-warning")

  def link_warning[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("link-warning")

  def list_group[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group")

  def list_group_flush[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-flush")

  def list_group_horizontal[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-horizontal")

  def list_group_horizontal_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-horizontal-lg")

  def list_group_horizontal_md[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-horizontal-md")

  def list_group_horizontal_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-horizontal-sm")

  def list_group_horizontal_xl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-horizontal-xl")

  def list_group_horizontal_xxl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-horizontal-xxl")

  def list_group_item[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-item")

  def list_group_item_action[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-item-action")

  def list_group_item_danger[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-item-danger")

  def list_group_item_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-item-dark")

  def list_group_item_info[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-item-info")

  def list_group_item_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-item-light")

  def list_group_item_primary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-item-primary")

  def list_group_item_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-item-secondary")

  def list_group_item_success[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-item-success")

  def list_group_item_warning[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-item-warning")

  def list_group_numbered[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-group-numbered")

  def list_inline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-inline")

  def list_inline_item[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-inline-item")

  def list_unstyled[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("list-unstyled")

  def m_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-0")

  def m_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-1")

  def m_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-2")

  def m_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-3")

  def m_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-4")

  def m_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-5")

  def m_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-auto")

  def mb_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-0")

  def mb_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-1")

  def mb_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-2")

  def mb_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-3")

  def mb_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-4")

  def mb_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-5")

  def mb_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-auto")

  def mb_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-lg-0")

  def mb_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-lg-1")

  def mb_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-lg-2")

  def mb_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-lg-3")

  def mb_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-lg-4")

  def mb_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-lg-5")

  def mb_lg_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-lg-auto")

  def mb_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-md-0")

  def mb_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-md-1")

  def mb_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-md-2")

  def mb_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-md-3")

  def mb_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-md-4")

  def mb_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-md-5")

  def mb_md_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-md-auto")

  def mb_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-sm-0")

  def mb_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-sm-1")

  def mb_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-sm-2")

  def mb_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-sm-3")

  def mb_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-sm-4")

  def mb_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-sm-5")

  def mb_sm_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-sm-auto")

  def mb_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xl-0")

  def mb_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xl-1")

  def mb_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xl-2")

  def mb_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xl-3")

  def mb_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xl-4")

  def mb_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xl-5")

  def mb_xl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xl-auto")

  def mb_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xxl-0")

  def mb_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xxl-1")

  def mb_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xxl-2")

  def mb_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xxl-3")

  def mb_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xxl-4")

  def mb_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xxl-5")

  def mb_xxl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mb-xxl-auto")

  def me_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-0")

  def me_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-1")

  def me_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-2")

  def me_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-3")

  def me_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-4")

  def me_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-5")

  def me_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-auto")

  def me_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-lg-0")

  def me_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-lg-1")

  def me_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-lg-2")

  def me_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-lg-3")

  def me_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-lg-4")

  def me_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-lg-5")

  def me_lg_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-lg-auto")

  def me_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-md-0")

  def me_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-md-1")

  def me_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-md-2")

  def me_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-md-3")

  def me_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-md-4")

  def me_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-md-5")

  def me_md_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-md-auto")

  def me_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-sm-0")

  def me_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-sm-1")

  def me_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-sm-2")

  def me_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-sm-3")

  def me_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-sm-4")

  def me_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-sm-5")

  def me_sm_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-sm-auto")

  def me_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xl-0")

  def me_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xl-1")

  def me_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xl-2")

  def me_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xl-3")

  def me_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xl-4")

  def me_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xl-5")

  def me_xl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xl-auto")

  def me_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xxl-0")

  def me_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xxl-1")

  def me_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xxl-2")

  def me_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xxl-3")

  def me_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xxl-4")

  def me_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xxl-5")

  def me_xxl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("me-xxl-auto")

  def mh_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mh-100")

  def min_vh_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("min-vh-100")

  def min_vw_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("min-vw-100")

  def m_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-lg-0")

  def m_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-lg-1")

  def m_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-lg-2")

  def m_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-lg-3")

  def m_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-lg-4")

  def m_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-lg-5")

  def m_lg_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-lg-auto")

  def m_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-md-0")

  def m_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-md-1")

  def m_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-md-2")

  def m_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-md-3")

  def m_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-md-4")

  def m_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-md-5")

  def m_md_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-md-auto")

  def modal[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal")

  def modal_backdrop[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-backdrop")

  def modal_body[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-body")

  def modal_content[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-content")

  def modal_dialog[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-dialog")

  def modal_dialog_centered[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-dialog-centered")

  def modal_dialog_scrollable[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-dialog-scrollable")

  def modal_footer[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-footer")

  def modal_fullscreen[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-fullscreen")

  def modal_fullscreen_lg_down[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-fullscreen-lg-down")

  def modal_fullscreen_md_down[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-fullscreen-md-down")

  def modal_fullscreen_sm_down[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-fullscreen-sm-down")

  def modal_fullscreen_xl_down[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-fullscreen-xl-down")

  def modal_fullscreen_xxl_down[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-fullscreen-xxl-down")

  def modal_header[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-header")

  def modal_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-lg")

  def modal_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-sm")

  def modal_title[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-title")

  def modal_xl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("modal-xl")

  def ms_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-0")

  def ms_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-1")

  def ms_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-2")

  def ms_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-3")

  def ms_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-4")

  def ms_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-5")

  def ms_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-auto")

  def ms_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-lg-0")

  def ms_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-lg-1")

  def ms_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-lg-2")

  def ms_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-lg-3")

  def ms_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-lg-4")

  def ms_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-lg-5")

  def ms_lg_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-lg-auto")

  def m_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-sm-0")

  def m_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-sm-1")

  def m_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-sm-2")

  def m_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-sm-3")

  def m_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-sm-4")

  def m_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-sm-5")

  def m_sm_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-sm-auto")

  def ms_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-md-0")

  def ms_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-md-1")

  def ms_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-md-2")

  def ms_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-md-3")

  def ms_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-md-4")

  def ms_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-md-5")

  def ms_md_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-md-auto")

  def ms_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-sm-0")

  def ms_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-sm-1")

  def ms_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-sm-2")

  def ms_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-sm-3")

  def ms_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-sm-4")

  def ms_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-sm-5")

  def ms_sm_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-sm-auto")

  def ms_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xl-0")

  def ms_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xl-1")

  def ms_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xl-2")

  def ms_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xl-3")

  def ms_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xl-4")

  def ms_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xl-5")

  def ms_xl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xl-auto")

  def ms_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xxl-0")

  def ms_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xxl-1")

  def ms_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xxl-2")

  def ms_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xxl-3")

  def ms_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xxl-4")

  def ms_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xxl-5")

  def ms_xxl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ms-xxl-auto")

  def mt_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-0")

  def mt_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-1")

  def mt_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-2")

  def mt_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-3")

  def mt_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-4")

  def mt_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-5")

  def mt_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-auto")

  def mt_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-lg-0")

  def mt_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-lg-1")

  def mt_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-lg-2")

  def mt_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-lg-3")

  def mt_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-lg-4")

  def mt_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-lg-5")

  def mt_lg_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-lg-auto")

  def mt_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-md-0")

  def mt_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-md-1")

  def mt_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-md-2")

  def mt_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-md-3")

  def mt_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-md-4")

  def mt_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-md-5")

  def mt_md_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-md-auto")

  def mt_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-sm-0")

  def mt_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-sm-1")

  def mt_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-sm-2")

  def mt_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-sm-3")

  def mt_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-sm-4")

  def mt_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-sm-5")

  def mt_sm_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-sm-auto")

  def mt_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xl-0")

  def mt_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xl-1")

  def mt_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xl-2")

  def mt_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xl-3")

  def mt_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xl-4")

  def mt_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xl-5")

  def mt_xl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xl-auto")

  def mt_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xxl-0")

  def mt_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xxl-1")

  def mt_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xxl-2")

  def mt_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xxl-3")

  def mt_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xxl-4")

  def mt_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xxl-5")

  def mt_xxl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mt-xxl-auto")

  def mw_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mw-100")

  def mx_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-0")

  def mx_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-1")

  def mx_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-2")

  def mx_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-3")

  def mx_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-4")

  def mx_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-5")

  def mx_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-auto")

  def m_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xl-0")

  def m_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xl-1")

  def m_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xl-2")

  def m_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xl-3")

  def m_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xl-4")

  def m_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xl-5")

  def m_xl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xl-auto")

  def mx_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-lg-0")

  def mx_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-lg-1")

  def mx_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-lg-2")

  def mx_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-lg-3")

  def mx_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-lg-4")

  def mx_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-lg-5")

  def mx_lg_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-lg-auto")

  def mx_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-md-0")

  def mx_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-md-1")

  def mx_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-md-2")

  def mx_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-md-3")

  def mx_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-md-4")

  def mx_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-md-5")

  def mx_md_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-md-auto")

  def mx_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-sm-0")

  def mx_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-sm-1")

  def mx_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-sm-2")

  def mx_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-sm-3")

  def mx_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-sm-4")

  def mx_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-sm-5")

  def mx_sm_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-sm-auto")

  def m_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xxl-0")

  def mx_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xl-0")

  def m_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xxl-1")

  def mx_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xl-1")

  def m_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xxl-2")

  def mx_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xl-2")

  def m_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xxl-3")

  def mx_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xl-3")

  def m_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xxl-4")

  def mx_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xl-4")

  def m_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xxl-5")

  def mx_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xl-5")

  def m_xxl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("m-xxl-auto")

  def mx_xl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xl-auto")

  def mx_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xxl-0")

  def mx_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xxl-1")

  def mx_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xxl-2")

  def mx_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xxl-3")

  def mx_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xxl-4")

  def mx_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xxl-5")

  def mx_xxl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("mx-xxl-auto")

  def my_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-0")

  def my_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-1")

  def my_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-2")

  def my_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-3")

  def my_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-4")

  def my_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-5")

  def my_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-auto")

  def my_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-lg-0")

  def my_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-lg-1")

  def my_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-lg-2")

  def my_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-lg-3")

  def my_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-lg-4")

  def my_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-lg-5")

  def my_lg_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-lg-auto")

  def my_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-md-0")

  def my_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-md-1")

  def my_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-md-2")

  def my_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-md-3")

  def my_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-md-4")

  def my_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-md-5")

  def my_md_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-md-auto")

  def my_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-sm-0")

  def my_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-sm-1")

  def my_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-sm-2")

  def my_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-sm-3")

  def my_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-sm-4")

  def my_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-sm-5")

  def my_sm_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-sm-auto")

  def my_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xl-0")

  def my_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xl-1")

  def my_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xl-2")

  def my_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xl-3")

  def my_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xl-4")

  def my_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xl-5")

  def my_xl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xl-auto")

  def my_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xxl-0")

  def my_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xxl-1")

  def my_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xxl-2")

  def my_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xxl-3")

  def my_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xxl-4")

  def my_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xxl-5")

  def my_xxl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("my-xxl-auto")

  def nav[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("nav")

  def navbar[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar")

  def navbar_brand[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-brand")

  def navbar_collapse[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-collapse")

  def navbar_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-dark")

  def navbar_expand[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-expand")

  def navbar_expand_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-expand-lg")

  def navbar_expand_md[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-expand-md")

  def navbar_expand_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-expand-sm")

  def navbar_expand_xl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-expand-xl")

  def navbar_expand_xxl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-expand-xxl")

  def navbar_nav[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-nav")

  def navbar_nav_scroll[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-nav-scroll")

  def navbar_text[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-text")

  def navbar_texta[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-texta")

  def navbar_toggler[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-toggler")

  def navbar_toggler_icon[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("navbar-toggler-icon")

  def nav_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("nav-fill")

  def nav_justified[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("nav-justified")

  def nav_link[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("nav-link")

  def nav_pills[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("nav-pills")

  def nav_tabs[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("nav-tabs")

  def nav_underline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("nav-underline")

  def object_fit_contain[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-contain")

  def object_fit_cover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-cover")

  def object_fit_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-fill")

  def object_fit_lg_contain[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-lg-contain")

  def object_fit_lg_cover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-lg-cover")

  def object_fit_lg_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-lg-fill")

  def object_fit_lg_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-lg-none")

  def object_fit_lg_scale[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-lg-scale")

  def object_fit_md_contain[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-md-contain")

  def object_fit_md_cover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-md-cover")

  def object_fit_md_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-md-fill")

  def object_fit_md_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-md-none")

  def object_fit_md_scale[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-md-scale")

  def object_fit_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-none")

  def object_fit_scale[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-scale")

  def object_fit_sm_contain[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-sm-contain")

  def object_fit_sm_cover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-sm-cover")

  def object_fit_sm_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-sm-fill")

  def object_fit_sm_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-sm-none")

  def object_fit_sm_scale[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-sm-scale")

  def object_fit_xl_contain[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-xl-contain")

  def object_fit_xl_cover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-xl-cover")

  def object_fit_xl_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-xl-fill")

  def object_fit_xl_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-xl-none")

  def object_fit_xl_scale[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-xl-scale")

  def object_fit_xxl_contain[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-xxl-contain")

  def object_fit_xxl_cover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-xxl-cover")

  def object_fit_xxl_fill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-xxl-fill")

  def object_fit_xxl_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-xxl-none")

  def object_fit_xxl_scale[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("object-fit-xxl-scale")

  def offcanvas[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offcanvas")

  def offcanvas_backdrop[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offcanvas-backdrop")

  def offcanvas_body[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offcanvas-body")

  def offcanvas_header[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offcanvas-header")

  def offcanvas_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offcanvas-lg")

  def offcanvas_md[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offcanvas-md")

  def offcanvas_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offcanvas-sm")

  def offcanvas_title[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offcanvas-title")

  def offcanvas_xl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offcanvas-xl")

  def offcanvas_xxl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offcanvas-xxl")

  def offset_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-1")

  def offset_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-10")

  def offset_11[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-11")

  def offset_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-2")

  def offset_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-3")

  def offset_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-4")

  def offset_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-5")

  def offset_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-6")

  def offset_7[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-7")

  def offset_8[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-8")

  def offset_9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-9")

  def offset_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-lg-0")

  def offset_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-lg-1")

  def offset_lg_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-lg-10")

  def offset_lg_11[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-lg-11")

  def offset_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-lg-2")

  def offset_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-lg-3")

  def offset_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-lg-4")

  def offset_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-lg-5")

  def offset_lg_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-lg-6")

  def offset_lg_7[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-lg-7")

  def offset_lg_8[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-lg-8")

  def offset_lg_9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-lg-9")

  def offset_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-md-0")

  def offset_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-md-1")

  def offset_md_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-md-10")

  def offset_md_11[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-md-11")

  def offset_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-md-2")

  def offset_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-md-3")

  def offset_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-md-4")

  def offset_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-md-5")

  def offset_md_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-md-6")

  def offset_md_7[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-md-7")

  def offset_md_8[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-md-8")

  def offset_md_9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-md-9")

  def offset_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-sm-0")

  def offset_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-sm-1")

  def offset_sm_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-sm-10")

  def offset_sm_11[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-sm-11")

  def offset_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-sm-2")

  def offset_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-sm-3")

  def offset_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-sm-4")

  def offset_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-sm-5")

  def offset_sm_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-sm-6")

  def offset_sm_7[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-sm-7")

  def offset_sm_8[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-sm-8")

  def offset_sm_9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-sm-9")

  def offset_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xl-0")

  def offset_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xl-1")

  def offset_xl_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xl-10")

  def offset_xl_11[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xl-11")

  def offset_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xl-2")

  def offset_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xl-3")

  def offset_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xl-4")

  def offset_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xl-5")

  def offset_xl_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xl-6")

  def offset_xl_7[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xl-7")

  def offset_xl_8[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xl-8")

  def offset_xl_9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xl-9")

  def offset_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xxl-0")

  def offset_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xxl-1")

  def offset_xxl_10[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xxl-10")

  def offset_xxl_11[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xxl-11")

  def offset_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xxl-2")

  def offset_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xxl-3")

  def offset_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xxl-4")

  def offset_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xxl-5")

  def offset_xxl_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xxl-6")

  def offset_xxl_7[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xxl-7")

  def offset_xxl_8[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xxl-8")

  def offset_xxl_9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("offset-xxl-9")

  def opacity_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("opacity-0")

  def opacity_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("opacity-100")

  def opacity_25[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("opacity-25")

  def opacity_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("opacity-50")

  def opacity_75[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("opacity-75")

  def order_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-0")

  def order_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-1")

  def order_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-2")

  def order_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-3")

  def order_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-4")

  def order_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-5")

  def order_first[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-first")

  def order_last[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-last")

  def order_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-lg-0")

  def order_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-lg-1")

  def order_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-lg-2")

  def order_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-lg-3")

  def order_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-lg-4")

  def order_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-lg-5")

  def order_lg_first[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-lg-first")

  def order_lg_last[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-lg-last")

  def order_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-md-0")

  def order_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-md-1")

  def order_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-md-2")

  def order_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-md-3")

  def order_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-md-4")

  def order_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-md-5")

  def order_md_first[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-md-first")

  def order_md_last[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-md-last")

  def order_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-sm-0")

  def order_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-sm-1")

  def order_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-sm-2")

  def order_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-sm-3")

  def order_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-sm-4")

  def order_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-sm-5")

  def order_sm_first[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-sm-first")

  def order_sm_last[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-sm-last")

  def order_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xl-0")

  def order_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xl-1")

  def order_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xl-2")

  def order_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xl-3")

  def order_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xl-4")

  def order_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xl-5")

  def order_xl_first[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xl-first")

  def order_xl_last[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xl-last")

  def order_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xxl-0")

  def order_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xxl-1")

  def order_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xxl-2")

  def order_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xxl-3")

  def order_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xxl-4")

  def order_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xxl-5")

  def order_xxl_first[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xxl-first")

  def order_xxl_last[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("order-xxl-last")

  def overflow_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("overflow-auto")

  def overflow_hidden[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("overflow-hidden")

  def overflow_scroll[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("overflow-scroll")

  def overflow_visible[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("overflow-visible")

  def overflow_x_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("overflow-x-auto")

  def overflow_x_hidden[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("overflow-x-hidden")

  def overflow_x_scroll[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("overflow-x-scroll")

  def overflow_x_visible[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("overflow-x-visible")

  def overflow_y_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("overflow-y-auto")

  def overflow_y_hidden[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("overflow-y-hidden")

  def overflow_y_scroll[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("overflow-y-scroll")

  def overflow_y_visible[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("overflow-y-visible")

  def p_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-0")

  def p_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-1")

  def p_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-2")

  def p_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-3")

  def p_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-4")

  def p_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-5")

  def page_item[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("page-item")

  def page_link[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("page-link")

  def pagination[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pagination")

  def pagination_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pagination-lg")

  def pagination_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pagination-sm")

  def pb_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-0")

  def pb_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-1")

  def pb_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-2")

  def pb_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-3")

  def pb_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-4")

  def pb_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-5")

  def pb_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-lg-0")

  def pb_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-lg-1")

  def pb_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-lg-2")

  def pb_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-lg-3")

  def pb_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-lg-4")

  def pb_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-lg-5")

  def pb_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-md-0")

  def pb_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-md-1")

  def pb_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-md-2")

  def pb_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-md-3")

  def pb_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-md-4")

  def pb_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-md-5")

  def pb_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-sm-0")

  def pb_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-sm-1")

  def pb_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-sm-2")

  def pb_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-sm-3")

  def pb_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-sm-4")

  def pb_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-sm-5")

  def pb_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-xl-0")

  def pb_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-xl-1")

  def pb_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-xl-2")

  def pb_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-xl-3")

  def pb_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-xl-4")

  def pb_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-xl-5")

  def pb_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-xxl-0")

  def pb_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-xxl-1")

  def pb_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-xxl-2")

  def pb_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-xxl-3")

  def pb_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-xxl-4")

  def pb_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pb-xxl-5")

  def pe_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-0")

  def pe_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-1")

  def pe_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-2")

  def pe_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-3")

  def pe_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-4")

  def pe_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-5")

  def pe_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-auto")

  def pe_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-lg-0")

  def pe_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-lg-1")

  def pe_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-lg-2")

  def pe_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-lg-3")

  def pe_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-lg-4")

  def pe_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-lg-5")

  def pe_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-md-0")

  def pe_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-md-1")

  def pe_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-md-2")

  def pe_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-md-3")

  def pe_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-md-4")

  def pe_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-md-5")

  def pe_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-none")

  def pe_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-sm-0")

  def pe_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-sm-1")

  def pe_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-sm-2")

  def pe_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-sm-3")

  def pe_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-sm-4")

  def pe_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-sm-5")

  def pe_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-xl-0")

  def pe_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-xl-1")

  def pe_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-xl-2")

  def pe_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-xl-3")

  def pe_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-xl-4")

  def pe_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-xl-5")

  def pe_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-xxl-0")

  def pe_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-xxl-1")

  def pe_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-xxl-2")

  def pe_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-xxl-3")

  def pe_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-xxl-4")

  def pe_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pe-xxl-5")

  def placeholder[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("placeholder")

  def placeholder_glow[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("placeholder-glow")

  def placeholder_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("placeholder-lg")

  def placeholder_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("placeholder-sm")

  def placeholder_wave[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("placeholder-wave")

  def placeholder_xs[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("placeholder-xs")

  def p_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-lg-0")

  def p_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-lg-1")

  def p_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-lg-2")

  def p_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-lg-3")

  def p_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-lg-4")

  def p_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-lg-5")

  def p_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-md-0")

  def p_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-md-1")

  def p_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-md-2")

  def p_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-md-3")

  def p_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-md-4")

  def p_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-md-5")

  def popover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("popover")

  def popover_body[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("popover-body")

  def popover_header[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("popover-header")

  def position_absolute[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("position-absolute")

  def position_fixed[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("position-fixed")

  def position_relative[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("position-relative")

  def position_static[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("position-static")

  def position_sticky[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("position-sticky")

  def progress[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("progress")

  def progress_bar[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("progress-bar")

  def progress_bar_animated[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("progress-bar-animated")

  def progress_bar_striped[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("progress-bar-striped")

  def progress_stacked[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("progress-stacked")

  def ps_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-0")

  def ps_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-1")

  def ps_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-2")

  def ps_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-3")

  def ps_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-4")

  def ps_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-5")

  def ps_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-lg-0")

  def ps_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-lg-1")

  def ps_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-lg-2")

  def ps_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-lg-3")

  def ps_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-lg-4")

  def ps_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-lg-5")

  def p_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-sm-0")

  def p_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-sm-1")

  def p_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-sm-2")

  def p_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-sm-3")

  def p_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-sm-4")

  def p_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-sm-5")

  def ps_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-md-0")

  def ps_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-md-1")

  def ps_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-md-2")

  def ps_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-md-3")

  def ps_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-md-4")

  def ps_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-md-5")

  def ps_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-sm-0")

  def ps_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-sm-1")

  def ps_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-sm-2")

  def ps_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-sm-3")

  def ps_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-sm-4")

  def ps_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-sm-5")

  def ps_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-xl-0")

  def ps_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-xl-1")

  def ps_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-xl-2")

  def ps_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-xl-3")

  def ps_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-xl-4")

  def ps_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-xl-5")

  def ps_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-xxl-0")

  def ps_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-xxl-1")

  def ps_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-xxl-2")

  def ps_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-xxl-3")

  def ps_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-xxl-4")

  def ps_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ps-xxl-5")

  def pt_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-0")

  def pt_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-1")

  def pt_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-2")

  def pt_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-3")

  def pt_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-4")

  def pt_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-5")

  def pt_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-lg-0")

  def pt_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-lg-1")

  def pt_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-lg-2")

  def pt_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-lg-3")

  def pt_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-lg-4")

  def pt_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-lg-5")

  def pt_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-md-0")

  def pt_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-md-1")

  def pt_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-md-2")

  def pt_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-md-3")

  def pt_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-md-4")

  def pt_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-md-5")

  def pt_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-sm-0")

  def pt_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-sm-1")

  def pt_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-sm-2")

  def pt_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-sm-3")

  def pt_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-sm-4")

  def pt_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-sm-5")

  def pt_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-xl-0")

  def pt_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-xl-1")

  def pt_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-xl-2")

  def pt_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-xl-3")

  def pt_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-xl-4")

  def pt_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-xl-5")

  def pt_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-xxl-0")

  def pt_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-xxl-1")

  def pt_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-xxl-2")

  def pt_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-xxl-3")

  def pt_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-xxl-4")

  def pt_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("pt-xxl-5")

  def px_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-0")

  def px_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-1")

  def px_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-2")

  def px_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-3")

  def px_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-4")

  def px_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-5")

  def p_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-xl-0")

  def p_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-xl-1")

  def p_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-xl-2")

  def p_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-xl-3")

  def p_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-xl-4")

  def p_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-xl-5")

  def px_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-lg-0")

  def px_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-lg-1")

  def px_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-lg-2")

  def px_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-lg-3")

  def px_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-lg-4")

  def px_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-lg-5")

  def px_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-md-0")

  def px_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-md-1")

  def px_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-md-2")

  def px_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-md-3")

  def px_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-md-4")

  def px_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-md-5")

  def px_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-sm-0")

  def px_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-sm-1")

  def px_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-sm-2")

  def px_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-sm-3")

  def px_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-sm-4")

  def px_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-sm-5")

  def p_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-xxl-0")

  def px_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-xl-0")

  def p_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-xxl-1")

  def px_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-xl-1")

  def p_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-xxl-2")

  def px_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-xl-2")

  def p_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-xxl-3")

  def px_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-xl-3")

  def p_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-xxl-4")

  def px_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-xl-4")

  def p_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("p-xxl-5")

  def px_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-xl-5")

  def px_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-xxl-0")

  def px_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-xxl-1")

  def px_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-xxl-2")

  def px_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-xxl-3")

  def px_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-xxl-4")

  def px_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("px-xxl-5")

  def py_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-0")

  def py_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-1")

  def py_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-2")

  def py_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-3")

  def py_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-4")

  def py_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-5")

  def py_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-lg-0")

  def py_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-lg-1")

  def py_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-lg-2")

  def py_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-lg-3")

  def py_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-lg-4")

  def py_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-lg-5")

  def py_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-md-0")

  def py_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-md-1")

  def py_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-md-2")

  def py_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-md-3")

  def py_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-md-4")

  def py_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-md-5")

  def py_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-sm-0")

  def py_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-sm-1")

  def py_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-sm-2")

  def py_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-sm-3")

  def py_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-sm-4")

  def py_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-sm-5")

  def py_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-xl-0")

  def py_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-xl-1")

  def py_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-xl-2")

  def py_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-xl-3")

  def py_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-xl-4")

  def py_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-xl-5")

  def py_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-xxl-0")

  def py_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-xxl-1")

  def py_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-xxl-2")

  def py_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-xxl-3")

  def py_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-xxl-4")

  def py_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("py-xxl-5")

  def ratio[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ratio")

  def ratio_16x9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ratio-16x9")

  def ratio_1x1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ratio-1x1")

  def ratio_21x9[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ratio-21x9")

  def ratio_4x3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("ratio-4x3")

  def rounded[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded")

  def rounded_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-0")

  def rounded_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-1")

  def rounded_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-2")

  def rounded_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-3")

  def rounded_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-4")

  def rounded_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-5")

  def rounded_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-bottom")

  def rounded_bottom_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-bottom-0")

  def rounded_bottom_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-bottom-1")

  def rounded_bottom_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-bottom-2")

  def rounded_bottom_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-bottom-3")

  def rounded_bottom_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-bottom-4")

  def rounded_bottom_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-bottom-5")

  def rounded_bottom_circle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-bottom-circle")

  def rounded_bottom_pill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-bottom-pill")

  def rounded_circle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-circle")

  def rounded_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-end")

  def rounded_end_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-end-0")

  def rounded_end_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-end-1")

  def rounded_end_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-end-2")

  def rounded_end_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-end-3")

  def rounded_end_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-end-4")

  def rounded_end_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-end-5")

  def rounded_end_circle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-end-circle")

  def rounded_end_pill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-end-pill")

  def rounded_pill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-pill")

  def rounded_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-start")

  def rounded_start_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-start-0")

  def rounded_start_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-start-1")

  def rounded_start_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-start-2")

  def rounded_start_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-start-3")

  def rounded_start_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-start-4")

  def rounded_start_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-start-5")

  def rounded_start_circle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-start-circle")

  def rounded_start_pill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-start-pill")

  def rounded_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-top")

  def rounded_top_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-top-0")

  def rounded_top_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-top-1")

  def rounded_top_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-top-2")

  def rounded_top_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-top-3")

  def rounded_top_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-top-4")

  def rounded_top_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-top-5")

  def rounded_top_circle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-top-circle")

  def rounded_top_pill[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("rounded-top-pill")

  def row[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row")

  def row_cols_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-1")

  def row_cols_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-2")

  def row_cols_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-3")

  def row_cols_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-4")

  def row_cols_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-5")

  def row_cols_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-6")

  def row_cols_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-auto")

  def row_cols_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-lg-1")

  def row_cols_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-lg-2")

  def row_cols_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-lg-3")

  def row_cols_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-lg-4")

  def row_cols_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-lg-5")

  def row_cols_lg_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-lg-6")

  def row_cols_lg_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-lg-auto")

  def row_cols_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-md-1")

  def row_cols_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-md-2")

  def row_cols_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-md-3")

  def row_cols_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-md-4")

  def row_cols_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-md-5")

  def row_cols_md_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-md-6")

  def row_cols_md_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-md-auto")

  def row_cols_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-sm-1")

  def row_cols_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-sm-2")

  def row_cols_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-sm-3")

  def row_cols_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-sm-4")

  def row_cols_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-sm-5")

  def row_cols_sm_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-sm-6")

  def row_cols_sm_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-sm-auto")

  def row_cols_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xl-1")

  def row_cols_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xl-2")

  def row_cols_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xl-3")

  def row_cols_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xl-4")

  def row_cols_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xl-5")

  def row_cols_xl_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xl-6")

  def row_cols_xl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xl-auto")

  def row_cols_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xxl-1")

  def row_cols_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xxl-2")

  def row_cols_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xxl-3")

  def row_cols_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xxl-4")

  def row_cols_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xxl-5")

  def row_cols_xxl_6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xxl-6")

  def row_cols_xxl_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-cols-xxl-auto")

  def row_gap_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-0")

  def row_gap_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-1")

  def row_gap_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-2")

  def row_gap_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-3")

  def row_gap_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-4")

  def row_gap_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-5")

  def row_gap_lg_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-lg-0")

  def row_gap_lg_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-lg-1")

  def row_gap_lg_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-lg-2")

  def row_gap_lg_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-lg-3")

  def row_gap_lg_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-lg-4")

  def row_gap_lg_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-lg-5")

  def row_gap_md_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-md-0")

  def row_gap_md_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-md-1")

  def row_gap_md_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-md-2")

  def row_gap_md_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-md-3")

  def row_gap_md_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-md-4")

  def row_gap_md_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-md-5")

  def row_gap_sm_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-sm-0")

  def row_gap_sm_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-sm-1")

  def row_gap_sm_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-sm-2")

  def row_gap_sm_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-sm-3")

  def row_gap_sm_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-sm-4")

  def row_gap_sm_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-sm-5")

  def row_gap_xl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-xl-0")

  def row_gap_xl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-xl-1")

  def row_gap_xl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-xl-2")

  def row_gap_xl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-xl-3")

  def row_gap_xl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-xl-4")

  def row_gap_xl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-xl-5")

  def row_gap_xxl_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-xxl-0")

  def row_gap_xxl_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-xxl-1")

  def row_gap_xxl_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-xxl-2")

  def row_gap_xxl_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-xxl-3")

  def row_gap_xxl_4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-xxl-4")

  def row_gap_xxl_5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("row-gap-xxl-5")

  def shadow[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("shadow")

  def shadow_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("shadow-lg")

  def shadow_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("shadow-none")

  def shadow_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("shadow-sm")

  def spinner_border[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("spinner-border")

  def spinner_border_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("spinner-border-sm")

  def spinner_grow[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("spinner-grow")

  def spinner_grow_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("spinner-grow-sm")

  def start_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("start-0")

  def start_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("start-100")

  def start_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("start-50")

  def sticky_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("sticky-bottom")

  def sticky_lg_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("sticky-lg-bottom")

  def sticky_lg_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("sticky-lg-top")

  def sticky_md_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("sticky-md-bottom")

  def sticky_md_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("sticky-md-top")

  def sticky_sm_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("sticky-sm-bottom")

  def sticky_sm_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("sticky-sm-top")

  def sticky_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("sticky-top")

  def sticky_xl_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("sticky-xl-bottom")

  def sticky_xl_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("sticky-xl-top")

  def sticky_xxl_bottom[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("sticky-xxl-bottom")

  def sticky_xxl_top[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("sticky-xxl-top")

  def stretched_link[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("stretched-link")

  def tab_content[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("tab-content")

  def table[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table")

  def table_active[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-active")

  def table_bordered[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-bordered")

  def table_borderless[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-borderless")

  def table_danger[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-danger")

  def table_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-dark")

  def table_group_divider[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-group-divider")

  def table_hover[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-hover")

  def table_info[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-info")

  def table_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-light")

  def table_primary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-primary")

  def table_responsive[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-responsive")

  def table_responsive_lg[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-responsive-lg")

  def table_responsive_md[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-responsive-md")

  def table_responsive_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-responsive-sm")

  def table_responsive_xl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-responsive-xl")

  def table_responsive_xxl[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-responsive-xxl")

  def table_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-secondary")

  def table_sm[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-sm")

  def table_striped[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-striped")

  def table_striped_columns[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-striped-columns")

  def table_success[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-success")

  def table_warning[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("table-warning")

  def text_bg_danger[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-bg-danger")

  def text_bg_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-bg-dark")

  def text_bg_info[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-bg-info")

  def text_bg_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-bg-light")

  def text_bg_primary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-bg-primary")

  def text_bg_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-bg-secondary")

  def text_bg_success[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-bg-success")

  def text_bg_warning[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-bg-warning")

  def text_black[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-black")

  def text_black_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-black-50")

  def text_body[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-body")

  def text_body_emphasis[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-body-emphasis")

  def text_body_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-body-secondary")

  def text_body_tertiary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-body-tertiary")

  def text_break[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-break")

  def text_capitalize[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-capitalize")

  def text_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-center")

  def text_danger[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-danger")

  def text_danger_emphasis[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-danger-emphasis")

  def text_dark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-dark")

  def text_dark_emphasis[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-dark-emphasis")

  def text_decoration_line_through[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-decoration-line-through")

  def text_decoration_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-decoration-none")

  def text_decoration_underline[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-decoration-underline")

  def text_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-end")

  def text_info[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-info")

  def text_info_emphasis[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-info-emphasis")

  def text_lg_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-lg-center")

  def text_lg_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-lg-end")

  def text_lg_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-lg-start")

  def text_light[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-light")

  def text_light_emphasis[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-light-emphasis")

  def text_lowercase[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-lowercase")

  def text_md_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-md-center")

  def text_md_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-md-end")

  def text_md_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-md-start")

  def text_muted[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-muted")

  def text_nowrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-nowrap")

  def text_opacity_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-opacity-100")

  def text_opacity_25[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-opacity-25")

  def text_opacity_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-opacity-50")

  def text_opacity_75[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-opacity-75")

  def text_primary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-primary")

  def text_primary_emphasis[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-primary-emphasis")

  def text_reset[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-reset")

  def text_secondary[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-secondary")

  def text_secondary_emphasis[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-secondary-emphasis")

  def text_sm_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-sm-center")

  def text_sm_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-sm-end")

  def text_sm_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-sm-start")

  def text_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-start")

  def text_success[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-success")

  def text_success_emphasis[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-success-emphasis")

  def text_truncate[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-truncate")

  def text_uppercase[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-uppercase")

  def text_warning[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-warning")

  def text_warning_emphasis[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-warning-emphasis")

  def text_white[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-white")

  def text_white_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-white-50")

  def text_wrap[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-wrap")

  def text_xl_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-xl-center")

  def text_xl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-xl-end")

  def text_xl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-xl-start")

  def text_xxl_center[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-xxl-center")

  def text_xxl_end[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-xxl-end")

  def text_xxl_start[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("text-xxl-start")

  def toast[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("toast")

  def toast_body[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("toast-body")

  def toast_container[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("toast-container")

  def toast_header[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("toast-header")

  def tooltip[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("tooltip")

  def tooltip_inner[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("tooltip-inner")

  def top_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("top-0")

  def top_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("top-100")

  def top_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("top-50")

  def translate_middle[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("translate-middle")

  def translate_middle_x[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("translate-middle-x")

  def translate_middle_y[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("translate-middle-y")

  def user_select_all[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("user-select-all")

  def user_select_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("user-select-auto")

  def user_select_none[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("user-select-none")

  def valid_feedback[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("valid-feedback")

  def valid_tooltip[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("valid-tooltip")

  def vh_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("vh-100")

  def visible[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("visible")

  def visually_hidden[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("visually-hidden")

  def visually_hidden_focusable[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("visually-hidden-focusable")

  def vr[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("vr")

  def vstack[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("vstack")

  def vw_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("vw-100")

  def w_100[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("w-100")

  def w_25[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("w-25")

  def w_50[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("w-50")

  def w_75[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("w-75")

  def was_validated[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("was-validated")

  def was_validatedtextarea[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("was-validatedtextarea")

  def w_auto[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("w-auto")

  def z_0[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("z-0")

  def z_1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("z-1")

  def z_2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("z-2")

  def z_3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("z-3")

  def z_n1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("z-n1")

  def show[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("show")

  def form_check_label[E <: FSXmlEnv : FSXmlSupport]: E#Elem = withClass("form-check-label")
}
