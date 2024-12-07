package com.fastscala.components.chartjs

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import io.circe.generic.semiauto
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}

import scala.xml.{Elem, NodeSeq}

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

  import ChartJsEncoders.*

  def installInCanvas(canvasId: String): Js = {
    import io.circe.generic.auto.*
    import io.circe.syntax.*

    JS(
      s"""const ctx = document.getElementById('$canvasId'); new Chart(ctx, ${this.asJson.deepDropNullValues.noSpaces}); """
    )
  }

  def rendered(): NodeSeq = renderedOn(<canvas></canvas>)

  def renderedOn(elem: Elem): NodeSeq = {
    val id = elem.getId.getOrElse(IdGen.id("chart_js_canvas"))
    val finalElem = elem.withId(id)
    finalElem ++ JS.inScriptTag(installInCanvas(id).onDOMContentLoaded)
  }
}
