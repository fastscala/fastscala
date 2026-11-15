package com.fastscala.scala_xml.xml

import com.fastscala.js.{EncJs, Js}
import com.fastscala.scala_xml.js.JS
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import scala.xml.*

/**
 * What the XmlJsStream perf and baseline specs write: pages, tables and rerenders as an fs-components application
 * builds them (ElemOptimized with String and Js attributes, plain scala-xml, script tags, text with chars to escape,
 * Portuguese text), deterministic (no random ids), and sinks to write them into.
 */
object XmlJsStreamFixtures {

  /** Counts the bytes written, and discards them; each write completes at once. */
  final class CountingSink extends Content.Sink {
    var count = 0L

    override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
      count += bb.remaining()
      bb.position(bb.limit())
      cb.succeeded()
    }
  }

  /** Keeps the bytes written; each write completes at once. */
  final class CollectingSink extends Content.Sink {
    val bytes = new ByteArrayOutputStream()
    var writes = 0
    var lastSeen = false

    override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
      if (bb.hasArray) {
        bytes.write(bb.array(), bb.arrayOffset() + bb.position(), bb.remaining())
        bb.position(bb.limit())
      } else while (bb.hasRemaining) bytes.write(bb.get())
      writes += 1
      if (last) lastSeen = true
      cb.succeeded()
    }
  }

  private def callback(id: Int, action: Int): Js = Js(s"""window._fs.callback('',"p$id","a$action",false,true,true,{});""")

  private def button(id: Int, label: String): Elem =
    ElemOptimized("button", "type" -> "button", "class" -> "btn btn-primary btn-sm", "id" -> s"btn$id", "onclick" -> callback(id, id * 3))(Text(label))

  private def icon(name: String): Elem = ElemOptimized("i", "class" -> s"bi bi-$name")()

  /** One card of a page: a header with buttons, a list, a form, a small table, plain scala-xml, a script tag. */
  def section(i: Int): Elem =
    ElemOptimized("div", "class" -> "card mb-3 shadow-sm", "id" -> s"section$i")(
      ElemOptimized("div", "class" -> "card-header d-flex justify-content-between align-items-center")(
        ElemOptimized("h5", "class" -> "mb-0")(Text(s"Section $i: Inscrição & reinscrição")),
        ElemOptimized("div", "class" -> "btn-group")(
          button(i * 10 + 1, "Editar"),
          button(i * 10 + 2, "Remover"),
          ElemOptimized("a", "href" -> s"/sections/$i?tab=notes&mode=edit", "class" -> "btn btn-link")(icon("gear"), Text(" Detalhes"))
        )
      ),
      ElemOptimized("div", "class" -> "card-body")(
        ElemOptimized("p", "class" -> "text-muted")(Text(s"Some notes for section $i, with <special> & \"escaped\" characters: código postal, São João, € 25,00.")),
        ElemOptimized("ul", "class" -> "list-group list-group-flush")(
          (1 to 5).map(j => ElemOptimized("li", "class" -> "list-group-item", "data-idx" -> j.toString)(Text(s"Item $j of $i"), Text(" — "), Atom(j * 1.5))) *
        ),
        ElemOptimized("form", "class" -> "row g-3", "onsubmit" -> Js(callback(i, 7), Js("return false;")))(
          ElemOptimized("label", "for" -> s"name$i", "class" -> "form-label")(Text("Encarregado de educação")),
          ElemOptimized("input", List("type" -> "text", "class" -> "form-control", "id" -> s"name$i", "value" -> s"José António $i", "placeholder" -> "Nome",
            "onchange" -> Js(Js("""window._fs.callback(document.getElementById(""""), Js(s"name$i"), Js("""").value,"p","x",false,true,true,{});"""))), minimizeEmpty = true)(),
          ElemOptimized("select", "class" -> "form-select")(
            (1 to 4).map(j => ElemOptimized("option", "value" -> j.toString)(Text(s"Opção $j"))) *
          )
        ),
        <table class="table table-striped table-sm" data-plain="true">
          <thead><tr><th>Name</th><th>Age</th><th>City</th></tr></thead>
          <tbody>{(1 to 4).map(r => <tr id={s"r$i-$r"}><td>{s"Person $r of s$i"}</td><td>{20 + r}</td><td>Lisboa &amp; Porto</td></tr>)}</tbody>
        </table>,
        Comment(s" section $i "),
        JS.inScriptTag(Js(s"""document.getElementById("section$i").classList.add("ready"); console.log('<loaded> $i');"""))
      )
    )

  /** A whole page: head (scripts, styles) and `sections` cards. */
  def page(sections: Int): Elem =
    <html lang="pt">
      <head>
        <meta charset="utf-8"/>
        <title>Página &amp; teste</title>
        <link rel="stylesheet" href="/static/css/bootstrap.min.css"/>
        <script type="text/javascript" src="/static/js/fs.js"></script>
        {Unparsed("<style>.card{border-width:2px}</style>")}
      </head>
      <body class="bg-light">
        {ElemOptimized("div", "class" -> "container", "id" -> "main")((1 to sections).map(section) *)}
        {JS.inScriptTag(Js("window.addEventListener('load', function() { document.body.classList.add('ready'); });"))}
      </body>
    </html>

  /** A table of ElemOptimized cells (as fs-components' tables are), each with an id and a class, some with a Js attribute. */
  def table(rows: Int, cols: Int): Elem =
    ElemOptimized("table", "class" -> "table table-striped table-hover", "id" -> "tbl")(
      ElemOptimized("thead")(ElemOptimized("tr")((1 to cols).map(c => ElemOptimized("th", "class" -> "text-center")(Text(s"Col $c"))) *)),
      ElemOptimized("tbody")(
        (1 to rows).map { r =>
          ElemOptimized("tr", "id" -> s"row$r", "class" -> (if (r % 2 == 0) "even" else "odd"))(
            (1 to cols).map { c =>
              if (c == cols) ElemOptimized("td", "class" -> "actions")(button(r * 100 + c, "Ação"))
              else ElemOptimized("td", "class" -> "data-cell px-2 py-1 text-center align-middle", "id" -> s"c$r-$c")(Text(if (c % 5 == 0) s"Célula <$r,$c>" else s"s1 r$r c$c"))
            } *
          )
        } *
      )
    )

  /** The same table, in plain scala-xml (Elem, UnprefixedAttribute). */
  def plainTable(rows: Int, cols: Int): Elem =
    <table class="table table-striped table-hover" id="tbl">
      <tbody>{(1 to rows).map(r => <tr id={s"row$r"} class={if (r % 2 == 0) "even" else "odd"}>{(1 to cols).map(c => <td class="data-cell px-2 py-1 text-center align-middle" id={s"c$r-$c"}>{if (c % 5 == 0) s"Célula <$r,$c>" else s"s1 r$r c$c"}</td>)}</tr>)}</tbody>
    </table>

  /** A rerender: the element replaced by `ns`, as HTML in a JavaScript string literal (as JS.replace does). */
  def rerender(ns: NodeSeq): Js =
    Js(Js("""(document.getElementById("main") ? document.getElementById("main").replaceWith((function() {var template = document.createElement('template'); template.innerHTML = """),
      JS.htmlAsJsStr(ns), Js("""; return template.content.firstChild;})()) : console.error("Element with id main not found"));"""))

  /** A rerender whose scripts are taken out of the HTML and run after it (as JS.replaceWithScriptExtraction does). */
  def rerenderWithScripts(ns: NodeSeq): Js = JS.replaceWithScriptExtraction("main", ns)

  /** Buttons whose onclick is itself a rerender (an EncJs in an attribute), all in a rerender: three levels of escaping. */
  def nestedRerender(buttons: Int): Js =
    rerender(ElemOptimized("div", "id" -> "main")(
      (1 to buttons).map { b =>
        ElemOptimized("button", "class" -> "btn btn-outline-secondary", "onclick" -> rerender(ElemOptimized("span", "class" -> "badge")(Text(s"Clicked <$b> é"))))(Text(s"Button $b"))
      } *
    ))

  /** Random XML (and Js) trees, for the baseline spec: every kind of node and Js XmlJsStream knows of. */
  def randomTree(rnd: scala.util.Random, depth: Int): Node = {
    val texts = Seq("a", "x < y & z", "\"q\" 'a'", "é € 😀", "  ", "\\ / `$`", "</script>", "\uD83D", "\u0001\t\n", "")
    def text = texts(rnd.nextInt(texts.size))
    def js(d: Int): Js = rnd.nextInt(6) match {
      case 0 => Js(text)
      case 1 if d > 0 => Js(js(d - 1), js(d - 1))
      case 2 if d > 0 => new EncJs(js(d - 1), inString = rnd.nextBoolean())
      case 3 if d > 0 => JS.htmlAsJsStr(NodeSeq.fromSeq(Seq.fill(rnd.nextInt(3))(randomTree(rnd, d - 1))))
      case 4 if d > 0 => JS.replaceWithScriptExtraction("e", NodeSeq.fromSeq(Seq.fill(rnd.nextInt(3))(randomTree(rnd, d - 1))))
      case _ => Js(s"f(${text.length})")
    }
    def children = Seq.fill(if (depth == 0) 0 else rnd.nextInt(4))(randomTree(rnd, depth - 1))
    rnd.nextInt(12) match {
      case 0 => Text(text)
      case 1 => Atom(rnd.nextInt(100))
      case 2 => Unparsed(s"<b>$text</b>")
      case 3 => Comment(text.filter(_ != '-'))
      case 4 => PCData(text)
      case 5 => JS.inScriptTag(js(depth))
      case 6 => <script type="text/javascript">{s"var v = '${text.length}';"}</script>
      case 7 => Group(children)
      case 8 => <p class={text} id="x" data-n={rnd.nextInt(9).toString}>{children}</p>
      case 9 => <ns:e xmlns:ns="urn:x" ns:a={text}>{children}</ns:e>
      case _ =>
        val attrs: List[(String, ElemOptimized.AttrValue)] = List.fill(rnd.nextInt(3))(
          (Seq("class", "id", "onclick", "title")(rnd.nextInt(4)), if (rnd.nextBoolean()) text else js(depth)))
        ElemOptimized(Seq("div", "span", "td")(rnd.nextInt(3)), attrs, minimizeEmpty = rnd.nextBoolean())(children *)
    }
  }
}
