package com.fastscala.core

import com.fastscala.js.Js
import com.fastscala.testutils.TestRequests
import org.eclipse.jetty.server.{Server, ServerConnector}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Duration

/**
 * FSSystemImpl as the Jetty handler of FastScala's own requests, over HTTP: callbacks, keep-alives, file downloads and
 * uploads, anonymous pages, and what's answered when the session, page or function isn't found.
 */
class FSSystemHandlerSpec extends AnyFlatSpec with BeforeAndAfterAll {

  private val system = FSLifecycleSpec.system // (one FSSystem per JVM: see FSLifecycleSpec)

  private val server = new Server()
  private val connector = new ServerConnector(server)
  connector.setPort(0)
  server.addConnector(connector)
  server.setHandler(system)

  /** A client of its own for each request (a new connection), unless one is given: see the test of connection reuse. */
  private def newClient(): HttpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(5)).build()

  override protected def beforeAll(): Unit = server.start()

  override protected def afterAll(): Unit = server.stop()

  private def url(path: String): URI = URI.create(s"http://localhost:${connector.getLocalPort}$path")

  private def post(path: String, body: String, session: Option[FSSession], contentType: String = "text/plain;charset=utf-8", timeoutSeconds: Int = 10): HttpResponse[String] = {
    val b = HttpRequest.newBuilder(url(path)).timeout(Duration.ofSeconds(timeoutSeconds)).header("Content-Type", contentType).POST(HttpRequest.BodyPublishers.ofString(body, UTF_8))
    session.foreach(s => b.header("Cookie", s"${system.FSSessionIdCookieName}=${s.id}"))
    newClient().send(b.build(), HttpResponse.BodyHandlers.ofString(UTF_8))
  }

  private def get(path: String, session: Option[FSSession], client: HttpClient = newClient(), timeoutSeconds: Int = 10): HttpResponse[Array[Byte]] = {
    val b = HttpRequest.newBuilder(url(path)).timeout(Duration.ofSeconds(timeoutSeconds)).GET()
    session.foreach(s => b.header("Cookie", s"${system.FSSessionIdCookieName}=${s.id}"))
    client.send(b.build(), HttpResponse.BodyHandlers.ofByteArray())
  }

  private def newPage(): FSPage = {
    val session = system.createSession()
    var page: FSPage = null
    session.createAndRenderPageWithImpl(new FSPageImpl[Unit] {
      override def render()(implicit fsc: FSContext): Unit = page = fsc.page
    })(using TestRequests.request("GET", "/"))
    page
  }

  private def funcIdOf(js: Js): String = """window\._fs\.callback\([^,]*,"[^"]+","([^"]+)"""".r.findFirstMatchIn(js.cmd).get.group(1)

  private val Prefix = "/" + FSLifecycleSpec.system.FSPrefix

  "A callback" should "run with what's posted, and answer its Js" in {
    val page = newPage()
    var got: String = null
    val funcId = funcIdOf(page.rootFSContext.callback(Js("x"), arg => { got = arg; Js(s"done('${arg.length}')") }))
    val resp = post(s"$Prefix/cb/${page.id}/$funcId", "é 😀 <x>", Some(page.session))
    assert(resp.statusCode() == 200)
    assert(resp.body() == "done('8')", resp.body())
    assert(got == "é 😀 <x>")
    assert(resp.headers().firstValue("Content-Type").orElse("").contains("javascript"), resp.headers())
  }

  it should "find the session from the fssid parameter too" in {
    val page = newPage()
    val funcId = funcIdOf(page.rootFSContext.callback(() => Js("ok()")))
    assert(post(s"$Prefix/cb/${page.id}/$funcId?${system.FSSessionIdCookieName}=${page.session.id}", "", None).body() == "ok()")
  }

  it should "answer an alert when it throws" in pendingUntilFixed {
    // BUG: handleCallbackException reads `debug` (config com.fastscala.core.debug), which reference.conf makes an object
    // (com.fastscala.core.debug.deleted_contexts_full_path follows debug=true): getBoolean throws, inside the body's
    // whenComplete, and no response is ever written: the request hangs until it times out.
    val page = newPage()
    val funcId = funcIdOf(page.rootFSContext.callback(() => throw new IllegalStateException("callback failed")))
    val body = post(s"$Prefix/cb/${page.id}/$funcId", "", Some(page.session), timeoutSeconds = 3).body()
    assert(body.contains("alert(") && body.contains("Internal\\x20error"), body)
  }

  it should "keep its context, page and session alive" in {
    val page = newPage()
    val funcId = funcIdOf(page.rootFSContext.callback(() => Js.Void))
    page.keepAliveAt = 1
    page.session.keepAliveAt = 1
    post(s"$Prefix/cb/${page.id}/$funcId", "", Some(page.session))
    assert(page.keepAliveAt > 1 && page.session.keepAliveAt > 1)
  }

  "A callback that's not found" should "ask to reload the page (whether the function, page or session is missing)" in {
    val page = newPage()
    for ((path, session) <- Seq(
      s"$Prefix/cb/${page.id}/no-such-func" -> Some(page.session),
      s"$Prefix/cb/no-such-page/1" -> Some(page.session),
      s"$Prefix/cb/${page.id}/1" -> None)) {
      val body = post(path, "", session).body()
      assert(body.contains("confirm(") && body.contains("reload"), s"$path: $body")
    }
  }

  it should "answer nothing with ignore_errors=true" in {
    val page = newPage()
    assert(post(s"$Prefix/cb/${page.id}/no-such-func?ignore_errors=true", "", Some(page.session)).body().trim.isEmpty)
  }

  "A keep-alive" should "mark the page as alive" in {
    val page = newPage()
    page.autoKeepAliveAt = 1
    val resp = post(s"$Prefix/ka/${page.id}", "", Some(page.session))
    assert(resp.statusCode() == 200 && page.autoKeepAliveAt > 1)
    assert(post(s"$Prefix/ka/no-such-page", "", Some(page.session)).statusCode() == 200)
  }

  "A file download" should "be the bytes, with the content type, as an attachment with the file name" in {
    val page = newPage()
    val bytes = Array.tabulate[Byte](10_000)(_.toByte)
    val path = page.rootFSContext.fileDownloadByteArray("report.bin", "application/octet-stream", () => bytes)
    val resp = get(path, Some(page.session))
    assert(resp.statusCode() == 200)
    assert(resp.body().sameElements(bytes))
    assert(resp.headers().firstValue("Content-Type").orElse("").startsWith("application/octet-stream"), resp.headers())
  }

  it should "stream what's written to the output stream" in {
    val page = newPage()
    val path = page.rootFSContext.fileDownloadStreaming("a.csv", "text/csv", out => (1 to 1000).foreach(i => out.write(s"$i,${i * 2}\n".getBytes(UTF_8))))
    val resp = get(path, Some(page.session))
    assert(new String(resp.body(), UTF_8).linesIterator.toSeq == (1 to 1000).map(i => s"$i,${i * 2}"))
  }

  it should "complete the response, so the connection can be used for the next request" in pendingUntilFixed {
    // BUG: OutputStreamResponse.respond returns true (handled) but never completes Jetty's callback: the response is
    // sent (the stream is closed) but the request never completes, and the next request on the connection hangs.
    val page = newPage()
    val path = page.rootFSContext.fileDownloadStreaming("a.txt", "text/plain", out => out.write("x".getBytes(UTF_8)))
    val client = newClient()
    assert(new String(get(path, Some(page.session), client).body(), UTF_8) == "x")
    assert(get("/not-fastscala", None, client, timeoutSeconds = 3).statusCode() == 404)
  }

  it should "redirect to / when it's not found" in {
    val page = newPage()
    val resp = get(s"$Prefix/file-download/${page.id}/no-such/x.txt", Some(page.session))
    assert(resp.statusCode() == 307 || resp.statusCode() == 302, resp.statusCode())
  }

  "A file upload" should "give the function the files posted (multipart)" in {
    val page = newPage()
    var got: Seq[(String, String, String)] = Nil
    val path = page.rootFSContext.fileUploadActionUrl(files => {
      got = files.map(f => (f.name, f.contentType, new String(f.bytes(), UTF_8)))
      Js("uploaded()")
    })
    val boundary = "XyZ123"
    val body =
      s"--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"a.txt\"\r\nContent-Type: text/plain\r\n\r\nhello é\r\n" +
        s"--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"b.csv\"\r\nContent-Type: text/csv\r\n\r\n1,2\r\n" +
        s"--$boundary--\r\n"
    val resp = post(path, body, Some(page.session), contentType = s"multipart/form-data; boundary=$boundary")
    assert(resp.body() == "uploaded()", resp.body())
    assert(got == Seq(("a.txt", "text/plain", "hello é"), ("b.csv", "text/csv", "1,2")))
  }

  it should "refuse what isn't multipart" in {
    val page = newPage()
    val path = page.rootFSContext.fileUploadActionUrl(_ => Js("uploaded()"))
    assert(post(path, "x", Some(page.session)).body().contains("alert("))
  }

  it should "answer the error when the function throws" in pendingUntilFixed {
    // BUG: when the upload's function throws, its error's Js is discarded and the exception rethrown, inside the
    // parser's whenComplete: no response is ever written (the request hangs until it times out).
    val page = newPage()
    val path = page.rootFSContext.fileUploadActionUrl(_ => throw new IllegalStateException("upload failed"))
    val boundary = "B0"
    val body = s"--$boundary\r\nContent-Disposition: form-data; name=\"f\"; filename=\"a.txt\"\r\nContent-Type: text/plain\r\n\r\nx\r\n--$boundary--\r\n"
    val resp = post(path, body, Some(page.session), contentType = s"multipart/form-data; boundary=$boundary", timeoutSeconds = 3)
    assert(resp.body().contains("alert("), resp.body())
  }

  "An anonymous page" should "be rendered (in a new page of the session)" in {
    val page = newPage()
    val path = page.rootFSContext.anonymousPageURL(fsc => s"<p>anon of ${fsc.session.id.length}</p>", "report")
    val pagesBefore = page.session.pages.size
    val resp = get(path, Some(page.session))
    assert(resp.statusCode() == 200)
    assert(new String(resp.body(), UTF_8) == s"<!DOCTYPE html><p>anon of ${page.session.id.length}</p>")
    assert(page.session.pages.size == pagesBefore + 1)
  }

  it should "redirect to / when it's not found" in {
    val page = newPage()
    assert(Set(302, 307).contains(get(s"$Prefix/anon/nope/x", Some(page.session)).statusCode()))
  }

  "Other requests" should "not be handled (404)" in {
    assert(get("/not-fastscala", None).statusCode() == 404)
  }
}
