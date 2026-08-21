package com.fastscala.components.form7.fields.file

import java.nio.file.{Files, Path}


case class F7UploadedFile(name: String, contentType: String, bytes: Array[Byte]) {

  def this(name: String, bytes: Array[Byte]) = this(name, Files.probeContentType(Path.of(name)), bytes)
}