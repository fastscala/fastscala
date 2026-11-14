package com.fastscala.scala_xml.js

import com.fastscala.core.FSContext
import com.fastscala.js.{CompositeJs, EncJs, Js, JsUtils, js}
import com.fastscala.scala_xml.rerenderers.{ContentRerenderer, ContentRerendererP, Rerenderer, RerendererP}
import com.fastscala.scala_xml.xml.ElemWithJsInScript
import com.fastscala.utils.IdGen

import scala.xml.*

class JsXmlUtils extends JsUtils {

  def rerenderable(render: Rerenderer => FSContext => Elem, idOpt: Option[String] = None, debugLabel: Option[String] = None): Rerenderer =
    new Rerenderer(rerenderer => fsc => render(rerenderer)(fsc), idOpt = idOpt, debugLabel = debugLabel)

  def rerenderableP[P](render: RerendererP[P] => FSContext => P => Elem, idOpt: Option[String] = None, debugLabel: Option[String] = None): RerendererP[P] =
    new RerendererP[P](rerenderer => fsc => param => render(rerenderer)(fsc)(param), idOpt = idOpt, debugLabel = debugLabel)

  def rerenderableContents(render: ContentRerenderer => FSContext => NodeSeq, id: Option[String] = None, debugLabel: Option[String] = None): ContentRerenderer =
    new ContentRerenderer(rerenderer => fsc => render(rerenderer)(fsc), idOpt = id, debugLabel = debugLabel)

  def rerenderableContentsP[P](render: ContentRerendererP[P] => FSContext => P => NodeSeq, id: Option[String] = None, debugLabel: Option[String] = None): ContentRerendererP[P] =
    new ContentRerendererP[P](rerenderer => fsc => param => render(rerenderer)(fsc)(param), idOpt = id, debugLabel = debugLabel)

  def append2BodyWithScriptExtraction(ns: NodeSeq): Js = {
    val (cleaned, js) = extractJsFromScriptTags(ns)
    append2Body(cleaned) & js
  }

  def append2Body(ns: NodeSeq): Js = {
    val elemId = IdGen.id("template")
    Js(Js(s"document.body.appendChild("), htmlToElement(ns, elemId), Js(")")) &
      removeId(elemId)
  }

  def append2(id: String, ns: NodeSeq): Js = {
    val elemId = IdGen.id("template")
    Js(Js(s"""document.getElementById("${escapeStr(id)}").appendChild("""), htmlToElement(ns, elemId), Js(""")""")) &
      removeId(elemId)
  }

  def prepend2(id: String, ns: NodeSeq): Js = {
    val elemId = IdGen.id("template")
    Js(Js(s"""document.getElementById("${escapeStr(id)}").insertBefore("""), htmlToElement(ns, elemId), Js(s""", document.getElementById("${escapeStr(id)}").firstChild)""")) &
      removeId(elemId)
  }

  def replaceWithScriptExtraction(id: String, by: NodeSeq): Js = {
    val (cleaned, js) = extractJsFromScriptTags(by)
    replace(id, cleaned) & js
  }

  def replace(id: String, by: NodeSeq): Js = Js(
    Js(s"""(document.getElementById("${escapeStr(id)}") ? document.getElementById("${escapeStr(id)}").replaceWith("""),
    htmlToElement(by),
    Js(s""") : console.error("Element with id ${escapeStr(id)} not found"));""")
  )

  /** `html`'s XML as a JavaScript string literal (escaped, and written as it's serialized: see [[EncJs]]). */
  def htmlAsJsStr(html: NodeSeq): Js = new EncJs(new NodeSeqJs(html), inString = true)

  def setContents(id: String, ns: NodeSeq): Js = js"${elementById(id)}.innerHTML = ${htmlAsJsStr(ns)};"

  def setContents(id: String, js: Js): Js = Js(s"""document.getElementById("${escapeStr(id)}").innerHTML = $js; """)

  def htmlToElement(html: NodeSeq, templateId: String = IdGen.id): Js =
    js"""(document.body.appendChild((function htmlToElement(html) {var template = document.createElement('template');template.setAttribute("id", ${asJsStr(templateId)});template.innerHTML = html.trim(); return template;})(${htmlAsJsStr(html)}))).content"""

