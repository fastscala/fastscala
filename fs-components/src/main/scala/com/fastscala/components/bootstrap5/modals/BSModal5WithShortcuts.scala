package com.fastscala.components.bootstrap5.modals

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS

trait BSModal5WithShortcuts extends BSModal5Base {

  def shortcuts(implicit fsc: FSContext): List[(List[String], Js)] = Nil

  override def eventListenerOnShown()(implicit fsc: FSContext): Js = super.eventListenerOnShown() & shortcuts.map({
    case (combos, func) =>
      Js(s"""Mousetrap(document.body).bind(${combos.map(JS.asJsStr).mkString("[", ",", "]")}, function(evt) { if (e.preventDefault) { e.preventDefault(); } else { e.returnValue = false; }; $func });""")
  }).reduceOption(_ & _).getOrElse(Js.Void)

  override def eventListenerOnHide()(implicit fsc: FSContext): Js = super.eventListenerOnHide() & shortcuts.map({
    case (combos, func) => Js(s"""Mousetrap.unbind(${combos.map(JS.asJsStr).mkString("[", ",", "]")});""")
  }).reduceOption(_ & _).getOrElse(Js.Void)
}
