package com.fastscala.templates.form5

import com.fastscala.core.{FSContext, FSXmlEnv}
import com.fastscala.js.Js
import com.fastscala.templates.form5.fields.QuerySerializableStringField

import java.net.URLEncoder
import scala.collection.JavaConverters.mapAsScalaMapConverter

trait QueryStringSavedForm[E <: FSXmlEnv] extends Form5[E] {

  override def initForm()(implicit fsc: FSContext): Unit = {
    super.initForm()
    rootField.fieldsMatching(_ => true).foreach({
      case f: QuerySerializableStringField[_] => Option(fsc.page.req.getParameter(f.queryStringParamName)).foreach(str => {
        f.loadFromString(str)
      })
      case _ =>
    })
  }

  override def afterSave()(implicit fsc: FSContext): Js = {
    super.afterSave() & {
      val newParams: Map[String, Array[String]] = rootField.fieldsMatching(_ => true).collect({
        case f: QuerySerializableStringField[_] => f.queryStringParamName -> f.saveToString().toArray
      }).toMap
      val existingParams: Map[String, Array[String]] = (fsc.page.req.getParameterMap.asScala -- newParams.keys).toMap
      Js.redirectTo(fsc.page.req.getRequestURI + "?" + (existingParams ++ newParams).flatMap({
        case (key, values) => values.map(v => URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")).toList
      }).mkString("&"))
    }
  }
}