  def forceHttps: Elem = {
    <script type="text/javascript">{
      scala.xml.Unparsed(
        """//<![CDATA[
          |if (location.protocol !== 'https:' && location.hostname !== 'localhost') { location.protocol = 'https:'; }
          |//]]>""".stripMargin)
    }</script>
  }

  def inScriptTag(js: Js): Elem = new ElemWithJsInScript(js)

  def inScriptTagModule(js: Js): Elem = new ElemWithJsInScript(js, "module")

  def showIf(b: Boolean)(ns: => NodeSeq): NodeSeq = if (b) ns else NodeSeq.Empty

  /**
   * In a single pass, the same as `(excludeJsInScriptTags(ns), extractJsInScriptTags(ns))`: `ns` without its JS
   * `<script>` tags (see [[isJsScriptTag]]), at any depth, and their contents, in document order, joined by `;`.
   * Subtrees without scripts aren't copied: if there are no scripts at all, `ns` itself is returned.
   * `ns` can be a single node or a sequence of nodes; a single node stays a single node, and a Group a Group.
   */
  def extractJsFromScriptTags(ns: NodeSeq): (NodeSeq, Js) = {
    val scripts = collection.mutable.ListBuffer[Js]()
    val cleaned: NodeSeq = ns match {
      case node: Node => extractJsFromScriptTagsRec(node, scripts)
      case nodes =>
        val children = extractJsFromScriptTagsSeq(nodes, scripts)
        if (children eq nodes) nodes else NodeSeq.fromSeq(children)
    }
    (cleaned, if (scripts.isEmpty) Js.Void else CompositeJs.joined(scripts.toSeq, Js(";")))
  }

  /** `node` without its scripts (`node` itself, by reference, if it has none), appending their JS to `scripts`. */
  private def extractJsFromScriptTagsRec(node: Node, scripts: collection.mutable.ListBuffer[Js]): NodeSeq = node match {
    // Group first: Group.label throws, so isJsScriptTag can't be called on it.
    case group: Group =>
      val nodes = extractJsFromScriptTagsSeq(group.nodes, scripts)
      if (nodes eq group.nodes) group else Group(nodes)
    case script: ElemWithJsInScript =>
      scripts.append(script.js)
      NodeSeq.Empty
    case script if isJsScriptTag(script) =>
      val js = scriptTagJs(script)
      if (js.nonEmpty) scripts.append(Js(js)) // empty scripts are skipped (as Js.Void would be by `&`)
      NodeSeq.Empty
    case elem: Elem =>
      val child = extractJsFromScriptTagsSeq(elem.child, scripts)
      if (child eq elem.child) elem else elem.copy(child = child)
    case node => node // Text, comments, etc.
  }

  /** `nodes` without their scripts (`nodes` itself, by reference, if they have none): only copied from the first change. */
  private def extractJsFromScriptTagsSeq(nodes: Seq[Node], scripts: collection.mutable.ListBuffer[Js]): Seq[Node] = {
    var result: collection.mutable.ArrayBuffer[Node] = null // stays null while nothing has changed
    var i = 0
    val it = nodes.iterator
    while (it.hasNext) {
      val node = it.next()
      val cleaned = extractJsFromScriptTagsRec(node, scripts)
      if (result == null && !(cleaned eq node)) {
        result = new collection.mutable.ArrayBuffer[Node](nodes.knownSize max 16)
        result ++= nodes.iterator.take(i) // the unchanged nodes before this one
      }
      if (result != null) result ++= cleaned
      i += 1
    }
    if (result == null) nodes else result.toSeq
  }

  /** The JS in a `<script>` tag: its children's text, without the CDATA markers added by [[inScriptTag]]. */
  private def scriptTagJs(script: Node): String = {
    val sb = new java.lang.StringBuilder()
    script.child.foreach(child => sb.append(child.toString()))
    // Literal (non-regex) replacements:
    sb.toString.replace("\n// <![CDATA[", "").replace("\n// ]]>", "")
  }


