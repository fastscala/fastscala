package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.table6.persisted.Table6PaginatedPersistedInQueryString
import com.fastscala.components.langpack.FSComponentsLangPack
import com.fastscala.core.{FSContext, FSPage}
import com.fastscala.testutils.PerfFS
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.{Elem, Node}

/** [[Table6PaginatedPersistedInQueryString]]: the page and the page size, in the URL's query string. */
class Table6PaginatedPersistedInQueryStringSpec extends AnyFlatSpec {

  import FSComponentsLangPack.Default.EN

  private class PeopleTable(name: String = "people", pageSize: Int = 10, startPage: Int = 0)
    extends Table6Base with Table6BootrapStyling with Table6StandardColumns with Table6PaginatedWithSeqDataSource with Table6PaginatedPersistedInQueryString {
    override type R = Int

    override def tableName: String = name

    override def columns()(implicit fsc: com.fastscala.core.FSPage): List[C] = List(ColStr("Id", _.toString))

    override def seqRowsSource: Seq[Int] = 1 to 25

    override def visiblePageSizes: List[Int] = List(5, 10, 20)

    override def defaultPageSize(implicit fsPage: FSPage): Int = pageSize

    override def defaultCurrentPage(implicit fsPage: FSPage): Int = startPage
  }

  private def elems(n: Node): Seq[Elem] = n match {
    case e: Elem => e +: e.child.flatMap(elems)
    case _ => Nil
  }

  private def ids(table: Elem): Seq[Int] =
    elems(table).filter(_.label == "tbody").flatMap(_.child.collect { case tr: Elem if tr.label == "tr" => tr.child.collect { case td: Elem => td.text.trim }.head.toInt })

  /** What the JavaScript sets and deletes in the URL, in order (the string literals' escapes read as JavaScript does). */
  private def changes(js: String): Seq[(String, Option[String])] = {
    def unescaped(s: String) = """\\x([0-9a-fA-F]{2})""".r.replaceAllIn(s, m => java.util.regex.Matcher.quoteReplacement(Integer.parseInt(m.group(1), 16).toChar.toString))
    """p\.(?:set\("([^"]*)","([^"]*)"\)|delete\("([^"]*)"\));""".r.findAllMatchIn(js)
      .map(m => if (m.group(1) != null) unescaped(m.group(1)) -> Some(unescaped(m.group(2))) else unescaped(m.group(3)) -> None).toSeq
  }

  /** A table in a page loaded with `uri`: what it renders, and the page's context. */
  private def loaded(uri: String, table: PeopleTable = new PeopleTable()): (PeopleTable, Elem, FSContext) = {
    val fsc = PerfFS.newPage(uri).rootFSContext
    (table, table.render()(using fsc), fsc)
  }

  "A paginated table persisted in the query string" should "be as its defaults without parameters" in {
    val (t, elem, fsc) = loaded("/people")
    given FSContext = fsc
    assert(ids(elem) == (1 to 10))
    assert(t.currentPage() == 0 && t.currentPageSize() == 10)
    assert(changes(t.rerender().cmd).isEmpty, "nothing differs from the defaults: nothing to put in the URL")
  }

  it should "load the page and the page size from its own parameters" in {
    val (t, elem, fsc) = loaded("/people?people-page=2&people-size=5&page=4&other-page=3")
    given FSContext = fsc
    assert(t.currentPage() == 1 && t.currentPageSize() == 5)
    assert(ids(elem) == (6 to 10))
    assert(ids(loaded("/people?people-page=3")._2) == (21 to 25))
    assert(ids(loaded("/people?people-size=20")._2) == (1 to 20))
  }

  it should "ignore what's not valid" in {
    for (uri <- Seq("/people?people-page=abc&people-size=xyz", "/people?people-page=0", "/people?people-page=-2", "/people?people-size=7", "/people?people-size=1000000",
      "/people?people-page=%ZZ", "/people?people-page=", "/people?people-page")) {
      val (t, elem, fsc) = loaded(uri)
      given FSContext = fsc
      assert(t.currentPage() == 0 && t.currentPageSize() == 10, uri)
      assert(ids(elem) == (1 to 10), uri)
    }
    assert(ids(loaded("/people?people-page=99")._2).isEmpty, "a page past the end: no rows, not a failure")
  }

