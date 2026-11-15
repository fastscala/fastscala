package com.fastscala.core

import com.fastscala.js.Js
import com.fastscala.testutils.TestRequests.request
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream

/**
 * Sessions, pages and contexts (and the callbacks, file uploads/downloads registered in them), outside a server.
 *
 * One spec for all of them: FSStats registers its metrics (the gauges checked here) globally, so there's one FSSystem
 * per JVM ([[FSLifecycleSpec.system]]), and its gauges must not be changed by other suites running at the same time.
 */
class FSLifecycleSpec extends AnyFlatSpec {

  import FSLifecycleSpec.*

  private given req: org.eclipse.jetty.server.Request = request("GET", "/page")

  private def newSession(): FSSession = {
    val s = new FSSession(system.synchronized { counter += 1; s"session-$counter" }, system)
    system.sessions(s.id) = s
    s
  }

  private def newPage(session: FSSession = newSession()): FSPage = {
    var page: FSPage = null
    session.createAndRenderPageWithImpl(new FSPageImpl[Unit] {
      override def render()(implicit fsc: FSContext): Unit = page = fsc.page
    })
    page
  }

  private def gauge(g: io.prometheus.metrics.core.metrics.Gauge): Double = g.get()

  // ---------- session data and ids ----------

  "FSSession data" should "be set, read, and cleared, by key" in {
    val s = newSession()
    assert(s.getDataOpt[String]("k").isEmpty)
    s.setData("k", "v")
    assert(s.getData[String]("k") == "v")
    assert(s.getDataOpt[String]("k").contains("v"))
    s.clear("k")
    assert(s.getDataOpt[String]("k").isEmpty)
    assertThrows[NoSuchElementException](s.getData[String]("k"))
  }

  "FSSession.nextID" should "be a sequence (random-ids is off by default), with the prefix" in {
    val s = newSession()
    assert(!system.useRandomIds)
    val a = s.nextID()
    val b = s.nextID("p")
    assert(a == "1" && b == "p2", (a, b))
  }

  "FSSessionVar" should "be the default until set, per session" in {
    object Counter extends FSSessionVar[Int](10)
    val s1 = newSession()
    val s2 = newSession()
    assert(Counter()(using s1) == 10)
    Counter.update(11)(using s1)
    assert(Counter()(using s1) == 11)
    assert(Counter()(using s2) == 10)
  }

  it should "evaluate the default once per session (it's stored)" in {
    var evaluated = 0
    object Lazy extends FSSessionVar[String]({ evaluated += 1; "d" })
    val s = newSession()
    Lazy()(using s)
    Lazy()(using s)
    assert(evaluated == 1)
  }

  "FSSessionVarOpt" should "be None until set, and cleared by setOrClear(None)" in {
    object Name extends FSSessionVarOpt[String]
    val s = newSession()
    assert(Name()(using s).isEmpty)
    Name.update("x")(using s)
    assert(Name()(using s).contains("x"))
    Name.setOrClear(Some("y"))(using s)
    assert(Name()(using s).contains("y"))
    Name.setOrClear(None)(using s)
    assert(Name()(using s).isEmpty)
    Name.update("z")(using s)
    Name.clear()(using s)
    assert(Name()(using s).isEmpty)
  }

  // ---------- pages ----------

  "FSSession.createPage" should "register a page, render it in its root context, and return what's rendered" in {
    val s = newSession()
    var ctx: FSContext = null
    val rendered = s.createPage(fsc => { ctx = fsc; "html" })
    assert(rendered == "html")
    assert(s.pages.size == 1)
    val page = s.pages.values.head
    assert(ctx eq page.rootFSContext)
    assert(page.session eq s)
    assert(s.nPages() == 1)
  }

  it should "give the page its debug label and onPageUnload" in {
    // Regression (fixed): createPage doesn't pass debugLbl and onPageUnload on to createAndRenderPageWithImpl: they're dropped.
    val s = newSession()
    s.createPage(_ => (), debugLbl = Some("my-page"), onPageUnload = () => Js("bye()"))
    val page = s.pages.values.head
    assert(page.debugLbl.contains("my-page"))
    assert(page.onPageUnload().cmd == "bye()")
  }

