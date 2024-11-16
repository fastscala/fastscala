package com.fastscala.templates.form7

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.form7.mixins.QuerySerializableStringF7Field
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import org.eclipse.jetty.server.Request

import java.net.URLEncoder
import scala.jdk.CollectionConverters.MapHasAsScala

trait QueryStringSavedForm extends Form7 {

  override def initForm()(implicit fsc: FSContext): Unit = {
    super.initForm()
    rootField.fieldAndChildreenMatchingPredicate(_ => true).foreach({
      case f: QuerySerializableStringF7Field => Option(Request.getParameters(fsc.page.req).getValue(f.queryStringParamName)).foreach(str => {
        f.loadFromString(str)
      })
      case _ =>
    })
  }

  override def postSubmitForm()(implicit fsc: FSContext): Js = {
    super.postSubmitForm() & {
      val newParams: Map[String, Array[String]] = rootField.fieldAndChildreenMatchingPredicate(_ => true).collect({
        case f: QuerySerializableStringF7Field => f.queryStringParamName -> f.saveToString().toArray
      }).toMap
      val existingParams: Map[String, Array[String]] = (Request.getParameters(fsc.page.req).toStringArrayMap.asScala -- newParams.keys).toMap
      JS.redirectTo(fsc.page.req.getHttpURI.getPath + "?" + (existingParams ++ newParams).flatMap({
        case (key, values) => values.map(v => URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")).toList
      }).mkString("&"))
    }
  }
}
