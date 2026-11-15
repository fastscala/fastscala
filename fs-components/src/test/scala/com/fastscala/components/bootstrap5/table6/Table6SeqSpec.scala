package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.langpack.FSComponentsLangPack
import com.fastscala.core.FSContext
import com.fastscala.testutils.PerfFS
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.{Elem, Node}

/** Table6 over a Seq: paginated (Table6PaginatedWithSeqDataSource) and sortable (Table6SortableWithSeqDataSource). */
class Table6SeqSpec extends AnyFlatSpec {

  import FSComponentsLangPack.Default.EN

  final case class Person(id: Int, name: String, age: Int)

  private val people: Seq[Person] = (1 to 25).map(i => Person(i, s"P${"%02d".format(26 - i)}", 20 + (i * 7) % 30))

  private class PeopleTable(pageSize: Int = 10, hideIfUnnecessary: Boolean = false, data: Seq[Person] = people)
    extends Table6Base with Table6BootrapStyling with Table6StandardColumns with Table6PaginatedWithSeqDataSource with Table6SortableWithSeqDataSource {
    override type R = Person

    val ColId = ColStr("Id", _.id.toString)
    val ColName = ColStr("Name", _.name)
    val ColAge = ColStr("Age", _.age.toString)

    override def columns(): List[C] = List(ColId, ColName, ColAge)

    override def seqRowsSource: Seq[Person] = data

    override def defaultPageSize: Int = pageSize

    override def hidePaginationBottomControlsIfUnnecessary: Boolean = hideIfUnnecessary

    override def rowsSorter: PartialFunction[C, Seq[Person] => Seq[Person]] = {
      case ColName => _.sortBy(_.name)
      case ColAge => _.sortBy(p => (p.age, p.id))
    }
  }

  private def elems(n: Node): Seq[Elem] = n match {
    case e: Elem => e +: e.child.flatMap(elems)
    case _ => Nil
  }

  /** The text of the rows' cells, row by row. */
  private def bodyRows(table: Elem): Seq[Seq[String]] =
    elems(table).filter(_.label == "tbody").flatMap(_.child.collect { case tr: Elem if tr.label == "tr" => tr.child.collect { case td: Elem => td.text.trim } })

  private def ids(table: Elem): Seq[Int] = bodyRows(table).map(_.head.toInt)

  private def newRoot(): FSContext = PerfFS.newPage().rootFSContext

  "A paginated table" should "show the first page, of the page size" in {
    given FSContext = newRoot()
    val t = new PeopleTable()
    val rendered = t.render()
    assert(ids(rendered) == (1 to 10))
    assert(bodyRows(rendered).head == Seq("1", "P25", "27"))
  }

  it should "show the page asked for, and a shorter last page" in {
    given FSContext = newRoot()
    val t = new PeopleTable()
    t.currentPage() = 1
    assert(ids(t.render()) == (11 to 20))
    t.currentPage() = 2
    assert(ids(t.render()) == (21 to 25))
  }

  it should "know how many pages there are (maxPages is the last page's index)" in {
    val t = new PeopleTable()
    given KnownTotalNumberOfRowsMaterialized = t.knownTotalNumberOfRows()
    assert(summon[KnownTotalNumberOfRowsMaterialized].value.contains(25))
    assert(t.maxPages == 2)
    assert(new PeopleTable(pageSize = 25).maxPages(using KnownTotalNumberOfRowsMaterialized(Some(25))) == 0)
    assert(new PeopleTable(pageSize = 5).maxPages(using KnownTotalNumberOfRowsMaterialized(Some(25))) == 4)
    assert(new PeopleTable().maxPages(using KnownTotalNumberOfRowsMaterialized(Some(0))) == 0)
  }

