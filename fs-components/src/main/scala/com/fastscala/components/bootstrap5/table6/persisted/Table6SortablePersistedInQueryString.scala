package com.fastscala.components.bootstrap5.table6.persisted

import com.fastscala.components.bootstrap5.table6.*
import com.fastscala.core.{FSContext, FSPage}
import com.fastscala.js.Js
import com.fastscala.utils.LazyWithPage
import com.typesafe.scalalogging.LazyLogging

/**
 * A sortable table whose sort column and direction are kept in the query string of the page's URL (see
 * [[Table6WithDataStorageInQueryString]]), e.g. `?people-sort=Name:desc` for the table named `people`: so reloading
 * the page, coming back to it or sharing its link shows the table sorted the same way.
 *
 * They're read from the URL the page was loaded with (the label of a column, see [[colLabel]], then `:asc` or `:desc`;
 * what's not valid falls back to the table's defaults), and written to it each time the sort column or direction
 * changes.
 */
trait Table6SortablePersistedInQueryString extends Table6Sortable with Table6WithDataStorageInQueryString with LazyLogging {

  /** The name the sort column and direction are kept under, as `<column label>:<asc|desc>`. */
  def PropNamePersistedSortCol: String = "sort"

  /** The sort column and direction that are in the query string (e.g. `sort=price:desc`), if there's a column with that label. */
  def getSortColumnAndDirFromQueryString(implicit page: FSPage): Option[(C, SortAsc)] =
    getProp(PropNamePersistedSortCol)(using page).flatMap(value => {
      // (Split at the last colon: a label may have colons of its own. Without a direction, it's the column's default.)
      val (label, ascOpt) = value.lastIndexOf(':') match {
        case -1 => (value, None)
        case idx => value.substring(idx + 1) match {
          case "asc" => (value.substring(0, idx), Some(true))
          case "desc" => (value.substring(0, idx), Some(false))
          case _ => (value, None)
        }
      }
      columns()(using page).find(col => isSortable(col) && colLabel(col) == label) match {
        case Some(col) => Some((col, ascOpt.getOrElse(defaultSortAsc(col))))
        case None =>
          logger.info(s"Should sort by $label, but there's no sortable column with that label.")
          None
      }
    })

  override def defaultSortColFinal(implicit page: FSPage): Option[C] =
    getSortColumnAndDirFromQueryString.map(_._1).orElse(super.defaultSortColFinal)

  // (The direction in the URL is the one of the sort column in the URL: another column gets its own default.)
  override def defaultSortAscFinal(col: C)(implicit page: FSPage): SortAsc =
    getSortColumnAndDirFromQueryString.collect({ case (`col`, asc) => asc }).getOrElse(super.defaultSortAscFinal(col))

  override def onSortColClickedServerSide(col: C)(implicit fsc: FSContext): Js =
    super.onSortColClickedServerSide(col) &
      setProp(
        PropNamePersistedSortCol,
        currentSortCol()(using fsc.page).map(col => s"${colLabel(col)}:${if (currentSortAsc()(using fsc.page)) "asc" else "desc"}")
      )(using fsc.page)
}
