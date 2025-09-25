package com.fastscala.components.bootstrap5.toast

import com.fastscala.components.bootstrap5.helpers.ClassEnrichableMutable
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.{JS, printBeforeExec}
import com.fastscala.utils.IdGen

import scala.xml.{Elem, NodeSeq}

trait BSToast2WithHeaderContentLeftIcon extends Mutable {

  var _headerContentLeftIcon: () => Option[Elem] = () => None
  var _headerContentLeftIconTransforms: Elem => Elem = identity[Elem]

  def headerContentLeftIcon: Option[Elem] = _headerContentLeftIcon()

  def headerContentLeftIcon(v: Option[Elem]): this.type = mutate({ _headerContentLeftIcon = () => v })

  def headerContentLeftIcon(v: Elem): this.type = mutate({ _headerContentLeftIcon = () => Some(v) })

  def onHeaderContentLeftIcon(f: Elem => Elem): this.type = mutate {
    _headerContentLeftIconTransforms = _headerContentLeftIconTransforms andThen f
  }
}

trait BSToast2WithHeaderContentTitle extends Mutable {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*
  var _headerContentTitle: () => Option[Elem] = () => None
  var _headerContentTitleTransforms: Elem => Elem = _.fw_bold.me_auto

  def headerContentTitle: Option[Elem] = _headerContentTitle()

  def headerContentTitle(v: Option[Elem]): this.type = mutate({ _headerContentTitle = () => v })

  def headerContentTitle(v: Elem): this.type = mutate({ _headerContentTitle = () => Some(v) })

  def headerContentTitle(v: String): this.type = mutate({ _headerContentTitle = () => Some(<span>{v}</span>) })

  def onHeaderContentTitle(f: Elem => Elem): this.type = mutate {
    _headerContentTitleTransforms = _headerContentTitleTransforms andThen f
  }
}

trait BSToast2WithHeaderContentSubTitle extends Mutable {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*
  var _headerContentSubTitle: () => Option[Elem] = () => None
  var _headerContentSubTitleTransforms: Elem => Elem = _.small_c.text_body_secondary

  def headerContentSubTitle: Option[Elem] = _headerContentSubTitle()

  def headerContentSubTitle(v: Option[Elem]): this.type = mutate({ _headerContentSubTitle = () => v })

  def headerContentSubTitle(v: Elem): this.type = mutate({ _headerContentSubTitle = () => Some(v) })

  def headerContentSubTitle(v: String): this.type = mutate({ _headerContentSubTitle = () => Some(<span>{v}</span>) })

  def onHeaderContentSubTitle(f: Elem => Elem): this.type = mutate {
    _headerContentSubTitleTransforms = _headerContentSubTitleTransforms andThen f
  }
}

trait BSToast2WithCloseBtn extends Mutable {

  var _closeBtnEnabled: () => Boolean = () => false

  var _closeBtnTransforms: Elem => Elem = identity[Elem]

  def closeBtnEnabled: Boolean = _closeBtnEnabled()

  def closeBtnEnabled(v: Boolean): this.type = mutate({ _closeBtnEnabled = () => v })

  def enableCloseBtn: this.type = closeBtnEnabled(true)

  def disableCloseBtn: this.type = closeBtnEnabled(false)

  def oncloseBtn(f: Elem => Elem): this.type = mutate {
    _closeBtnTransforms = _closeBtnTransforms andThen f
  }
}

trait BSToast2WithHeader extends Mutable {

  var _headerEnabled: () => Boolean = () => false

  def headerEnabled: Boolean = _headerEnabled()

  def headerEnabled(v: Boolean): this.type = mutate({ _headerEnabled = () => v })

  def enableHeader: this.type = headerEnabled(true)

  def disableHeader: this.type = headerEnabled(false)
}

trait BSToast2WithContainer extends Mutable {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  private val ContainerBase: Elem = div.toast_container.p_3

  private val ContainerTopLeft = ContainerBase.withId("bs_toast2_container_top_left").position_fixed.top_0.start_0

  private val ContainerTopCenter = ContainerBase.withId("bs_toast2_container_top_center").position_fixed.top_0.start_50.translate_middle_x

  private val ContainerTopRight = ContainerBase.withId("bs_toast2_container_top_right").position_fixed.top_0.end_0

  private val ContainerMiddleLeft = ContainerBase.withId("bs_toast2_container_middle_left").position_fixed.top_50.start_0.translate_middle_y

  private val ContainerMiddleCenter = ContainerBase.withId("bs_toast2_container_middle_center").position_fixed.top_50.start_50.translate_middle

