package com.fastscala.scala_xml.xml

import com.fastscala.scala_xml.testutils.XmlStreamTestUtils
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.*

/** [[VectorArrays]]: a small Vector's array, read in place; and XmlJsStream writing Vectors of every size. */
class VectorArraysSpec extends AnyFlatSpec {

  "arrayOfVector1" should "be the elements of a Vector of 1 to 32 elements, in order" in {
    for (n <- 1 to 32) {
      val v = Vector.tabulate(n)(i => s"e$i")
      val arr = VectorArrays.arrayOfVector1(v)
      assert(arr != null, s"$n elements: ${v.getClass}")
      assert(arr.toSeq == v, n)
    }
  }

  it should "be null for anything else (an empty or bigger Vector, other seqs)" in {
    for (seq <- Seq[AnyRef](Vector(), Vector.tabulate(33)(identity), Vector.tabulate(5000)(identity), List(1, 2), Array(1).toSeq, "x"))
      assert(VectorArrays.arrayOfVector1(seq) == null, seq.getClass)
  }

  it should "not change the Vector (it's only read)" in {
    val v = Vector("a", "b")
    VectorArrays.arrayOfVector1(v)
    assert(v == Vector("a", "b"))
  }

  "XmlJsStream" should "write children in Vectors of every size as toString does" in {
    for (n <- (0 to 70) ++ Seq(1000, 1025, 40_000)) {
      val e = new Elem(null, "ul", Null, TopScope, false, Vector.tabulate(n)(i => <li>{s"item $i <&>"}</li>) *)
      val written = XmlStreamTestUtils.writtenNode(e)
      assert(written == e.toString, n)
    }
  }
}
