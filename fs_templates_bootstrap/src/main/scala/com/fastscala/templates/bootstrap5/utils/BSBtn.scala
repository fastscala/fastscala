package com.fastscala.templates.bootstrap5.utils

import com.fastscala.js.Js
import IcnFA.RichIcn
import com.fastscala.core.FSContext
import com.fastscala.utils.IdGen

import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import scala.xml.NodeSeq

case class BSBtn(
                  cls: String,
                  content: NodeSeq = NodeSeq.Empty,
                  onclickOpt: Option[FSContext => Js] = None,
                  hrefOpt: Option[String] = None,
                  targetOpt: Option[String] = None,
                  styleOpt: Option[String] = None,
                  idOpt: Option[String] = None,
                  titleOpt: Option[String] = None
                ) {

  override def toString: String = cls

  def xs: BSBtn = padding(2, 10)

  def sm: BSBtn = copy(cls = cls + " btn-sm ")

  def lg: BSBtn = copy(cls = cls + " btn-lg ")

  def padding(height: Int, width: Int): BSBtn = withStyle(s";padding: ${height}px ${width}px;")

  def padding(top: Int, right: Int, bottom: Int, left: Int): BSBtn = withStyle(s";padding: ${top}px ${right}px ${bottom}px ${left}px;")

  def margin(height: Int, width: Int): BSBtn = withStyle(s";margin: ${height}px ${width}px;")

  def margin(top: Int, right: Int, bottom: Int, left: Int): BSBtn = withStyle(s";margin: ${top}px ${right}px ${bottom}px ${left}px;")

  def disabled: BSBtn = withClass("disabled")

  def withContent(content: NodeSeq) = copy(content = content)

  def withClass(s: String) = copy(cls = cls + " " + s)

  def icn(i: IcnFA.FaIcn) = copy(content = i.icn ++ <span> </span> ++ content)

  def lbl(s: String) = copy(content = content ++ scala.xml.Text(s))

  def ns(ns: NodeSeq) = copy(content = ns)

  def onclick(jsCmd: Js) = copy(onclickOpt = Some(fsc => onclickOpt.getOrElse((_: FSContext) => Js.void)(fsc) & jsCmd))

  def ajax(jsCmd: FSContext => Js) = copy(onclickOpt = Some(fsc => onclickOpt.getOrElse((_: FSContext) => Js.void)(fsc) & fsc.callback(() => jsCmd(fsc))))

  def ajaxOnce(jsCmd: FSContext => Js, moreThanOnceRslt: Option[Js] = None) = {
    val used = new AtomicBoolean(false)
    ajax(fsc => {
      if (!used.getAndSet(true)) jsCmd(fsc)
      else moreThanOnceRslt.getOrElse(Js.void)
    })
  }

  def ajaxConfirm(question: String, jsCmd: FSContext => Js) =
    copy(onclickOpt = Some(fsc => Js.confirm(question, onclickOpt.getOrElse((_: FSContext) => Js.void)(fsc) & fsc.callback(() => jsCmd(fsc)))))

  def href(s: String) = copy(hrefOpt = Some(s))

  def target(s: String) = copy(targetOpt = Some(s))

  def targetBlank = target("_blank")

  def withStyle(s: String) = copy(styleOpt = Some(styleOpt.getOrElse("") + ";" + s))

  def id(s: String) = copy(idOpt = Some(s))

  def title(s: String) = copy(titleOpt = Some(s))

  def withRandomId = id(IdGen.id("btn"))

  def id = idOpt.getOrElse(null)

  def btn(implicit fsc: FSContext) = <button class={cls} id={id} style={styleOpt.orNull} title={titleOpt.orNull} onclick={onclickOpt.map(_(fsc).cmd).orNull}>{content}</button>

  def toggle(get: => Boolean, set: Boolean => Js, selected: BSBtn => BSBtn = identity[BSBtn], unselected: BSBtn => BSBtn = identity[BSBtn])(implicit fsc: FSContext) = {
    val finalBtn = if (idOpt.isDefined) this else this.id(IdGen.id)

    def generate: scala.xml.Elem =
      if (get) selected(finalBtn).ajax(implicit fsc => {set(false) & Js.replace(finalBtn.idOpt.get, generate)}).btn
      else unselected(finalBtn).ajax(implicit fsc => {set(true) & Js.replace(finalBtn.idOpt.get, generate)}).btn

    generate
  }

  def span(implicit fsc: FSContext) = <span class={cls} id={id} style={styleOpt.orNull} title={titleOpt.orNull} onclick={onclickOpt.map(_(fsc).cmd).orNull}>{content}</span>

  def btnLink(implicit fsc: FSContext) = <a class={cls} id={id} style={styleOpt.orNull} title={titleOpt.orNull} href={hrefOpt.getOrElse("javascript:void(0)")} onclick={onclickOpt.map(_(fsc).cmd).orNull}>{content}</a>

  def disableAfterOneClick() = copy(onclickOpt = Some(fsc => disable() & onclickOpt.getOrElse((_: FSContext) => Js.void)(fsc)))

  def disable(): Js = Js.fromString(s"$$('#$id').addClass('disabled').attr('disabled','true');")

  def enable(): Js = Js.fromString(s"$$('#$id').removeClass('disabled').removeAttr('disabled');")

  def toPrimary: Js = changeToStyleWithClasses("btn btn-primary")

  def toSecondary: Js = changeToStyleWithClasses("btn btn-secondary")

  def toInfo: Js = changeToStyleWithClasses("btn btn-info")

  def toDanger: Js = changeToStyleWithClasses("btn btn-danger")

  def toDark: Js = changeToStyleWithClasses("btn btn-dark")

  def toLight: Js = changeToStyleWithClasses("btn btn-light")

  def toSuccess: Js = changeToStyleWithClasses("btn btn-success")

  def toWarning: Js = changeToStyleWithClasses("btn btn-warning")

  def toLink: Js = changeToStyleWithClasses("btn btn-link")

  def toOutlineDanger: Js = changeToStyleWithClasses("btn btn-outline-danger")

  def toOutlineDark: Js = changeToStyleWithClasses("btn btn-outline-dark")

  def toOutlineInfo: Js = changeToStyleWithClasses("btn btn-outline-info")

  def toOutlineLight: Js = changeToStyleWithClasses("btn btn-outline-light")

  def toOutlinePrimary: Js = changeToStyleWithClasses("btn btn-outline-primary")

  def toOutlineSecondary: Js = changeToStyleWithClasses("btn btn-outline-secondary")

  def toOutlineSuccess: Js = changeToStyleWithClasses("btn btn-outline-success")

  def toOutlineWarning: Js = changeToStyleWithClasses("btn btn-outline-warning")

  def changeToStyleWithClasses(classes: String): Js = Js.fromString(s"$$('#$id').removeClass('${styleClasses.mkString(" ")}').addClass('$classes');")

  val styleClasses = Vector(
    "btn-primary"
    , "btn-secondary"
    , "btn-info"
    , "btn-danger"
    , "btn-dark"
    , "btn-light"
    , "btn-success"
    , "btn-warning"
    , "btn-link"
    , "btn-outline-danger"
    , "btn-outline-dark"
    , "btn-outline-info"
    , "btn-outline-light"
    , "btn-outline-primary"
    , "btn-outline-secondary"
    , "btn-outline-success"
    , "btn-outline-warning"
    , "btn"
  )

  def Btn = withClass("")

  def BtnClass = withClass(" btn ")

  def NavLink = withClass(" nav-link ")

  def BtnToolbar = withClass("btn-toolbar")

  def BtnCheck = withClass("btn-check")

  def BtnClose = withClass("btn btn-close")

  def BtnCloseWhite = withClass("btn btn-close-white")

  def BtnGroupVertical = withClass("btn-group-vertical")

  def BtnPrimary = withClass("btn btn-primary")

  def BtnSecondary = withClass("btn btn-secondary")

  def BtnInfo = withClass("btn btn-info")

  def BtnDanger = withClass("btn btn-danger")

  def BtnDark = withClass("btn btn-dark")

  def BtnLight = withClass("btn btn-light")

  def BtnSuccess = withClass("btn btn-success")

  def BtnWarning = withClass("btn btn-warning")

  def BtnLink = withClass("btn btn-link")

  def BtnOutlineDanger = withClass("btn btn-outline-danger")

  def BtnOutlineDark = withClass("btn btn-outline-dark")

  def BtnOutlineInfo = withClass("btn btn-outline-info")

  def BtnOutlineLight = withClass("btn btn-outline-light")

  def BtnOutlinePrimary = withClass("btn btn-outline-primary")

  def BtnOutlineSecondary = withClass("btn btn-outline-secondary")

  def BtnOutlineSuccess = withClass("btn btn-outline-success")

  def BtnOutlineWarning = withClass("btn btn-outline-warning")

}

object BSBtn extends BSBtn(
  cls = ""
  , content = NodeSeq.Empty
  , onclickOpt = None
  , hrefOpt = None
  , targetOpt = None
  , styleOpt = None
  , idOpt = None
  , titleOpt = None
)