  private val ContainerMiddleRight = ContainerBase.withId("bs_toast2_container_middle_right").position_fixed.top_50.end_0.translate_middle_y

  private val ContainerBottomLeft = ContainerBase.withId("bs_toast2_container_bottom_left").position_fixed.bottom_0.start_0

  private val ContainerBottomCenter = ContainerBase.withId("bs_toast2_container_bottom_center").position_fixed.bottom_0.start_50.translate_middle_x

  private val ContainerBottomRight = ContainerBase.withId("bs_toast2_container_bottom_right").position_fixed.bottom_0.end_0

  val AllContainerIds = List(ContainerTopLeft, ContainerTopCenter, ContainerTopRight, ContainerMiddleLeft, ContainerMiddleCenter, ContainerMiddleRight, ContainerBottomLeft, ContainerBottomCenter, ContainerBottomRight).map(_.getId)

  var _toastContainer: () => Elem = () => ContainerTopRight

  def toastContainer: Elem = _toastContainer()

  def toastContainer(elem: Elem): this.type = mutate({ _toastContainer = () => elem })

  def positionTopLeft: this.type = toastContainer(ContainerTopLeft)

  def positionTopCenter: this.type = toastContainer(ContainerTopCenter)

  def positionTopRight: this.type = toastContainer(ContainerTopRight)

  def positionMiddleLeft: this.type = toastContainer(ContainerMiddleLeft)

  def positionMiddleCenter: this.type = toastContainer(ContainerMiddleCenter)

  def positionMiddleRight: this.type = toastContainer(ContainerMiddleRight)

  def positionBottomLeft: this.type = toastContainer(ContainerBottomLeft)

  def positionBottomCenter: this.type = toastContainer(ContainerBottomCenter)

  def positionBottomRight: this.type = toastContainer(ContainerBottomRight)
}

trait BSToast2WithAnimation extends Mutable {

  var _animationEnabled: () => Boolean = () => true

  def animationEnabled: Boolean = _animationEnabled()

  def animationEnabled(v: Boolean): this.type = mutate({ _animationEnabled = () => v })

  def enableAnimation: this.type = animationEnabled(true)

  def disableAnimation: this.type = animationEnabled(false)
}

trait BSToast2WithAutoHide extends Mutable {

  var _autoHideEnabled: () => Boolean = () => true

  def autoHideEnabled: Boolean = _autoHideEnabled()

  def autoHideEnabled(v: Boolean): this.type = mutate({ _autoHideEnabled = () => v })

  def enableAutoHide: this.type = autoHideEnabled(true)

  def disableAutoHide: this.type = autoHideEnabled(false)
}

trait BSToast2WithDelay extends Mutable {

  var _delay: () => Int = () => 5000

  def delay: Int = _delay()

  def delay(v: Int): this.type = mutate({ _delay = () => v })
}

