package com.fastscala.components.chartjs

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

                            /** Border radius when hovered. */
                            , hoverBorderRadius: Option[Double] = None

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

                            /** If true, null or undefined values will not be used for spacing calculations when determining bar size. */
                            , skipNull: Option[Boolean] = None

                            /** The ID of the group to which this dataset belongs to (when stacked, each group will be a separate stack). */
                            , stack: Option[String] = None

                            /** The ID of the x axis to plot this dataset on. */
                            , xAxisID: Option[String] = None

                            /** The ID of the y axis to plot this dataset on. */
                            , yAxisID: Option[String] = None
                          ) extends ChartDataset {

  def encode(): Json = {
    import io.circe.*
    import io.circe.generic.semiauto.*
    import io.circe.syntax.*
    implicit val ChartDatasetDataEncoder: Encoder[ChartDatasetData] = Encoder.instance[ChartDatasetData](_.encode())
    implicit val BarChartDatasetEncoder: Encoder[BarChartDataset] = deriveEncoder[BarChartDataset]
    this.asJson
  }
}

/** A dataset of a doughnut or of a pie chart (the two are the same chart: a pie has no cutout). */
case class DoughnutChartDataset(
                                 label: Option[String] = None
                                 , data: Option[ChartDatasetData] = None

                                 /** The colors of the arcs, one for each value. */
                                 , backgroundColor: Option[List[String]] = None

                                 /** 'center' (borders overlap) or 'inner' (the border is inside the arc). */
                                 , borderAlign: Option[String] = None
                                 , borderColor: Option[List[String]] = None
                                 , borderDash: Option[List[Double]] = None
                                 , borderDashOffset: Option[Double] = None

                                 /** 'round', 'bevel' or 'miter'. */
                                 , borderJoinStyle: Option[String] = None

                                 /** Border radius, number for all corners, object to specify the radius of each corner (outerStart, outerEnd, innerStart, innerEnd) */
                                 , borderRadius: Option[Json] = None
                                 , borderWidth: Option[Double] = None

                                 /** Per-dataset override for the sweep that the arcs cover (in degrees). */
                                 , circumference: Option[Double] = None

                                 /** How to clip relative to chartArea. Positive value allows overflow, negative value clips that many pixels inside chartArea. 0 = clip at chartArea. */
                                 , clip: Option[Double] = None

                                 /** Configures the visibility state of the dataset. Set it to true, to hide the dataset from the chart. */
                                 , hidden: Option[Boolean] = None
                                 , hoverBackgroundColor: Option[List[String]] = None
                                 , hoverBorderColor: Option[List[String]] = None
                                 , hoverBorderDash: Option[List[Double]] = None
                                 , hoverBorderDashOffset: Option[Double] = None
                                 , hoverBorderJoinStyle: Option[String] = None
                                 , hoverBorderWidth: Option[Double] = None

                                 /** Arc offset when hovered (in pixels). */
                                 , hoverOffset: Option[Double] = None

                                 /** Arc offset (in pixels). */
                                 , offset: Option[Double] = None

                                 /** Per-dataset override for the starting angle to draw arcs from (in degrees). */
                                 , rotation: Option[Double] = None

                                 /** Fixed arc offset (in pixels). Similar to offset but applies to all arcs. */
                                 , spacing: Option[Double] = None

                                 /** How the spacing between the arcs is made: 'angular' (default), 'proportional' or 'parallel'. (Chart.js 4.6+) */
                                 , spacingMode: Option[String] = None

                                 /** The relative thickness of the dataset: with more than one dataset, how much of the chart area each one takes. */
                                 , weight: Option[Double] = None
                               ) extends ChartDataset {

  def encode(): Json = {
    import io.circe.*
    import io.circe.generic.semiauto.*
    import io.circe.syntax.*
    implicit val ChartDatasetDataEncoder: Encoder[ChartDatasetData] = Encoder.instance[ChartDatasetData](_.encode())
    implicit val DoughnutChartDatasetEncoder: Encoder[DoughnutChartDataset] = deriveEncoder[DoughnutChartDataset]
    this.asJson
  }
}

