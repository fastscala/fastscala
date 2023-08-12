package com.fastscala.templates.utils

import com.fastscala.core.FSContext
import com.fastscala.js.Js

import scala.xml.{Elem, Node, NodeSeq}

case class Button(
                   classes: String = "btn",
                   styles: String = "",
                   onclick: Option[Js] = None,
                   href: Option[String] = None,
                   target: Option[String] = None,
                   id: Option[String] = None,
                   contentsNs: NodeSeq = NodeSeq.Empty
                 ) {

  def lg = copy(classes = classes + " btn-lg")
  def md = copy(classes = classes + " btn-md")
  def sm = copy(classes = classes + " btn-sm")
  def xs = copy(classes = classes + " btn-xs")

  def fullcolor = copy(classes = classes + " btn-fullcolor")
  def fullwhite = copy(classes = classes + " btn-fullwhite")
  def fullblack = copy(classes = classes + " btn-fullblack")

  def text(s: String) = copy(contentsNs = scala.xml.Text(s))
  def contents(ns: Node) = copy(contentsNs = ns)

  def onclick(onclick: Js) = copy(onclick = Some(onclick))
  def ajax(callback: () => Js)(implicit fsc: FSContext) = copy(onclick = Some(fsc.callback(() => callback())))

  def id(_id: String): Button = this.copy(`id` = Some(_id))

  def renderButton: Elem =
    <button class={classes}
            style={styles}
            onclick={onclick.map(_.cmd).getOrElse(null)}
            href={href.getOrElse(null)}
            target={target.getOrElse(null)}
            id={id.getOrElse(null)}
    >{contentsNs}</button>
}

