package com.fastscala.testutils

import java.util.regex.Pattern

/**
 * (A copy of fs-scala-xml's test helper com.fastscala.scala_xml.testutils.AttributeOrder: fs-components' tests
 * don't see fs-scala-xml's.)
 *
 * For comparing XML output where attribute order doesn't matter: [[com.fastscala.scala_xml.xml.XmlJsStream]] writes an
 * ElemOptimized's attributes in its own order, which can differ from toString's.
 */
object AttributeOrder {

  private val StartTag = Pattern.compile("""<([\w:.-]+)((?:\s+[\w:.-]+="[^"]*")*)(\s*/?)>""")
  private val Attr = Pattern.compile("""\s+([\w:.-]+)="([^"]*)"""")

  /**
   * `xml` with the attributes of every start tag sorted by name. (Escaped attribute values have no `"` or `>`, so a
   * regex finds every tag and attribute; a tag with a single-quoted value is left as it is.)
   */
  def normalized(xml: String): String = {
    val out = new java.lang.StringBuilder(xml.length)
    val m = StartTag.matcher(xml)
    var last = 0
    while (m.find()) {
      out.append(xml, last, m.start())
      val attrs = Seq.newBuilder[(String, String)]
      val am = Attr.matcher(m.group(2))
      while (am.find()) attrs += am.group(1) -> am.group(2)
      out.append('<').append(m.group(1))
      attrs.result().sortBy(_._1).foreach { case (k, v) => out.append(' ').append(k).append("=\"").append(v).append('"') }
      out.append(m.group(3)).append('>')
      last = m.end()
    }
    out.append(xml, last, xml.length)
    out.toString
  }
}
