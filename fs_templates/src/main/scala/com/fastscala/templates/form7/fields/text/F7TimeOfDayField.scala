package com.fastscala.templates.form7.fields.text

import com.fastscala.templates.form7.F7Field
import com.fastscala.templates.form7.mixins._
import com.fastscala.templates.form7.renderers._
import com.fastscala.xml.scala_xml.FSScalaXmlSupport
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import java.util.regex.Pattern
import scala.util.chaining.scalaUtilChainingOps
import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq


class F7TimeOfDayField()(implicit renderer: TextF7FieldRenderer)
  extends F7TextField[Option[Int]]
    with F7FieldWithPrefix
    with F7FieldWithSuffix
    with F7FieldWithMin
    with F7FieldWithStep
    with F7FieldWithMax {

  override def defaultValue: Option[Int] = None

  override def validate(): Seq[(F7Field, NodeSeq)] = super.validate() ++
    (if (required() && currentValue.isEmpty) Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())


  def toString(value: Option[Int]): String = value.map(value => DateTimeFormat.forPattern("HH:mm").print(new DateTime().withTime(value / 60, value % 60, 0, 0))).map(_.trim).getOrElse("")

  def fromString(str: String): Either[String, Option[Int]] = {
    if (str.trim == "") {
      Right(None)
    } else {
      str
        .toLowerCase
        .trim
        .replaceAll("^" + Pattern.quote(prefix.toLowerCase) + " *", "")
        .replaceAll(" *" + Pattern.quote(suffix.toLowerCase) + "$", "")
        .pipe(txt => {
          Try(DateTimeFormat.forPattern("HH:mm").parseLocalTime(txt)) match {
            case Failure(exception) => Left(s"Not a time?: $txt")
            case Success(parsed) => Right(Some(parsed.getHourOfDay * 60 + parsed.getMinuteOfHour))
          }
        })
    }
  }
}