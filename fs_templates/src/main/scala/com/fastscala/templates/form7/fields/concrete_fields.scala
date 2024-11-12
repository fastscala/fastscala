package com.fastscala.templates.form7.fields


//object F6CodeField {
//
//  def cssImports: NodeSeq = {
//    <link href={"https://cdn.jsdelivr.net/npm/ace-builds@1.31.1/css/ace.min.css"} rel="stylesheet"></link>
//  }
//
//  def jsImports: NodeSeq = {
//    <script src={"https://cdn.jsdelivr.net/npm/ace-builds@1.31.1/src-min-noconflict/ace.min.js"}></script>
//    <script src={"https://cdn.jsdelivr.net/npm/ace-builds@1.31.1/src-min-noconflict/mode-html.js"}></script>
//    <script src={"https://cdn.jsdelivr.net/npm/ace-builds@1.31.1/src-min-noconflict/theme-textmate.js"}></script>
//  }
//}
//
//class F6CodeField(
//                   get: () => String
//                   , set: String => Js
//                   , label: Option[NodeSeq] = None
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
//  override def reRender()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = {
//    if (!hints.contains(OnSaveRerender)) super.reRender()
//    else Js.void
//  }
//
//  override def loadFromString(str: String): Seq[(ValidatableField, NodeSeq)] = {
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
//            , label: Option[NodeSeq] = label
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
//  override def onEvent(event: FormEvent)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
//    case PerformSave => set(currentValue)
//    case _ => Js.void
//  })
//
//  override def errors(): Seq[(ValidatableField, NodeSeq)] = super.errors() ++
//    (if (required() && currentValue.trim == "") Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
//
//  override def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
//    if (!enabled) <div style="display:none;" id={aroundId}></div>
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

//
//trait FileUploadFieldRenderer {
//
//  def transformFormElem(field: F6FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem
//
//  def transforLabelElem(field: F6FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem
//
//  def transforSubmitButtonElem(field: F6FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem
//
//  def transforResetButtonElem(field: F6FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem
//
//  def transforFileInputElem(field: F6FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem
//}
//
//class F6FileUploadField(
//                         get: () => Option[(String, Array[Byte])]
//                         , set: Option[(String, Array[Byte])] => Js
//                         , submitBtn: Elem
//                         , renderPreview: FSContext => Option[(String, Array[Byte])] => Elem = _ => _ => <div></div>
//                         , resetBtn: Option[Elem] = None
//                         , label: Option[NodeSeq] = None
//                         , name: Option[String] = None
//                         , tabindex: Option[Int] = None
//
//                         , val disabled: () => Boolean = () => false
//                         , val readOnly: () => Boolean = () => false
//                         , val enabled: () => Boolean = () => true
//                         , val deps: Set[FormField] = Set()
//
//                         , val transformFormElem: Elem => Elem = identity[Elem]
//                         , val transforLabelElem: Elem => Elem = identity[Elem]
//                         , val transforSubmitButtonElem: Elem => Elem = identity[Elem]
//                         , val transforResetButtonElem: Elem => Elem = identity[Elem]
//                         , val transforFileInputElem: Elem => Elem = identity[Elem]
//                       )(implicit renderer: FileUploadFieldRenderer) extends StandardFormField with ValidatableField {
//
//  var currentValue: Option[(String, Array[Byte])] = get()
//
//  override def onEvent(event: FormEvent)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
//    case PerformSave => set(currentValue)
//    case _ => Js.void
//  })
//
//  override def errors(): Seq[(ValidatableField, NodeSeq)] = Nil
//
//  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
//    if (!enabled) <div style="display:none;" id={aroundId}></div>
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
//          label.map(label => <label for={elemId}>{label}</label>).map(_.pipe(renderer.transforLabelElem(this)).pipe(transforLabelElem)).getOrElse(NodeSeq.Empty)
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
//          }).cmd).withAttr("style")(cur => if (currentValue.isDefined) cur.getOrElse("") else cur.getOrElse("") + ";display:none;")).getOrElse(NodeSeq.Empty)
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
//            , submitBtn: Elem = submitBtn
//            , renderPreview: FSContext => Option[(String, Array[Byte])] => Elem = renderPreview
//            , resetBtn: Option[Elem] = resetBtn
//            , label: Option[NodeSeq] = label
//            , name: Option[String] = name
//            , tabindex: Option[Int] = tabindex
//            , disabled: () => Boolean = disabled
//            , readOnly: () => Boolean = readOnly
//            , enabled: () => Boolean = enabled
//            , deps: Set[FormField] = deps
//            , transformFormElem: Elem => Elem = transformFormElem
//            , transforLabelElem: Elem => Elem = transforLabelElem
//            , transforSubmitButtonElem: Elem => Elem = transforSubmitButtonElem
//            , transforResetButtonElem: Elem => Elem = transforResetButtonElem
//            , transforFileInputElem: Elem => Elem = transforFileInputElem
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
