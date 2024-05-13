package com.fastscala.templates.bootstrap5.classes

import com.fastscala.utils.ElemTransformers

import scala.xml.{Elem, NodeSeq}

trait BSCombinedHelper[T] extends BSBaseHelper[T]
  with BSOtherHelper[T]

object BSHelpers extends BSCombinedHelper[Elem] {

  override def withClass(clas: String): Elem = div.addClass(clas)

  def del: Elem = <del></del>

  def s: Elem = <s></s>

  def ins: Elem = <ins></ins>

  def u: Elem = <u></u>

  def a: Elem = <a></a>

  def small: Elem = <small></small>

  def strong: Elem = <strong></strong>

  def em: Elem = <em></em>

  def mark: Elem = <mark></mark>

  def pre: Elem = <pre></pre>

  def td: Elem = <td></td>

  def tr: Elem = <tr></tr>

  def tbody: Elem = <tbody></tbody>

  def thead: Elem = <thead></thead>

  def img: Elem = <img/>

  def div: Elem = <div></div>

  def ul: Elem = <ul></ul>

  def li: Elem = <li></li>

  def style: Elem = <style></style>

  def input: Elem = <input/>

  def button: Elem = <button></button>

  def span: Elem = <span></span>

  def label: Elem = <label></label>

  def b: Elem = <b/>

  def p: Elem = <p/>

  def abbr: Elem = <abbr/>

  def h1: Elem = <h1/>

  def h2: Elem = <h2/>

  def h3: Elem = <h3/>

  def h4: Elem = <h4/>

  def h5: Elem = <h5/>

  def h6: Elem = <h6/>

  def hr: Elem = <hr/>

  implicit class ElemEnricher(val elem: Elem) extends ElemTransformers with BSCombinedHelper[Elem] {
    override def withClass(clas: String): Elem = elem.addClass(clas)
  }

  implicit class StrEnricher(str: String) extends BSCombinedHelper[String] {
    override def withClass(clas: String): String = str + " " + clas
  }

  def main(args: Array[String]): Unit = {
    println(div.form_check.getClassAttr)
  }
}