  "FSSession.deletePagesOlderThan" should "delete the pages not kept alive since then (and their anonymous pages)" in {
    val s = newSession()
    val oldPage = newPage(s)
    val newer = newPage(s)
    oldPage.keepAliveAt = 1000
    s.anonymousPages("anon-old") = new FSAnonymousPage("anon-old", s, _ => "x")
    s.anonymousPages("anon-old").keepAliveAt = 1000
    s.deletePagesOlderThan(2000)
    assert(s.pages.keySet == Set(newer.id))
    assert(s.anonymousPages.isEmpty)
  }

  "FSSession.deletePages" should "delete only the given pages that are this session's" in {
    val s = newSession()
    val p1 = newPage(s)
    val p2 = newPage(s)
    val other = newPage()
    s.deletePages(Set(p1, other))
    assert(s.pages.keySet == Set(p2.id))
    assert(other.session.pages.contains(other.id))
  }

  "FSSession.delete" should "remove the session from the system, with its pages" in {
    val s = newSession()
    newPage(s)
    s.delete()
    assert(!system.sessions.contains(s.id))
    assert(s.pages.isEmpty && s.anonymousPages.isEmpty)
  }

  it should "update the current pages gauge" in {
    // Regression (fixed): the gauges are decremented by pages.size / anonymousPages.size after the maps are emptied: by 0.
    system.synchronized {
      val s = newSession()
      newPage(s)
      newPage(s)
      val before = gauge(system.stats.currentPages)
      s.delete()
      assert(gauge(system.stats.currentPages) == before - 2)
    }
  }

  "FSPage.delete" should "remove its callbacks, file uploads and downloads" in {
    val page = newPage()
    val root = page.rootFSContext
    root.callback(() => Js("a()"))
    root.fileUploadActionUrl(_ => Js.Void)
    root.fileDownloadByteArray("f.txt", "text/plain", () => Array[Byte](1))
    page.delete()
    assert(page.callbacks.isEmpty && page.fileUploadCallbacks.isEmpty && page.fileDownloadCallbacks.isEmpty)
  }

  it should "forget the deleted callbacks in the contexts that created them" in {
    // Regression (fixed): it removes the page's id (`-= id`) from each context's functionsGenerated, not the callback's.
    val page = newPage()
    val root = page.rootFSContext
    root.callback(() => Js("a()"))
    page.delete()
    assert(root.functionsGenerated.isEmpty, root.functionsGenerated)
  }

  it should "update the current callbacks gauge" in {
    // Regression (fixed): decremented by callbacks.size after the map is emptied: by 0.
    system.synchronized {
      val page = newPage()
      page.rootFSContext.callback(() => Js("a()"))
      page.rootFSContext.callback(() => Js("b()"))
      val before = gauge(system.stats.currentCallbacks)
      page.delete()
      assert(gauge(system.stats.currentCallbacks) == before - 2)
    }
  }

  "FSPage.fsPageScript" should "define window._fs, with the session's and page's ids, and FastScala's paths" in {
    val page = newPage()
    val script = page.fsPageScript()(using page.rootFSContext).cmd
    assert(script.contains(s"""sessionId: ${JS.asJsStr(page.session.id).cmd},"""), script)
    assert(script.contains(s"""pageId: ${JS.asJsStr(page.id).cmd},"""), script)
    assert(script.contains(s""""/${system.FSPrefix}/cb/"+pageId+"/"+funcId"""), script)
    assert(script.contains(s""""/${system.FSPrefix}/ka/"+pageId"""), script)
    assert(!script.contains("window._fs.initWebSocket();"), script)
    assert(page.fsPageScript(openWSSessionAtStart = true)(using page.rootFSContext).cmd.contains("window._fs.initWebSocket();"))
  }

  "FSPage.inContextFor" should "create a context for a key once, and reuse it" in {
    val page = newPage()
    val key = new Object
    val c1 = page.inContextFor(key)(identity)
    val c2 = page.inContextFor(key)(identity)
    assert(c1 eq c2)
    assert(page.key2FSContext(key) eq c1)
  }

