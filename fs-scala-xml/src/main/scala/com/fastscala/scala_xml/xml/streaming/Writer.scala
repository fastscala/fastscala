package com.fastscala.scala_xml.xml.streaming

trait Writer {

  def appendScratch(f: StringBuilder => Unit): Unit
  
  def append(c: Char): Unit

  def append2(c1: Char, c2: Char): Unit

  def append(s: String): Unit
  
  def escape(text: String): Unit
}
