package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.helpers.BSHelpers.RichElemBootstrapClasses
import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.core.{FSContext, FSPage}
import com.fastscala.js.Js
import com.fastscala.scala_xml.xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.utils.{Lazy, LazyWithPage}

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

trait Table6Paginated extends Table6Base {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def defaultNumberOfAdditionalPagesEachSide = 3

  def defaultCurrentPage(implicit fsPage: FSPage): Int = 0

  def defaultCurrentPageFinal(implicit fsPage: FSPage): Int = defaultCurrentPage

  def defaultPageSize(implicit fsPage: FSPage): Int = 100

  def defaultPageSizeFinal(implicit fsPage: FSPage): Int = defaultPageSize

  def visiblePageSizes: List[Int] = List(10, 20, 50, 100, 500)

  def hidePaginationBottomControlsIfUnnecessary: Boolean = false

  lazy val currentPageSize = LazyWithPage(implicit fsPage => defaultPageSizeFinal)

  lazy val currentPage = LazyWithPage(implicit fsPage => defaultCurrentPageFinal)

  def maxPages(implicit fsc: FSContext, knownTotalNumberOfRowsMat: KnownTotalNumberOfRowsMaterialized): Int = knownTotalNumberOfRowsMat.value.map(n => (n - 1) / currentPageSize()(using fsc.page)).getOrElse(currentPage()(using fsc.page) + 10)

  override def rowsHints()(implicit fsc: FSContext): Seq[RowsHint] = super.rowsHints() :+ PagingRowsHint(
    offset = currentPage()(using fsc.page) * currentPageSize()(using fsc.page)
    , limit = currentPageSize()(using fsc.page)
  )

  def visiblePages()(implicit fsc: FSContext, knownTotalNumberOfRowsMat: KnownTotalNumberOfRowsMaterialized): List[Int] =
    (math.max(0, currentPage()(using fsc.page) - defaultNumberOfAdditionalPagesEachSide) to math.min(maxPages, (currentPage()(using fsc.page) + (defaultNumberOfAdditionalPagesEachSide * 2)))).take(defaultNumberOfAdditionalPagesEachSide * 2 + 1)
      .toList
      .filter(_ >= 0)

  var onTableSizeInfoTransforms: Elem => Elem = _.text_center.align_content_center

  def onTableSizeInfo(f: Elem => Elem): this.type = mutate {
    onTableSizeInfoTransforms = onTableSizeInfoTransforms.pipe(onTableSizeInfoTransforms => elem => f(onTableSizeInfoTransforms(elem)))
  }

  var onPaginationButtonsTransforms: Elem => Elem = _.btn_sm

  def onPaginationButtons(f: Elem => Elem): this.type = mutate {
    onPaginationButtonsTransforms = onPaginationButtonsTransforms.pipe(onPaginationButtonsTransforms => elem => f(onPaginationButtonsTransforms(elem)))
  }

  var onPageSizeSelectTransforms: Elem => Elem = _.form_select_sm.addStyle("max-width: 200px; float: right;")

  def onPageSizeSelect(f: Elem => Elem): this.type = mutate {
    onPageSizeSelectTransforms = onPageSizeSelectTransforms.pipe(onPageSizeSelectTransforms => elem => f(onPageSizeSelectTransforms(elem)))
  }

  def onSetCurrentPageServerSide(page: Int)(implicit fsc: FSContext): Js = {
    currentPage() = page
    rerender()
  }

  def onChangeCurrentPageSizeServerSide(pageSize: Int)(implicit fsc: FSContext): Js = {
    currentPageSize() = pageSize
    currentPage() = 0
    rerender()
  }

  def renderPagesButtons()(implicit fsc: FSContext, knownTotalNumberOfRowsMat: KnownTotalNumberOfRowsMaterialized): Elem = div.d_grid.d_flex.mb_3.mx_3.gap_1.apply {
    BSBtn().BtnLight.lbl("«")
      .callback(implicit fsc => onSetCurrentPageServerSide(math.max(0, currentPage()(using fsc.page) - 1)))
      .btn.pipe(onPaginationButtonsTransforms) ++
      visiblePages().map(page => {
        (if (currentPage()(using fsc.page) == page) BSBtn().BtnPrimary else BSBtn().BtnLight)
          .lbl((page + 1).toString)
          .callback(implicit fsc => onSetCurrentPageServerSide(page))
          .btn.pipe(onPaginationButtonsTransforms)
      }).mkNS ++
      BSBtn().BtnLight.lbl("»")
        .callback(implicit fsc => onSetCurrentPageServerSide(math.min(maxPages, currentPage()(using fsc.page) + 1)))
        .btn.pipe(onPaginationButtonsTransforms)
  }

  def renderPageSizeDropdown()(implicit fsc: FSContext, knownTotalNumberOfRowsMat: KnownTotalNumberOfRowsMaterialized): Elem = {
    ImmediateInputFields.select[Int](
      () => visiblePageSizes,
      () => currentPageSize()(using fsc.page),
      pageSize => onChangeCurrentPageSizeServerSide(pageSize)
    ).mb_3.mx_3.pipe(onPageSizeSelectTransforms)
  }

  def renderTableSizeInfo(from: Int, to: Int, pageSize: Int, total: Option[Int])(implicit fsc: FSContext, knownTotalNumberOfRowsMat: KnownTotalNumberOfRowsMaterialized): NodeSeq = {
    langPack.tableSizeInfo(from, to, pageSize, total).map(mesg => <div>{mesg}</div>.pipe(onTableSizeInfoTransforms)).getOrElse(NodeSeq.Empty)
  }

  def renderTableSizeInfo()(implicit fsc: FSContext, knownTotalNumberOfRowsMat: KnownTotalNumberOfRowsMaterialized): NodeSeq = {
    knownTotalNumberOfRowsMat.value.map(knownTotalNumberOfRows => {
      val from = currentPage()(using fsc.page) * currentPageSize()(using fsc.page) + 1
      val to = math.min(knownTotalNumberOfRows, (currentPage()(using fsc.page) + 1) * currentPageSize()(using fsc.page))
      renderTableSizeInfo(from, to, to - from + 1, Some(knownTotalNumberOfRows))
    }).getOrElse(renderTableSizeInfo(currentPage()(using fsc.page) * currentPageSize()(using fsc.page) + 1, (currentPage()(using fsc.page) + 1) * currentPageSize()(using fsc.page), currentPageSize()(using fsc.page), None))
  }

  def renderPaginationBottomControls()(implicit fsc: FSContext, knownTotalNumberOfRowsMat: KnownTotalNumberOfRowsMaterialized): NodeSeq = {
    // (maxPages is the last page's index: 0 when there's a single page.)
    if (hidePaginationBottomControlsIfUnnecessary && maxPages < 1) NodeSeq.Empty
    else {
      row.apply {
        col.apply(renderPagesButtons()) ++
          col.apply(renderTableSizeInfo()) ++
          col.apply(renderPageSizeDropdown())
      }
    }
  }

  override def transformTableWrapperElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)], knownTotalNumberOfRows: KnownTotalNumberOfRowsMaterialized): Elem =
    super.transformTableWrapperElem(elem).withAppendedToContents(renderPaginationBottomControls())
}
