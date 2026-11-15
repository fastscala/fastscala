package com.fastscala.scala_xml.js

import com.fastscala.js.{Js, JsEscape}
import com.fastscala.scala_xml.xml.{ElemOptimized, XmlJsStream}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import scala.sys.process.*
import scala.xml.*

/**
 * [[JsXmlUtils.append2BodyWithScriptExtraction]] and [[JsXmlUtils.replaceWithScriptExtraction]] (a
 * [[JsWithScriptDetection]]): the JavaScript they are, what it does when it runs (in node, with a fake DOM: those tests
 * are canceled where there's no `node`), and how XmlJsStream writes it.
 */
class ScriptExtractionSpec extends AnyFlatSpec {

  private def script(code: String): Elem = <script type="text/javascript">{code}</script>

  private def byId(id: String): String = s"""document.getElementById("${JsEscape.escape(id)}")"""

  /** The id of the temporary template in `cmd` (as it's written there: escaped, e.g. its `-` as `\x2d`). */
  private def templateIdIn(cmd: String): String =
    """template\.setAttribute\("id", "([^"]+)"\)""".r.findFirstMatchIn(cmd).get.group(1)

  private def written(js: Js): String = {
    val bytes = new ByteArrayOutputStream()
    XmlJsStream.writeJs(js, new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
        while (bb.hasRemaining) bytes.write(bb.get())
        cb.succeeded()
      }
    })
    bytes.toString(UTF_8)
  }

  // ---------- the JavaScript they are ----------

  "append2BodyWithScriptExtraction" should "be append2Body of the HTML without its scripts, and then the scripts" in {
    val cmd = JS.append2BodyWithScriptExtraction(<p>x</p> ++ script("run()")).cmd
    val templateId = templateIdIn(cmd)
    val html = "\"" + JsEscape.escape("<p>x</p>") + "\""
    assert(cmd ==
      s"""document.body.appendChild((document.body.appendChild((function htmlToElement(html) {var template = document.createElement('template');template.setAttribute("id", "$templateId");template.innerHTML = html.trim(); return template;})($html))).content);""" +
        s"""document.getElementById("$templateId").remove();""" +
        ";run()")
  }

  it should "be just append2Body when there are no scripts (nothing after it)" in {
    val cmd = JS.append2BodyWithScriptExtraction(<p>x</p>).cmd
    assert(cmd.startsWith("document.body.appendChild("))
    assert(cmd.endsWith(s"""document.getElementById("${templateIdIn(cmd)}").remove();"""))
    // As append2Body's (but for the template's id).
    val plain = JS.append2Body(<p>x</p>).cmd
    assert(cmd.replace(templateIdIn(cmd), "ID") == plain.replace(templateIdIn(plain), "ID"))
  }

  "replaceWithScriptExtraction" should "be replace by the HTML without its scripts, the template's removal, and then the scripts" in {
    val cmd = JS.replaceWithScriptExtraction("my-target", <div class="a">{script("run()")}<b>t</b></div>).cmd
    val templateId = templateIdIn(cmd)
    val html = "\"" + JsEscape.escape("""<div class="a"><b>t</b></div>""") + "\""
    assert(cmd ==
      s"""(${byId("my-target")} ? ${byId("my-target")}.replaceWith(""" +
        s"""(document.body.appendChild((function htmlToElement(html) {var template = document.createElement('template');template.setAttribute("id", "$templateId");template.innerHTML = html.trim(); return template;})($html))).content""" +
        s""") : console.error("Element with id ${JsEscape.escape("my-target")} not found"));""" +
        s"""(function(template) {if (template) template.remove();})(document.getElementById("$templateId"));""" +
        ";run()")
  }

  it should "start as replace does (the same, up to the template's removal)" in {
    val html = <div><p title={"\"q\" & <x>"}>{"a 'b' é 😀"}</p></div>
    val cmd = JS.replaceWithScriptExtraction("t", html).cmd
    val plain = JS.replace("t", html).cmd
    assert(cmd.replace(templateIdIn(cmd), "ID").startsWith(plain.replace(templateIdIn(plain), "ID")))
  }

  for ((name, make) <- Seq[(String, NodeSeq => Js)](
    "append2BodyWithScriptExtraction" -> (ns => JS.append2BodyWithScriptExtraction(ns)),
    "replaceWithScriptExtraction" -> (ns => JS.replaceWithScriptExtraction("target", ns)),
  )) {
    name should "take the scripts out of the HTML, at any depth, and run them in document order" in {
      val ns = script("a()") ++ <div><span>{JS.inScriptTag(Js("b()"))}</span>{<script type="module">c()</script>}<i>kept</i></div> ++ script("d()")
      val cmd = make(ns).cmd
      assert(!cmd.contains("script"))
      assert(cmd.contains(JsEscape.escape("<div><span></span><i>kept</i></div>")))
      assert(cmd.endsWith(";a();b();c();d()"))
    }

    it should "keep the scripts that aren't JavaScript in the HTML" in {
      val cmd = make(<div><script type="text/template">tpl</script>{script("run()")}</div>).cmd
      assert(cmd.contains(JsEscape.escape("""<div><script type="text/template">tpl</script></div>""")))
      assert(cmd.endsWith(";run()"))
    }

    it should "have balanced brackets, outside its string literals" in {
      for (ns <- Seq[NodeSeq](<p>x</p>, <p>{"( [ { \" ' \\"}</p> ++ script("f(1)"), NodeSeq.Empty, script("g()"))) {
        val cmd = make(ns).cmd
        var (depth, i, min) = (0, 0, 0)
        while (i < cmd.length) {
          cmd(i) match {
            case q@('"' | '\'') => // skip the string literal
              i += 1
              while (cmd(i) != q) i += (if (cmd(i) == '\\') 2 else 1)
            case '(' | '[' | '{' => depth += 1
            case ')' | ']' | '}' => depth -= 1; min = math.min(min, depth)
            case _ =>
          }
          i += 1
        }
        assert(depth == 0 && min == 0, cmd)
      }
    }

    it should "look for the scripts once, when it's first written (not when it's created)" in {
      var read = 0
      // Nodes that count how many times they're read: when the scripts are looked for, and when they're written.
      val counting = new NodeSeq {
        override def theSeq: Seq[Node] = { read += 1; Seq(<p>x</p>, script("run()")) }
      }
      val js = make(counting)
      assert(read == 0, "the scripts were looked for when it was created")
      val first = js.cmd
      assert(read > 0 && first.endsWith(";run()"))
      val detection = js.asInstanceOf[JsWithScriptDetection]
      assert(detection.js eq detection.js)
      val readAfterFirst = read
      detection.js
      assert(read == readAfterFirst, "the scripts were looked for again")
      assert(js.cmd == first)
    }
  }

  // ---------- what it does, when it runs ----------

  private lazy val nodeAvailable: Boolean = scala.util.Try(Seq("node", "--version").!!).isSuccess

  /**
   * Runs `code` in node, with a fake DOM: `existingIds` are the elements there are. Returns what happened, in order:
   * `template <id>` (a template appended to the body), `append <html>` / `replace <id> <html>` (with the template's
   * content), `remove <id>`, `error <message>` (console.error), `call <name>` (the functions a, b, c, run), and
   * `threw <message>` if it threw.
   */
  private def run(code: String, existingIds: Seq[String] = Nil): Seq[String] = {
    val harness =
      s"""const log = [];
         |const byId = {};
         |function element(id) {
         |  return {id, remove() { log.push('remove ' + this.id); delete byId[this.id]; },
         |    replaceWith(fragment) { log.push('replace ' + this.id + ' ' + fragment.html); fragment.html = ''; }};
         |}
         |for (const id of ${existingIds.map(id => "'" + id + "'").mkString("[", ",", "]")}) byId[id] = element(id);
         |globalThis.document = {
         |  body: {appendChild(x) {
         |    if (x.isTemplate) { byId[x.id] = x; log.push('template ' + x.id); }
         |    else { log.push('append ' + x.html); x.html = ''; }
         |    return x;
         |  }},
         |  createElement(tag) {
         |    const template = element(null);
         |    template.isTemplate = tag === 'template';
         |    template.content = {html: ''};
         |    template.setAttribute = function(k, v) { this[k] = v; };
         |    Object.defineProperty(template, 'innerHTML', {set(html) { this.content.html = html; }});
         |    return template;
         |  },
         |  getElementById(id) { return byId[id] || null; },
         |};
         |console.error = message => log.push('error ' + message);
         |for (const name of ['a', 'b', 'c', 'run']) globalThis[name] = () => log.push('call ' + name);
         |try { (0, eval)(require('fs').readFileSync(process.argv[2], 'utf8')); } catch (e) { log.push('threw ' + e.message); }
         |process.stdout.write(JSON.stringify(log));
         |""".stripMargin
    val dir = Files.createTempDirectory("script-extraction-spec")
    try {
      val (harnessFile, codeFile) = (dir.resolve("harness.js"), dir.resolve("code.js"))
      Files.writeString(harnessFile, harness)
      Files.writeString(codeFile, code)
      val out = Seq("node", harnessFile.toString, codeFile.toString).!!.trim
      // (A JSON array of strings: none of the logged values has a `","` in it.)
      out.stripPrefix("[\"").stripSuffix("\"]").split("\",\"").toSeq.filter(_.nonEmpty).map(_.replace("\\\"", "\"").replace("\\\\", "\\"))
    } finally {
      Files.list(dir).forEach(f => Files.delete(f))
      Files.delete(dir)
    }
  }

  "append2BodyWithScriptExtraction, when it runs" should "append the HTML without its scripts, remove the template, and then run the scripts" in {
    assume(nodeAvailable, "node isn't available")
    val js = JS.append2BodyWithScriptExtraction(script("a()") ++ <div class="x">{script("b()")}<b>t &amp; u</b></div> ++ script("run()"))
    for (code <- Seq(js.cmd, written(js))) {
      val log = run(code)
      val id = log.head.stripPrefix("template ")
      assert(id.endsWith("template"), log)
      assert(log == Seq(s"template $id", """append <div class="x"><b>t &amp; u</b></div>""", s"remove $id", "call a", "call b", "call run"))
    }
  }

  it should "append the HTML when there are no scripts" in {
    assume(nodeAvailable, "node isn't available")
    val log = run(JS.append2BodyWithScriptExtraction(<p>x</p>).cmd)
    assert(log.map(_.takeWhile(_ != ' ')) == Seq("template", "append", "remove"))
    assert(log(1) == "append <p>x</p>")
  }

  "replaceWithScriptExtraction, when it runs" should "replace the element by the HTML without its scripts, remove the template, and then run the scripts" in {
    assume(nodeAvailable, "node isn't available")
    val js = JS.replaceWithScriptExtraction("target", <div id="target">{script("a()")}<i>new</i>{JS.inScriptTag(Js("run()"))}</div>)
    for (code <- Seq(js.cmd, written(js))) {
      val log = run(code, existingIds = Seq("target"))
      val id = log.head.stripPrefix("template ")
      assert(log == Seq(s"template $id", """replace target <div id="target"><i>new</i></div>""", s"remove $id", "call a", "call run"))
    }
  }

  it should "log an error, not throw, and still run the scripts, when the element isn't there (no template is created)" in {
    assume(nodeAvailable, "node isn't available")
    val log = run(JS.replaceWithScriptExtraction("missing", <p>{script("run()")}</p>).cmd)
    assert(log == Seq("error Element with id missing not found", "call run"))
  }

  it should "escape the id (in the element's lookup, and in the error's message)" in {
    assume(nodeAvailable, "node isn't available")
    val log = run(JS.replaceWithScriptExtraction("a\"b'c", <p>x</p>).cmd)
    assert(log == Seq("error Element with id a\"b'c not found"))
  }

  // ---------- written by XmlJsStream ----------

  /** As `js`, but its cmd can't be built: so it's written as its parts (the XML as it's serialized). */
  private def noCmd(js: Js): Js = {
    val d = js.asInstanceOf[JsWithScriptDetection]
    new JsWithScriptDetection(d.before, d.ns, d.after, d.scripts) {
      override def cmd: String = throw new IllegalStateException("JsWithScriptDetection.cmd was built")
    }
  }

  "XmlJsStream" should "write them as their cmd, without building it (the XML is written as it's serialized)" in {
    val ns = script("a()") ++ <div title={"\"q\" & <x>"}>{"t é 😀 '"}{script("b()")}</div>
    for (js <- Seq(JS.append2BodyWithScriptExtraction(ns), JS.replaceWithScriptExtraction("target", ns))) {
      assert(written(js) == js.cmd)
      assert(written(noCmd(js)) == js.cmd)
      assert(written(Js(Js("before();"), noCmd(js), Js(";after()"))) == "before();" + js.cmd + ";after()")
    }
  }

  it should "write a big one (many chunks) as its cmd" in {
    val ns = <div>{(1 to 3_000).map(i => <p class="row">{s"Row $i é \"q\" & <x>"}{if (i % 500 == 0) script(s"row($i)") else NodeSeq.Empty}</p>)}</div>
    val js = JS.replaceWithScriptExtraction("main", ns)
    val out = written(noCmd(js))
    assert(out.length == js.cmd.length && out == js.cmd)
    assert(out.endsWith(";row(500);row(1000);row(1500);row(2000);row(2500);row(3000)"))
  }

  it should "write one in an attribute (a rerender in an onclick) as its cmd, without building it" in {
    val rerender = JS.replaceWithScriptExtraction("inner", <b title="&quot;">{"t & <u>"}{script("run()")}</b>)
    val expected = new NodeSeqJs(ElemOptimized("button", "onclick" -> rerender)(Text("Go"))).cmd
    assert(written(new NodeSeqJs(ElemOptimized("button", "onclick" -> noCmd(rerender))(Text("Go")))) == expected)
    assert(written(JS.htmlAsJsStr(ElemOptimized("button", "onclick" -> noCmd(rerender))(Text("Go")))) == JS.htmlAsJsStr(ElemOptimized("button", "onclick" -> rerender)(Text("Go"))).cmd)
  }
}
