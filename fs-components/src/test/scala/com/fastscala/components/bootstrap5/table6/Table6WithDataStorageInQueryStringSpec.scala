package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.langpack.FSComponentsLangPack
import com.fastscala.core.{FSContext, FSPage}
import com.fastscala.testutils.PerfFS
import org.scalatest.flatspec.AnyFlatSpec

/** [[Table6WithDataStorageInQueryString]]: a table's named values, in the URL's query string, under `<tableName>-<name>`. */
class Table6WithDataStorageInQueryStringSpec extends AnyFlatSpec {

  import FSComponentsLangPack.Default.EN

  private class StoredTable(name: String = "people", pushes: Boolean = false)
    extends Table6Base with Table6BootrapStyling with Table6StandardColumns with Table6SeqDataSource with Table6WithDataStorageInQueryString {
    override type R = Int

    override def tableName: String = name

    override def columns()(implicit fsc: com.fastscala.core.FSPage): List[C] = List(ColStr("N", _.toString))

    override def seqRowsSource: Seq[Int] = 1 to 3

    override def queryStringStoragePushesHistory: Boolean = pushes
  }

  private def page(uri: String): FSPage = PerfFS.newPage(uri)

  /** What the JavaScript sets and deletes in the URL, in order. */
  private def changes(js: String): Seq[(String, Option[String])] = {
    // (The string literals' escapes, as JavaScript reads them: e.g. `-` is written `\x2d`.)
    def unescaped(s: String) = """\\x([0-9a-fA-F]{2})""".r.replaceAllIn(s, m => java.util.regex.Matcher.quoteReplacement(Integer.parseInt(m.group(1), 16).toChar.toString))
    """p\.(?:set\("([^"]*)","([^"]*)"\)|delete\("([^"]*)"\));""".r.findAllMatchIn(js)
      .map(m => if (m.group(1) != null) unescaped(m.group(1)) -> Some(unescaped(m.group(2))) else unescaped(m.group(3)) -> None).toSeq
  }

  "getProp" should "read the table's own parameters: <tableName>-<name>" in {
    given FSPage = page("/x?people-page=3&people-size=50&page=9&other-page=7")
    val t = new StoredTable()
    assert(t.getProp("page").contains("3"))
    assert(t.getProp("size").contains("50"))
    assert(t.getProp("sort").isEmpty)
    assert(new StoredTable("other").getProp("page").contains("7"))
    assert(new StoredTable("none").getProp("page").isEmpty)
    assert(t.queryStringParamName("page") == "people-page")
  }

  it should "decode the parameters, use the first of a repeated one, and skip what's malformed" in {
    given FSPage = page("/x?people-q=a%26b+c%3D%C3%A9&people-page=2&people-page=3&people-bad=%ZZ&&=&people-empty=&people-flag")
    val t = new StoredTable()
    assert(t.getProp("q").contains("a&b c=é"))
    assert(t.getProp("page").contains("2"))
    assert(t.getProp("bad").isEmpty)
    assert(t.getProp("empty").contains(""))
    assert(t.getProp("flag").contains(""))
  }

  it should "find nothing in a page without a request, or without a query string" in {
    assert(new StoredTable().getProp("page")(using PerfFS.newPage()).isEmpty)
    assert(new StoredTable().getProp("page")(using page("/x")).isEmpty)
  }

  "setProp" should "be what getProp returns from then on" in {
    given FSPage = page("/x?people-page=3")
    val t = new StoredTable()
    t.setProp("page", Some("4"))
    assert(t.getProp("page").contains("4"))
    t.setProp("size", "20")
    assert(t.getProp("size").contains("20"))
    t.clearProp("page")
    assert(t.getProp("page").isEmpty)
    t.setProp("size", None)
    assert(t.getProp("size").isEmpty)
  }

  it should "be put in the URL by the next rerender, all at once, and only once" in {
    val p = page("/x?people-page=3")
    given FSPage = p
    given FSContext = p.rootFSContext
    val t = new StoredTable()
    t.render()
    assert(changes(t.rerender().cmd).isEmpty, "nothing was set")

    t.setProp("page", "4")
    t.setProp("size", "20")
    t.clearProp("sort")
    t.setProp("page", "5") // (set again: its last value, in its new place)
    val js = t.rerender().cmd
    assert(changes(js) == Seq("people-size" -> Some("20"), "people-sort" -> None, "people-page" -> Some("5")))
    assert(js.contains("new URL(window.location.href)"))
    assert(js.contains("window.history.replaceState(window.history.state,'',u.href)") && !js.contains("pushState"))
    assert("""history\.replaceState\(window""".r.findAllIn(js).size == 1, "a single change of the URL")

    assert(changes(t.rerender().cmd).isEmpty, "it's in the URL already")
    assert(t.getProp("page").contains("5") && t.getProp("size").contains("20"), "and still what getProp returns")
  }

  it should "add browser history entries, when asked to" in {
    given FSPage = page("/x")
    val t = new StoredTable(pushes = true)
    t.setProp("page", "2")
    val js = t.flushQueryStringStorage().cmd
    assert(js.contains("window.history.pushState(window.history.state,'',u.href)") && !js.contains("replaceState"))
    assert(t.flushQueryStringStorage().cmd == "")
  }

  it should "be escaped in the JavaScript" in {
    given FSPage = page("/x")
    val t = new StoredTable("a\"b")
    t.setProp("k", "</script>\n'x'")
    val js = t.flushQueryStringStorage().cmd
    assert(js.contains("""p.set("a\x22b\x2dk","""), js)
    assert(!js.contains("</script>") && !js.contains("\n"), js)
  }

  "Tables in the same page" should "each keep their own values" in {
    val p = page("/x?a-page=2&b-page=3")
    given FSPage = p
    val (a, b) = (new StoredTable("a"), new StoredTable("b"))
    assert(a.getProp("page").contains("2") && b.getProp("page").contains("3"))
    a.setProp("page", "9")
    assert(b.getProp("page").contains("3"))
    assert(changes(a.flushQueryStringStorage().cmd) == Seq("a-page" -> Some("9")))
    assert(changes(b.flushQueryStringStorage().cmd).isEmpty)
  }
}
