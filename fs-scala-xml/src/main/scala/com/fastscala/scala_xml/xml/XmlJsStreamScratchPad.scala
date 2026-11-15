package com.fastscala.scala_xml.xml

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import scala.xml.Node

object XmlJsStreamScratchPad {

  /** Collects everything written into it. */
  private final class CollectingSink extends Content.Sink {
    val bytes = new ByteArrayOutputStream()

    override def write(last: Boolean, byteBuffer: ByteBuffer, callback: Callback): Unit = {
      while (byteBuffer.hasRemaining) bytes.write(byteBuffer.get())
      callback.succeeded()
    }

    def text: String = bytes.toString(UTF_8)
  }

  /** What XmlJsStream writes for `value` (a Js or a Node). */
  private def written(value: Js | Node): String = {
    val sink = new CollectingSink
    value match {
      case js: Js => XmlJsStream.writeJs(js, sink)
      case node: Node => XmlJsStream.writeNode(node, sink)
    }
    sink.text
  }

  private def show(name: String, value: Js | Node): Unit = println(s"$name:\n${written(value)}\n")

  def main(args: Array[String]): Unit = {
    val case1 = JS.alert("hello")
    val case2 = ElemOptimized("div", "onclick" -> case1)()
    val case3 = JS.replace("my-div", case2)
    val case4 = ElemOptimized("button", "onclick" -> case3)()
    val case5 = JS.append2Body(case4)

    show("case1", case1)
    show("case2", case2)
    show("case3", case3)
    show("case4", case4)
    show("case5", case5)
  }
}
