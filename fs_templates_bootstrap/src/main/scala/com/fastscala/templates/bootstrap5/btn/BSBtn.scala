package com.fastscala.templates.bootstrap5.utils

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.bootstrap5.helpers.{AttrEnrichableImmutable, ClassEnrichableImmutable}
import com.fastscala.templates.bootstrap5.utils.IcnFA.RichIcn
import com.fastscala.utils.IdGen
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import java.util.concurrent.atomic.AtomicBoolean
import scala.xml.{Elem, NodeSeq}

object BSBtn {

  def apply(): BSBtn = new BSBtn("", NodeSeq.Empty)
}

case class BSBtn(
                  cls: String,
                  content: NodeSeq,
                  onclickOpt: Option[FSContext => Js] = None,
                  hrefOpt: Option[String] = None,
                  targetOpt: Option[String] = None,
                  styleOpt: Option[String] = None,
                  idOpt: Option[String] = None,
                  titleOpt: Option[String] = None,
                  additionalAttrs: Seq[(String, String)] = Nil
                ) extends ClassEnrichableImmutable[BSBtn] with AttrEnrichableImmutable[BSBtn] {

  override def setAttribute(name: String, value: String): BSBtn = withAdditionalAttrs(name -> value)

  override def addClass(s: String): BSBtn = copy(cls = cls + " " + s)

  override def toString: String = cls

  def xs: BSBtn = padding(2, 10)

  def sm: BSBtn = copy(cls = cls + " btn-sm ")

  def lg: BSBtn = copy(cls = cls + " btn-lg ")

  def padding(height: Int, width: Int): BSBtn = withStyle(s";padding: ${height}px ${width}px;")

  def padding(top: Int, right: Int, bottom: Int, left: Int): BSBtn = withStyle(s";padding: ${top}px ${right}px ${bottom}px ${left}px;")

  def margin(height: Int, width: Int): BSBtn = withStyle(s";margin: ${height}px ${width}px;")

  def margin(top: Int, right: Int, bottom: Int, left: Int): BSBtn = withStyle(s";margin: ${top}px ${right}px ${bottom}px ${left}px;")

  def disabled: BSBtn = addClass("disabled")

  def withContent(content: NodeSeq): BSBtn = copy(content = content)

  def withClass(s: String): BSBtn = copy(cls = s)

  def withAdditionalAttrs(attrs: (String, String)*): BSBtn = copy(additionalAttrs = additionalAttrs ++ attrs)

  def icn(i: BsIcn.type => BsIcn.BsIcn): BSBtn = icn(i(BsIcn).icn)

  def icn(i: Elem): BSBtn = copy(content = i ++ <span> </span> ++ content)

  def lbl(s: String): BSBtn = copy(content = content ++ scala.xml.Text(s))

  def ns(ns: NodeSeq): BSBtn = copy(content = ns)

  def onclick(jsCmd: Js): BSBtn = copy(onclickOpt = Some(fsc => onclickOpt.getOrElse((_: FSContext) => JS.void)(fsc) & jsCmd))

  def ajax(jsCmd: FSContext => Js): BSBtn = copy(onclickOpt = Some(fsc => onclickOpt.getOrElse((_: FSContext) => JS.void)(fsc) & fsc.callback(() => jsCmd(fsc))))

  def ajaxOnce(jsCmd: FSContext => Js, moreThanOnceRslt: Option[Js] = None): BSBtn = {
    val used = new AtomicBoolean(false)
    ajax(fsc => {
      if (!used.getAndSet(true)) jsCmd(fsc)
      else moreThanOnceRslt.getOrElse(JS.void)
    })
  }

  def ajaxConfirm(question: String, jsCmd: FSContext => Js): BSBtn =
    copy(onclickOpt = Some(fsc => JS.confirm(question, onclickOpt.getOrElse((_: FSContext) => JS.void)(fsc) & fsc.callback(() => jsCmd(fsc)))))

  def href(s: String): BSBtn = copy(hrefOpt = Some(s))

  def target(s: String): BSBtn = copy(targetOpt = Some(s))

  def targetBlank: BSBtn = target("_blank")

  def withStyle(s: String): BSBtn = copy(styleOpt = Some(styleOpt.getOrElse("") + ";" + s))

  def id(s: String): BSBtn = copy(idOpt = Some(s))

  def title(s: String): BSBtn = copy(titleOpt = Some(s))

  def withRandomId: BSBtn = id(IdGen.id("btn"))

  def id: String = idOpt.getOrElse(null)

  def toggle(
              get: => Boolean,
              set: Boolean => Js,
              selected: BSBtn => BSBtn = identity[BSBtn],
              unselected: BSBtn => BSBtn = identity[BSBtn]
            )(implicit fsc: FSContext): Elem = {
    val finalBtn = if (idOpt.isDefined) this else this.id(IdGen.id)

    def generate: Elem =
      if (get) selected(finalBtn).ajax(implicit fsc => {
        set(false) & JS.replace(finalBtn.idOpt.get, generate)
      }).btn
      else unselected(finalBtn).ajax(implicit fsc => {
        set(true) & JS.replace(finalBtn.idOpt.get, generate)
      }).btn

    generate
  }

  def disableAfterOneClick(): BSBtn = copy(onclickOpt = Some(fsc => disable() & onclickOpt.getOrElse((_: FSContext) => JS.void)(fsc)))

  def disable(): Js = JS.fromString(s"$$('#$id').addClass('disabled').attr('disabled','true');")

  def enable(): Js = JS.fromString(s"$$('#$id').removeClass('disabled').removeAttr('disabled');")

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

  def changeToStyleWithClasses(classes: String): Js = JS.fromString(s"$$('#$id').removeClass('${styleClasses.mkString(" ")}').addClass('$classes');")

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

  def Btn: BSBtn = addClass("")

  def BtnClass: BSBtn = addClass(" btn ")

  def NavLink: BSBtn = addClass(" nav-link ")

  def BtnToolbar: BSBtn = addClass("btn-toolbar")

  def BtnCheck: BSBtn = addClass("btn-check")

  def BtnClose: BSBtn = addClass("btn btn-close")

  def BtnCloseWhite: BSBtn = addClass("btn btn-close-white")

  def BtnGroupVertical: BSBtn = addClass("btn-group-vertical")

  def BtnPrimary: BSBtn = addClass("btn btn-primary")

  def BtnSecondary: BSBtn = addClass("btn btn-secondary")

  def BtnInfo: BSBtn = addClass("btn btn-info")

  def BtnDanger: BSBtn = addClass("btn btn-danger")

  def BtnDark: BSBtn = addClass("btn btn-dark")

  def BtnLight: BSBtn = addClass("btn btn-light")

  def BtnSuccess: BSBtn = addClass("btn btn-success")

  def BtnWarning: BSBtn = addClass("btn btn-warning")

  def BtnLink: BSBtn = addClass("btn btn-link")

  def BtnOutlineDanger: BSBtn = addClass("btn btn-outline-danger")

  def BtnOutlineDark: BSBtn = addClass("btn btn-outline-dark")

  def BtnOutlineInfo: BSBtn = addClass("btn btn-outline-info")

  def BtnOutlineLight: BSBtn = addClass("btn btn-outline-light")

  def BtnOutlinePrimary: BSBtn = addClass("btn btn-outline-primary")

  def BtnOutlineSecondary: BSBtn = addClass("btn btn-outline-secondary")

  def BtnOutlineSuccess: BSBtn = addClass("btn btn-outline-success")

  def BtnOutlineWarning: BSBtn = addClass("btn btn-outline-warning")

  // ================================== RENDERING ==================================

  def btn(implicit fsc: FSContext): Elem = <button class={cls} id={id} type="button" style={styleOpt.orNull} title={titleOpt.orNull} onclick={onclickOpt.map(_(fsc).cmd).orNull}>{content}</button>.withAttrs(additionalAttrs: _*)

  def btnRerender(implicit fsc: FSContext): Js = JS.replace(id, btn)

  def span(implicit fsc: FSContext): Elem = <span class={cls} id={id} style={styleOpt.orNull} title={titleOpt.orNull} onclick={onclickOpt.map(_(fsc).cmd).orNull}>{content}</span>.withAttrs(additionalAttrs: _*)

  def spanRerender(implicit fsc: FSContext): Js = JS.replace(id, span)

  def btnLink(implicit fsc: FSContext): Elem = <a class={cls} id={id} style={styleOpt.orNull} title={titleOpt.orNull} href={hrefOpt.getOrElse("javascript:void(0)")} onclick={onclickOpt.map(_(fsc).cmd).orNull}>{content}</a>.withAttrs(additionalAttrs: _*)

  def btnLinkRerender(implicit fsc: FSContext): Js = JS.replace(id, btnLink)
}

