package com.fastscala.chartjs

import com.fastscala.core.{FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.utils.IdGen
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem
import io.circe.generic.semiauto
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}

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

  import ChartJsEncoders._
  def installInCanvas(canvasId: String): Js = {
    import io.circe.generic.auto._
    import io.circe.syntax._

    Js(
      s"""const ctx = document.getElementById('$canvasId'); new Chart(ctx, ${this.asJson.deepDropNullValues.noSpaces}); """
    )
  }
}

object ChartJs {

  import com.fastscala.core.FSXmlUtils._

  def rendered[E <: FSXmlEnv : FSXmlSupport](chart: ChartJs): E#NodeSeq = renderedOn(chart, <canvas></canvas>.asFSXml())

  def renderedOn[E <: FSXmlEnv : FSXmlSupport](chart: ChartJs, elem: E#Elem): E#NodeSeq = {
    val id = elem.getId.getOrElse(IdGen.id("chart_js_canvas"))
    val finalElem = elem.withId(id)
    finalElem ++ chart.installInCanvas(id).onDOMContentLoaded.inScriptTag
  }
}