  "FSPage.inContextClearedFor" should "delete the key's context (and its callbacks), and give a new one in its place" in {
    val page = newPage()
    val key = new Object
    val old = page.rootFSContext.runInContextFor(key)(identity)
    old.callback(() => Js("old()"))
    assert(page.callbacks.size == 1)
    val fresh = page.inContextClearedFor(key)(identity)
    assert(old.deleted && !fresh.deleted)
    assert(fresh ne old)
    assert(page.key2FSContext(key) eq fresh)
    assert(fresh.parentFSContext.contains(page.rootFSContext))
    assert(page.rootFSContext.children.contains(fresh) && !page.rootFSContext.children.contains(old))
    assert(page.callbacks.isEmpty)
  }

  it should "be inContextFor when there's no context for the key yet" in {
    val page = newPage()
    val key = new Object
    val c = page.inContextClearedFor(key)(identity)
    assert(page.key2FSContext(key) eq c)
  }

  "FSPage.inContextForOption" should "be None without a (live) context for the key" in {
    val page = newPage()
    val key = new Object
    assert(page.inContextForOption(key)(identity).isEmpty)
    val c = page.rootFSContext.runInContextFor(key)(identity)
    assert(page.inContextForOption(key)(identity).contains(c))
    c.delete()
    assert(page.inContextForOption(key)(identity).isEmpty)
  }

  "FSPage.deleteContextFor" should "delete the key's context, if there is one" in {
    val page = newPage()
    val key = new Object
    page.deleteContextFor(key)
    val c = page.rootFSContext.runInContextFor(key)(identity)
    page.deleteContextFor(key)
    assert(c.deleted)
    page.deleteContextFor(key) // already deleted: nothing happens
  }

  "FSPage.setupKeepAlive" should "send keep-alives, every period" in {
    val page = newPage()
    val code = page.setupKeepAlive().cmd
    assert(code.contains(s"""window._fs.keepAlive("${page.id}")"""), code)
    assert(code.contains(s"setTimeout(sendKeepAlive, ${page.periodicKeepAlivePeriod})"), code)
  }

  // ---------- gc ----------

  /** A system whose memory is always this free (sharing the stats: they're registered once per JVM). */
  private def systemWithFreeMemory(percent: Double): FSSystemImpl = new FSSystemImpl(appName = "gc-test", stats = system.stats) {
    override protected def freeMemoryPercent(): Double = percent
  }

  private def addSessionWithPages(fss: FSSystemImpl, pages: Int): FSSession = {
    val s = new FSSession(fss.synchronized { counter += 1; s"gc-session-$counter" }, fss)
    fss.sessions(s.id) = s
    for (_ <- 1 to pages) {
      val page = new FSPage(s.nextID(), s, req, new FSPageImpl[Unit] { override def render()(implicit fsc: FSContext): Unit = () })
      s.pages(page.id) = page
    }
    s
  }

  "FSSystemImpl.gc" should "do nothing while there's enough free memory" in {
    val fss = systemWithFreeMemory(100)
    addSessionWithPages(fss, 3)
    fss.gc()
    assert(fss.sessions.size == 1 && fss.sessions.values.head.pages.size == 3)
  }

  it should "delete pages (and the sessions left empty) while memory is low, until nothing is left" in {
    val fss = systemWithFreeMemory(0)
    addSessionWithPages(fss, 5)
    addSessionWithPages(fss, 2)
    fss.gc()
    assert(fss.sessions.isEmpty)
  }

  it should "stop when nothing is left to delete (not call itself until the stack overflows)" in {
    val fss = systemWithFreeMemory(0)
    fss.gc() // nothing at all
    val s = addSessionWithPages(fss, 0)
    fss.gc()
    assert(fss.sessions.isEmpty, "the empty session should be deleted")
  }

  // ---------- contexts ----------

