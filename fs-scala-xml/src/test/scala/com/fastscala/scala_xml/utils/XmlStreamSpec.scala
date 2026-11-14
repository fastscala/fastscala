package com.fastscala.scala_xml.utils

import org.scalatest.flatspec.AnyFlatSpec

import java.io.StringWriter
import scala.xml.*

class XmlStreamSpec extends AnyFlatSpec {

  private def seq(ns: NodeSeq): String = { val w = new StringWriter; XmlStream.writeSeq(ns, w); w.toString }

  private def node(n: Node): String = { val w = new StringWriter; XmlStream.writeNode(n, w); w.toString }

  /** `prefix` + `text` escaped by [[XmlEscape]], into a [[StringBuilderLike]] (other than XmlStream's buffers). */
  private def escaped(text: String, prefix: String = ""): String = {
    val sb = new java.lang.StringBuilder(prefix)
    XmlEscape.escape(text, new StringBuilderLike {
      override def append(s: String): Unit = sb.append(s)
      override def append(s: String, start: Int, end: Int): Unit = sb.append(s, start, end)
    })
    sb.toString
  }

  private val samples: Seq[(String, NodeSeq)] = Seq(
    "text element" -> <p>hello</p>,
    "nested elements with attributes" -> <div class="a b" id="x"><span data-v="1">t</span><b/></div>,
    "escaping in text and attributes" -> <p title={"\"q\" & <x>"}>{"a < b & c"}</p>,
    "self-closing element" -> <br/>,
    "empty non-void element" -> <div></div>,
    "comment, entity ref, PCData, unparsed" -> <div><!-- c -->&amp;{PCData("x<y")}{Unparsed("<raw/>")}</div>,
    "several top-level nodes" -> (<b>1</b> ++ Text(" & ") ++ <i>2</i>),
    "groups, nested" -> <div>{Group(Seq(<b>g</b>, Group(Seq(Text("t"), <i/>))))}</div>,
    "namespaces" -> <a:x xmlns:a="urn:a"><a:y><z xmlns="urn:z"><w/></z></a:y></a:x>,
    "only Atoms (not Text) as children: separated by spaces, as toString" -> <p>{"a"}{"b"}{42}{"c < d"}</p>,
    "Atoms, Unparsed and PCData as the only children" -> <p>{"a"}{Unparsed("<u/>")}{PCData("c")}{1.5}</p>,
    "Atoms with Text in between: not spaced" -> <p>{"a"}-{"b"}</p>,
    "a single Atom child: no space" -> <p>{"only"}</p>,
    "Atoms in a Group: not spaced" -> <p>{Group(Seq(new Atom("a"), new Atom("b")))}</p>,
    "siblings after a closed element" -> <ul><li>1</li><li><b>2</b></li><li>3</li></ul>,
  )

  samples.foreach { case (name, ns) =>
    "writeSeq" should s"write the same as toString ($name)" in {
      assert(seq(ns) == ns.toString)
    }
  }

  "writeNode" should "write the same as toString" in {
    val n = <div a="1"><p>x</p><br/></div>
    assert(node(n) == n.toString)
  }

  it should "write a group's nodes" in {
    assert(node(Group(Seq(<b>1</b>, <i>2</i>))) == "<b>1</b><i>2</i>")
  }

  "writeSeq" should "write nothing for an empty NodeSeq" in {
    assert(seq(NodeSeq.Empty) == "")
  }

  it should "handle very deep trees without overflowing the stack" in {
    val depth = 100_000
    val deep = (1 to depth).foldLeft[Elem](<p>leaf</p>)((acc, _) => <div>{acc}</div>)
    val out = seq(deep)
    assert(out == "<div>" * depth + "<p>leaf</p>" + "</div>" * depth)
  }

  it should "write a single text node bigger than the chunk size" in {
    val big = "x&<" * (XmlStream.ChunkSize * 3)
    val ns = <p>{big}</p>
    assert(seq(ns) == ns.toString)
  }

