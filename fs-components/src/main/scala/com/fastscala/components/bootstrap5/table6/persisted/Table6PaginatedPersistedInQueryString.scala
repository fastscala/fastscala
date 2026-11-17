package com.fastscala.components.bootstrap5.table6.persisted

import com.fastscala.components.bootstrap5.table6.{Table6Paginated, Table6WithDataStorageInQueryString}
import com.fastscala.core.{FSContext, FSPage}
import com.fastscala.js.Js
import com.fastscala.utils.LazyWithPage

/**
 * A paginated table whose page and page size are kept in the query string of the page's URL (see
 * [[Table6WithDataStorageInQueryString]]), e.g. `?people-page=3&people-size=50` for the table named `people`: so
 * reloading the page, coming back to it or sharing its link shows the same page of the table.
 *
 * They're read from the URL the page was loaded with (if they're valid: a page from 1 on, a page size that's one of
 * [[visiblePageSizes]]; otherwise the table's defaults), and written to it with each [[rerender]] (as every change
 * of page or page size does). What's as the table's defaults isn't in the URL.
 */
trait Table6PaginatedPersistedInQueryString extends Table6Paginated with Table6WithDataStorageInQueryString {

  /** The name the page is kept under. (In the URL it's numbered from 1, as it's shown.) */
  def PropNamePersistedCurrentPage: String = "page"

  /** The name the page size is kept under. */
  def PropNamePersistedPageSize: String = "size"

  // (These, not the defaults, are overridden: a table that overrides defaultPageSize still gets what's in the URL.)
  override lazy val currentPageSize: LazyWithPage[Int] = LazyWithPage(implicit fsPage =>
    getProp(PropNamePersistedPageSize).flatMap(_.toIntOption).filter(visiblePageSizes.contains).getOrElse(defaultPageSize)
  )

  override lazy val currentPage: LazyWithPage[Int] = LazyWithPage(implicit fsPage =>
    getProp(PropNamePersistedCurrentPage).flatMap(_.toIntOption).filter(_ >= 1).getOrElse(defaultCurrentPage)
  )

  override def onSetCurrentPageServerSide(page: Int)(implicit fsc: FSContext): Js =
    super.onSetCurrentPageServerSide(page) &
      setProp(PropNamePersistedCurrentPage, currentPage().toString)(using fsc.page)

  override def onChangeCurrentPageSizeServerSide(pageSize: Int)(implicit fsc: FSContext): Js =
    super.onChangeCurrentPageSizeServerSide(pageSize) &
      setProp(PropNamePersistedPageSize, currentPageSize().toString)(using fsc.page)
}