  "FSContext.callback" should "register the function in the page, and be the Js that calls it" in {
    val page = newPage()
    val root = page.rootFSContext
    val js = root.callback(Js("document.title"), arg => Js(s"got($arg)")).cmd
    val (funcId, func) = page.callbacks.head
    assert(js == s"""window._fs.callback(document.title,"${page.id}","$funcId",false,true,true,{});""", js)
    assert(func.func("x").cmd == "got(x)")
    assert(root.functionsGenerated.contains(funcId))
  }

  it should "pass '' as the argument when there's none, and the flags given" in {
    val page = newPage()
    val js = page.rootFSContext.callback(Js.Void, _ => Js.Void, async = false, expectReturn = false, ignoreErrors = true, env = Js("{a:1}")).cmd
    assert(js.matches("""window\._fs\.callback\('',"[^"]+","[^"]+",true,false,false,\{a:1\}\);"""), js)
  }

  "FSContext.exec" should "be a callback with '' as the argument" in {
    val page = newPage()
    var ran = false
    val js = page.rootFSContext.exec(() => { ran = true; Js.Void }).cmd
    assert(js.startsWith("window._fs.callback(''"), js)
    page.callbacks.head._2.func("")
    assert(ran)
  }

  "FSContext.runInNewOrRenewedChildContextFor" should "replace the key's context by a new child, deleting the old one's callbacks" in {
    val page = newPage()
    val root = page.rootFSContext
    val key = new Object
    val c1 = root.runInNewOrRenewedChildContextFor(key, Some("child"))(identity)
    c1.callback(() => Js("x()"))
    val c2 = root.runInNewOrRenewedChildContextFor(key)(identity)
    assert(c1.deleted && !c2.deleted)
    assert(root.children.toSet == Set(c2))
    assert(page.key2FSContext(key) eq c2)
    assert(page.callbacks.isEmpty)
    assert(c2.depth == 1 && root.depth == 0)
  }

  "FSContext.runInContextFor" should "reuse the key's context" in {
    val page = newPage()
    val key = new Object
    val c1 = page.rootFSContext.runInContextFor(key)(identity)
    val c2 = page.rootFSContext.runInContextFor(key)(identity)
    assert(c1 eq c2)
    assert(c1.debugLbl.contains(s"ctx4$key"))
  }

  it should "refuse to create a child of a deleted context" in {
    val page = newPage()
    val c = page.rootFSContext.runInContextFor(new Object)(identity)
    c.delete()
    assertThrows[Exception](c.runInContextFor(new Object)(identity))
  }

  "FSContext.delete" should "delete it and its descendants, with their callbacks, uploads and downloads" in {
    val page = newPage()
    val root = page.rootFSContext
    val (k1, k2) = (new Object, new Object)
    val child = root.runInContextFor(k1)(identity)
    val grandchild = child.runInContextFor(k2)(identity)
    child.callback(() => Js.Void)
    grandchild.callback(() => Js.Void)
    grandchild.fileUploadActionUrl(_ => Js.Void)
    grandchild.fileDownloadByteArray("a.txt", "text/plain", () => Array[Byte]())
    val kept = root.callback(() => Js.Void)
    assert(page.callbacks.size == 3)
    child.delete()
    assert(child.deleted && grandchild.deleted && !root.deleted)
    assert(page.callbacks.size == 1)
    assert(page.fileUploadCallbacks.isEmpty && page.fileDownloadCallbacks.isEmpty)
    assert(!page.key2FSContext.contains(k1) && !page.key2FSContext.contains(k2))
    assert(root.children.isEmpty)
  }

  "FSContext.all" should "be it and all its descendants" in {
    val page = newPage()
    val root = page.rootFSContext
    val a = root.runInContextFor(new Object)(identity)
    val b = a.runInContextFor(new Object)(identity)
    val c = root.runInContextFor(new Object)(identity)
    assert(root.all.toSet == Set(root, a, b, c))
    assert(a.all.toSet == Set(a, b))
  }

