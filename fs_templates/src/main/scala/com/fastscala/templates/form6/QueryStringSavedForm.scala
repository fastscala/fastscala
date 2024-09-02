package com.fastscala.templates.form6

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form6.fields.QuerySerializableStringF6Field
import org.eclipse.jetty.server.Request
import scala.jdk.CollectionConverters.MapHasAsScala

import java.net.URLEncoder

trait QueryStringSavedForm extends Form6 {

  override def initForm()(implicit fsc: FSContext): Unit = {
    super.initForm()
    rootField.fieldsMatching(_ => true).foreach({
      case f: QuerySerializableStringF6Field => Option(Request.getParameters(fsc.page.req).getValue(f.queryStringParamName)).foreach(str => {
        f.loadFromString(str)
      })
      case _ =>
    })
  }

  override def postSave()(implicit fsc: FSContext): Js = {
    super.postSave() & {
      val newParams: Map[String, Array[String]] = rootField.fieldsMatching(_ => true).collect({
        case f: QuerySerializableStringF6Field => f.queryStringParamName -> f.saveToString().toArray
      }).toMap
      val existingParams: Map[String, Array[String]] = (Request.getParameters(fsc.page.req).toStringArrayMap.asScala -- newParams.keys).toMap
      Js.redirectTo(fsc.page.req.getHttpURI.getPath + "?" + (existingParams ++ newParams).flatMap({
        case (key, values) => values.map(v => URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")).toList
      }).mkString("&"))
    }
  }
}
