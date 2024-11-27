package com.fastscala.components.chartjs

class ChartType(val name: String)

object BarChartType extends ChartType("bar")

object BubbleChartType extends ChartType("bubble")

object DoughnutChartType extends ChartType("doughnut")

object LineChartType extends ChartType("line")

object ScatterChartType extends ChartType("scatter")

object PolarAreaChartType extends ChartType("polarArea")

object RadarChartType extends ChartType("radar")
