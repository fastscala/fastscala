package com.fastscala.routing.resp

import java.nio.ByteBuffer
import java.nio.charset.Charset

class OkTextBased(text: String, val contentType: String) extends TextResponse {
  override def status: HttpStatus = HttpStatuses.OK

  override protected def encodedContents(charset: Charset): List[ByteBuffer] = List(ByteBuffer.wrap(text.getBytes(charset)))
}