/** A dataset of a line chart or of a scatter chart (a scatter chart is a line chart with showLine = false, and with its data as points: [[PointsChartDatasetData]]). */
case class LineChartDataset(
                            label: Option[String] = None
                            , data: Option[ChartDatasetData] = None

                            /** The line fill color. */
                            , backgroundColor: Option[String] = None

                            /** Cap style of the line: 'butt', 'round' or 'square'. */
                            , borderCapStyle: Option[String] = None

                            /** The line color. */
                            , borderColor: Option[String] = None

                            /** Length and spacing of dashes. */
                            , borderDash: Option[List[Double]] = None
                            , borderDashOffset: Option[Double] = None

                            /** 'round', 'bevel' or 'miter'. */
                            , borderJoinStyle: Option[String] = None

                            /** The line width (in pixels). */
                            , borderWidth: Option[Double] = None

                            /** How to clip relative to chartArea. Positive value allows overflow, negative value clips that many pixels inside chartArea. 0 = clip at chartArea. */
                            , clip: Option[Double] = None

                            /** 'default' (a custom weighted cubic interpolation) or 'monotone' (preserves monotonicity: better for y = f(x) datasets). */
                            , cubicInterpolationMode: Option[String] = None

                            /** Draw the active points of a dataset over the other points of the dataset. */
                            , drawActiveElementsOnTop: Option[Boolean] = None

                            /** How to fill the area under the line: false, true (= 'origin'), 'origin', 'start', 'end', the index of another dataset (absolute: 1, or relative: '-1', '+1'), or an object ({target: ..., above: ..., below: ...}). */
                            , fill: Option[Json] = None

                            /** Configures the visibility state of the dataset. Set it to true, to hide the dataset from the chart. */
                            , hidden: Option[Boolean] = None
                            , hoverBackgroundColor: Option[String] = None
                            , hoverBorderCapStyle: Option[String] = None
                            , hoverBorderColor: Option[String] = None
                            , hoverBorderDash: Option[List[Double]] = None
                            , hoverBorderDashOffset: Option[Double] = None
                            , hoverBorderJoinStyle: Option[String] = None
                            , hoverBorderWidth: Option[Double] = None

                            /** The base axis of the dataset. 'x' for horizontal lines and 'y' for vertical lines. */
                            , indexAxis: Option[String] = None

                            /** The drawing order of dataset. Also affects order for stacking, tooltip and legend. */
                            , order: Option[Int] = None

                            /** The fill color for points. */
                            , pointBackgroundColor: Option[String] = None

                            /** The border color for points. */
                            , pointBorderColor: Option[String] = None

                            /** The width of the point border in pixels. */
                            , pointBorderWidth: Option[Double] = None

                            /** The pixel size of the non-displayed point that reacts to mouse events. */
                            , pointHitRadius: Option[Double] = None
                            , pointHoverBackgroundColor: Option[String] = None
                            , pointHoverBorderColor: Option[String] = None
                            , pointHoverBorderWidth: Option[Double] = None
                            , pointHoverRadius: Option[Double] = None

                            /** The radius of the point shape. If set to 0, the point is not rendered. */
                            , pointRadius: Option[Double] = None

                            /** The rotation of the point in degrees. */
                            , pointRotation: Option[Double] = None

                            /** Style of the point: 'circle', 'cross', 'crossRot', 'dash', 'line', 'rect', 'rectRounded', 'rectRot', 'star', 'triangle' or false (no point). */
                            , pointStyle: Option[Json] = None

                            /** If false, the line is not drawn for this dataset. */
                            , showLine: Option[Boolean] = None

                            /** If true, lines will be drawn between points with no or null data. If false, points with null data will create a break in the line. Can also be a number specifying the maximum gap length to span. */
                            , spanGaps: Option[Json] = None

                            /** The ID of the group to which this dataset belongs to (when stacked, each group will be a separate stack). */
                            , stack: Option[String] = None

                            /** If the line is shown as a stepped line: false, true (= 'before'), 'before', 'after' or 'middle'. (The tension is ignored, when stepped.) */
                            , stepped: Option[Json] = None

                            /** Bezier curve tension of the line. Set to 0 to draw straight lines. */
                            , tension: Option[Double] = None

                            /** The ID of the x axis to plot this dataset on. */
                            , xAxisID: Option[String] = None

                            /** The ID of the y axis to plot this dataset on. */
                            , yAxisID: Option[String] = None
                          ) extends ChartDataset {

  def encode(): Json = {
    import io.circe.*
    import io.circe.generic.semiauto.*
    import io.circe.syntax.*
    implicit val ChartDatasetDataEncoder: Encoder[ChartDatasetData] = Encoder.instance[ChartDatasetData](_.encode())
    implicit val LineChartDatasetEncoder: Encoder[LineChartDataset] = deriveEncoder[LineChartDataset]
    this.asJson
  }
}