  "FSContext.fullPath" should "be the labels from the root, with deleted contexts marked" in {
    val page = newPage()
    val child = page.rootFSContext.runInNewOrRenewedChildContextFor(new Object, Some("modal"))(identity)
    assert(child.fullPath == "page_root_context => modal")
    child.delete()
    assert(child.fullPath == "page_root_context => [del: modal]")
  }

  "FSContext.deleteOlderThan" should "delete the contexts not kept alive since then" in {
    val page = newPage()
    val root = page.rootFSContext
    val old = root.runInContextFor(new Object)(identity)
    val recent = root.runInContextFor(new Object)(identity)
    old.keepAliveAt = 1000
    root.deleteOlderThan(2000)
    assert(old.deleted && !recent.deleted && !root.deleted)
  }

  "FSContext.keepAlive" should "keep alive the context, its ancestors, the page and the session" in {
    val page = newPage()
    val root = page.rootFSContext
    val child = root.runInContextFor(new Object)(identity)
    root.keepAliveAt = 1
    child.keepAliveAt = 1
    page.keepAliveAt = 1
    page.session.keepAliveAt = 1
    child.keepAlive()
    assert(child.keepAliveAt > 1 && root.keepAliveAt > 1 && page.keepAliveAt > 1 && page.session.keepAliveAt > 1)
  }

  "FSContext.fileDownload*" should "register the download, and be its URL (the file name URL-encoded)" in {
    val page = newPage()
    val url = page.rootFSContext.fileDownloadByteArray("relatório final.pdf", "application/pdf", () => Array[Byte](1, 2))
    val (id, download) = page.fileDownloadCallbacks.head
    assert(url == s"/${system.FSPrefix}/file-download/${page.id}/$id/relat%C3%B3rio+final.pdf", url)
    assert(download.contentType == "application/pdf" && download.fileName == "relatório final.pdf")
    assert(download.content.left.toOption.get().sameElements(Array[Byte](1, 2)))
  }

  it should "stream, with the size given" in {
    val page = newPage()
    page.rootFSContext.fileDownloadStreaming("a.csv", "text/csv", out => out.write("a,b".getBytes), size = Some(3))
    val download = page.fileDownloadCallbacks.head._2
    val (write, size) = download.content.toOption.get
    val bytes = new ByteArrayOutputStream()
    write(bytes)
    assert(bytes.toString == "a,b" && size.contains(3))
  }

  it should "detect the content type from the file name" in {
    val page = newPage()
    page.rootFSContext.fileDownloadByteArrayAutodetectContentType("x.html", () => Array[Byte]())
    assert(page.fileDownloadCallbacks.head._2.contentType == "text/html")
  }

  "FSContext.fileUploadActionUrl" should "register the upload, and be its URL" in {
    val page = newPage()
    val url = page.rootFSContext.fileUploadActionUrl(files => Js(s"got(${files.size})"))
    val (id, upload) = page.fileUploadCallbacks.head
    assert(url == s"/${system.FSPrefix}/file-upload/${page.id}/$id")
    assert(upload.func(Seq(new FSUploadedFile("a", "text/plain", () => Array(), () => null))).cmd == "got(1)")
  }

  "FSContext.anonymousPageURL" should "register an anonymous page in the session, and be its URL" in {
    val page = newPage()
    val url = page.rootFSContext.anonymousPageURL(_ => "<p>hi</p>", "report")
    val (id, anon) = page.session.anonymousPages.head
    assert(url == s"/${system.FSPrefix}/anon/$id/report")
    assert(anon.session eq page.session)
  }

  "FSUploadedFile" should "be matched as (name, content type, bytes)" in {
    val f = new FSUploadedFile("a.txt", "text/plain", () => "abc".getBytes, () => null)
    f match {
      case FSUploadedFile(name, ct, bytes) => assert(name == "a.txt" && ct == "text/plain" && new String(bytes) == "abc")
    }
    assert(f.toString == "FSUploadedFile(name: a.txt, contentType: text/plain)")
  }
}

object FSLifecycleSpec {
  // FSStats registers its metrics globally: a single FSSystem per JVM.
  lazy val system: FSSystem = new FSSystemImpl(appName = "fs-lifecycle-spec")
  private var counter = 0
}
