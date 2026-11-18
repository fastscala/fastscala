package com.fastscala.components.bootstrap5.table6.persisted

import com.fastscala.components.bootstrap5.table6.*
import com.fastscala.core.{FSContext, FSPage}
import com.fastscala.js.Js
import com.fastscala.utils.LazyWithPage

import scala.xml.Elem

/**
 * A table with column selection whose visible columns are kept in the query string of the page's URL (see
 * [[Table6WithDataStorageInQueryString]]), e.g. `?people-cols=Name,Capital` for the table named `people`: so
 * reloading the page, coming back to it or sharing its link shows the same columns.
 *
 * They're read from the URL the page was loaded with (the keys of the visible columns, see [[colLabel]],
 * separated by commas; keys of no column are ignored), and written to it each time a column is shown or hidden. If
 * the visible columns are the table's initial ones, they're not in the URL.
 */
trait Table6ColumnSelectionPersistedInQueryString extends Table6ColumnSelection with Table6WithDataStorageInQueryString {

  /** The name the visible columns' keys are kept under. */
  def PropNamePersistedSelectedCols: String = "cols"

  /** What separates the columns' keys: no key may contain it. */
  def persistedSelectedColsSeparator: String = ","

  private val selectedColumnsInQueryString: LazyWithPage[Set[String]] = LazyWithPage(page => {
    val inQueryString = getProp(PropNamePersistedSelectedCols)(using page).getOrElse("")
    inQueryString.split(java.util.regex.Pattern.quote(persistedSelectedColsSeparator)).toSet.filter(_ != "")
  })

  override def columnStartsVisibleFinal(c: C)(implicit page: FSPage): Boolean =
    if (selectedColumnsInQueryString().isEmpty) super.columnStartsVisibleFinal(c)(using page)
    else selectedColumnsInQueryString().contains(colLabel(c))

  override def onColumnSelectionChange()(implicit fsc: FSContext): Js = {
    val visibleCols = allColumns.filter(currentSelectedCols()(using fsc.page).contains)
    setProp(
      PropNamePersistedSelectedCols,
      Some(visibleCols.map(colLabel).mkString(persistedSelectedColsSeparator))
        .filter(_ => visibleCols != allColumns.filter(columnStartsVisible(_)(using fsc.page)))
    )(using fsc.page)
  }
}