abstract class BSToast2Base
    extends ClassEnrichableMutable
    with Mutable
    with BSToast2WithContainer
    with BSToast2WithHeaderContentLeftIcon
    with BSToast2WithHeaderContentTitle
    with BSToast2WithHeaderContentSubTitle
    with BSToast2WithCloseBtn
    with BSToast2WithAnimation
    with BSToast2WithAutoHide
    with BSToast2WithDelay
    with BSToast2WithHeader {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  val toastId = IdGen.id("toast")
  val toastHeaderId = IdGen.id("toast-header")
  val toastBodyId = IdGen.id("toast-body")

  var toastClasses = ""

  override def addClass(clas: String): this.type = {
    toastClasses += s" $clas"
    this
  }

  var toastContainerTransforms: Elem => Elem = identity[Elem]
  var toastTransforms: Elem => Elem = _.withId(toastId)
    .toast
    .withRole("alert")
    .withAttrs("aria-live" -> "assertive", "aria-atomic" -> "true", "data-bs-delay" -> delay.toString)
    .withAttrIf(!autoHideEnabled, "data-bs-autohide" -> "false").withAttrIf(!animationEnabled, "data-bs-animation" -> "false")
  var toastHeaderTransforms: Elem => Elem = _.withId(toastHeaderId).toast_header
  var toastBodyTransforms: Elem => Elem = _.withId(toastBodyId).toast_body
  var toastHeaderCloseBtnTransforms: Elem => Elem = _.btn_close

  def onToastContainer(f: Elem => Elem): this.type = mutate {
    toastContainerTransforms = toastContainerTransforms andThen f
  }

  def onToast(f: Elem => Elem): this.type = mutate {
    toastTransforms = toastTransforms andThen f
  }

  def onToastHeader(f: Elem => Elem): this.type = mutate {
    toastHeaderTransforms = toastHeaderTransforms andThen f
  }

  def onToastBody(f: Elem => Elem): this.type = mutate {
    toastBodyTransforms = toastBodyTransforms andThen f
  }

  def onToastHeaderCloseBtn(f: Elem => Elem): this.type = mutate {
    toastHeaderCloseBtnTransforms = toastHeaderCloseBtnTransforms andThen f
  }

  def append2DOM()(implicit fsc: FSContext): Js = {
    val finalToastContainer = toastContainerTransforms(toastContainer)
    Js(s"""if (document.body.querySelector("#${finalToastContainer.getId}") == null) {${JS.append2Body(finalToastContainer)}}""") &
      JS.append2(toastContainer.getId, renderToast())
  }

  def installAndShow(backdrop: Boolean = true, backdropStatic: Boolean = false, focus: Boolean = true, keyboard: Boolean = true)(implicit fsc: FSContext): Js =
    install(backdrop, backdropStatic, focus, keyboard) & show()

  def install(backdrop: Boolean = true, backdropStatic: Boolean = false, focus: Boolean = true, keyboard: Boolean = true)(implicit fsc: FSContext): Js = append2DOM()

  def dispose(): Js = JS(s"""bootstrap.Toast.getOrCreateInstance(${JS.elementById(toastId)}).dispose()""")

  def remove(): Js = AllContainerIds.map(JS.removeId(_)).reduceOption(_ & _).getOrElse(Js.Void)

  def hideAndRemove(): Js = hide() & onHidden(remove())

  def hide(): Js = JS(s"""bootstrap.Toast.getOrCreateInstance(${JS.elementById(toastId)}).hide()""")

  def show(): Js = JS(s"""bootstrap.Toast.getOrCreateInstance(${JS.elementById(toastId)}).show()""")

  def onShow(js: Js): Js = JS(s"""$$('#$toastId').on('show.bs.toast', function (e) {${js.cmd}});""")

  def onShown(js: Js): Js = JS(s"""$$('#$toastId').on('shown.bs.toast', function (e) {${js.cmd}});""")

  def onHide(js: Js): Js = JS(s"""$$('#$toastId').on('hide.bs.toast', function (e) {${js.cmd}});""")

  def onHidden(js: Js): Js = JS(s"""$$('#$toastId').on('hidden.bs.toast', function (e) {${js.cmd}});""")

  def removeOnHidden(): Js = onHidden(remove())

  def toastHeaderCloseBtn(): Elem = toastHeaderCloseBtnTransforms(<button type="button" data-bs-dismiss="toast" aria-label="Close"></button>)

  def toastHeader(): Elem = div.apply {
    headerContentLeftIcon.getOrElse(NodeSeq.Empty) ++
      headerContentTitle.getOrElse(NodeSeq.Empty) ++
      headerContentSubTitle.getOrElse(NodeSeq.Empty) ++
      JS.showIf(closeBtnEnabled)(toastHeaderCloseBtn())
  }

  def toastContents(): Elem

  def renderToast()(implicit fsc: FSContext): Elem = {
    toastTransforms {
      div.apply {
        JS.showIf(headerEnabled)(toastHeaderTransforms(toastHeader())) ++
          toastBodyTransforms(toastContents())
      }
    }
  }
}

object BSToast2 {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  class BSToast2WithoutPosition(toast: BSToast2Base) {

    def positionTopLeft: BSToast2Base = toast.positionTopLeft

    def positionTopCenter: BSToast2Base = toast.positionTopCenter

    def positionTopRight: BSToast2Base = toast.positionTopRight

    def positionMiddleLeft: BSToast2Base = toast.positionMiddleLeft

    def positionMiddleCenter: BSToast2Base = toast.positionMiddleCenter

    def positionMiddleRight: BSToast2Base = toast.positionMiddleRight

    def positionBottomLeft: BSToast2Base = toast.positionBottomLeft

    def positionBottomCenter: BSToast2Base = toast.positionBottomCenter

    def positionBottomRight: BSToast2Base = toast.positionBottomRight
  }

  def Simple(header: Elem)(contents: Elem): BSToast2WithoutPosition = new BSToast2WithoutPosition(new BSToast2Base {
    override def toastHeader(): Elem = <div>{header.me_auto}</div>

    override def toastContents(): Elem = contents
  })

  def VerySimple(header: String, contents: String): BSToast2Base = VerySimple(h6.apply(header))(p.apply(contents))

  def VerySimple(header: Elem)(contents: Elem): BSToast2Base = new BSToast2Base {
    override def toastHeader(): Elem = <div>{header.me_auto}</div>

    override def toastContents(): Elem = contents
  }.positionTopRight.enableCloseBtn
}
