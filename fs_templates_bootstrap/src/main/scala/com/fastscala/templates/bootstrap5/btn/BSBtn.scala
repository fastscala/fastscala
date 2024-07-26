package com.fastscala.templates.bootstrap5.utils

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.utils.IcnFA.RichIcn
import com.fastscala.utils.IdGen
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem

import java.util.concurrent.atomic.AtomicBoolean

object BSBtn {

  def apply[E <: FSXmlEnv : FSXmlSupport](): BSBtn[E] = new BSBtn[E]("", implicitly[FSXmlSupport[E]].Empty)
}

case class BSBtn[E <: FSXmlEnv : FSXmlSupport](
                                                cls: String,
                                                content: E#NodeSeq,
                                                onclickOpt: Option[FSContext => Js] = None,
                                                hrefOpt: Option[String] = None,
                                                targetOpt: Option[String] = None,
                                                styleOpt: Option[String] = None,
                                                idOpt: Option[String] = None,
                                                titleOpt: Option[String] = None
                                              ) {

  import com.fastscala.core.FSXmlUtils._

  override def toString: String = cls

  def xs: BSBtn[E] = padding(2, 10)

  def sm: BSBtn[E] = copy(cls = cls + " btn-sm ")

  def lg: BSBtn[E] = copy(cls = cls + " btn-lg ")

  def padding(height: Int, width: Int): BSBtn[E] = withStyle(s";padding: ${height}px ${width}px;")

  def padding(top: Int, right: Int, bottom: Int, left: Int): BSBtn[E] = withStyle(s";padding: ${top}px ${right}px ${bottom}px ${left}px;")

  def margin(height: Int, width: Int): BSBtn[E] = withStyle(s";margin: ${height}px ${width}px;")

  def margin(top: Int, right: Int, bottom: Int, left: Int): BSBtn[E] = withStyle(s";margin: ${top}px ${right}px ${bottom}px ${left}px;")

  def disabled: BSBtn[E] = withClass("disabled")

  def withContent(content: E#NodeSeq): BSBtn[E] = copy(content = content)

  def withClass(s: String): BSBtn[E] = copy(cls = cls + " " + s)

  def icn(i: BsIcn.type => BsIcn.BsIcn): BSBtn[E] = icn(i(BsIcn).icn[E])

  def icn(i: E#Elem): BSBtn[E] = copy(content = i ++ <span> </span>.asFSXml() ++ content)

  def lbl(s: String): BSBtn[E] = copy(content = content ++ implicitly[FSXmlSupport[E]].buildText(s))

  def ns(ns: E#NodeSeq): BSBtn[E] = copy(content = ns)

  def onclick(jsCmd: Js): BSBtn[E] = copy(onclickOpt = Some(fsc => onclickOpt.getOrElse((_: FSContext) => Js.void)(fsc) & jsCmd))

  def ajax(jsCmd: FSContext => Js): BSBtn[E] = copy(onclickOpt = Some(fsc => onclickOpt.getOrElse((_: FSContext) => Js.void)(fsc) & fsc.callback(() => jsCmd(fsc))))

  def ajaxOnce(jsCmd: FSContext => Js, moreThanOnceRslt: Option[Js] = None): BSBtn[E] = {
    val used = new AtomicBoolean(false)
    ajax(fsc => {
      if (!used.getAndSet(true)) jsCmd(fsc)
      else moreThanOnceRslt.getOrElse(Js.void)
    })
  }

  def ajaxConfirm(question: String, jsCmd: FSContext => Js): BSBtn[E] =
    copy(onclickOpt = Some(fsc => Js.confirm(question, onclickOpt.getOrElse((_: FSContext) => Js.void)(fsc) & fsc.callback(() => jsCmd(fsc)))))

  def href(s: String): BSBtn[E] = copy(hrefOpt = Some(s))

  def target(s: String): BSBtn[E] = copy(targetOpt = Some(s))

  def targetBlank: BSBtn[E] = target("_blank")

  def withStyle(s: String): BSBtn[E] = copy(styleOpt = Some(styleOpt.getOrElse("") + ";" + s))

  def id(s: String): BSBtn[E] = copy(idOpt = Some(s))

  def title(s: String): BSBtn[E] = copy(titleOpt = Some(s))

  def withRandomId: BSBtn[E] = id(IdGen.id("btn"))

  def id: String = idOpt.getOrElse(null)

  def btn(implicit fsc: FSContext): E#Elem = <button class={cls} id={id} type="button" style={styleOpt.orNull} title={titleOpt.orNull} onclick={onclickOpt.map(_(fsc).cmd).orNull}>{content}</button>.asFSXml()

  def toggle(
              get: => Boolean,
              set: Boolean => Js,
              selected: BSBtn[E] => BSBtn[E] = identity[BSBtn[E]],
              unselected: BSBtn[E] => BSBtn[E] = identity[BSBtn[E]]
            )(implicit fsc: FSContext): E#Elem = {
    val finalBtn = if (idOpt.isDefined) this else this.id(IdGen.id)

    def generate: E#Elem =
      if (get) selected(finalBtn).ajax(implicit fsc => {
        set(false) & Js.replace(finalBtn.idOpt.get, generate)
      }).btn
      else unselected(finalBtn).ajax(implicit fsc => {
        set(true) & Js.replace(finalBtn.idOpt.get, generate)
      }).btn

    generate
  }

  def span(implicit fsc: FSContext): E#Elem = <span class={cls} id={id} style={styleOpt.orNull} title={titleOpt.orNull} onclick={onclickOpt.map(_(fsc).cmd).orNull}>{content}</span>.asFSXml()

  def btnLink(implicit fsc: FSContext): E#Elem = <a class={cls} id={id} style={styleOpt.orNull} title={titleOpt.orNull} href={hrefOpt.getOrElse("javascript:void(0)")} onclick={onclickOpt.map(_(fsc).cmd).orNull}>{content}</a>.asFSXml()

  def disableAfterOneClick(): BSBtn[E] = copy(onclickOpt = Some(fsc => disable() & onclickOpt.getOrElse((_: FSContext) => Js.void)(fsc)))

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

  def Btn: BSBtn[E] = withClass("")

  def BtnClass: BSBtn[E] = withClass(" btn ")

  def NavLink: BSBtn[E] = withClass(" nav-link ")

  def BtnToolbar: BSBtn[E] = withClass("btn-toolbar")

  def BtnCheck: BSBtn[E] = withClass("btn-check")

  def BtnClose: BSBtn[E] = withClass("btn btn-close")

  def BtnCloseWhite: BSBtn[E] = withClass("btn btn-close-white")

  def BtnGroupVertical: BSBtn[E] = withClass("btn-group-vertical")

  def BtnPrimary: BSBtn[E] = withClass("btn btn-primary")

  def BtnSecondary: BSBtn[E] = withClass("btn btn-secondary")

  def BtnInfo: BSBtn[E] = withClass("btn btn-info")

  def BtnDanger: BSBtn[E] = withClass("btn btn-danger")

  def BtnDark: BSBtn[E] = withClass("btn btn-dark")

  def BtnLight: BSBtn[E] = withClass("btn btn-light")

  def BtnSuccess: BSBtn[E] = withClass("btn btn-success")

  def BtnWarning: BSBtn[E] = withClass("btn btn-warning")

  def BtnLink: BSBtn[E] = withClass("btn btn-link")

  def BtnOutlineDanger: BSBtn[E] = withClass("btn btn-outline-danger")

  def BtnOutlineDark: BSBtn[E] = withClass("btn btn-outline-dark")

  def BtnOutlineInfo: BSBtn[E] = withClass("btn btn-outline-info")

  def BtnOutlineLight: BSBtn[E] = withClass("btn btn-outline-light")

  def BtnOutlinePrimary: BSBtn[E] = withClass("btn btn-outline-primary")

  def BtnOutlineSecondary: BSBtn[E] = withClass("btn btn-outline-secondary")

  def BtnOutlineSuccess: BSBtn[E] = withClass("btn btn-outline-success")

  def BtnOutlineWarning: BSBtn[E] = withClass("btn btn-outline-warning")
}

