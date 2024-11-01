package com.fastscala.core

trait FSXmlEnv {
  type NodeSeq
  type Elem
}

trait FSXmlSupport[Env <: FSXmlEnv] {

  def render(elem: Env#NodeSeq): String

  def buildElemFrom[E <: FSXmlEnv : FSXmlSupport](other: E#Elem): Env#Elem

  def buildNodeSeqFrom[E <: FSXmlEnv : FSXmlSupport](other: E#NodeSeq): Env#NodeSeq

  def Empty: Env#NodeSeq

  def buildUnparsed(unparsed: String): Env#NodeSeq

  def buildText(text: String): Env#NodeSeq

  def buildElem(label: String, attrs: (String, String)*)(children: Env#NodeSeq*): Env#Elem

  def label(elem: Env#Elem): String

  def contents(elem: Env#Elem): Env#NodeSeq

  def attribute(elem: Env#Elem, attrName: String): Option[String]

  def attributes(elem: Env#Elem): List[(String, String)]

  def transformAttribute(elem: Env#Elem, attrName: String, transform: Option[String] => String): Env#Elem

  def transformContents[E <: FSXmlEnv : FSXmlSupport](elem: Env#Elem, transform: Env#NodeSeq => E#NodeSeq): Env#Elem

  def concat(ns1: Env#NodeSeq, ns2: Env#NodeSeq): Env#NodeSeq

  def elem2NodeSeq(elem: Env#Elem): Env#NodeSeq
}
