package com.fastscala.components.bootstrap5.utils

import com.fastscala.components.bootstrap5.helpers.BSHelpers.s
import com.fastscala.core.circe.CirceSupport.FSContextWithCirceSupport
import com.fastscala.core.{FSContext, FSUploadedFile}
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS

import java.io.ByteArrayInputStream
import java.util.Base64
import scala.xml.Elem

object FileUpload {

  def pasteFromClipboard(processUpload: Seq[FSUploadedFile] => Js)(implicit fsc: FSContext): Elem = {
    val callback = fsc.callbackJSON(JS("[fileName, fileType, base64String]"), json => {
      json.arrayOrObject(
        JS.void,
        {
          case Vector(fileName, fileType, contentsEncoded) =>
            val bytes = Base64.getDecoder().decode(contentsEncoded.asString.get)
            processUpload(
              Seq(new FSUploadedFile(
                fileName.asString.get,
                fileType.asString.get,
                () => bytes,
                () => new ByteArrayInputStream(bytes)
              ))
            )
        },
        obj => JS.void
      )
    })
    JS.inScriptTag(JS(
      s"""document.addEventListener('paste', function (evt) {
         |  Array.from(evt.clipboardData.items).forEach(item => {
         |    const blob = item.getAsFile();
         |    const fileName = blob.name;
         |    const fileType = blob.type;
         |    const reader = new FileReader();
         |    reader.onloadend = () => {
         |    	const base64String = reader.result
         |      	.replace('data:', '')
         |      	.replace(/^.+,/, '');
         |      $callback
         |    };
         |    reader.readAsDataURL(blob);
         |  });
         |});
         |""".stripMargin
    ).onDOMContentLoaded)
  }
}
