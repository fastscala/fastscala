package com.fastscala.components.chartjs

import com.fastscala.scala_xml.xml.XmlJsStream
import io.circe.Json
import io.circe.parser.parse
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import scala.xml.{Elem, NodeSeq}

class ChartJsSpec extends AnyFlatSpec {

  private val chart = ChartJs(
    `type` = BarChartType,
    data = Some(ChartData(
      labels = Some(List("Jan", "Feb")),
      datasets = Some(List(BarChartDataset(label = Some("Sales"), data = Some(SimpleNumbersChartDatasetData(List(1.5, 2))), backgroundColor = Some("#f00"))))
    )),
    options = Json.obj("responsive" -> Json.True)
  )

  /** The JSON given to `new Chart(ctx, ...)`. */
  private def config(installJs: String): Json = {
    val start = installJs.indexOf("new Chart(ctx, ") + "new Chart(ctx, ".length
    parse(installJs.substring(start, installJs.lastIndexOf(");"))).toTry.get
  }

  private def written(ns: NodeSeq): String = {
    val bytes = new ByteArrayOutputStream()
    XmlJsStream.writeSeq(ns, new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = { while (bb.hasRemaining) bytes.write(bb.get()); cb.succeeded() }
    })
    bytes.toString(UTF_8)
  }

  "installInCanvas" should "create the chart on the canvas, with its config as JSON (without nulls)" in {
    val js = chart.installInCanvas("c1").cmd
    assert(js.startsWith("const ctx = document.getElementById(\"c1\"); new Chart(ctx, "), js)
    val json = config(js)
    assert(json.hcursor.downField("type").as[String].contains("bar"))
    assert(json.hcursor.downField("data").downField("labels").as[List[String]].contains(List("Jan", "Feb")))
    val dataset = json.hcursor.downField("data").downField("datasets").downN(0)
    assert(dataset.downField("label").as[String].contains("Sales"))
    assert(dataset.downField("data").as[List[Double]].contains(List(1.5, 2.0)))
    assert(dataset.downField("backgroundColor").as[String].contains("#f00"))
    assert(dataset.downField("borderColor").focus.isEmpty, "a None is left out")
    assert(json.hcursor.downField("options").downField("responsive").as[Boolean].contains(true))
  }

  it should "leave out the data when there's none" in {
    assert(config(ChartJs(LineChartType).installInCanvas("c").cmd).hcursor.downField("data").focus.isEmpty)
  }

  "The chart types" should "have Chart.js's names" in {
    assert(Seq(BarChartType, BubbleChartType, DoughnutChartType, LineChartType, ScatterChartType, PolarAreaChartType, RadarChartType).map(_.name) ==
      Seq("bar", "bubble", "doughnut", "line", "scatter", "polarArea", "radar"))
  }

  "rendered / renderedOn" should "be the canvas (with an id) and the script creating the chart on it" in {
    val ns = chart.renderedOn(<canvas id="my-chart" class="w-100"></canvas>)
    val canvas = ns.head.asInstanceOf[Elem]
    assert(canvas.label == "canvas" && canvas.attribute("id").map(_.text).contains("my-chart"))
    assert(ns(1).label == "script")
    assert(ns.toString.contains("document.getElementById(\"my\\x2dchart\")"), ns) // (the id escaped, as a JavaScript string)
    val generated = chart.rendered().head.asInstanceOf[Elem]
    assert(generated.attribute("id").exists(_.text.startsWith("id")))
  }

  it should "keep labels with </script> inside the script" in {
    // Regression (fixed): the config's JSON is put in the <script> as it is (Unparsed): a label with "</script>" ends the script, and
    // what follows it is HTML (an injection, with labels from users).
    val evil = ChartJs(BarChartType, data = Some(ChartData(labels = Some(List("</script><img src=x onerror=alert(1)>")))))
    val html = written(evil.renderedOn(<canvas id="c"></canvas>))
    assert(!html.contains("</script><img"), html)
    // ... and the label is still the same, for Chart.js: "\u003c" is "<", in JSON as in JavaScript.
    assert(config(evil.installInCanvas("c").cmd).hcursor.downField("data").downField("labels").downN(0).as[String].contains("</script><img src=x onerror=alert(1)>"))
  }
}
