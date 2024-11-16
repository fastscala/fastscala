//  Copyright (c) 2014 David Miguel Antunes <davidmiguel {at} antunes.net>
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.

package com.fastscala.db.util

import scala.xml.*


object Utils {

  var enableProfiling = true

  implicit def __print[T](v: T): Object {def #!(s: String, t: T => String): T} = new {
    def #!(s: String = "", t: T => String = _.toString): T = { println(s + t(v)); v }
  }


  def time[T](f: => T)(r: Long => Unit): T = {
    val start = System.currentTimeMillis()
    val v = f
    r(System.currentTimeMillis() - start)
    v
  }

  def printNs = (ns: NodeSeq) => {println(ns); ns}
  def printNs(s: String) = (ns: NodeSeq) => {println(s + ":\n" + ns); ns}

  var idx = 0
  var lastOpen = false
  var enabled = true

  /**
   * Profile
   */
  def %?[T](s: String)(b: => T): T = %?[T](s, null)(b)

  def %??[T1, T2](s: String)(f: T1 => T2): T1 => T2 = (v: T1) => %?(s)(f(v))

  def disableProfiling[T](b: => T): T = {
    enabled = false
    val (ret, ex) = try {
      val ret = b
      (Some(ret), None)
    } catch {
      case t: Throwable => (None, Some(t))
    }
    enabled = true
    (ret, ex) match {
      case (Some(ret), _) => ret
      case (_, Some(t)) => throw t
      case _ => ???
    }
  }

  def %?[T](s: String, rslt: T => String)(b: => T): T = {
    if (enabled) {
      val space = (0 until idx).map(_ => "  ").mkString

      if (lastOpen) println()
      print(space + s"Starting '$s'...")

      lastOpen = true
      idx = idx + 2

      val start = System.currentTimeMillis()
      val (ret, ex) = try {
        val ret = b
        (Some(ret), None)
      } catch {
        case t: Throwable => (None, Some(t))
      }
      val took = System.currentTimeMillis() - start

      idx = idx - 2

      val exception = ex match {
        case Some(t) => {
          s" !! {Exception: '${t.getMessage}'} @ " + Thread.currentThread().getStackTrace.mkString("\n", "\n", "\n")
        }
        case None => ""
      }

      val rsltStr = ret.flatMap(ret => Option(rslt).map(_(ret))).map(" [" + _ + "]").getOrElse("")
      println(
        if (lastOpen) s" [${took}ms]$exception$rsltStr" else s"${space}Finished '$s' [${took}ms]$exception$rsltStr"
      )
      ex.foreach(_.printStackTrace())
      lastOpen = false

      (ret, ex) match {
        case (Some(ret), _) => ret
        case (_, Some(t)) => throw t
        case _ => ???
      }
    } else {
      b
    }

  }
}
