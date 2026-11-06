package com.fastscala.scala_xml.js

import com.fastscala.js.Js
import org.owasp.encoder.Encode
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.*

class JsXmlUtilsSpec extends AnyFlatSpec {

  private def js(code: String): Elem = <script type="text/javascript">{code}</script>

  private def module(code: String): Elem = <script type="module">{code}</script>

  /** How the DOM methods reference an element id (escaped for a JS string literal). */
  private def byId(id: String): String = s"""document.getElementById("${Encode.forJavaScript(id)}")"""

  // ---------- extractJsInScriptTags ----------

  "extractJsInScriptTags" should "extract a top-level script" in {
    assert(JS.extractJsInScriptTags(js("a()")).cmd == "a()")
  }

  it should "extract nested text/javascript and module scripts, in document order, joined by ';'" in {
    val ns = <div><p>a</p>{js("one()")}<span>{module("two()")}</span></div>
    assert(JS.extractJsInScriptTags(ns).cmd == "one();two()")
  }

  it should "extract scripts from a NodeSeq with several top-level nodes" in {
    val ns: NodeSeq = <p>x</p> ++ js("one()") ++ <div>{js("two()")}</div>
    assert(JS.extractJsInScriptTags(ns).cmd == "one();two()")
  }

  it should "extract scripts from a Group" in {
    assert(JS.extractJsInScriptTags(Group(Seq(js("g()"), <b>t</b>))).cmd == "g()")
  }

  it should "ignore scripts without type or with a non-JS type" in {
    val ns = <div><script type="text/template">tpl</script><script>noType()</script></div>
    assert(JS.extractJsInScriptTags(ns) == Js.Void)
  }

  it should "skip empty scripts" in {
    val ns = <div>{js("")}{js("a()")}</div>
    assert(JS.extractJsInScriptTags(ns).cmd == "a()")
  }

  it should "return Js.Void when there are no scripts" in {
    assert(JS.extractJsInScriptTags(<div><p>no scripts</p></div>) == Js.Void)
    assert(JS.extractJsInScriptTags(Text("hi")) == Js.Void)
    assert(JS.extractJsInScriptTags(NodeSeq.Empty) == Js.Void)
  }

  it should "strip the CDATA markers added by inScriptTag" in {
    val ns = <div>{JS.inScriptTag(Js("a()"))}{JS.inScriptTagModule(Js("b()"))}</div>
    assert(JS.extractJsInScriptTags(ns).cmd == "\na()\n;\nb()\n")
  }

  // ---------- excludeJsInScriptTags ----------

  "excludeJsInScriptTags" should "remove a top-level script" in {
    assert(JS.excludeJsInScriptTags(js("a()")).isEmpty)
  }

  it should "remove nested scripts, keeping everything else" in {
    val ns = <div id="d" class="c"><p>a</p>{js("one()")}<span>{module("two()")}</span></div>
    assert(JS.excludeJsInScriptTags(ns).toString == """<div id="d" class="c"><p>a</p><span></span></div>""")
  }

  it should "remove scripts from a NodeSeq with several top-level nodes" in {
    val ns: NodeSeq = <p>x</p> ++ js("one()") ++ <div>{js("two()")}</div>
    assert(JS.excludeJsInScriptTags(ns).toString == "<p>x</p><div></div>")
  }

  it should "remove scripts from a Group" in {
    assert(JS.excludeJsInScriptTags(Group(Seq(js("g()"), <b>t</b>))).toString == "<b>t</b>")
  }

  it should "keep scripts without type or with a non-JS type" in {
    val ns = <div><script type="text/template">tpl</script><script>noType()</script></div>
    assert(JS.excludeJsInScriptTags(ns).toString == ns.toString)
  }

  it should "keep text, comments and empty input unchanged" in {
    assert(JS.excludeJsInScriptTags(Text("hi")).toString == "hi")
    assert(JS.excludeJsInScriptTags(Comment("c")).toString == "<!--c-->")
    assert(JS.excludeJsInScriptTags(NodeSeq.Empty).isEmpty)
  }

  it should "remove exactly the scripts that extractJsInScriptTags extracts" in {
    val ns: NodeSeq = <p>x</p> ++ <div>{js("a()")}<script>kept()</script></div>
    val remaining = JS.excludeJsInScriptTags(ns)
    assert(JS.extractJsInScriptTags(ns).cmd == "a()")
    assert(JS.extractJsInScriptTags(remaining) == Js.Void)
    assert(remaining.toString == "<p>x</p><div><script>kept()</script></div>")
  }

  it should "return a single node as that node, and a Group as a Group" in {
    assert(JS.excludeJsInScriptTags(<div>{js("a()")}</div>).isInstanceOf[Elem])
    assert(JS.excludeJsInScriptTags(Group(Seq(<b>t</b>))).isInstanceOf[Group])
  }

  // ---------- *WithScriptExtraction ----------