  it should "produce the same output across many chunk boundaries" in {
    val ns = <div>{(1 to 20_000).map(i => <span class="c" title={s"t$i \"q\""}>{s"$i & <x>"}<br/></span>)}</div>
    assert(seq(ns) == ns.toString)
  }

  it should "write everything before returning, in chunks, without flushing or closing the writer" in {
    val writes = collection.mutable.ArrayBuffer.empty[Int]
    var flushed, closed = false
    val sb = new java.lang.StringBuilder
    val w = new java.io.Writer {
      override def write(cbuf: Array[Char], off: Int, len: Int): Unit = { writes += len; sb.append(cbuf, off, len) }
      override def flush(): Unit = flushed = true
      override def close(): Unit = closed = true
    }
    val ns = <div>{(1 to 5_000).map(i => <p>{i.toString}</p>)}</div>
    XmlStream.writeSeq(ns, w)
    assert(sb.toString == ns.toString)
    assert(writes.size > 1 && writes.size < ns.toString.length / (XmlStream.ChunkSize / 2))
    assert(!flushed && !closed)
  }

  it should "fail like Utility.serialize (and so toString) for unknown Node subclasses" in {
    val custom = new Node {
      override def label: String = "custom"
      override def child: Seq[Node] = Nil
    }
    assertThrows[IllegalArgumentException](Utility.serialize(custom))
    assertThrows[IllegalArgumentException](seq(<div>{custom}</div>))
  }

  // ---------- escape / attributes ----------

  "XmlEscape.escape" should "match Utility.escape for every char" in {
    val all = (0 until 0x10000).map(_.toChar).mkString
    assert(escaped(all) == Utility.escape(all))
  }

  it should "match Utility.escape for random strings" in {
    val rnd = new scala.util.Random(42)
    val alphabet = "ab &<>\"'\n\r\t\u0000\u0001\u001f\u007f é€😀"
    (1 to 2_000).foreach { _ =>
      val str = Seq.fill(rnd.nextInt(40))(alphabet(rnd.nextInt(alphabet.length))).mkString
      assert(escaped(str) == Utility.escape(str), s"for ${str.toList}")
    }
  }

  it should "handle empty strings and strings with nothing to escape" in {
    assert(escaped("") == "")
    assert(escaped("plain", prefix = "x") == "xplain")
  }

  private val attributeSamples: Seq[(String, Elem)] = Seq(
    "several attributes" -> <a href="/x?a=1&amp;b=2" class="c d" id="i"/>,
    "quotes, apostrophes, < > & and control chars" -> Elem(null, "p", new UnprefixedAttribute("t", "\"q\" 'a' <x> & \u0001\n\t", Null), TopScope, true),
    "empty value" -> Elem(null, "p", new UnprefixedAttribute("e", "", Null), TopScope, true),
    "prefixed attribute" -> <x:a xmlns:x="urn:x" x:k="v" k2="w"/>,
    "value with entity ref nodes" -> Elem(null, "p", new UnprefixedAttribute("a", Seq(Text("x"), EntityRef("amp"), Text("y")), Null), TopScope, true),
    "unparsed value with a double quote (scala-xml uses single quotes)" -> Elem(null, "p", new UnprefixedAttribute("a", Seq(Unparsed("say \"hi\"")), Null), TopScope, true),
    "PCData value" -> Elem(null, "p", new UnprefixedAttribute("a", Seq(PCData("c")), Null), TopScope, true),
    "null value (only the space is written)" -> Elem(null, "p", new UnprefixedAttribute("a", null: Seq[Node], new UnprefixedAttribute("b", "1", Null)), TopScope, true),
    "prefixed element name" -> <h:div xmlns:h="urn:h"><h:span>t</h:span></h:div>,
    "text subclass" -> <p>{new Text("a < b") { override def buildString(sb: StringBuilder) = sb.append("custom") }}</p>,
    "embedded values (Atoms): strings, numbers, objects" -> <p>{"a < \"b\" & c"} {42} {3.5} {Some("<x>")}</p>,
    "atom attribute value" -> Elem(null, "p", new UnprefixedAttribute("a", Seq(new Atom(7)), new UnprefixedAttribute("b", Seq(new Atom("<\"q\">")), Null)), TopScope, true),
    "atom subclass" -> <p>{new Atom("a < b") { override def buildString(sb: StringBuilder) = sb.append("custom") }}</p>,
  )

