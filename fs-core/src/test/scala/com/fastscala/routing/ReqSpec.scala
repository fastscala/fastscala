package com.fastscala.routing

import com.fastscala.routing.method.*
import com.fastscala.routing.req.*
import com.fastscala.testutils.TestRequests.request
import org.scalatest.flatspec.AnyFlatSpec

/** The request extractors: `Get(...)`, `Post(...)`, ... (by method, with the path's segments), and `Req(...)`. */
class ReqSpec extends AnyFlatSpec {

  "Get/Post/..." should "match their method only, with the path's non-empty segments" in {
    request("GET", "/a/b/c") match {
      case Get("a", "b", "c") =>
      case other => fail(s"not matched: $other")
    }
    assert(Get.unapplySeq(request("GET", "/a//b/")).contains(List("a", "b")))
    assert(Get.unapplySeq(request("GET", "/")).contains(Nil))
    assert(Get.unapplySeq(request("POST", "/a")).isEmpty)
    assert(Post.unapplySeq(request("POST", "/a")).contains(List("a")))
    for ((extractor, method) <- Seq(Head -> "HEAD", Put -> "PUT", Delete -> "DELETE", Connect -> "CONNECT", Options -> "OPTIONS", Trace -> "TRACE", Patch -> "PATCH")) {
      assert(extractor.unapplySeq(request(method, "/x/y")).contains(List("x", "y")), method)
      assert(extractor.unapplySeq(request("GET", "/x/y")).isEmpty, method)
    }
  }

  it should "match the query string out (only the path is split)" in {
    assert(Get.unapplySeq(request("GET", "/a/b?x=1&y=/z")).contains(List("a", "b")))
  }

  "Method.fromString" should "know every HTTP method, and nothing else" in {
    val all = Seq("GET" -> GET, "HEAD" -> HEAD, "POST" -> POST, "PUT" -> PUT, "DELETE" -> DELETE, "CONNECT" -> CONNECT, "OPTIONS" -> OPTIONS, "TRACE" -> TRACE, "PATCH" -> PATCH)
    for ((name, method) <- all) assert(Method.fromString(name) eq method)
    assert(!Method.fromString.isDefinedAt("get"))
    assert(!Method.fromString.isDefinedAt("FOO"))
  }

  "Req" should "be the method, the segments without the extension, the extension (lower case), and whether it ends with /" in {
    assert(Req.unapply(request("GET", "/static/img/logo.png")).contains((GET, List("static", "img", "logo"), "png", false)))
    assert(Req.unapply(request("POST", "/api/v1/items.json")).contains((POST, List("api", "v1", "items"), "json", false)))
    assert(Req.unapply(request("FOO", "/a.png")).isEmpty)
  }

  it should "tell when the path ends with /" in {
    assert(Req.unapply(request("GET", "/dir/index.html/")).exists(_._4))
  }

  it should "have no extension for a path without one" in {
    // Regression (fixed): without a ".ext" at the end, the regex doesn't match, and `ext` is the whole path (lower case).
    assert(Req.unapply(request("GET", "/users/Profile")).contains((GET, List("users", "Profile"), "", false)))
  }

  it should "not throw for a path with regex chars and no extension" in {
    // Regression (fixed): `ext` (the whole path, see above) is put in a regex unquoted: "(" makes it throw PatternSyntaxException.
    assert(Req.unapply(request("GET", "/a(b")).isDefined)
  }

  it should "take an upper case extension off the last segment" in {
    // Regression (fixed): `ext` is lower-cased before it's taken off the path, so ".PNG" stays: List("img", "logo.PNG").
    assert(Req.unapply(request("GET", "/img/logo.PNG")).contains((GET, List("img", "logo"), "png", false)))
  }
}
