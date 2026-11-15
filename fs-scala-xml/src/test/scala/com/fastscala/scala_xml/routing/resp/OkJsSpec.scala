package com.fastscala.scala_xml.routing.resp

import com.fastscala.js.{Js, JsWriter}
import com.fastscala.scala_xml.xml.XmlJsStream
import com.fastscala.routing.resp.{Ok, Response}
import com.fastscala.js.EncJs
import com.fastscala.scala_xml.js.NodeSeqJs
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.{Handler, Request, Server, ServerConnector, Response as JettyServerResponse}
import org.eclipse.jetty.util.Callback
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import scala.jdk.OptionConverters.*
import scala.xml.*

/** [[Ok.js]] (written as it's produced, by each [[JsWriter]]), served by a real Jetty server, over HTTP/1.1. */
class OkJsSpec extends AnyFlatSpec with BeforeAndAfterAll {

  /** The response the server sends to the next request. */
  private val next = new AtomicReference[() => Response]()

  /** How the last response's callback was completed (None: succeeded), and how many times. */
  private val lastOutcome = new AtomicReference[Option[Throwable]]()
  private val completions = new AtomicInteger()

  private val server = new Server()
  private val connector = new ServerConnector(server)
  connector.setPort(0)
  server.addConnector(connector)
  server.setHandler(new Handler.Abstract {
    override def handle(request: Request, response: JettyServerResponse, callback: Callback): Boolean =
      next.get()().respond(response, Callback.from(
        () => { completions.incrementAndGet(); lastOutcome.set(None); callback.succeeded() },
        (t: Throwable) => { completions.incrementAndGet(); lastOutcome.set(Some(t)); callback.failed(t) }
      ))
  })

  private val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()

  override protected def beforeAll(): Unit = server.start()

  override protected def afterAll(): Unit = server.stop()

  private def get(response: => Response): HttpResponse[String] = {
    next.set(() => response)
    lastOutcome.set(null)
    completions.set(0)
    val req = HttpRequest.newBuilder(URI.create(s"http://localhost:${connector.getLocalPort}/")).GET().build()
    client.send(req, HttpResponse.BodyHandlers.ofString(UTF_8))
  }

  /** How the last response's callback was completed, waiting (up to 10 s) for it; checking it was completed once. */
  private def outcome(): Option[Throwable] = {
    val deadline = System.nanoTime() + 10_000_000_000L
    while (lastOutcome.get() == null && System.nanoTime() < deadline) Thread.sleep(5)
    val o = lastOutcome.get()
    assert(o != null, "the response's callback wasn't completed")
    Thread.sleep(20) // (a second completion would come right after the first)
    assert(completions.get() == 1, s"the callback was completed ${completions.get()} times")
    o
  }

  private def header(resp: HttpResponse[?], name: String): Option[String] = resp.headers().firstValue(name).toScala

  private val plainJs: Js = Js(Js("var a = 1"), Js(";"), Js("console.log('é € 😀', a)"))

  private val page: NodeSeq =
    <div>{(1 to 20_000).map(i => <p class="row" title={s"r$i"}>{s"Row $i é € 😀 \"q\" & <x> \\ '"}</p>)}</div>

  /** JavaScript with HTML in it (as a rerender's): a string literal with the page, escaped. */
  private def htmlJs: Js = Js(Js("document.body.innerHTML = '"), new EncJs(new NodeSeqJs(page)), Js("';"))

  private val cases: Seq[(String, () => Js)] = Seq("plain JavaScript" -> (() => plainJs), "JavaScript with a big page in it" -> (() => htmlJs))

  private val writers: Seq[(String, JsWriter)] = Seq("Js.cmd" -> JsWriter.Cmd, "XmlJsStream" -> XmlJsStream)

  for ((writerName, writer) <- writers; (name, js) <- cases) {
    // Both end the content with their last write: JsWriter.Cmd's only one (the whole cmd), so it always gets a
    // Content-Length; XmlJsStream's is the only one, and so gets a Content-Length, if the JavaScript fits in a chunk.
    val singleWrite = writer == JsWriter.Cmd || js().cmd.getBytes(UTF_8).length <= XmlJsStream.ChunkSize

    s"Ok.js (with $writerName)" should s"send the JavaScript, ${if (singleWrite) "with a Content-Length" else "chunked"} ($name)" in {
      val resp = get(Ok.js(js(), writer))
      assert(resp.statusCode() == 200)
      val same = resp.body() == js().cmd
      assert(same, s"body: ${resp.body().length} chars, expected ${js().cmd.length}")
      assert(header(resp, "Content-Type").contains("text/javascript;charset=utf-8"))
      if (singleWrite) {
        assert(header(resp, "Content-Length").contains(js().cmd.getBytes(UTF_8).length.toString), resp.headers())
      } else {
        assert(header(resp, "Content-Length").isEmpty, resp.headers())
        assert(header(resp, "Transfer-Encoding").contains("chunked"), resp.headers())
      }
      assert(outcome() == None)
    }
  }

  /** Fails however it's written: its cmd can't be built. */
  private val failingJs: Js = new Js {
    override def cmd: String = throw new IllegalStateException("can't")
  }

  for ((writerName, writer) <- writers) {
    s"Ok.js (with $writerName)" should "fail the callback, once, if writing the JavaScript fails" in {
      val result = scala.util.Try(get(Ok.js(Js(Js("before();"), failingJs), writer)))
      assert(result.isFailure || result.get.statusCode() != 200, result)
      assert(outcome().exists(_.getMessage == "can't"))
    }
  }
}
