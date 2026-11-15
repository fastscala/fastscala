package com.fastscala.js

import com.fastscala.js.JsHelper.RichJs
import org.scalatest.flatspec.AnyFlatSpec

class JsSpec extends AnyFlatSpec {

  "Js(String)" should "be Js.Void for an empty String, a RawJs otherwise" in {
    assert(Js("") eq Js.Void)
    assert(Js("a()").isInstanceOf[RawJs])
    assert(Js("a()").cmd == "a()")
  }

  "Js(Js*)" should "be Js.Void with no Js, a CompositeJs of them otherwise" in {
    assert(Js() eq Js.Void)
    val c = Js(Js("a"), Js("b"))
    assert(c.isInstanceOf[CompositeJs] && c.cmd == "ab")
  }

  "&" should "join with ';', and skip Js.Void on either side" in {
    val a = Js("a()")
    val b = Js("b()")
    assert((a & b).cmd == "a();b()")
    assert((a & Js.Void) eq a)
    assert((Js.Void & b) eq b)
    assert((Js.Void & Js.Void) eq Js.Void)
    assert((a & b & a).cmd == "a();b();a()")
  }

  "isVoid" should "be whether the code is blank" in {
    assert(Js.Void.isVoid)
    assert(Js("  \n").isVoid)
    assert(!Js("x").isVoid)
    assert(Js(Js(" "), Js.Void).isVoid)
  }

  "toString" should "be the code" in {
    assert(Js(Js("a"), Js("b")).toString == "ab")
  }

  "onDOMContentLoaded" should "run the code once the DOM is loaded (or now, if it is)" in {
    val code = Js("init()").onDOMContentLoaded.cmd
    assert(code.startsWith("(function () {var load = function() {init();};"), code)
    assert(code.contains("document.addEventListener('DOMContentLoaded'"), code)
    assert(code.contains("/complete|interactive|loaded/.test(document.readyState)"), code)
  }

  "asVar" should "put the value in a variable, and give the variable to the code" in {
    var name: Js = null
    val code = Js("1 + 2").asVar { v => name = v; Js(s"alert(${v.cmd})") }.cmd
    assert(name.cmd.matches("v_[0-9A-F]+"), name.cmd)
    assert(code == s"var ${name.cmd} = (1 + 2); alert(${name.cmd})", code)
  }

  it should "use a different name each time" in {
    val names = Seq.fill(50)({ var n = ""; Js("x").asVar { v => n = v.cmd; Js.Void }; n })
    assert(names.distinct.size > 45)
  }
}
