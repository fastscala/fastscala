package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.helpers.BSHelpers.RichElemBootstrapClasses
import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.core.FSContext
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.utils.Lazy

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

trait Table6Paginated extends Table6Base {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def defaultNumberOfAdditionalPagesEachSide = 3

  def defaultCurrentPage = 0

  def defaultPageSize = 100

  def visiblePageSizes: List[Int] = List(10, 20, 50, 100, 500)

  def hidePaginationBottomControlsIfUnnecessary: Boolean = false

  lazy val currentPageSize = Lazy(defaultPageSize)

  lazy val currentPage = Lazy(defaultCurrentPage)

  def maxPages: Int = knownTotalNumberOfRows().map(n => (n - 1) / currentPageSize()).getOrElse(currentPage() + 10)

  override def rowsHints(): Seq[RowsHint] = super.rowsHints() :+ PagingRowsHint(
    offset = currentPage() * currentPageSize()
    , limit = currentPageSize()
  )

  def visiblePages(): List[Int] =
    (math.max(0, currentPage() - defaultNumberOfAdditionalPagesEachSide) to math.min(maxPages, (currentPage() + (defaultNumberOfAdditionalPagesEachSide * 2)))).take(defaultNumberOfAdditionalPagesEachSide * 2 + 1)
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

  var onPageSizeSelectTransforms: Elem => Elem = _.form_select_sm.withStyle("max-width: 200px; float: right;")

  def onPageSizeSelect(f: Elem => Elem): this.type = mutate {
    onPageSizeSelectTransforms = onPageSizeSelectTransforms.pipe(onPageSizeSelectTransforms => elem => f(onPageSizeSelectTransforms(elem)))
  }

  def renderPagesButtons()(implicit fsc: FSContext): Elem = div.d_grid.d_flex.mb_3.mx_3.gap_1.apply {
    BSBtn().BtnLight.lbl("«").callback(implicit fsc => {
      currentPage() = math.max(0, currentPage() - 1)
      rerender()
    }).btn.pipe(onPaginationButtonsTransforms) ++
      visiblePages().map(page => {
        (if (currentPage() == page) BSBtn().BtnPrimary else BSBtn().BtnLight)
          .lbl((page + 1).toString)
          .callback(implicit fsc => {
            currentPage() = page
            rerender()
          })
          .btn.pipe(onPaginationButtonsTransforms)
      }).mkNS ++
      BSBtn().BtnLight.lbl("»").callback(implicit fsc => {
        currentPage() = math.min(maxPages, currentPage() + 1)
        rerender()
      }).btn.pipe(onPaginationButtonsTransforms)
  }

  def renderPageSizeDropdown()(implicit fsc: FSContext): Elem = {
    ImmediateInputFields.select[Int](
      () => visiblePageSizes,
      () => currentPageSize(),
      pageSize => {
        currentPageSize() = pageSize
        currentPage() = 0
        rerender()
      }
    ).mb_3.mx_3.pipe(onPageSizeSelectTransforms)
  }

  def renderTableSizeInfo(from: Int, to: Int, pageSize: Int, total: Option[Int])(implicit fsc: FSContext): NodeSeq = {
    total.map(total => <div>Showing {pageSize} of {total}</div>.pipe(onTableSizeInfoTransforms)).getOrElse(NodeSeq.Empty)
  }

  def renderTableSizeInfo()(implicit fsc: FSContext): NodeSeq = {
    knownTotalNumberOfRows().map(knownTotalNumberOfRows => {
      val from = currentPage() * currentPageSize() + 1
      val to = math.min(knownTotalNumberOfRows, (currentPage() + 1) * currentPageSize())
      renderTableSizeInfo(from, to, to - from + 1, Some(knownTotalNumberOfRows))
    }).getOrElse(renderTableSizeInfo(currentPage() * currentPageSize() + 1, (currentPage() + 1) * currentPageSize(), currentPageSize(), None))
  }

  def renderPaginationBottomControls()(implicit fsc: FSContext): NodeSeq = {
    if (hidePaginationBottomControlsIfUnnecessary && maxPages <= 1) NodeSeq.Empty
    else {
      row.apply {
        col.apply(renderPagesButtons()) ++
          col.apply(renderTableSizeInfo()) ++
          col.apply(renderPageSizeDropdown())
      }
    }
  }

  override def transformTableWrapperElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)], knownTotalNumberOfRows: Option[Int]): Elem =
    super.transformTableWrapperElem(elem).withAppendedToContents(renderPaginationBottomControls())
}