  attributeSamples.foreach { case (name, elem) =>
    "writeNode" should s"write elements and attributes exactly as toString ($name)" in {
      assert(node(elem) == elem.toString)
    }
  }

  // ---------- stack ----------

  "writeSeq" should "handle children that aren't indexed (List, Vector, NodeSeq, a Node itself)" in {
    val inner = <i>n</i>
    val ns = <div>{
      Seq(
        Elem(null, "a", Null, TopScope, false, List(<b>1</b>, Text("t"), <c/>) *),
        Elem(null, "v", Null, TopScope, false, Vector(<b>2</b>) *),
        new Elem(null, "s", Null, TopScope, false, (<x/> ++ <y/>) *),
        new Elem(null, "n", Null, TopScope, false, inner *), // a Node is a Seq of itself
      )
    }</div>
    assert(seq(ns) == ns.toString)
  }

  it should "grow the stack past its initial size and restore scopes when levels finish" in {
    // Each level declares a namespace, so writing the wrong parent scope after a pop would change the output.
    val deep = (1 to 200).foldLeft[Elem](<leaf/>)((acc, d) =>
      Elem(null, "l", Null, NamespaceBinding(s"p$d", s"urn:$d", TopScope), false, acc, <after/>))
    assert(seq(deep) == deep.toString)
    assert(seq(deep ++ deep) == (deep ++ deep).toString)
  }

  it should "write a comment that looks like the internal end marker" in {
    val ns = <div>{Comment("end")}<p>after</p></div>
    assert(seq(ns) == "<div><!--end--><p>after</p></div>")
  }

  it should "handle wide and deep trees with groups at every level" in {
    def build(depth: Int): Node =
      if (depth == 0) Text("x & y")
      else Group(Seq(<d n={depth.toString}>{build(depth - 1)}</d>, Text(" "), <e/>))
    val ns = <root>{(1 to 50).map(_ => build(60))}</root>
    assert(seq(ns) == ns.toString)
  }

  // ---------- namespaces (NamespaceBinding) ----------
  //
  // An element's namespace declarations (xmlns, xmlns:p) are its scope, a NamespaceBinding chain, not its attributes:
  // each element writes only the bindings of its scope that aren't in its parent's (up to the parent's scope).

