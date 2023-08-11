package com.fastscala.chartjs

import com.fastscala.js.Js
import com.fastscala.utils.IdGen
import io.circe.{Encoder, Json}
import io.circe.generic.semiauto
import io.circe.syntax.EncoderOps

import scala.xml.NodeSeq

object ChartJsNullable2Option {
  implicit def nullable2Option[T <: AnyRef](v: T): Option[T] = if (v == null) None else Some(v)
}

object ChartJsEncoders {

  implicit val ChartTypeEncoder: Encoder[ChartType] = Encoder.instance[ChartType](_.name.asJson)
  implicit val ChartDatasetDataEncoder: Encoder[ChartDatasetData] = Encoder.instance[ChartDatasetData](_.encode())
  implicit val ChartDatasetEncoder: Encoder[ChartDataset] = Encoder.instance[ChartDataset](_.encode())
  implicit val ChartDataEncoder: Encoder[ChartData] = semiauto.deriveEncoder[ChartData]
  implicit val ChartJsEncoder: Encoder[ChartJs] = semiauto.deriveEncoder[ChartJs]
}

case class ChartJs(
                    `type`: ChartType
                    , data: Option[ChartData] = None
                    , options: Json = Json.Null
                  ) {

  def installInCanvas(canvasId: String): Js = {
    import ChartJsEncoders._
    import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

    Js(
      s"""const ctx = document.getElementById('$canvasId'); new Chart(ctx, ${this.asJson.deepDropNullValues.noSpaces}); """
    )
  }

  def rendered(): NodeSeq = {
    val id = IdGen.id("chart_js_canvas")

    <canvas id={id}></canvas> ++ installInCanvas(id).onDOMContentLoaded.inScriptTag
  }

}
