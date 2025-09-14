package com.fastscala.components.aceeditor

import com.fastscala.components.aceeditor.json.{BindKey, Command, OnChangeEvent, SelectionRange}
import com.fastscala.components.utils.{ElemWithId, ElemWithRandomId}
import com.fastscala.core.FSContext
import com.fastscala.core.circe.CirceSupport.FSContextWithCirceSupport
import com.fastscala.js.{Js, JsFunc0, JsFunc1}
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromNodeSeq
import com.fastscala.scala_xml.js.{JS, print2Console, printBeforeExec}
import com.fastscala.utils.Lazy

import scala.collection.mutable.ListBuffer
import scala.xml.{Elem, NodeSeq}

object AceEditor {

  def jsImports(language: Language.Value, theme: Theme.Value): NodeSeq =
    <script src="https://cdn.jsdelivr.net/npm/ace-builds@1.37.5/src-min-noconflict/ace.js" integrity="sha256-ii6u0BJo0Yx3y2GCg5RRVXCO2bo6oysPMXp8ik9sLQc=" crossorigin="anonymous"></script> ++
      jsForTheme(theme) ++
      jsForLanguage(language)

  def jsBase: NodeSeq =
    <script src="https://cdn.jsdelivr.net/npm/ace-builds@1.37.5/src-min-noconflict/ace.js" integrity="sha256-ii6u0BJo0Yx3y2GCg5RRVXCO2bo6oysPMXp8ik9sLQc=" crossorigin="anonymous"></script>

  def jsForTheme(theme: Theme.Value): NodeSeq = <script src={
    s"https://cdn.jsdelivr.net/npm/ace-builds@1.37.5/src-min-noconflict/theme-${theme}.js"
  }></script>

  def jsForAllThemes: NodeSeq = Theme.values.toList.map(jsForTheme).mkNS

  def jsForLanguage(language: Language.Value): NodeSeq = <script src={
    s"https://cdn.jsdelivr.net/npm/ace-builds@1.37.5/src-noconflict/snippets/${language}.min.js"
  }></script>

  def cssImports: NodeSeq =
    <link href="https://cdn.jsdelivr.net/npm/ace-builds@1.37.5/css/ace.min.css" rel="stylesheet"/>
}

trait AceEditor extends ElemWithRandomId {

  def initalValue: String

  private lazy val currentValueHolder: Lazy[ListBuffer[String]] = Lazy(ListBuffer(initalValue.split("\\n")*))

  def currentValue: String = currentValueHolder().mkString(System.lineSeparator())

  def currentValue_=(v: String) = currentValueHolder() = ListBuffer(v.split("\\n")*)

  // =================== EditSessionOptions ===================
  def defaultWrap: Option["off" | "free" | "printmargin" | Boolean | Int] = None

