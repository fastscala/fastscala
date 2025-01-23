package com.fastscala.components.aceeditor

object Theme extends Enumeration {

  // Dark:
  val chrome = Value("chrome")
  val clouds = Value("clouds")
  val crimson_editor = Value("crimson_editor")
  val dawn = Value("dawn")
  val dreamweaver = Value("dreamweaver")
  val eclipse = Value("eclipse")
  val github = Value("github")
  val solarized_light = Value("solarized_light")
  val textmate = Value("textmate")
  val tomorrow = Value("tomorrow")
  val xcode = Value("xcode")
  val kuroir = Value("kuroir")
  val katzenmilch = Value("katzenmilch")
  //Light:
  val ambiance = Value("ambiance")
  val chaos = Value("chaos")
  val clouds_midnight = Value("clouds_midnight")
  val cobalt = Value("cobalt")
  val idle_fingers = Value("idle_fingers")
  val kr_theme = Value("kr_theme")
  val merbivore = Value("merbivore")
  val merbivore_soft = Value("merbivore_soft")
  val mono_industrial = Value("mono_industrial")
  val monokai = Value("monokai")
  val pastel_on_dark = Value("pastel_on_dark")
  val solarized_dark = Value("solarized_dark")
  val terminal = Value("terminal")
  val tomorrow_night = Value("tomorrow_night")
  val tomorrow_night_blue = Value("tomorrow_night_blue")
  val tomorrow_night_bright = Value("tomorrow_night_bright")
  val tomorrow_night_eighties = Value("tomorrow_night_eighties")
  val twilight = Value("twilight")
  val vibrant_ink = Value("vibrant_ink")

  def all = List(
    chrome
    , clouds
    , crimson_editor
    , dawn
    , dreamweaver
    , eclipse
    , github
    , solarized_light
    , textmate
    , tomorrow
    , xcode
    , kuroir
    , katzenmilch
    , ambiance
    , chaos
    , clouds_midnight
    , cobalt
    , idle_fingers
    , kr_theme
    , merbivore
    , merbivore_soft
    , mono_industrial
    , monokai
    , pastel_on_dark
    , solarized_dark
    , terminal
    , tomorrow_night
    , tomorrow_night_blue
    , tomorrow_night_bright
    , tomorrow_night_eighties
    , twilight
    , vibrant_ink
  )
}
