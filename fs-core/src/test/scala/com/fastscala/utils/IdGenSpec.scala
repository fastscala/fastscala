package com.fastscala.utils

import org.scalatest.flatspec.AnyFlatSpec

import java.util.Base64

class IdGenSpec extends AnyFlatSpec {

  "IdGen.id" should "be 'id' and hex digits, different each time (usable as an HTML id)" in {
    val ids = Seq.fill(1000)(IdGen.id)
    assert(ids.forall(_.matches("id[0-9a-f]{1,16}")), ids.find(!_.matches("id[0-9a-f]{1,16}")))
    assert(ids.distinct.size == ids.size)
  }

  "IdGen.id(suffix)" should "end with -suffix" in {
    val id = IdGen.id("modal")
    assert(id.matches("id[0-9a-f]{1,16}-modal"), id)
  }

  "IdGen.secureId" should "be URL-safe Base64 of 32 random bytes by default" in {
    val ids = Seq.fill(200)(IdGen.secureId())
    assert(ids.distinct.size == ids.size)
    ids.foreach { id =>
      assert(!id.contains("/") && !id.contains("+"), id)
      assert(Base64.getUrlDecoder.decode(id).length == 32, id)
    }
  }

  it should "have as many random bytes as asked for" in {
    // Regression (fixed): nBytes is ignored: always 32 bytes.
    assert(Base64.getUrlDecoder.decode(IdGen.secureId(nBytes = 16)).length == 16)
    assert(Base64.getUrlDecoder.decode(IdGen.secureId(nBytes = 64)).length == 64)
  }
}