  def setWrap(v: "off" | "free" | "printmargin" | Boolean | Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("wrap", v)}});""")

  def defaultWrapMethod: Option[WrapMethod.Value] = None

  def setWrapMethod(v: WrapMethod.Value): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("wrapMethod", v)}});""")

  def defaultIndentedSoftWrap: Option[Boolean] = None

  def setIndentedSoftWrap(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("indentedSoftWrap", v)}});""")

  def defaultFirstLineNumber: Option[Int] = None

  def setFirstLineNumber(v: Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("firstLineNumber", v)}});""")

  def defaultUseWorker: Option[Boolean] = None

  def setUseWorker(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("useWorker", v)}});""")

  def defaultUseSoftTabs: Option[Boolean] = None

  def setUseSoftTabs(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("useSoftTabs", v)}});""")

  def defaultTabSize: Option[Int] = None

  def setTabSize(v: Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("tabSize", v)}});""")

  def defaultNavigateWithinSoftTabs: Option[Boolean] = None

  def setNavigateWithinSoftTabs(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("navigateWithinSoftTabs", v)}});""")

  def defaultFoldStyle: Option[FoldStyle.Value] = None

  def setFoldStyle(v: FoldStyle.Value): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("foldStyle", v)}});""")

  def defaultOverwrite: Option[Boolean] = None

  def setOverwrite(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("overwrite", v)}});""")

  def defaultNewLineMode: Option[NewLineMode.Value] = None

  def setNewLineMode(v: NewLineMode.Value): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("newLineMode", v)}});""")

  def defaultMode: Language.Value

  def setMode(v: Language.Value): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("mode", v)}});""")

  // =================== VirtualRendererOptions ===================
  def defaultAnimatedScroll: Option[Boolean] = None

  def setAnimatedScroll(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("animatedScroll", v)}});""")

  def defaultShowInvisibles: Option[Boolean] = None

  def setShowInvisibles(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("showInvisibles", v)}});""")

  def defaultShowPrintMargin: Option[Boolean] = None

  def setShowPrintMargin(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("showPrintMargin", v)}});""")

  def defaultPrintMarginColumn: Option[Int] = None

  def setPrintMarginColumn(v: Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("printMarginColumn", v)}});""")

  def defaultPrintMargin: Option[Either[Boolean, Int]] = None

  def setPrintMargin(v: Either[Boolean, Int]): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("printMargin", v)}});""")

  def defaultShowGutter: Option[Boolean] = None

  def setShowGutter(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("showGutter", v)}});""")

  def defaultFadeFoldWidgets: Option[Boolean] = None

  def setFadeFoldWidgets(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("fadeFoldWidgets", v)}});""")

  def defaultShowFoldWidgets: Option[Boolean] = None

  def setShowFoldWidgets(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("showFoldWidgets", v)}});""")

  def defaultShowLineNumbers: Option[Boolean] = None

  def setShowLineNumbers(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("showLineNumbers", v)}});""")

  def defaultDisplayIndentGuides: Option[Boolean] = None

  def setDisplayIndentGuides(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("displayIndentGuides", v)}});""")

  def defaultHighlightIndentGuides: Option[Boolean] = None

  def setHighlightIndentGuides(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("highlightIndentGuides", v)}});""")

  def defaultHighlightGutterLine: Option[Boolean] = None

  def setHighlightGutterLine(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("highlightGutterLine", v)}});""")

  def defaultHScrollBarAlwaysVisible: Option[Boolean] = None

  def setHScrollBarAlwaysVisible(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("hScrollBarAlwaysVisible", v)}});""")

  def defaultVScrollBarAlwaysVisible: Option[Boolean] = None

  def setVScrollBarAlwaysVisible(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("vScrollBarAlwaysVisible", v)}});""")

  def defaultFontSize: Option[Either[String, Int]] = None

  def setFontSize(v: Either[String, Int]): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("fontSize", v)}});""")

  def defaultFontFamily: Option[String] = None

  def setFontFamily(v: String): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("fontFamily", v)}});""")

  def defaultMaxLines: Option[Int] = None

  def setMaxLines(v: Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("maxLines", v)}});""")

  def defaultMinLines: Option[Int] = None

  def setMinLines(v: Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("minLines", v)}});""")

  def defaultScrollPastEnd: Option[Int] = None

  def setScrollPastEnd(v: Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("scrollPastEnd", v)}});""")

  def defaultFixedWidthGutter: Option[Boolean] = None

  def setFixedWidthGutter(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("fixedWidthGutter", v)}});""")

  def defaultCustomScrollbar: Option[Boolean] = None

  def setCustomScrollbar(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("customScrollbar", v)}});""")

  def defaultTheme: Theme.Value

  def setTheme(v: Theme.Value): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("theme", v)}});""")

  def defaultHasCssTransforms: Option[Boolean] = None

  def setHasCssTransforms(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("hasCssTransforms", v)}});""")

  def defaultMaxPixelHeight: Option[Int] = None

  def setMaxPixelHeight(v: Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("maxPixelHeight", v)}});""")

  def defaultUseSvgGutterIcons: Option[Boolean] = None

  def setUseSvgGutterIcons(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("useSvgGutterIcons", v)}});""")

  def defaultShowFoldedAnnotations: Option[Boolean] = None

  def setShowFoldedAnnotations(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("showFoldedAnnotations", v)}});""")

  def defaultUseResizeObserver: Option[Boolean] = None

  def setUseResizeObserver(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("useResizeObserver", v)}});""")

  // =================== MouseHandlerOptions ===================
  def defaultScrollSpeed: Option[Int] = None

  def setScrollSpeed(v: Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("scrollSpeed", v)}});""")

  def defaultDragDelay: Option[Int] = None

  def setDragDelay(v: Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("dragDelay", v)}});""")

  def defaultDragEnabled: Option[Boolean] = None

  def setDragEnabled(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("dragEnabled", v)}});""")

  def defaultFocusTimeout: Option[Int] = None

  def setFocusTimeout(v: Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("focusTimeout", v)}});""")

  def defaultTooltipFollowsMouse: Option[Boolean] = None

  def setTooltipFollowsMouse(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("tooltipFollowsMouse", v)}});""")

  // =================== EditorOptions ===================

  def defaultSelectionStyle: Option[SelectionStyle.Value] = None

  def setSelectionStyle(v: SelectionStyle.Value): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("selectionStyle", v)}});""")

  def defaultHighlightActiveLine: Option[Boolean] = None

  def setHighlightActiveLine(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("highlightActiveLine", v)}});""")

  def defaultHighlightSelectedWord: Option[Boolean] = None

  def setHighlightSelectedWord(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("highlightSelectedWord", v)}});""")

  def defaultReadOnly: Option[Boolean] = None

  def setReadOnly(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("readOnly", v)}});""")

  /** copy/cut the full line if selection is empty, defaults to false
    */
  def defaultCopyWithEmptySelection: Option[Boolean] = None

  def setCopyWithEmptySelection(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("copyWithEmptySelection", v)}});""")

  def defaultCursorStyle: Option[CursorStyle.Value] = None

  def setCursorStyle(v: CursorStyle.Value): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("cursorStyle", v)}});""")

  def defaultMergeUndoDeltas: Option[Either[Boolean, "always"]] = None

  def setMergeUndoDeltas(v: Either[Boolean, "always"]): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("mergeUndoDeltas", v)}});""")

  def defaultBehavioursEnabled: Option[Boolean] = None

  def setBehavioursEnabled(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("behavioursEnabled", v)}});""")

  def defaultWrapBehavioursEnabled: Option[Boolean] = None

  def setWrapBehavioursEnabled(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("wrapBehavioursEnabled", v)}});""")

  def defaultEnableAutoIndent: Option[Boolean] = None

  def setEnableAutoIndent(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("enableAutoIndent", v)}});""")

  def defaultEnableBasicAutocompletion: Option[Boolean] = None // TODO: : boolean | Completer[];

  def setEnableBasicAutocompletion(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("enableBasicAutocompletion", v)}});""")

  def defaultEnableLiveAutocompletion: Option[Boolean] = None // TODO: : boolean | Completer[];

  def setEnableLiveAutocompletion(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("enableLiveAutocompletion", v)}});""")

  def defaultLiveAutocompletionDelay: Option[Int] = None

  def setLiveAutocompletionDelay(v: Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("liveAutocompletionDelay", v)}});""")

  def defaultLiveAutocompletionThreshold: Option[Int] = None

  def setLiveAutocompletionThreshold(v: Int): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("liveAutocompletionThreshold", v)}});""")

  def defaultEnableSnippets: Option[Boolean] = None

  def setEnableSnippets(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("enableSnippets", v)}});""")

  /** this is needed if editor is inside scrollable page
    */
  def defaultAutoScrollEditorIntoView: Option[Boolean] = None

  def setAutoScrollEditorIntoView(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("autoScrollEditorIntoView", v)}});""")

  def defaultKeyboardHandler: Option[Either[String, Null]] = None

  def setKeyboardHandler(v: Either[String, Null]): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("keyboardHandler", v)}});""")

  def defaultPlaceholder: Option[String] = None

  def setPlaceholder(v: String): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("placeholder", v)}});""")

  def defaultRelativeLineNumbers: Option[Boolean] = None

  def setRelativeLineNumbers(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("relativeLineNumbers", v)}});""")

  def defaultEnableMultiselect: Option[Boolean] = None

  def setEnableMultiselect(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("enableMultiselect", v)}});""")

  def defaultEnableKeyboardAccessibility: Option[Boolean] = None

  def setEnableKeyboardAccessibility(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("enableKeyboardAccessibility", v)}});""")

  def defaultEnableCodeLens: Option[Boolean] = None

  def setEnableCodeLens(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("enableCodeLens", v)}});""")

  def defaultTextInputAriaLabel: Option[String] = None

  def setTextInputAriaLabel(v: String): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("textInputAriaLabel", v)}});""")

  def defaultEnableMobileMenu: Option[Boolean] = None

  def setEnableMobileMenu(v: Boolean): Js = Js(s"""ace.edit("$elemId").setOptions({${toJson("enableMobileMenu", v)}});""")

  def addCommand(name: String, bindKey: BindKey, exec: FSContext => Js)(implicit fsc: FSContext): Js = {
    import com.fastscala.components.utils.upickle.JsWriter
    import upickle.default.*
    fsc.runInNewOrRenewedChildContextFor((this, bindKey))(implicit fsc => {
      Js(s"""editor.commands.addCommand(${write(Command(name, bindKey, exec(fsc)))})""")
    })
  }

  def aceEditorJsReference: Js = Js(s"""ace.edit("$elemId")""")

  def currentId: Js = Js(s"""$aceEditorJsReference.currentId""")

  def onChangeServerSide(currentValue: String)(implicit fsc: FSContext): Js = JS.void

  var lastId = 0L
  var changesQueue = List[OnChangeEvent]()

  def applyOnChangeEventToCurrentState(event: OnChangeEvent)(implicit fsc: FSContext): Js = synchronized {
    changesQueue ::= event

    changesQueue
      .sortBy(_.id)
      .map(event => {

        if (event.id == lastId + 1) {
          //        println("EVENT: " + event)
          //        println("INITIAL STATE:")
          //        currentValueHolder().zipWithIndex.foreach({
          //          case (line, idx) => printf("%4d: %s\n", idx, line)
          //        })

          val state = currentValueHolder()

          event match {
            case OnChangeEvent(start, end, "remove", lines, id) if start.row == end.row =>
              val line = state(start.row)
              state(start.row) = line.substring(0, start.column) + line.substring(end.column, line.length)
            case OnChangeEvent(start, end, "remove", lines, id) =>
              state(start.row) = state(start.row).take(start.column) + state(end.row).drop(end.column)
              state.remove(start.row + 1, end.row - start.row)
            case OnChangeEvent(start, end, "insert", List(text), id) if start.row == end.row =>
              val line = state(start.row)
              state(start.row) = line.substring(0, start.column) + text + line.substring(start.column, line.length)
            case OnChangeEvent(start, end, "insert", firstLine :: rest, id) =>
              val line = state(start.row)
              state(start.row) = line.substring(0, start.column) + firstLine
              state.insertAll(start.row + 1, rest.dropRight(1) ::: (rest.last + line.substring(start.column, line.length)) :: Nil)
            case OnChangeEvent(start, end, action, lines, id) => println(s"UNKNOWN ACTION: $action")
          }

          //        println("FINAL STATE:")
          //        currentValueHolder().zipWithIndex.foreach({
          //          case (line, idx) => printf("%4d: %s\n", idx, line)
          //        })

          lastId = event.id

          onChangeServerSide(currentValue)
        } else {
          JS.void
        }

      })
      .reduceOption(_ & _)
      .getOrElse(JS.void)
  }

  def toJson(name: String, v: Any): String = (name, v) match {
    case (name, Left(v))  => toJson(name, v)
    case (name, Right(v)) => toJson(name, v)

    case ("theme", v: Enumeration#Value) => s"$name: 'ace/theme/$v'"
    case ("mode", v: Enumeration#Value)  => s"$name: 'ace/mode/$v'"
    case (name, v: Enumeration#Value)    => s"$name: '$v'"
    case (name, s: String)               => s"$name: '$s'"
    case (name, null)                    => s"$name: null"
    case (name, other)                   => s"$name: $other"
  }

  def defaultOptionsJson: String = List(
    defaultWrap.map(v => toJson("wrap", v)),
    defaultWrapMethod.map(v => toJson("wrapMethod", v)),
    defaultIndentedSoftWrap.map(v => toJson("indentedSoftWrap", v)),
    defaultFirstLineNumber.map(v => toJson("firstLineNumber", v)),
    defaultUseWorker.map(v => toJson("useWorker", v)),
    defaultUseSoftTabs.map(v => toJson("useSoftTabs", v)),
    defaultTabSize.map(v => toJson("tabSize", v)),
    defaultNavigateWithinSoftTabs.map(v => toJson("navigateWithinSoftTabs", v)),
    defaultFoldStyle.map(v => toJson("foldStyle", v)),
    defaultOverwrite.map(v => toJson("overwrite", v)),
    defaultNewLineMode.map(v => toJson("newLineMode", v)),
    Some(defaultMode).map(v => toJson("mode", v)),
    defaultAnimatedScroll.map(v => toJson("animatedScroll", v)),
    defaultShowInvisibles.map(v => toJson("showInvisibles", v)),
    defaultShowPrintMargin.map(v => toJson("showPrintMargin", v)),
    defaultPrintMarginColumn.map(v => toJson("printMarginColumn", v)),
    defaultPrintMargin.map(v => toJson("printMargin", v)),
    defaultShowGutter.map(v => toJson("showGutter", v)),
    defaultFadeFoldWidgets.map(v => toJson("fadeFoldWidgets", v)),
    defaultShowFoldWidgets.map(v => toJson("showFoldWidgets", v)),
    defaultShowLineNumbers.map(v => toJson("showLineNumbers", v)),
    defaultDisplayIndentGuides.map(v => toJson("displayIndentGuides", v)),
    defaultHighlightIndentGuides.map(v => toJson("highlightIndentGuides", v)),
    defaultHighlightGutterLine.map(v => toJson("highlightGutterLine", v)),
    defaultHScrollBarAlwaysVisible.map(v => toJson("hScrollBarAlwaysVisible", v)),
    defaultVScrollBarAlwaysVisible.map(v => toJson("vScrollBarAlwaysVisible", v)),
    defaultFontSize.map(v => toJson("fontSize", v)),
    defaultFontFamily.map(v => toJson("fontFamily", v)),
    defaultMaxLines.map(v => toJson("maxLines", v)),
    defaultMinLines.map(v => toJson("minLines", v)),
    defaultScrollPastEnd.map(v => toJson("scrollPastEnd", v)),
    defaultFixedWidthGutter.map(v => toJson("fixedWidthGutter", v)),
    defaultCustomScrollbar.map(v => toJson("customScrollbar", v)),
    Some(defaultTheme).map(v => toJson("theme", v)),
    defaultHasCssTransforms.map(v => toJson("hasCssTransforms", v)),
    defaultMaxPixelHeight.map(v => toJson("maxPixelHeight", v)),
    defaultUseSvgGutterIcons.map(v => toJson("useSvgGutterIcons", v)),
    defaultShowFoldedAnnotations.map(v => toJson("showFoldedAnnotations", v)),
    defaultUseResizeObserver.map(v => toJson("useResizeObserver", v)),
    defaultScrollSpeed.map(v => toJson("scrollSpeed", v)),
    defaultDragDelay.map(v => toJson("dragDelay", v)),
    defaultDragEnabled.map(v => toJson("dragEnabled", v)),
    defaultFocusTimeout.map(v => toJson("focusTimeout", v)),
    defaultTooltipFollowsMouse.map(v => toJson("tooltipFollowsMouse", v)),
    defaultSelectionStyle.map(v => toJson("selectionStyle", v)),
    defaultHighlightActiveLine.map(v => toJson("highlightActiveLine", v)),
    defaultHighlightSelectedWord.map(v => toJson("highlightSelectedWord", v)),
    defaultReadOnly.map(v => toJson("readOnly", v)),
    defaultCopyWithEmptySelection.map(v => toJson("copyWithEmptySelection", v)),
    defaultCursorStyle.map(v => toJson("cursorStyle", v)),
    defaultMergeUndoDeltas.map(v => toJson("mergeUndoDeltas", v)),
    defaultBehavioursEnabled.map(v => toJson("behavioursEnabled", v)),
    defaultWrapBehavioursEnabled.map(v => toJson("wrapBehavioursEnabled", v)),
    defaultEnableAutoIndent.map(v => toJson("enableAutoIndent", v)),
    defaultEnableBasicAutocompletion.map(v => toJson("enableBasicAutocompletion", v)),
    defaultEnableLiveAutocompletion.map(v => toJson("enableLiveAutocompletion", v)),
    defaultLiveAutocompletionDelay.map(v => toJson("liveAutocompletionDelay", v)),
    defaultLiveAutocompletionThreshold.map(v => toJson("liveAutocompletionThreshold", v)),
    defaultEnableSnippets.map(v => toJson("enableSnippets", v)),
    defaultAutoScrollEditorIntoView.map(v => toJson("autoScrollEditorIntoView", v)),
    defaultKeyboardHandler.map(v => toJson("keyboardHandler", v)),
    defaultPlaceholder.map(v => toJson("placeholder", v)),
    defaultRelativeLineNumbers.map(v => toJson("relativeLineNumbers", v)),
    defaultEnableMultiselect.map(v => toJson("enableMultiselect", v)),
    defaultEnableKeyboardAccessibility.map(v => toJson("enableKeyboardAccessibility", v)),
    defaultEnableCodeLens.map(v => toJson("enableCodeLens", v)),
    defaultTextInputAriaLabel.map(v => toJson("textInputAriaLabel", v)),
    defaultEnableMobileMenu.map(v => toJson("enableMobileMenu", v))
  ).flatten.mkString("{", ",", "}")

  def jsRef = Js(s"ace.edit(\"$elemId\")")

  def onChange(js: JsFunc1): Js = Js(s"$jsRef.session.on('change', $js);")

  def onChangeSelection(js: Js): Js = Js(s"$jsRef.session.on('changeSelection', $js);")

  def setupInternalChangeListener()(implicit fsc: FSContext): Js = {
    import OnChangeEvent.*
    onChange(JsFunc1(arg => {
      Js(s"""var editor = ace.edit("$elemId");
            |if (typeof editor.currentId == "undefined") { editor.currentId = 0 }
            |editor.currentId = editor.currentId + 1;
            |$arg.id = editor.currentId;
            |""".stripMargin) &
        fsc.callbackJSONDecoded[OnChangeEvent](arg, event => applyOnChangeEventToCurrentState(event))
    }))
  }

  def initialize()(implicit fsc: FSContext): Js = {
    Js(s"""var editor = ace.edit("$elemId", $defaultOptionsJson);""") &
      setupInternalChangeListener()
  }

  def render()(implicit fsc: FSContext): Elem = {
    <div id={elemId}>{currentValue}</div>
  }
}
