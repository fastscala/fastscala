package com.fastscala.components.chartjs

import com.fastscala.scala_xml.xml.streaming.XmlJsStream
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
    assert(Seq(BarChartType, BubbleChartType, DoughnutChartType, PieChartType, LineChartType, ScatterChartType, PolarAreaChartType, RadarChartType).map(_.name) ==
      Seq("bar", "bubble", "doughnut", "pie", "line", "scatter", "polarArea", "radar"))
  }

  "jsImports" should "load the version of Chart.js that this package is written for" in {
    val script = ChartJs.jsImports.head.asInstanceOf[Elem]
    assert(script.label == "script")
    assert(script.attribute("src").map(_.text).contains("https://cdn.jsdelivr.net/npm/chart.js@4.6.1/dist/chart.umd.min.js"))
    assert(script.attribute("integrity").exists(_.text.startsWith("sha256-")))
  }

  "A bar dataset" should "have the options of Chart.js 4: hoverBorderRadius, skipNull" in {
    val json = BarChartDataset(hoverBorderRadius = Some(4), skipNull = Some(true)).encode().deepDropNullValues
    assert(json == Json.obj("hoverBorderRadius" -> Json.fromDoubleOrNull(4), "skipNull" -> Json.True))
  }

  "A doughnut (or pie) dataset" should "have a color for each arc, and the spacing options" in {
    val pie = ChartJs(PieChartType, data = Some(ChartData(
      labels = Some(List("A", "B")),
      datasets = Some(List(DoughnutChartDataset(data = Some(SimpleNumbersChartDatasetData(List(1, 3))), backgroundColor = Some(List("#f00", "#0f0")), spacing = Some(2), spacingMode = Some("parallel"))))
    )))
    val json = config(pie.installInCanvas("c").cmd)
    assert(json.hcursor.downField("type").as[String].contains("pie"))
    val dataset = json.hcursor.downField("data").downField("datasets").downN(0)
    assert(dataset.downField("data").as[List[Double]].contains(List(1.0, 3.0)))
    assert(dataset.downField("backgroundColor").as[List[String]].contains(List("#f00", "#0f0")))
    assert(dataset.downField("spacing").as[Double].contains(2.0))
    assert(dataset.downField("spacingMode").as[String].contains("parallel"))
    assert(dataset.downField("weight").focus.isEmpty, "a None is left out")
  }

  /** The first dataset of a chart with this dataset, as it's given to Chart.js. */
  private def dataset(tpe: ChartType, ds: ChartDataset) =
    config(ChartJs(tpe, data = Some(ChartData(datasets = Some(List(ds))))).installInCanvas("c").cmd).hcursor.downField("data").downField("datasets").downN(0)

  "A line dataset" should "have the line and point options, and gaps (nulls) in its data" in {
    val ds = dataset(LineChartType, LineChartDataset(
      label = Some("Temp"), data = Some(NullableNumbersChartDatasetData(List(Some(1), None, Some(3)))), borderColor = Some("#00f"), borderDash = Some(List(4, 2)),
      fill = Some(Json.fromString("origin")), spanGaps = Some(Json.True), stepped = Some(Json.fromString("middle")), tension = Some(0.4), pointRadius = Some(0)
    ))
    assert(ds.downField("data").as[List[Option[Double]]].contains(List(Some(1.0), None, Some(3.0))))
    assert(ds.downField("borderColor").as[String].contains("#00f"))
    assert(ds.downField("borderDash").as[List[Double]].contains(List(4.0, 2.0)))
    assert(ds.downField("fill").as[String].contains("origin"))
    assert(ds.downField("spanGaps").as[Boolean].contains(true))
    assert(ds.downField("stepped").as[String].contains("middle"))
    assert(ds.downField("tension").as[Double].contains(0.4))
    assert(ds.downField("pointRadius").as[Double].contains(0.0))
    assert(ds.downField("showLine").focus.isEmpty, "a None is left out")
  }

  "A scatter dataset" should "be a line dataset with points ({x, y}) as its data" in {
    val ds = dataset(ScatterChartType, LineChartDataset(data = Some(PointsChartDatasetData(List(ChartPoint(-10, 0), ChartPoint(0.5, 5.5)))), pointBackgroundColor = Some("#f00")))
    assert(ds.downField("data").focus.contains(parse("""[{"x":-10.0,"y":0.0},{"x":0.5,"y":5.5}]""").toTry.get))
    assert(ds.downField("pointBackgroundColor").as[String].contains("#f00"))
  }

  "A bubble dataset" should "have bubbles ({x, y, r}) as its data" in {
    val ds = dataset(BubbleChartType, BubbleChartDataset(data = Some(BubblesChartDatasetData(List(ChartBubble(20, 30, 15)))), backgroundColor = Some("#f00"), hoverRadius = Some(2)))
    assert(ds.downField("data").focus.contains(parse("""[{"x":20.0,"y":30.0,"r":15.0}]""").toTry.get))
    assert(ds.downField("backgroundColor").as[String].contains("#f00"))
    assert(ds.downField("hoverRadius").as[Double].contains(2.0))
  }

  "A radar dataset" should "have the line and point options" in {
    val ds = dataset(RadarChartType, RadarChartDataset(data = Some(SimpleNumbersChartDatasetData(List(1, 2, 3))), fill = Some(Json.True), pointStyle = Some(Json.fromString("rect")), tension = Some(0.1)))
    assert(ds.downField("data").as[List[Double]].contains(List(1.0, 2.0, 3.0)))
    assert(ds.downField("fill").as[Boolean].contains(true))
    assert(ds.downField("pointStyle").as[String].contains("rect"))
    assert(ds.downField("tension").as[Double].contains(0.1))
  }

  "A polar area dataset" should "have a color for each arc" in {
    val ds = dataset(PolarAreaChartType, PolarAreaChartDataset(data = Some(SimpleNumbersChartDatasetData(List(11, 16))), backgroundColor = Some(List("#f00", "#0f0")), circular = Some(false)))
    assert(ds.downField("data").as[List[Double]].contains(List(11.0, 16.0)))
    assert(ds.downField("backgroundColor").as[List[String]].contains(List("#f00", "#0f0")))
    assert(ds.downField("circular").as[Boolean].contains(false))
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
