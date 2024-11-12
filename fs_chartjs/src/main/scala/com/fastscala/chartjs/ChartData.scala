package com.fastscala.chartjs

import io.circe.Json

case class ChartData(
                      datasets: Option[List[ChartDataset]] = None
                      , labels: Option[List[String]] = None
                    )
trait ChartDataset {
  def encode(): Json

  def label: Option[String]

  def data: Option[ChartDatasetData]
}

case class BarChartDataset(
                            label: Option[String] = None
                            , data: Option[ChartDatasetData] = None
                            , backgroundColor: Option[String] = None

                            /** Percent (0-1) of the available width each bar should be within the category width. 1.0 will take the whole category width and put the bars right next to each other. */
                            , barPercentage: Option[Double] = None

                            /** Manually set width of each bar in pixels. If set to 'flex', it computes "optimal" sample widths that globally arrange bars side by side. If not set (default), bars are equally sized based on the smallest interval. */
                            , barThickness: Option[Double] = None

                            /** The base value for the bar in data units along the value axis. */
                            , base: Option[Double] = None
                            , borderColor: Option[String] = None

                            /** Border radius */
                            , borderRadius: Option[Double] = None

                            /** Skipped (excluded) border: 'start', 'end', 'left', 'right', 'bottom', 'top', 'middle', false (none) or true (all). */
                            , borderSkipped: Option[Json] = None

                            /** Width of the border, number for all sides, object to specify width for each side specifically */
                            , borderWidth: Option[Double] = None

                            /** Percent (0-1) of the available width each category should be within the sample width. */
                            , categoryPercentage: Option[Double] = None

                            /** How to clip relative to chartArea. Positive value allows overflow, negative value clips that many pixels inside chartArea. 0 = clip at chartArea. Clipping can also be configured per side: clip: {left: 5, top: false, right: -2, bottom: 0} */
                            , clip: Option[Double] = None

                            /** Should the bars be grouped on index axis */
                            , grouped: Option[Boolean] = None

                            /** Configures the visibility state of the dataset. Set it to true, to hide the dataset from the chart. */
                            , hidden: Option[Boolean] = None
                            , hoverBackgroundColor: Option[String] = None
                            , hoverBorderColor: Option[String] = None
                            , hoverBorderWidth: Option[Double] = None

                            /** The base axis of the chart. 'x' for vertical charts and 'y' for horizontal charts. */
                            , indexAxis: Option[String] = None

                            /** Amount to inflate the rectangle(s). This can be used to hide artifacts between bars. Unit is pixels. 'auto' translates to 0.33 pixels when barPercentage * categoryPercentage is 1, else 0. */
                            , inflateAmount: Option[Json] = None

                            /** Set this to ensure that bars are not sized thicker than this. */
                            , maxBarThickness: Option[Double] = None

                            /** Set this to ensure that bars have a minimum length in pixels. */
                            , minBarLength: Option[Double] = None

                            /** Chart.js is fastest if you provide data with indices that are unique, sorted, and consistent across datasets and provide the normalized: true option to let Chart.js know that you have done so. */
                            , normalized: Option[Boolean] = None

                            /** The drawing order of dataset. Also affects order for stacking, tooltip and legend. */
                            , order: Option[Int] = None

                            /** How to parse the dataset. The parsing can be disabled by specifying parsing: false at chart options or dataset. If parsing is disabled, data must be sorted and in the formats the associated chart type and scales use internally. */
                            , parsing: Option[Json] = None

                            /** Point style for the legend */
                            , pointStyle: Option[String] = None

                            /** The ID of the group to which this dataset belongs to (when stacked, each group will be a separate stack). */
                            , stack: Option[String] = None

                            /** The ID of the x axis to plot this dataset on. */
                            , xAxisID: Option[String] = None

                            /** The ID of the y axis to plot this dataset on. */
                            , yAxisID: Option[String] = None
                          ) extends ChartDataset {

  def encode(): Json = {
    import io.circe._, io.circe.generic.semiauto._, io.circe.syntax._
    implicit val ChartDatasetDataEncoder: Encoder[ChartDatasetData] = Encoder.instance[ChartDatasetData](_.encode())
    implicit val BarChartDatasetEncoder: Encoder[BarChartDataset] = deriveEncoder[BarChartDataset]
    this.asJson
  }
}

trait ChartDatasetData {

  def encode(): Json
}

case class SimpleNumbersChartDatasetData(data: List[Double]) extends ChartDatasetData {
  override def encode(): Json = {
    import io.circe.generic.auto._, io.circe.syntax._
    data.asJson
  }
}