/** A dataset of a radar chart. */
case class RadarChartDataset(
                             label: Option[String] = None
                             , data: Option[ChartDatasetData] = None

                             /** The line fill color. */
                             , backgroundColor: Option[String] = None

                             /** Cap style of the line: 'butt', 'round' or 'square'. */
                             , borderCapStyle: Option[String] = None

                             /** The line color. */
                             , borderColor: Option[String] = None

                             /** Length and spacing of dashes. */
                             , borderDash: Option[List[Double]] = None
                             , borderDashOffset: Option[Double] = None

                             /** 'round', 'bevel' or 'miter'. */
                             , borderJoinStyle: Option[String] = None

                             /** The line width (in pixels). */
                             , borderWidth: Option[Double] = None

                             /** How to clip relative to chartArea. Positive value allows overflow, negative value clips that many pixels inside chartArea. 0 = clip at chartArea. */
                             , clip: Option[Double] = None

                             /** How to fill the area under the line: false, true (= 'origin'), 'origin', 'start', 'end', the index of another dataset (absolute: 1, or relative: '-1', '+1'), or an object ({target: ..., above: ..., below: ...}). */
                             , fill: Option[Json] = None

                             /** Configures the visibility state of the dataset. Set it to true, to hide the dataset from the chart. */
                             , hidden: Option[Boolean] = None
                             , hoverBackgroundColor: Option[String] = None
                             , hoverBorderCapStyle: Option[String] = None
                             , hoverBorderColor: Option[String] = None
                             , hoverBorderDash: Option[List[Double]] = None
                             , hoverBorderDashOffset: Option[Double] = None
                             , hoverBorderJoinStyle: Option[String] = None
                             , hoverBorderWidth: Option[Double] = None

                             /** The drawing order of dataset. Also affects order for stacking, tooltip and legend. */
                             , order: Option[Int] = None

                             /** The fill color for points. */
                             , pointBackgroundColor: Option[String] = None

                             /** The border color for points. */
                             , pointBorderColor: Option[String] = None

                             /** The width of the point border in pixels. */
                             , pointBorderWidth: Option[Double] = None

                             /** The pixel size of the non-displayed point that reacts to mouse events. */
                             , pointHitRadius: Option[Double] = None
                             , pointHoverBackgroundColor: Option[String] = None
                             , pointHoverBorderColor: Option[String] = None
                             , pointHoverBorderWidth: Option[Double] = None
                             , pointHoverRadius: Option[Double] = None

                             /** The radius of the point shape. If set to 0, the point is not rendered. */
                             , pointRadius: Option[Double] = None

                             /** The rotation of the point in degrees. */
                             , pointRotation: Option[Double] = None

                             /** Style of the point: 'circle', 'cross', 'crossRot', 'dash', 'line', 'rect', 'rectRounded', 'rectRot', 'star', 'triangle' or false (no point). */
                             , pointStyle: Option[Json] = None

                             /** If true, lines will be drawn between points with no or null data. If false, points with null data will create a break in the line. Can also be a number specifying the maximum gap length to span. */
                             , spanGaps: Option[Json] = None

                             /** Bezier curve tension of the line. Set to 0 to draw straight lines. */
                             , tension: Option[Double] = None
                           ) extends ChartDataset {

  def encode(): Json = {
    import io.circe.*
    import io.circe.generic.semiauto.*
    import io.circe.syntax.*
    implicit val ChartDatasetDataEncoder: Encoder[ChartDatasetData] = Encoder.instance[ChartDatasetData](_.encode())
    implicit val RadarChartDatasetEncoder: Encoder[RadarChartDataset] = deriveEncoder[RadarChartDataset]
    this.asJson
  }
}

/** A dataset of a bubble chart: its data are bubbles ([[BubblesChartDatasetData]]). */
case class BubbleChartDataset(
                              label: Option[String] = None
                              , data: Option[ChartDatasetData] = None
                              , backgroundColor: Option[String] = None
                              , borderColor: Option[String] = None
                              , borderWidth: Option[Double] = None

                              /** How to clip relative to chartArea. Positive value allows overflow, negative value clips that many pixels inside chartArea. 0 = clip at chartArea. */
                              , clip: Option[Double] = None

                              /** Draw the active bubbles of a dataset over the other bubbles of the dataset. */
                              , drawActiveElementsOnTop: Option[Boolean] = None

                              /** Configures the visibility state of the dataset. Set it to true, to hide the dataset from the chart. */
                              , hidden: Option[Boolean] = None
                              , hoverBackgroundColor: Option[String] = None
                              , hoverBorderColor: Option[String] = None
                              , hoverBorderWidth: Option[Double] = None

                              /** Additional radius when hovered (in pixels). */
                              , hoverRadius: Option[Double] = None

                              /** Additional radius for hit detection (in pixels). */
                              , hitRadius: Option[Double] = None

                              /** The drawing order of dataset. Also affects order for stacking, tooltip and legend. */
                              , order: Option[Int] = None

                              /** Style of the bubble: 'circle', 'cross', 'crossRot', 'dash', 'line', 'rect', 'rectRounded', 'rectRot', 'star' or 'triangle'. */
                              , pointStyle: Option[String] = None

                              /** Bubble rotation (in degrees). */
                              , rotation: Option[Double] = None

                              /** Bubble radius (in pixels), for the bubbles whose data has no radius (r). */
                              , radius: Option[Double] = None
                            ) extends ChartDataset {

  def encode(): Json = {
    import io.circe.*
    import io.circe.generic.semiauto.*
    import io.circe.syntax.*
    implicit val ChartDatasetDataEncoder: Encoder[ChartDatasetData] = Encoder.instance[ChartDatasetData](_.encode())
    implicit val BubbleChartDatasetEncoder: Encoder[BubbleChartDataset] = deriveEncoder[BubbleChartDataset]
    this.asJson
  }
}

