package com.fastscala.templates.bootstrap5.classes

trait BSOtherHelper[T] {

  def withClass(clas: String): T

  def show: T = withClass("show")

  def form_check_label: T = withClass("form-check-label")
}
