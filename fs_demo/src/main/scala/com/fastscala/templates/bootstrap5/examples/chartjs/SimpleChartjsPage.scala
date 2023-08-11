package com.fastscala.templates.bootstrap5.examples.chartjs

import com.fastscala.chartjs.{BarChartDataset, BarChartType, ChartData, ChartDataset, ChartJs, SimpleNumbersChartDatasetData}
import com.fastscala.code.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.examples.ExampleWithCodePage
import com.fastscala.templates.bootstrap5.modals.BSModal5Base
import com.fastscala.templates.bootstrap5.utils.BSBtn
import io.circe.syntax.EncoderOps

import scala.xml.NodeSeq

class SimpleChartjsPage extends ExampleWithCodePage("/com/fastscala/templates/bootstrap5/examples/chartjs/SimpleChartjsPage.scala") {

  override def pageTitle: String = "Simple chart.js Example"

  override def append2Body(): NodeSeq = super.append2Body() ++
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    // === code snippet ===
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    /*
    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['Red', 'Blue', 'Yellow', 'Green', 'Purple', 'Orange'],
        datasets: [{
          label: '# of Votes',
          data: [12, 19, 3, 5, 2, 3],
          borderWidth: 1
        }]
      },
      options: {
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    });
     */

    import com.fastscala.chartjs.ChartJsNullable2Option._
    <canvas id="chart"></canvas> ++ ChartJs(
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
    ).installInCanvas("chart").onDOMContentLoaded.inScriptTag
    // === code snippet ===
  }
}
