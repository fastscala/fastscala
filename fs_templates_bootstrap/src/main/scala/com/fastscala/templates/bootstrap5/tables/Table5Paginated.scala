package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.{FSContext, FSXmlEnv}
import com.fastscala.templates.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.utils.Lazy

trait Table5Paginated[E <: FSXmlEnv] extends Table5Base[E] {

  import com.fastscala.core.FSXmlUtils._
  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  def defaultNumberOfAdditionalPagesEachSide = 2

  def defaultCurrentPage = 0

  def defaultPageSize = 100

  def visiblePageSizes: List[Int] = List(10, 20, 50, 100, 500)

  lazy val currentPageSize = Lazy(defaultPageSize)

  lazy val currentPage = Lazy(defaultCurrentPage)

  override def rowsHints(): Seq[RowsHint] = super.rowsHints() :+ PagingRowsHint(
    offset = currentPage() * currentPageSize()
    , limit = currentPageSize()
  )

  def visiblePages(): List[Int] =
    (math.max(0, currentPage() - defaultNumberOfAdditionalPagesEachSide) to (currentPage() + (defaultNumberOfAdditionalPagesEachSide * 2))).take(defaultNumberOfAdditionalPagesEachSide * 2 + 1)
      .toList
      .filter(_ >= 0)

  def renderPagesButtons()(implicit fsc: FSContext): E#Elem = div.d_grid.d_flex.mb_3.mx_3.gap_1.apply {
    BSBtn().BtnLight.lbl("«").ajax(implicit fsc => {
      currentPage() = math.max(0, currentPage() - 1)
      rerenderTableAround()
    }).btn ++
      visiblePages().map(page => {
        (if (currentPage() == page) BSBtn().BtnPrimary else BSBtn().BtnLight)
          .lbl((page + 1).toString)
          .ajax(implicit fsc => {
            currentPage() = page
            rerenderTableAround()
          })
          .btn
      }).mkNS() ++
      BSBtn().BtnLight.lbl("»").ajax(implicit fsc => {
        currentPage() = currentPage() + 1
        rerenderTableAround()
      }).btn
  }

  def renderPageSizeDropdown()(implicit fsc: FSContext): E#Elem = {
    ImmediateInputFields.select[E, Int](
      () => visiblePageSizes,
      () => currentPageSize(),
      pageSize => {
        currentPageSize() = pageSize
        tableRenderer.rerenderer.rerender()
      },
      style = "max-width: 200px; float: right;"
    ).mb_3.mx_3
  }

  def renderPaginationBottomControls()(implicit fsc: FSContext): E#Elem = {
    row.apply {
      col.apply(renderPagesButtons()) ++
        col.apply(renderPageSizeDropdown())
    }
  }

  override def renderAroundContents()(implicit fsc: FSContext): E#NodeSeq = {
    super.renderAroundContents() ++
      renderPaginationBottomControls()
  }
}