  /**
   * Removes the JS `<script>` tags (see [[isJsScriptTag]]), at any depth, from `ns`.
   * `ns` can be a single node or a sequence of nodes.
   */
  def excludeJsInScriptTags(ns: NodeSeq): NodeSeq =
    if (hasJsInScriptTags(ns)) excludeJsInScriptTagsRec(ns) else ns // nothing to remove: no copying

  private def excludeJsInScriptTagsRec(ns: NodeSeq): NodeSeq = ns match {
    case _ if !hasJsInScriptTags(ns) => ns
    case group: Group => Group(excludeJsInScriptTags(group.nodes: NodeSeq))
    case node: ElemWithJsInScript => NodeSeq.Empty
    case node: Elem if isJsScriptTag(node) => NodeSeq.Empty
    case node: Elem => node.copy(child = excludeJsInScriptTags(node.child: NodeSeq))
    case node: Node => node // Text, comments, etc.
    case nodes => nodes.flatMap(excludeJsInScriptTags(_))
  }

  inline private def isJsScriptTag(node: Node): Boolean =
    node.label == "script" && node.attribute("type").exists(_.exists(node => {
      val str = node.toString
      str == "text/javascript" || str == "module"
    }))

  /**
   * The contents of all JS `<script>` tags (see [[isJsScriptTag]]) in `ns`, at any depth, in document order, joined by `;`.
   * `ns` can be a single node or a sequence of nodes.
   */
  def extractJsInScriptTags(ns: NodeSeq): Js = {
    if (!hasJsInScriptTags(ns)) return Js.Void
    val scripts = collection.mutable.ListBuffer[Js]()
    collectJsInScriptTags(ns.iterator :: Nil, scripts)
    if (scripts.size == 0) Js.Void else CompositeJs.joined(scripts.toSeq, Js(";"))
  }

  /**
   * Whether `ns` contains any JS `<script>` tag (see [[isJsScriptTag]]), at any depth. Stops at the first one found.
   * `ns` can be a single node or a sequence of nodes.
   */
  def hasJsInScriptTags(ns: NodeSeq): Boolean = hasJsInScriptTags(ns.iterator :: Nil)

  /**
   * Depth-first, in document order, with an explicit stack (one iterator per level still being visited) instead of
   * the call stack, so it's tail recursive and deep trees can't overflow the stack.
   */
  @scala.annotation.tailrec private def hasJsInScriptTags(stack: List[Iterator[Node]]): Boolean = stack match {
    case Nil => false
    case it :: rest if !it.hasNext => hasJsInScriptTags(rest)
    case it :: _ => it.next() match {
      // Group first: Group.label throws, so isJsScriptTag can't be called on it.
      case group: Group => hasJsInScriptTags(group.nodes.iterator :: stack)
      case _: ElemWithJsInScript => true
      case node if node.label == "script" && isJsScriptTag(node) => true
      case node => hasJsInScriptTags(node.child.iterator :: stack)
    }
  }

  /** Depth-first, in document order, with an explicit stack (see [[hasJsInScriptTags]]), so it's tail recursive. */
  @scala.annotation.tailrec private def collectJsInScriptTags(stack: List[Iterator[Node]], scripts: collection.mutable.ListBuffer[Js]): Unit = stack match {
    case Nil =>
    case it :: rest if !it.hasNext => collectJsInScriptTags(rest, scripts)
    case it :: _ => it.next() match {
      // Group first: Group.label throws, so isJsScriptTag can't be called on it.
      case group: Group => collectJsInScriptTags(group.nodes.iterator :: stack, scripts)
      case script: ElemWithJsInScript =>
        scripts.append(script.js)
        collectJsInScriptTags(stack, scripts)
      case script if isJsScriptTag(script) =>
        val js = scriptTagJs(script)
        // Empty scripts are skipped (as Js.Void would be by `&`).
        if (js.nonEmpty) scripts.append(Js(js))
        collectJsInScriptTags(stack, scripts)
      case node => collectJsInScriptTags(node.child.iterator :: stack, scripts)
    }
  }
}
