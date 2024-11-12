package com.fastscala.core

trait FSXmlEnv {
  type NodeSeq
  type Elem
}

object FSXmlEnv {
  type AuxNodeSeq[T] = FSXmlEnv {type NodeSeq = T}
  type AuxElem[T] = FSXmlEnv {type Elem = T}
}

type FSXmlNodeSeq[E <: FSXmlEnv] = E match {
  case FSXmlEnv.AuxNodeSeq[t] => t
}

type FSXmlElem[E <: FSXmlEnv] = E match {
  case FSXmlEnv.AuxElem[t] => t
}

trait FSXmlSupport[E <: FSXmlEnv] {

  def render(elem: FSXmlNodeSeq[E]): String

  def buildElemFrom[E2 <: FSXmlEnv](other: FSXmlElem[E2])(using env: FSXmlSupport[E2]): FSXmlElem[E]

  def buildNodeSeqFrom[E2 <: FSXmlEnv](other: FSXmlNodeSeq[E2])(using env: FSXmlSupport[E2]): FSXmlNodeSeq[E]

  def Empty: FSXmlNodeSeq[E]

  def buildUnparsed(unparsed: String): FSXmlNodeSeq[E]

  def buildText(text: String): FSXmlNodeSeq[E]

  def buildElem(label: String, attrs: (String, String)*)(children: FSXmlNodeSeq[E]*): FSXmlElem[E]

  def label(elem: FSXmlElem[E]): String

  def getId(elem: FSXmlElem[E]): Option[String]

  def contents(elem: FSXmlElem[E]): FSXmlNodeSeq[E]

  def attribute(elem: FSXmlElem[E], attrName: String): Option[String]

  def attributes(elem: FSXmlElem[E]): List[(String, String)]

  def transformAttribute(elem: FSXmlElem[E], attrName: String, transform: Option[String] => String): FSXmlElem[E]

  def transformContents[E2 <: FSXmlEnv](elem: FSXmlElem[E], transform: FSXmlNodeSeq[E] => FSXmlNodeSeq[E2])(using env: FSXmlSupport[E2]): FSXmlElem[E]

  def concat(ns1: FSXmlNodeSeq[E], ns2: FSXmlNodeSeq[E]): FSXmlNodeSeq[E]

  def elem2NodeSeq(elem: FSXmlElem[E]): FSXmlNodeSeq[E]
}