  "append2BodyWithScriptExtraction" should "append the HTML without the scripts, then run the scripts" in {
    val cmd = JS.append2BodyWithScriptExtraction(<p>x</p> ++ js("run()")).cmd
    assert(cmd.contains(Encode.forJavaScript("<p>x</p>")))
    assert(!cmd.contains("script"))
    assert(cmd.endsWith(";run()"))
  }

  "replaceWithScriptExtraction" should "replace with the HTML without the scripts, then run the scripts" in {
    val cmd = JS.replaceWithScriptExtraction("target", <div>{js("run()")}</div>).cmd
    assert(cmd.contains(byId("target")))
    assert(cmd.contains(Encode.forJavaScript("<div></div>")))
    assert(!cmd.contains("script"))
    assert(cmd.endsWith(";run()"))
  }

  // ---------- DOM manipulation ----------

  "htmlToElement" should "create a template with the given id and the escaped HTML" in {
    val html = <p class="a">"x" &amp; 'y'</p>
    val cmd = JS.htmlToElement(html, "tpl-id").cmd
    assert(cmd.contains("""template.setAttribute("id", "tpl-id")"""))
    assert(cmd.contains("\"" + Encode.forJavaScript(html.toString) + "\""))
    assert(!cmd.contains("<p")) // no raw HTML inside the JS string
    assert(cmd.endsWith(".content"))
  }

  "append2Body" should "append to the body and remove the temporary template" in {
    val cmd = JS.append2Body(<p>x</p>).cmd
    assert(cmd.startsWith("document.body.appendChild("))
    val templateId = """template.setAttribute\("id", "([^"]+)"\)""".r.findFirstMatchIn(cmd).get.group(1)
    assert(cmd.endsWith(s"""document.getElementById("$templateId").remove();"""))
  }

  "append2" should "append to the element with the (escaped) id" in {
    val cmd = JS.append2("my\"id", <p>x</p>).cmd
    assert(cmd.startsWith(byId("my\"id") + ".appendChild("))
    assert(cmd.endsWith(".remove();"))
  }

  "prepend2" should "insert before the first child of the element" in {
    val cmd = JS.prepend2("list", <li>x</li>).cmd
    assert(cmd.startsWith(byId("list") + ".insertBefore("))
    assert(cmd.contains(byId("list") + ".firstChild)"))
    assert(cmd.endsWith(".remove();"))
  }

  "replace" should "replace the element, or log an error if it doesn't exist" in {
    val cmd = JS.replace("target", <p>x</p>).cmd
    assert(cmd.startsWith(s"(${byId("target")} ? ${byId("target")}.replaceWith("))
    assert(cmd.contains(s"""console.error("Element with id ${Encode.forJavaScript("target")} not found")"""))
  }

  "setContents" should "set the innerHTML to the escaped HTML" in {
    val html = <p>"x"</p>
    assert(JS.setContents("target", html).cmd == s"""${byId("target")}.innerHTML = "${Encode.forJavaScript(html.toString)}"; """)
  }

  it should "set the innerHTML to the result of a Js expression" in {
    assert(JS.setContents("target", Js("html")).cmd == s"""${byId("target")}.innerHTML = html; """)
  }

  // ---------- script tags ----------

  "inScriptTag" should "wrap the code in a text/javascript script tag with CDATA markers" in {
    val elem = JS.inScriptTag(Js("a()"))
    assert(elem.label == "script")
    assert(elem.attribute("type").map(_.text).contains("text/javascript"))
    assert(elem.toString == "<script type=\"text/javascript\">\n// <![CDATA[\na()\n// ]]>\n</script>")
  }

  "inScriptTagModule" should "wrap the code in a module script tag" in {
    val elem = JS.inScriptTagModule(Js("a()"))
    assert(elem.attribute("type").map(_.text).contains("module"))
    assert(elem.text.contains("a()"))
  }

  "the Js.inScriptTag / inScriptTagModule extensions" should "be the same as the JS methods" in {
    assert(Js("a()").inScriptTag.toString == JS.inScriptTag(Js("a()")).toString)
    assert(Js("a()").inScriptTagModule.toString == JS.inScriptTagModule(Js("a()")).toString)
  }

  "forceHttps" should "be a script that switches to https (except on localhost)" in {
    val elem = JS.forceHttps
    assert(elem.label == "script")
    assert(elem.text.contains("location.protocol = 'https:'"))
    assert(elem.text.contains("location.hostname !== 'localhost'"))
    assert(JS.extractJsInScriptTags(elem).cmd.contains("location.protocol = 'https:'"))
  }

  // ---------- showIf ----------

  "showIf" should "render the contents only when the condition holds" in {
    assert(JS.showIf(true)(<p>x</p>).toString == "<p>x</p>")
    assert(JS.showIf(false)(<p>x</p>).isEmpty)
  }

  it should "not evaluate the contents when the condition is false" in {
    var evaluated = false
    JS.showIf(false) {
      evaluated = true
      <p>x</p>
    }
    assert(!evaluated)
  }
}
