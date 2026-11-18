package com.fastscala.components.chartjs

import com.fastscala.js.Js
import com.fastscala.scala_xml.xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.{JS, printBeforeExec}
import com.fastscala.utils.IdGen
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

object ChartJs {

  /** The version of Chart.js that this package is written for (and that [[jsImports]] loads). */
  val version = "4.6.1"

  /** Without the fields that are null (the Nones), in all objects. The nulls in arrays stay: a null in a dataset's data is a gap, and
   * without it the values after it are in the wrong place. */
  def dropNullFields(json: Json): Json = json.arrayOrObject(
    json,
    values => Json.fromValues(values.map(dropNullFields)),
    obj => Json.fromJsonObject(obj.filter(!_._2.isNull).mapValues(dropNullFields))
  )

  /** The script that loads Chart.js (for the page's head, or anywhere before the charts). */
  def jsImports: NodeSeq =
    <script src={s"https://cdn.jsdelivr.net/npm/chart.js@$version/dist/chart.umd.min.js"} integrity="sha256-hzZMwrKnamaEkp59a4Cxiol3lNs4QAyv8fzxeRxpJdI=" crossorigin="anonymous"></script>
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

    Js(
      // The JSON goes in a <script> as it is: its '<'s (only ever in its strings) are escaped, so a "</script>" in a
      // label can't end the script. (The same JSON for JavaScript: "\u003c" is "<".)
      s"""const ctx = document.getElementById(${JS.asJsStr(canvasId).cmd}); new Chart(ctx, ${ChartJs.dropNullFields(this.asJson).noSpaces.replace("<", "\\u003c")}); """
    )
  }

  def rendered(): NodeSeq = renderedOn(<canvas></canvas>)

  def renderedOn(elem: Elem): NodeSeq = {
    val id = elem.getIdOpt.getOrElse(IdGen.id("chart_js_canvas"))
    val finalElem = elem.withId(id)
    finalElem ++ JS.inScriptTag(installInCanvas(id).onDOMContentLoaded)
  }
}
