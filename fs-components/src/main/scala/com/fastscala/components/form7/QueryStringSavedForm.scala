package com.fastscala.components.form7

import com.fastscala.components.form5.fields.StringSerializableField
import com.fastscala.components.form7.mixins.{F7FieldWithId, StringSerializableF7Field}
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.{JS, printBeforeExec}
import org.eclipse.jetty.server.Request

import java.net.URLEncoder
import scala.jdk.CollectionConverters.MapHasAsScala

trait QueryStringSavedForm extends Form7 {

  type F7FieldWithIdAndStringSerializableF7Field = F7FieldWithId & StringSerializableF7Field

  override def initForm()(implicit fsc: FSContext): Unit = {
    super.initForm()
    rootField.fieldAndChildreenMatchingPredicate(_ => true).foreach({
      case f: F7FieldWithIdAndStringSerializableF7Field if f.id.isDefined =>
        Option(Request.getParameters(fsc.page.req).getValue(f.id.get)).foreach(str => {
          f.loadFromString(str)
        })
      case _ =>
    })
  }

  override def postSubmitForm()(implicit fsc: FSContext): Js = {
    super.postSubmitForm() & {
      val newParams: Map[String, Array[String]] = rootField.fieldAndChildreenMatchingPredicate(_ => true).collect({
        case f: F7FieldWithIdAndStringSerializableF7Field if f.id.isDefined => f.id.get -> f.saveToString().toArray
      }).toMap
      val existingParams: Map[String, Array[String]] = (Request.getParameters(fsc.page.req).toStringArrayMap.asScala -- newParams.keys).toMap
      val updatedUrl = fsc.page.req.getHttpURI.getPath + "?" + (existingParams ++ newParams).flatMap({ case (key, values) =>
        values.map(v => URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")).toList
      }).mkString("&").replaceAll("\\?$", "")
      Js(s"""if (history.pushState) {
            |   window.history.pushState({path:${JS.asJsStr(updatedUrl)}},'',${JS.asJsStr(updatedUrl)});
            |} else {
            |   ${JS.redirectTo(updatedUrl)}
            |}
            |""".stripMargin).printBeforeExec
    }
  }
}
