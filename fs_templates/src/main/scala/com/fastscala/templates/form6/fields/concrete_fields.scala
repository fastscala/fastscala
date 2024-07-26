package com.fastscala.templates.form6.fields

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem


trait TextF6FieldRenderer[E <: FSXmlEnv] {

  def defaultRequiredFieldLabel: String

  def render[T](field: F6TextField[E, T])(label: Option[E#NodeSeq], inputElem: E#Elem, error: Option[E#NodeSeq])(implicit hints: Seq[RenderHint]): E#Elem
}

trait TextareaF6FieldRenderer[E <: FSXmlEnv] {

  def defaultRequiredFieldLabel: String

  def render[T](field: F6TextareaField[E, T])(label: Option[E#NodeSeq], inputElem: E#Elem, error: Option[E#NodeSeq])(implicit hints: Seq[RenderHint]): E#Elem
}

trait SelectF6FieldRenderer[E <: FSXmlEnv] {

  def defaultRequiredFieldLabel: String

  def render[T](field: F6SelectFieldBase[E, T])(label: Option[E#Elem], elem: E#Elem, error: Option[E#NodeSeq])(implicit hints: Seq[RenderHint]): E#Elem

  def renderOption[T](field: F6SelectFieldBase[E, T])(
    selected: Boolean,
    value: String,
    label: E#NodeSeq
  )(implicit hints: Seq[RenderHint]): E#Elem
}

trait MultiSelectF6FieldRenderer[E <: FSXmlEnv] {

  def defaultRequiredFieldLabel: String

  def render[T](field: F6MultiSelectFieldBase[E, T])(label: Option[E#Elem], elem: E#Elem, error: Option[E#NodeSeq])(implicit hints: Seq[RenderHint]): E#Elem

  def renderOption[T](field: F6MultiSelectFieldBase[E, T])(
    selected: Boolean,
    value: String,
    label: E#NodeSeq
  )(implicit hints: Seq[RenderHint]): E#Elem
}

trait CheckboxF6FieldRenderer[E <: FSXmlEnv] {

  def render(field: F6CheckboxField[E])(label: Option[E#Elem], elem: E#Elem, error: Option[E#NodeSeq])(implicit hints: Seq[RenderHint]): E#Elem
}

//object F6CodeField {
//
//  def cssImports: E#NodeSeq = {
//    <link href={"https://cdn.jsdelivr.net/npm/ace-builds@1.31.1/css/ace.min.css"} rel="stylesheet"></link>
//  }
//
//  def jsImports: E#NodeSeq = {
//    <script src={"https://cdn.jsdelivr.net/npm/ace-builds@1.31.1/src-min-noconflict/ace.min.js"}></script>
//    <script src={"https://cdn.jsdelivr.net/npm/ace-builds@1.31.1/src-min-noconflict/mode-html.js"}></script>
//    <script src={"https://cdn.jsdelivr.net/npm/ace-builds@1.31.1/src-min-noconflict/theme-textmate.js"}></script>
//  }
//}
//
//class F6CodeField(
//                   get: () => String
//                   , set: String => Js
//                   , label: Option[E#NodeSeq] = None
//                   , name: Option[String] = None
//                   , placeholder: Option[String] = None
//                   , tabindex: Option[Int] = None
//                   , maxlength: Option[Int] = None
//                   , required: () => Boolean = () => false
//                   , val disabled: () => Boolean = () => false
//                   , val readOnly: () => Boolean = () => false
//                   , val enabled: () => Boolean = () => true
//                   , val deps: Set[FormField] = Set(),
//                   saveEveryMillis: Int = 1000,
//                   height: String = "800px"
//                 )(
//                   implicit renderer: TextareaFieldRenderer
//                 ) extends StandardFormField with ValidatableField with StringSerializableField {
//
//  lazy val editorId = "__editor" + IdGen.id
//  lazy val timeoutId = editorId + "_TO"
//  lazy val savedId = editorId + "_saved"
//  lazy val savingId = editorId + "_saving"
//
//  var currentValue = get()
//
//  override def reRender()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = {
//    if (!hints.contains(OnSaveRerender)) super.reRender()
//    else Js.void
//  }
//
//  override def loadFromString(str: String): Seq[(ValidatableField, E#NodeSeq)] = {
//    currentValue = str
//    set(currentValue)
//    Nil
//  }
//
//  override def saveToString(): Option[String] = Some(currentValue).filter(_ != "")
//
//  def withLabel(label: String) = copy(label = Some(scala.xml.Text(label)))
//
//  def copy(
//            get: () => String = get
//            , set: String => Js = set
//            , label: Option[E#NodeSeq] = label
//            , name: Option[String] = name
//            , placeholder: Option[String] = placeholder
//            , tabindex: Option[Int] = tabindex
//            , maxlength: Option[Int] = maxlength
//            , required: () => Boolean = required
//            , enabled: () => Boolean = enabled
//            , deps: Set[FormField] = deps
//            , saveEveryMillis: Int = saveEveryMillis
//            , height: String = height
//          ): F6CodeField = {
//    new F6CodeField(
//      get = get
//      , set = set
//      , label = label
//      , name = name
//      , placeholder = placeholder
//      , tabindex = tabindex
//      , maxlength = maxlength
//      , required = required
//      , enabled = enabled
//      , deps = deps
//      , saveEveryMillis = saveEveryMillis
//      , height = height
//    )
//  }
//
//  override def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
//    case PerformSave => set(currentValue)
//    case _ => Js.void
//  })
//
//  override def errors(): Seq[(ValidatableField, E#NodeSeq)] = super.errors() ++
//    (if (required() && currentValue.trim == "") Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
//
//  override def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem = {
//    if (!enabled()) <div style="display:none;" id={aroundId}></div>
//    else {
//      withFieldRenderHints { implicit hints =>
//
//        val onChangeCallbackJS = fsc.callback(Js(s"window.$editorId.session.getValue()"), value => {
//          currentValue = value
//          form.onEvent(ChangedField(this)) & (if (hints.contains(ShowValidationsHint)) reRender() else Js.void)
//        })
//
//        val onChangeJS =
//          s"""function() {
//             |  $$('#$savedId').hide();
//             |  $$('#$savingId').show();
//             |  clearTimeout(window.$timeoutId);
//             |  window.$timeoutId = setTimeout(
//             |    function() {${onChangeCallbackJS.cmd}; $$('#$savedId').show(); $$('#$savingId').hide();},
//             |    $saveEveryMillis
//             |  );
//             |}
//             |""".stripMargin
//
//        <div id={aroundId}>
//          <p id={savedId} style="font-weight: bold; color: green;">Saved</p>
//          <p id={savingId} style="font-weight: bold; color: #999; display: none;">Saving...</p>
//          {
//          Unparsed(
//            s"""<style type="text/css" media="screen">
//               |  #${editorId} { width: 100%; height: 100%; }
//               |</style>
//               |""".stripMargin
//          )
//          }
//          <div style={s"width:100%;height: $height;display:inline-block;position:relative;"}>
//            <div id={editorId}>{currentValue}</div>
//          </div>
//
//          {
//            Unparsed(
//              s"""<script>window.$editorId = ace.edit(${Js.asJsStr(editorId).cmd});
//                 |window.$editorId.setTheme("ace/theme/textmate");
//                 |window.$editorId.session.setMode("ace/mode/html");
//                 |window.$editorId.session.on('change', ${onChangeJS});</script>""".stripMargin
//            )
//          }
//        </div>
//      }
//    }
//  }
//
//  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
//}
//
trait ButtonF6FieldRenderer[E <: FSXmlEnv] {
  def render(field: F6SaveButtonField[E, _])(btn: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem
}

class F6SaveButtonField[E <: FSXmlEnv, B <% E#Elem](
                                                     btn: FSContext => B
                                                     , val toInitialState: B => B = identity[B] _
                                                     , val toChangedState: B => B = identity[B] _
                                                     , val toErrorState: B => B = identity[B] _
                                                   )(implicit fsXmlSupport: FSXmlSupport[E], renderer: ButtonF6FieldRenderer[E])
  extends StandardF6Field[E]
    with F6FieldWithReadOnly[E]
    with F6FieldWithDependencies[E]
    with F6FieldWithDisabled[E]
    with F6FieldWithEnabled[E] {

  import com.fastscala.core.FSXmlUtils._

  override def fieldsMatching(predicate: PartialFunction[F6Field[E], Boolean]): List[F6Field[E]] = if (predicate.applyOrElse[F6Field[E], Boolean](this, _ => false)) List(this) else Nil

  val btnRenderer = Js.rerenderableP[E, (B => B, Form6[E])](_ => implicit fsc => {
    case (transformer, form) => (transformer(btn(fsc)): E#Elem).withId(elemId).addOnClick((Js.focus(elemId) & form.onSaveClientSide()).cmd)
  })

  override def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case AfterSave =>
      //btnRenderer.rerender((toInitialState, form)).printToConsoleBefore()
      Js.void
    case BeforeSave => Js.void
    case ErrorsOnSave =>
      //btnRenderer.rerender((toErrorState, form)).printToConsoleBefore()
      Js.void
    case ChangedField(_) =>
      //btnRenderer.rerender((toChangedState, form)).printToConsoleBefore()
      Js.void
    case PerformSave => Js.void
    case _ => Js.void
  })

  override def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>.asFSXml()
    else {
      withFieldRenderHints { implicit hints =>
        renderer.render(this)({
          if (hints.contains(FailedSaveStateHint)) btnRenderer.render((toErrorState, form))
          else btnRenderer.render((toInitialState, form))
        })
      }
    }
}
//
//trait FileUploadFieldRenderer[E <: FSXmlEnv] {
//
//  def transformFormElem(field: F6FileUploadField)(elem: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = elem
//
//  def transforLabelElem(field: F6FileUploadField)(elem: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = elem
//
//  def transforSubmitButtonElem(field: F6FileUploadField)(elem: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = elem
//
//  def transforResetButtonElem(field: F6FileUploadField)(elem: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = elem
//
//  def transforFileInputElem(field: F6FileUploadField)(elem: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = elem
//}
//
//class F6FileUploadField(
//                         get: () => Option[(String, Array[Byte])]
//                         , set: Option[(String, Array[Byte])] => Js
//                         , submitBtn: E#Elem
//                         , renderPreview: FSContext => Option[(String, Array[Byte])] => E#Elem = _ => _ => <div></div>
//                         , resetBtn: Option[E#Elem] = None
//                         , label: Option[E#NodeSeq] = None
//                         , name: Option[String] = None
//                         , tabindex: Option[Int] = None
//
//                         , val disabled: () => Boolean = () => false
//                         , val readOnly: () => Boolean = () => false
//                         , val enabled: () => Boolean = () => true
//                         , val deps: Set[FormField] = Set()
//
//                         , val transformFormElem: E#Elem => E#Elem = identity[E#Elem]
//                         , val transforLabelElem: E#Elem => E#Elem = identity[E#Elem]
//                         , val transforSubmitButtonElem: E#Elem => E#Elem = identity[E#Elem]
//                         , val transforResetButtonElem: E#Elem => E#Elem = identity[E#Elem]
//                         , val transforFileInputElem: E#Elem => E#Elem = identity[E#Elem]
//                       )(implicit renderer: FileUploadFieldRenderer) extends StandardFormField with ValidatableField {
//
//  var currentValue: Option[(String, Array[Byte])] = get()
//
//  override def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
//    case PerformSave => set(currentValue)
//    case _ => Js.void
//  })
//
//  override def errors(): Seq[(ValidatableField, E#NodeSeq)] = Nil
//
//  def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem = {
//    if (!enabled()) <div style="display:none;" id={aroundId}></div>
//    else {
//      withFieldRenderHints { implicit hints =>
//        val targetId = IdGen.id("targetFrame")
//        val inputId = IdGen.id("input")
//        val buttonId = IdGen.id("btn")
//        val resetButtonId = IdGen.id("reset-btn")
//
//        val previewRenderer = Js.rerenderable(rerenderer => implicit fsc => renderPreview(fsc)(currentValue))
//        val actionUrl = fsc.fileUploadActionUrl({
//          case Seq(uploadedFile: FSUploadedFile, _ *) =>
//            currentValue = Some((uploadedFile.submittedFileName, uploadedFile.content))
//            previewRenderer.rerender() &
//              form.onEvent(ChangedField(this)) &
//              (if (hints.contains(ShowValidationsHint)) reRender() else Js.void) &
//              Js.show(resetButtonId)
//        })
//        <form target={targetId} action={actionUrl} method="post" encoding="multipart/form-data" enctype="multipart/form-data" id={aroundId}>
//          <iframe id={targetId} name={targetId} src="about:blank" onload="eval(this.contentWindow.document.body.innerText)" style="width:0;height:0;border:0px solid #fff;"><html><body></body></html></iframe>
//          {
//          label.map(label => <label for={elemId}>{label}</label>).map(_.pipe(renderer.transforLabelElem(this)).pipe(transforLabelElem)).getOrElse(E#NodeSeq.Empty)
//          }
//          {
//          previewRenderer.render()
//          }
//          {
//            <input name="file" type="file" id={inputId} onchange={Js.show(buttonId).cmd}/>.pipe(renderer.transforFileInputElem(this)).pipe(transforFileInputElem)
//          }
//          {
//          submitBtn.pipe(renderer.transforSubmitButtonElem(this)).pipe(transforSubmitButtonElem).withId(buttonId).withStyle("display:none").withTypeSubmit()
//          }
//          {
//          resetBtn.map(renderer.transforResetButtonElem(this)).map(transforResetButtonElem).map(_.withId(resetButtonId).addOnClick(fsc.callback(() => {
//            currentValue = None
//            previewRenderer.rerender() &
//              form.onEvent(ChangedField(this)) &
//              (if (hints.contains(ShowValidationsHint)) reRender() else Js.void) &
//              Js.hide(resetButtonId)
//          }).cmd).withAttr("style")(cur => if (currentValue.isDefined) cur.getOrElse("") else cur.getOrElse("") + ";display:none;")).getOrElse(E#NodeSeq.Empty)
//          }
//        </form>.pipe(renderer.transformFormElem(this)).pipe(transformFormElem)
//      }
//    }
//  }
//
//  override def fieldsMatching(predicate: PartialFunction[FormField, Boolean]): List[FormField] = if (predicate.applyOrElse[FormField, Boolean](this, _ => false)) List(this) else Nil
//
//  def withLabel(label: String) = copy(label = Some(<span>{label}</span>))
//
//  def copy(
//            get: () => Option[(String, Array[Byte])] = get
//            , set: Option[(String, Array[Byte])] => Js = set
//            , submitBtn: E#Elem = submitBtn
//            , renderPreview: FSContext => Option[(String, Array[Byte])] => E#Elem = renderPreview
//            , resetBtn: Option[E#Elem] = resetBtn
//            , label: Option[E#NodeSeq] = label
//            , name: Option[String] = name
//            , tabindex: Option[Int] = tabindex
//            , disabled: () => Boolean = disabled
//            , readOnly: () => Boolean = readOnly
//            , enabled: () => Boolean = enabled
//            , deps: Set[FormField] = deps
//            , transformFormElem: E#Elem => E#Elem = transformFormElem
//            , transforLabelElem: E#Elem => E#Elem = transforLabelElem
//            , transforSubmitButtonElem: E#Elem => E#Elem = transforSubmitButtonElem
//            , transforResetButtonElem: E#Elem => E#Elem = transforResetButtonElem
//            , transforFileInputElem: E#Elem => E#Elem = transforFileInputElem
//          ): F6FileUploadField = new F6FileUploadField(
//    get = get
//    , set = set
//    , submitBtn = submitBtn
//    , renderPreview = renderPreview
//    , resetBtn = resetBtn
//    , label = label
//    , name = name
//    , tabindex = tabindex
//    , disabled = disabled
//    , readOnly = readOnly
//    , enabled = enabled
//    , deps = deps
//    , transformFormElem = transformFormElem
//    , transforLabelElem = transforLabelElem
//    , transforSubmitButtonElem = transforSubmitButtonElem
//    , transforResetButtonElem = transforResetButtonElem
//    , transforFileInputElem = transforFileInputElem
//  )
//}