  private val namespaceSamples: Seq[(String, NodeSeq)] = Seq(
    "default namespace" -> <a xmlns="urn:a"><b><c/></b></a>,
    "several prefixes on one element" -> <r xmlns:p="urn:p" xmlns:q="urn:q"><p:a q:k="v"/><q:b/></r>,
    "a prefix redeclared with another URI (shadowing)" -> <a xmlns:p="urn:1"><p:b xmlns:p="urn:2"><p:c/></p:b><p:d/></a>,
    // (`xmlns=""` can't be written in a Scala 3 XML literal: built with its NamespaceBinding.)
    "the default namespace undeclared" -> {
      val outer = NamespaceBinding(null, "urn:a", TopScope)
      val undeclared = NamespaceBinding(null, "", outer)
      Elem(null, "a", Null, outer, true, Elem(null, "b", Null, undeclared, true, Elem(null, "c", Null, undeclared, true)), Elem(null, "d", Null, outer, true))
    },
    "declarations added at several depths" -> <a xmlns:p="urn:p"><b xmlns:q="urn:q"><c xmlns:r="urn:r"><p:x q:y="1" r:z="2"/></c></b></a>,
    "siblings: the second doesn't repeat the first's declarations" -> <a><b xmlns:p="urn:p"><p:c/></b><d><e xmlns:p="urn:p"/></d></a>,
    "top-level siblings, each with its own declarations" -> (<a xmlns:p="urn:p"><p:b/></a> ++ <c xmlns:p="urn:p"/> ++ <d/>),
    // (As toString, a Group's nodes are written in its own scope, TopScope: they redeclare the element's namespaces.)
    "groups inside a namespaced element" ->
      <a xmlns:p="urn:p">{Group(Seq(<p:b/>, <c xmlns:q="urn:q"><q:d/></c>, Group(Seq(<p:e/>))))}</a>,
    "inline SVG" ->
      <div><svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 16 16"><use xlink:href="#icon"/><path d="M0 0h16"/></svg><p>after</p></div>,
    "prefixed attribute values that need escaping" -> <a xmlns:p="urn:p" p:t={"x < \"y\" & z"}/>,
    "namespace URI that needs escaping" -> Elem(null, "a", Null, NamespaceBinding("p", "urn:a&b?c=\"d\"", TopScope), true),
    // A child built on its own (its scope chain doesn't go through the parent's): it writes its whole chain.
    "a namespaced element built separately, inside another scope" -> {
      val icon: Elem = (<svg xmlns="http://www.w3.org/2000/svg"><path d="M1 1"/></svg>) // (in parens: two literals in a row would be one NodeBuffer)
      <a xmlns:p="urn:p"><p:b>{icon}</p:b>{icon}</a>
    },
  )

  namespaceSamples.foreach { case (name, ns) =>
    "writeSeq" should s"write namespace declarations as toString ($name)" in {
      assert(seq(ns) == ns.toString)
    }
  }

  it should "write each declaration only on the element that adds it" in {
    val out = seq(<a xmlns:p="urn:p"><p:b><p:c/></p:b><d xmlns:q="urn:q"/></a>)
    assert(out == """<a xmlns:p="urn:p"><p:b><p:c/></p:b><d xmlns:q="urn:q"/></a>""")
  }

  {
    import com.fastscala.scala_xml.testutils.AttributeOrder.normalized
    import com.fastscala.scala_xml.testutils.ElemUtilsV1

    val svg = <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 16 16"><use xlink:href="#icon"/></svg>
    // ElemOptimized has its own attribute writing, and keeps the original's scope: check it's written too.
    val fastCases: Seq[(String, Elem)] = Seq(
      "ElemOptimized" -> { import ElemUtilsV1.RichElem; svg.addClass("icon").withAttr("width" -> "16") },
    )
    fastCases.foreach { case (name, e) =>
      "writeNode" should s"write the namespace declarations of a $name (attributes in any order)" in {
        assert(!e.getClass.eq(classOf[Elem]), e.getClass)
        val out = node(<div>{e}</div>)
        assert(normalized(out) == normalized(<div>{e}</div>.toString), out)
        assert(out.contains("""xmlns="http://www.w3.org/2000/svg"""") && out.contains("""xmlns:xlink="http://www.w3.org/1999/xlink""""), out)
        assert(out.contains("""<use xlink:href="#icon"/>"""), out)
      }
    }
  }

  "XmlEscape.escape" should "match Utility.escape for every character below 128, alone and inside text" in {
    (0 until 128).map(_.toChar).foreach { c =>
      for (text <- Seq(c.toString, s"ab${c}cd", s"$c$c")) {
        val expected = Utility.escape(text)
        assert(escaped(text) == expected, s"char ${c.toInt}")
      }
    }
  }

  it should "append text that needs no escaping unchanged" in {
    assert(escaped("plain text\n\twith tabs, newlines and ünïcödé", prefix = "x") == "xplain text\n\twith tabs, newlines and ünïcödé")
  }
}
