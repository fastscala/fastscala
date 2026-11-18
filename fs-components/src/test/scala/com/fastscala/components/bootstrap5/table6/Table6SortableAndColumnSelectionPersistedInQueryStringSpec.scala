package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.table6.persisted.*
import com.fastscala.components.langpack.FSComponentsLangPack
import com.fastscala.core.{FSContext, FSPage}
import com.fastscala.testutils.PerfFS
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.{Elem, Node}

/** [[Table6SortablePersistedInQueryString]] and [[Table6ColumnSelectionPersistedInQueryString]]. */
class Table6SortableAndColumnSelectionPersistedInQueryStringSpec extends AnyFlatSpec {

  import FSComponentsLangPack.Default.EN

  private class PeopleTable
    extends Table6Base with Table6BootrapStyling with Table6StandardColumns with Table6SortableWithSeqDataSource
      with Table6ColumnSelection with Table6SortablePersistedInQueryString with Table6ColumnSelectionPersistedInQueryString {
    override type R = (Int, String)

    override def tableName: String = "people"

    val ColId = ColStr("Id", _._1.toString)
    val ColName = ColStr("Name", _._2)
    val ColNotes = ColStr("Notes", _ => "-")

    override lazy val allColumns: List[C] = List(ColId, ColName, ColNotes)

    override lazy val initiallyVisibleColumns: Set[C] = Set(ColId, ColName)

    override def defaultSortCol(implicit page: FSPage): Option[C] = Some(ColId)

    override def rowsSorter: PartialFunction[C, Seq[R] => Seq[R]] = {
      case ColId => _.sortBy(_._1)
      case ColName => _.sortBy(_._2)
    }

    override def seqRowsSource: Seq[R] = Seq(2 -> "b", 1 -> "c", 3 -> "a")
  }

  private def elems(n: Node): Seq[Elem] = n match {
    case e: Elem => e +: e.child.flatMap(elems)
    case _ => Nil
  }

  private def rows(table: Elem): Seq[Seq[String]] =
    elems(table).filter(_.label == "tbody").flatMap(_.child.collect { case tr: Elem if tr.label == "tr" => tr.child.collect { case td: Elem => td.text.trim } })

  /** What the JavaScript sets and deletes in the URL, in order (the string literals' escapes read as JavaScript does). */
  private def changes(js: String): Seq[(String, Option[String])] = {
    def unescaped(s: String) = """\\x([0-9a-fA-F]{2})""".r.replaceAllIn(s, m => java.util.regex.Matcher.quoteReplacement(Integer.parseInt(m.group(1), 16).toChar.toString))
    """p\.(?:set\("([^"]*)","([^"]*)"\)|delete\("([^"]*)"\));""".r.findAllMatchIn(js)
      .map(m => if (m.group(1) != null) unescaped(m.group(1)) -> Some(unescaped(m.group(2))) else unescaped(m.group(3)) -> None).toSeq
  }

  private def loaded(uri: String): (PeopleTable, Elem, FSContext) = {
    val fsc = PerfFS.newPage(uri).rootFSContext
    val table = new PeopleTable()
    (table, table.render()(using fsc), fsc)
  }

  "A table persisted in the query string" should "be as its defaults without parameters" in {
    val (_, elem, _) = loaded("/people")
    assert(rows(elem) == Seq(Seq("1", "c"), Seq("2", "b"), Seq("3", "a")))
  }

  it should "load the sort column and direction" in {
    assert(rows(loaded("/people?people-sort=Name")._2).map(_.head) == Seq("3", "2", "1"))
    assert(rows(loaded("/people?people-sort=Name:desc")._2).map(_.head) == Seq("1", "2", "3"))
    assert(rows(loaded("/people?people-sort=Id:desc")._2).map(_.head) == Seq("3", "2", "1"))
    assert(rows(loaded("/people?people-sort=Name:asc")._2).map(_.head) == Seq("3", "2", "1"))
  }

  it should "ignore a sort that's not valid" in {
    for (uri <- Seq("/people?people-sort=Nope", "/people?people-sort=Notes", "/people?people-sort=Name:up", "/people?people-sort=:desc", "/people?sort=Name"))
      assert(rows(loaded(uri)._2).map(_.head) == Seq("1", "2", "3"), uri)
  }

  it should "write the sort column and direction when a header is clicked" in {
    val (t, _, fsc) = loaded("/people")
    given FSContext = fsc
    assert(changes(t.onSortColClickedServerSide(t.ColName).cmd) == Seq("people-sort" -> Some("Name:asc")))
    assert(changes(t.onSortColClickedServerSide(t.ColName).cmd) == Seq("people-sort" -> Some("Name:desc")))
    assert(changes(t.onSortColClickedServerSide(t.ColId).cmd) == Seq("people-sort" -> Some("Id:asc")))
    assert(rows(t.render()).map(_.head) == Seq("1", "2", "3"))
  }

  it should "load the visible columns" in {
    assert(rows(loaded("/people?people-cols=Name,Notes,Nope")._2) == Seq(Seq("c", "-"), Seq("b", "-"), Seq("a", "-")))
    assert(rows(loaded("/people?people-cols=Notes&people-sort=Name")._2).size == 3, "sorted by a column that's not visible")
  }

  it should "write the visible columns when they change, except when they're the initial ones" in {
    val (t, _, fsc) = loaded("/people")
    given FSContext = fsc
    def select(col: t.C, selected: Boolean): String = {
      if (selected) t.currentSelectedCols()(using fsc.page) += col else t.currentSelectedCols()(using fsc.page) -= col
      t.onColumnSelectionChange().cmd
    }
    assert(changes(select(t.ColNotes, true)) == Seq("people-cols" -> Some("Id,Name,Notes")))
    assert(changes(select(t.ColId, false)) == Seq("people-cols" -> Some("Name,Notes")))
    select(t.ColId, true)
    assert(changes(select(t.ColNotes, false)) == Seq("people-cols" -> None))
    assert(rows(t.render()) == Seq(Seq("1", "c"), Seq("2", "b"), Seq("3", "a")))
  }

  it should "keep in the URL columns that were loaded from it" in {
    val (t, _, fsc) = loaded("/people?people-cols=Name,Notes")
    given FSContext = fsc
    t.currentSelectedCols()(using fsc.page) += t.ColId
    assert(changes(t.onColumnSelectionChange().cmd) == Seq("people-cols" -> Some("Id,Name,Notes")))
    t.currentSelectedCols()(using fsc.page) -= t.ColId
    assert(changes(t.onColumnSelectionChange().cmd) == Seq("people-cols" -> Some("Name,Notes")))
  }

  it should "load the visible columns also in a table with its own columnStartsVisible" in {
    val fsc = PerfFS.newPage("/people?people-cols=Notes").rootFSContext
    val t = new PeopleTable() {
      override def columnStartsVisible(c: C)(implicit page: FSPage): Boolean = c == ColName
    }
    assert(rows(t.render()(using fsc)) == Seq(Seq("-"), Seq("-"), Seq("-")))
    val t2 = new PeopleTable() {
      override def columnStartsVisible(c: C)(implicit page: FSPage): Boolean = c == ColName
    }
    assert(rows(t2.render()(using PerfFS.newPage("/people").rootFSContext)) == Seq(Seq("c"), Seq("b"), Seq("a")))
  }

  it should "load the columns for the selection dropdown, also if it's rendered before the table" in {
    val fsc = PerfFS.newPage("/people?people-cols=Notes").rootFSContext
    given FSContext = fsc
    val t = new PeopleTable()
    t.columnSelectionDropdown()
    assert(t.visibleColumns() == List(t.ColNotes))
  }
}