  it should "use the table's own defaults, where the URL has nothing" in {
    val (t, elem, fsc) = loaded("/people?people-page=2", new PeopleTable(pageSize = 5))
    given FSContext = fsc
    assert(t.currentPageSize() == 5 && ids(elem) == (6 to 10))
    assert(ids(loaded("/people", new PeopleTable(pageSize = 5, startPage = 2))._2) == (11 to 15))
    assert(ids(loaded("/people?people-size=20", new PeopleTable(pageSize = 5))._2) == (1 to 20), "what's in the URL wins over the table's default")
  }

  it should "put the page and the page size in the URL when the table is rerendered" in {
    val (t, _, fsc) = loaded("/people")
    given FSContext = fsc
    t.currentPage() = 2
    val js = t.rerender().cmd
    assert(changes(js) == Seq("people-page" -> Some("3")))
    assert(js.contains("window.history.replaceState(window.history.state,'',u.href)"))

    t.currentPageSize() = 5
    t.currentPage() = 0
    assert(changes(t.rerender().cmd) == Seq("people-page" -> None, "people-size" -> Some("5")))
    assert(changes(t.rerender().cmd).isEmpty, "nothing changed since")
  }

  it should "take out of the URL what's back to the table's defaults" in {
    val (t, _, fsc) = loaded("/people?people-page=3&people-size=5&q=x")
    given FSContext = fsc
    assert(changes(t.rerender().cmd).isEmpty, "as loaded: the URL has it already")
    t.currentPage() = 0
    t.currentPageSize() = 10
    val js = t.rerender().cmd
    assert(changes(js) == Seq("people-page" -> None, "people-size" -> None))
    assert(!js.contains("\"q\""), "other parameters are left alone")
  }

  it should "compare with the table's own defaults" in {
    val (t, _, fsc) = loaded("/people", new PeopleTable(pageSize = 5, startPage = 1))
    given FSContext = fsc
    assert(changes(t.rerender().cmd).isEmpty)
    t.currentPage() = 0
    t.currentPageSize() = 10
    assert(changes(t.rerender().cmd) == Seq("people-page" -> Some("1"), "people-size" -> Some("10")))
  }

  it should "load back what it wrote" in {
    val (t, _, fsc) = loaded("/people")
    given FSContext = fsc
    t.currentPage() = 3
    t.currentPageSize() = 5
    val query = changes(t.rerender().cmd).collect({ case (k, Some(v)) => s"$k=$v" }).mkString("&")
    assert(query == "people-page=4&people-size=5")
    assert(ids(loaded(s"/people?$query")._2) == (16 to 20))
  }

  it should "write what its pagination buttons change" in {
    val (t, elem, fsc) = loaded("/people")
    given FSContext = fsc
    // The "»" button's callback moves to the next page and rerenders: the response carries the URL change.
    t.currentPage() = t.currentPage() + 1
    assert(changes(t.rerender().cmd) == Seq("people-page" -> Some("2")))
    assert(ids(t.render()) == (11 to 20))
  }

  it should "use other names, when given" in {
    val t = new PeopleTable() {
      override def persistedPagePropName: String = "p"

      override def persistedPageSizePropName: String = "n"
    }
    val (_, elem, fsc) = loaded("/people?people-p=2&people-n=5&people-page=3", t)
    given FSContext = fsc
    assert(ids(elem) == (6 to 10))
    t.currentPage() = 0
    assert(changes(t.rerender().cmd) == Seq("people-p" -> None))
  }

  "Tables in the same page" should "each have their own page" in {
    val fsc = PerfFS.newPage("/x?a-page=2&b-page=3&b-size=5").rootFSContext
    given FSContext = fsc
    val (a, b) = (new PeopleTable("a"), new PeopleTable("b"))
    assert(ids(a.render()) == (11 to 20))
    assert(ids(b.render()) == (11 to 15))
    a.currentPage() = 0
    assert(changes(a.rerender().cmd) == Seq("a-page" -> None))
    assert(changes(b.rerender().cmd).isEmpty)
  }
}
