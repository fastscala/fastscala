package com.fastscala.scala_xml.routing.resp

import com.fastscala.routing.resp.{Ok, Response}
import org.eclipse.jetty.server.{Handler, Request, Server, ServerConnector, Response as JettyServerResponse}
import org.eclipse.jetty.util.Callback
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference
import scala.jdk.OptionConverters.*
import scala.xml.*

/** [[OkHtmlStreaming]] served by a real Jetty server (on a random port), fetched over HTTP/1.1. */
class OkHtmlStreamingSpec extends AnyFlatSpec with BeforeAndAfterAll {

  /** The response the server sends to the next request. */
  private val next = new AtomicReference[() => Response]()

  /** How the last response's callback was completed: None (succeeded) or the failure. */
  private val lastOutcome = new AtomicReference[Option[Throwable]]()

  /**
   * How the last response's callback was completed, waiting (up to 10 s) for it: the client can have the whole
   * response before the server's handler has returned (e.g. a single write, with a Content-Length).
   */
  private def outcome(): Option[Throwable] = {
    val deadline = System.nanoTime() + 10_000_000_000L
    while (lastOutcome.get() == null && System.nanoTime() < deadline) Thread.sleep(5)
    val o = lastOutcome.get()
    assert(o != null, "the response's callback wasn't completed")
    o
  }

  private val server = new Server()
  private val connector = new ServerConnector(server)
  connector.setPort(0)
  server.addConnector(connector)
  server.setHandler(new Handler.Abstract {
    override def handle(request: Request, response: JettyServerResponse, callback: Callback): Boolean =
      next.get()().respond(response, Callback.from(
        () => { lastOutcome.set(None); callback.succeeded() },
        (t: Throwable) => { lastOutcome.set(Some(t)); callback.failed(t) }
      ))
  })

  private val DocType = "<!DOCTYPE html>"

  private val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()

  override protected def beforeAll(): Unit = server.start()

  override protected def afterAll(): Unit = server.stop()

  private def get(response: => Response): HttpResponse[String] = send(response, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))

  private def send[T](response: => Response, bodyHandler: HttpResponse.BodyHandler[T]): HttpResponse[T] = {
    next.set(() => response)
    lastOutcome.set(null)
    val req = HttpRequest.newBuilder(URI.create(s"http://localhost:${connector.getLocalPort}/")).GET().build()
    client.send(req, bodyHandler)
  }

  private def header(resp: HttpResponse[?], name: String): Option[String] = resp.headers().firstValue(name).toScala

  private val small: NodeSeq = <html><head><title>T</title></head><body><p class="a" id="x">Hello &amp; "bye" ünïcödé €😀</p></body></html>

  private val big: NodeSeq =
    <html><body>{(1 to 50_000).map(i => <div class="row" data-i={i.toString}><span>{s"Row $i < & > \"q\""}</span></div>)}</body></html>

  "OkHtmlStreaming" should "send a page that fits in one chunk in a single write: Jetty then sets the Content-Length" in {
    val resp = get(new OkHtmlStreaming(small))
    assert(resp.statusCode() == 200)
    assert(resp.body() == DocType + "\n" + small.toString)
    assert(header(resp, "Content-Type").contains("text/html;charset=utf-8"))
    // The only write is the last one, so Jetty knows the length: no chunked encoding.
    val bytes = (DocType + "\n" + small.toString).getBytes(StandardCharsets.UTF_8).length
    assert(header(resp, "Content-Length").contains(bytes.toString), resp.headers())
    assert(header(resp, "Transfer-Encoding").isEmpty, resp.headers())
    assert(outcome() == None)
  }

  it should "send a bigger page chunked, without a Content-Length" in {
    val page = <html><body>{(1 to 2_000).map(i => <p>{s"Row $i"}</p>)}</body></html> // a few chunks
    val resp = get(new OkHtmlStreaming(page))
    assert(resp.body() == DocType + "\n" + page.toString)
    assert(header(resp, "Content-Length").isEmpty, resp.headers())
    assert(header(resp, "Transfer-Encoding").contains("chunked"), resp.headers())
    assert(outcome() == None)
  }

  it should "send the same page as Ok.html (which has a Content-Length), with a newline after the doctype" in {
    val streamed = get(new OkHtmlStreaming(small)).body()
    val whole = get(Ok.html(small.toString))
    assert(header(whole, "Content-Length").isDefined)
    assert(whole.body() == DocType + small.toString) // (Ok.html writes no newline after the doctype)
    assert(streamed == DocType + "\n" + small.toString)
  }

  it should "stream a page of several MB, in many chunks" in {
    val resp = get(new OkHtmlStreaming(big))
    val expected = DocType + "\n" + big.toString
    assert(expected.length > 3_000_000)
    // (Compared as a Boolean, so a failure doesn't print MBs.)
    val same = resp.body() == expected
    assert(same, s"body: ${resp.body().length} chars, expected ${expected.length}")
    assert(header(resp, "Content-Length").isEmpty)
    assert(outcome() == None)
  }

  it should "send the headers and the doctype it's given" in {
    val resp = get(new OkHtmlStreaming(small) {
      override def docType: String = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">"
    }.addHeaders("Cache-Control" -> "no-cache", "X-Test" -> "1"))
    assert(resp.body().startsWith("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">\n<html>"))
    assert(header(resp, "Cache-Control").contains("no-cache") && header(resp, "X-Test").contains("1"))
  }

  it should "encode the page in UTF-8: 2, 3 and 4-byte chars, in text and attributes, across many chunks" in {
    val page = <html><body>{(1 to 20_000).map(i =>
      <p title={s"título $i €"}>{s"Inscrição $i — ünïcödé € 😀 & <ok>"}</p>)}</body></html>
    val resp = send(new OkHtmlStreaming(page), HttpResponse.BodyHandlers.ofByteArray())
    val expected = (DocType + "\n" + page.toString).getBytes(StandardCharsets.UTF_8)
    assert(expected.length > 1_000_000)
    val same = java.util.Arrays.equals(resp.body(), expected)
    assert(same, s"body: ${resp.body().length} bytes, expected ${expected.length}")
    assert(header(resp, "Content-Type").contains("text/html;charset=utf-8"))
  }

  it should "abort the response (not end it normally) and fail the callback if writing fails midway" in {
    // XmlJsStream (as toString) throws on unknown Node subclasses: here after ~1 MB has been sent.
    val failing = new Node {
      override def label: String = "custom"
      override def child: Seq[Node] = Nil
    }
    val page = <html><body>{(1 to 20_000).map(i => <p>{s"Before the failure, row $i"}</p>)}{failing}<p>never sent</p></body></html>
    val result = scala.util.Try(get(new OkHtmlStreaming(page)))
    // The client sees the connection aborted: no complete (chunked) response, so either an error or no end of page.
    assert(result.isFailure || !result.get.body().contains("never sent"), result)
    val failure = outcome()
    assert(failure.exists(_.isInstanceOf[IllegalArgumentException]), failure)
  }
}