  it should "show the pages around the current one" in {
    val t = new PeopleTable(pageSize = 1)
    given KnownTotalNumberOfRowsMaterialized = KnownTotalNumberOfRowsMaterialized(Some(25))
    assert(t.visiblePages() == (0 to 6).toList)
    t.currentPage() = 10
    assert(t.visiblePages() == (7 to 13).toList)
    t.currentPage() = 24
    assert(t.visiblePages() == (21 to 24).toList)
  }

  it should "page with offset and limit hints" in {
    val t = new PeopleTable()
    t.currentPage() = 2
    assert(t.rowsHints().collect { case h: PagingRowsHint => h } == Seq(PagingRowsHint(20, 10)))
  }

  it should "render the pagination controls" in {
    given FSContext = newRoot()
    val html = new PeopleTable().render().toString
    assert(html.contains(">«<") && html.contains(">»<") && html.contains(">3<"), html.take(3000))
  }

  it should "hide the pagination controls, when asked to, if there's a single page" in {
    given FSContext = newRoot()
    assert(!new PeopleTable(pageSize = 100, hideIfUnnecessary = true).render().toString.contains(">«<"))
    assert(new PeopleTable(pageSize = 5, hideIfUnnecessary = true).render().toString.contains(">«<"))
  }

  it should "show the pagination controls when there are two pages" in {
    // Regression (fixed): they're hidden if maxPages <= 1, but maxPages is the last page's index: with 2 pages, it's 1.
    given FSContext = newRoot()
    assert(new PeopleTable(pageSize = 15, hideIfUnnecessary = true).render().toString.contains(">«<"))
  }

  "A sortable table" should "sort by the column, ascending or descending, before paging" in {
    given FSContext = newRoot()
    val t = new PeopleTable()
    t.currentSortCol() = Some(t.ColName)
    t.currentSortAsc() = true
    assert(bodyRows(t.render()).map(_(1)) == (1 to 10).map(i => "P%02d".format(i)))
    t.currentSortAsc() = false
    assert(bodyRows(t.render()).map(_(1)) == (25 to 16 by -1).map(i => "P%02d".format(i)))
  }

  it should "leave the rows as they are for a column it can't sort by" in {
    given FSContext = newRoot()
    val t = new PeopleTable()
    assert(!t.isSortable(t.ColId) && t.isSortable(t.ColName))
    t.currentSortCol() = Some(t.ColId)
    t.currentSortAsc() = false
    assert(ids(t.render()) == (1 to 10))
  }

  it should "show which column it's sorted by, and how" in {
    given FSContext = newRoot()
    val t = new PeopleTable()
    val unsorted = t.render().toString
    assert(unsorted.contains("bi-chevron-expand") && !unsorted.contains("bi-chevron-double"))
    t.currentSortCol() = Some(t.ColAge)
    assert(t.render().toString.contains("bi-chevron-double-down"))
    t.currentSortAsc() = false
    assert(t.render().toString.contains("bi-chevron-double-up"))
  }

  it should "sort by a column when its header is clicked, and reverse the order on a second click" in {
    val root = newRoot()
    given FSContext = root
    val t = new PeopleTable()
    t.render()
    val page = root.page
    def clickHeader(label: String): Unit = {
      val rendered = t.render()
      val th = elems(rendered).find(e => e.label == "th" && e.text.trim == label).get
      val onclick = th.attribute("onclick").get.text
      val funcId = """window\._fs\.callback\('',"[^"]+","([^"]+)"""".r.findFirstMatchIn(onclick).get.group(1)
      page.callbacks(funcId).func("")
    }
    clickHeader("Age")
    assert(t.currentSortCol().contains(t.ColAge) && t.currentSortAsc())
    clickHeader("Age")
    assert(t.currentSortCol().contains(t.ColAge) && !t.currentSortAsc())
    clickHeader("Name")
    assert(t.currentSortCol().contains(t.ColName) && t.currentSortAsc())
  }

  "An empty table" should "render no rows" in {
    given FSContext = newRoot()
    assert(bodyRows(new PeopleTable(data = Nil).render()).isEmpty)
  }
}