/** A dataset of a polar area chart. */
case class PolarAreaChartDataset(
                                 label: Option[String] = None
                                 , data: Option[ChartDatasetData] = None

                                 /** The colors of the arcs, one for each value. */
                                 , backgroundColor: Option[List[String]] = None

                                 /** 'center' (borders overlap) or 'inner' (the border is inside the arc). */
                                 , borderAlign: Option[String] = None
                                 , borderColor: Option[List[String]] = None
                                 , borderDash: Option[List[Double]] = None
                                 , borderDashOffset: Option[Double] = None

                                 /** 'round', 'bevel' or 'miter'. */
                                 , borderJoinStyle: Option[String] = None
                                 , borderWidth: Option[Double] = None

                                 /** By default the arcs are curved. If false, the arcs are flat. */
                                 , circular: Option[Boolean] = None

                                 /** How to clip relative to chartArea. Positive value allows overflow, negative value clips that many pixels inside chartArea. 0 = clip at chartArea. */
                                 , clip: Option[Double] = None

                                 /** Configures the visibility state of the dataset. Set it to true, to hide the dataset from the chart. */
                                 , hidden: Option[Boolean] = None
                                 , hoverBackgroundColor: Option[List[String]] = None
                                 , hoverBorderColor: Option[List[String]] = None
                                 , hoverBorderDash: Option[List[Double]] = None
                                 , hoverBorderDashOffset: Option[Double] = None
                                 , hoverBorderJoinStyle: Option[String] = None
                                 , hoverBorderWidth: Option[Double] = None

                                 /** Fixed arc offset (in pixels). */
                                 , spacing: Option[Double] = None

                                 /** How the spacing between the arcs is made: 'angular', 'proportional' or 'parallel'. (Chart.js 4.6+) */
                                 , spacingMode: Option[String] = None
                               ) extends ChartDataset {

  def encode(): Json = {
    import io.circe.*
    import io.circe.generic.semiauto.*
    import io.circe.syntax.*
    implicit val ChartDatasetDataEncoder: Encoder[ChartDatasetData] = Encoder.instance[ChartDatasetData](_.encode())
    implicit val PolarAreaChartDatasetEncoder: Encoder[PolarAreaChartDataset] = deriveEncoder[PolarAreaChartDataset]
    this.asJson
  }
}

trait ChartDatasetData {

  def encode(): Json
}

case class SimpleNumbersChartDatasetData(data: List[Double]) extends ChartDatasetData {
  override def encode(): Json = {
    import io.circe.generic.auto.*
    import io.circe.syntax.*
    data.asJson
  }
}

/** Numbers, some of them missing (None: null, for Chart.js): a gap in a line (see spanGaps), a bar that's not there. */
case class NullableNumbersChartDatasetData(data: List[Option[Double]]) extends ChartDatasetData {
  override def encode(): Json = Json.fromValues(data.map(_.fold(Json.Null)(Json.fromDoubleOrNull)))
}

case class ChartPoint(x: Double, y: Double)

/** Points ({x, y}): the data of a scatter chart (and possible for a line chart, with a linear x axis). */
case class PointsChartDatasetData(data: List[ChartPoint]) extends ChartDatasetData {
  override def encode(): Json = Json.fromValues(data.map(p => Json.obj("x" -> Json.fromDoubleOrNull(p.x), "y" -> Json.fromDoubleOrNull(p.y))))
}

/** r: the radius of the bubble in pixels (it's not scaled by the chart). */
case class ChartBubble(x: Double, y: Double, r: Double)

/** Bubbles ({x, y, r}): the data of a bubble chart. */
case class BubblesChartDatasetData(data: List[ChartBubble]) extends ChartDatasetData {
  override def encode(): Json = Json.fromValues(data.map(b => Json.obj("x" -> Json.fromDoubleOrNull(b.x), "y" -> Json.fromDoubleOrNull(b.y), "r" -> Json.fromDoubleOrNull(b.r))))
}
