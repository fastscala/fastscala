package com.fastscala.demo.docs.other

import com.fastscala.components.chartjs.*
import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.components.chartjs.ChartJsNullable2Option.nullable2Option

import scala.xml.NodeSeq

class SimpleChartjsPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Simple chart.js Example"

  override def append2Body(): NodeSeq = super.append2Body() ++
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Source") {
      <canvas id="chart"></canvas> ++ JS.inScriptTag(ChartJs(
        `type` = BarChartType,
        data = ChartData(
          datasets = List(
            BarChartDataset(
              label = "dataset 1",
              data = SimpleNumbersChartDatasetData(List(12, 19, 3, 5, 2, 3.0)),
              borderColor = "#91C8E4",
              backgroundColor = "#4682A9"
            )
          ),
          labels = List("Test", "Test", "Test", "Test")
        )
      ).installInCanvas("chart").onDOMContentLoaded)
    }
    closeSnippet()
  }
}
