package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.fields.file.F7UploadedFile
import com.fastscala.components.form7.mixins.mainelem.F7FieldWithMainElem
import com.fastscala.components.form7.{F7Field, F7FieldMixinStatus, Form7, RenderHint}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithFileUploadSettings extends F7Field with Mutable {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  private val _previewRenderer: F7FieldMixinStatus[Seq[F7UploadedFile] => FSContext => Elem] = F7FieldMixinStatus(_ => _ => <div></div>.withStyle(";display:none;"))
  private val _submitBtnTransforms: F7FieldMixinStatus[Elem => Elem] = F7FieldMixinStatus((_: Elem).apply("Upload").btn.btn_success.mt_2.w_100)
  private val _progressElemTransforms: F7FieldMixinStatus[Elem => Elem] = F7FieldMixinStatus((_: Elem).withStyle("height: 20px;").mt_2)
  private val _progressBarElemTransforms: F7FieldMixinStatus[Elem => Elem] = F7FieldMixinStatus(identity[Elem])
  private val _multiple: F7FieldMixinStatus[Boolean] = F7FieldMixinStatus(false)
  private val _clipboardUploadEnabled: F7FieldMixinStatus[Boolean] = F7FieldMixinStatus(false)
  private val _acceptTypes: F7FieldMixinStatus[Option[String]] = F7FieldMixinStatus(None)
  private val _uploadTimeoutMillis: F7FieldMixinStatus[Long] = F7FieldMixinStatus(600000L)

  def previewRenderer: Seq[F7UploadedFile] => FSContext => Elem = _previewRenderer()

  def submitBtnTransforms: Elem => Elem = _submitBtnTransforms()

  def progressElemTransforms: Elem => Elem = _progressElemTransforms()

  def progressBarElemTransforms: Elem => Elem = _progressBarElemTransforms()

  def multiple: Boolean = _multiple()

  def clipboardUploadEnabled: Boolean = _clipboardUploadEnabled()

  def acceptTypes: Option[String] = _acceptTypes()

  def uploadTimeoutMillis: Long = _uploadTimeoutMillis()

  def previewRenderer(v: Seq[F7UploadedFile] => FSContext => Elem): this.type = mutate {
    _previewRenderer() = () => v
  }

  def submitBtnTransforms(v: Elem => Elem): this.type = mutate {
    _submitBtnTransforms() = () => v
  }

  def progressElemTransforms(v: Elem => Elem): this.type = mutate {
    _progressElemTransforms() = () => v
  }

  def progressBarElemTransforms(v: Elem => Elem): this.type = mutate {
    _progressBarElemTransforms() = () => v
  }

  def multiple(v: Boolean): this.type = mutate {
    _multiple() = () => v
  }

  def clipboardUploadEnabled(v: Boolean): this.type = mutate {
    _clipboardUploadEnabled() = () => v
  }

  def acceptTypes(v: Option[String]): this.type = mutate {
    _acceptTypes() = () => v
  }

  def uploadTimeoutMillis(v: Long): this.type = mutate {
    _uploadTimeoutMillis() = () => v
  }
}
