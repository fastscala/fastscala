package com.fastscala.templates.utils

import scala.xml.{Elem, UnprefixedAttribute}

object ThemeClasses {

  implicit class RichElem(elem: Elem) {

    def addClass(`class`: String) = new Elem(
      elem.prefix,
      elem.label,
      elem.attributes.append(new UnprefixedAttribute("class", Option(elem.attributes.apply("class")).map(_.toString() + " ").getOrElse("") + `class`, scala.xml.Null)),
      elem.scope,
      elem.minimizeEmpty,
      elem.child: _*
    )

    def container_fluid: Elem = addClass("container-fluid")

    def row: Elem = addClass("row")

    def col_md_1: Elem = addClass("col-md-1")

    def col_md_2: Elem = addClass("col-md-2")

    def col_md_3: Elem = addClass("col-md-3")

    def col_md_4: Elem = addClass("col-md-4")

    def col_md_5: Elem = addClass("col-md-5")

    def col_md_6: Elem = addClass("col-md-6")

    def col_md_7: Elem = addClass("col-md-7")

    def col_md_8: Elem = addClass("col-md-8")

    def col_md_9: Elem = addClass("col-md-9")

    def col_md_10: Elem = addClass("col-md-10")

    def col_md_11: Elem = addClass("col-md-11")

    def col_md_12: Elem = addClass("col-md-12")

    def col_sm_1: Elem = addClass("col-sm-1")

    def col_sm_2: Elem = addClass("col-sm-2")

    def col_sm_3: Elem = addClass("col-sm-3")

    def col_sm_4: Elem = addClass("col-sm-4")

    def col_sm_5: Elem = addClass("col-sm-5")

    def col_sm_6: Elem = addClass("col-sm-6")

    def col_sm_7: Elem = addClass("col-sm-7")

    def col_sm_8: Elem = addClass("col-sm-8")

    def col_sm_9: Elem = addClass("col-sm-9")

    def col_sm_10: Elem = addClass("col-sm-10")

    def col_sm_11: Elem = addClass("col-sm-11")

    def col_sm_12: Elem = addClass("col-sm-12")
  }

  def main(args: Array[String]): Unit = {
    println(<div class="here"></div>.col_sm_12)
    println(<div/>.col_sm_12)
  }
}
