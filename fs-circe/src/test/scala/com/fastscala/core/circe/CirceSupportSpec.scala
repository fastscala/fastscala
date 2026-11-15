package com.fastscala.core.circe

import com.fastscala.core.*
import com.fastscala.core.circe.CirceSupport.*
import com.fastscala.js.Js
import io.circe.{Decoder, Json}
import io.circe.generic.semiauto.deriveDecoder
import org.scalatest.flatspec.AnyFlatSpec

final case class Point(x: Int, y: Int)

object Point {
  given Decoder[Point] = deriveDecoder
}

/** callbackJSON / callbackJSONDecoded: callbacks whose argument is sent as JSON, and parsed (decoded) on the server. */
class CirceSupportSpec extends AnyFlatSpec {

  import CirceSupportSpec.*

  private def newPage(): FSPage = new FSPage(session.nextID(), session, null, NoImpl)

  /** The callback registered last in the page: called with `arg`, as the client would. */
  private def call(page: FSPage, arg: String): Js = page.callbacks.values.maxBy(_.id.toLong).func(arg)

  "callbackJSON" should "send the argument JSON.stringify-ed" in {
    val page = newPage()
    val js = page.rootFSContext.callbackJSON(Js("{a: 1}"), _ => Js.Void).cmd
    assert(js.startsWith("window._fs.callback(JSON.stringify({a: 1}),"), js)
  }

  it should "send '' when there's no argument" in {
    val page = newPage()
    val js = page.rootFSContext.callbackJSON(Js.Void, _ => Js.Void).cmd
    assert(js.startsWith("window._fs.callback('',"), js)
  }

  it should "give the function the parsed JSON" in {
    val page = newPage()
    var got: Json = null
    page.rootFSContext.callbackJSON(Js("x"), json => { got = json; Js("ok()") })
    assert(call(page, """{"a":[1,2],"b":"é"}""").cmd == "ok()")
    assert(got == Json.obj("a" -> Json.arr(Json.fromInt(1), Json.fromInt(2)), "b" -> Json.fromString("é")))
  }

  it should "throw when what's sent isn't JSON" in {
    val page = newPage()
    page.rootFSContext.callbackJSON(Js("x"), _ => Js.Void)
    val ex = intercept[Exception](call(page, "{not json"))
    assert(ex.getMessage.startsWith("Failed to parse JSON"), ex.getMessage)
  }

  it should "pass the flags on to the callback" in {
    val page = newPage()
    val js = page.rootFSContext.callbackJSON(Js("x"), _ => Js.Void, async = false, expectReturn = false, ignoreErrors = true).cmd
    assert(js.endsWith(",true,false,false,{});"), js)
  }

  "callbackJSONDecoded" should "give the function the decoded value" in {
    val page = newPage()
    var got: Point = null
    val js = page.rootFSContext.callbackJSONDecoded[Point](Js("p"), p => { got = p; Js.Void }).cmd
    assert(js.startsWith("window._fs.callback(JSON.stringify(p),"), js)
    call(page, """{"x":1,"y":-2}""")
    assert(got == Point(1, -2))
  }

  it should "throw, with what was sent, when it can't be decoded" in {
    val page = newPage()
    page.rootFSContext.callbackJSONDecoded[Point](Js("p"), _ => Js.Void)
    val ex = intercept[Exception](call(page, """{"x":"one"}"""))
    assert(ex.getMessage.contains("""{"x":"one"}"""), ex.getMessage)
  }
}

object CirceSupportSpec {
  // FSStats registers its metrics globally: a single FSSystem per JVM.
  lazy val system: FSSystem = new FSSystemImpl(appName = "circe-support-spec")
  lazy val session: FSSession = new FSSession("circe-support-spec-session", system)

  object NoImpl extends FSPageImpl[Unit] {
    override def render()(implicit fsc: FSContext): Unit = ()
  }
}
