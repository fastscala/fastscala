package com.fastscala.demo.docs.about

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.demo.docs.forms.DefaultBSForm6Renderer
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form6.DefaultForm6
import com.fastscala.templates.form6.fields.{F6Field, F6SaveButtonField, F6StringField, F6VerticalField}
import com.fastscala.xml.scala_xml.JS
import io.circe.Decoder
import io.circe.generic.semiauto

import java.net.{HttpURLConnection, URL, URLEncoder}
import java.util.Date
import scala.io.Source
import scala.util.Try
import scala.xml.NodeSeq

class AboutPage extends MultipleCodeExamples2Page {

  override def pageTitle: String = "FastScala | About"

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderHtml() {
      alert.alert_success.withRole("alert").d_flex.justify_content_between.align_items_center.apply {
        div.apply("Interested in learning more about the FastScala framework? Register now for a free live demo/training here!:") ++
          BSBtn().BtnPrimary.lbl("Register for Free Training!").href("https://training.fastscala.com/").btnLink.ms_3
      } ++
        h3.apply("What is the FastScala framework?") ++
        <p>
          The FastScala web framework allows you to do <b>fast web application development</b> in Scala.
        </p>
        <p>
          You can <u>very easily</u> <b>create callbacks</b> that you send to the client side, which will be executed in
          the server and will return back the next action to be executed on the server (rerender part of the page, run some
          javascript, redirect, alert, anything).
        </p>
    }

    renderSnippet("Create a callback") {
      val callbackJs = fsc.callback(() => JS.alert(s"Current date/time on server: ${new Date().toString}"))
      <p>Clicking the button runs the javacript:</p>
      <pre>{callbackJs.cmd}</pre>
      <button class="btn btn-primary d-block mx-auto" onclick={callbackJs.cmd}>Check time on server</button>
    }
    renderHtml() {
      <p>
        Builing on top of this basic <b>callback</b> funcionality, we can create a great development experience which
        allows you to build web applications much faster.
      </p>
      <p>
        See bellow the available library to build Bootstrap buttons easily:
      </p>
    }
    renderSnippet("Building on top of the basics") {
      BSBtn().BtnPrimary.lg.lbl("Check time on server")
        .ajax(_ => JS.alert(s"Current date/time on server: ${new Date().toString}"))
        .btn.m_3.shadow.mx_auto.d_block
    }
    renderHtml() {
      <p>
        Your imagination is the limit - easily control the client side from the client side:
      </p>
    }
    renderSnippet("Check time on server basic example") {
      <div id="current-time"><span>{new Date().toString}</span></div> ++
        BSBtn().BtnPrimary.sm.lbl("Update time")
          .ajax(_ => JS.setContents("current-time", <span>{new Date().toString}</span>)).sm.btn
    }
    renderHtml() {
      <p>
        Building on these fundations we introduce more advanced components which make you go even faster and safer:
      </p>
    }
    renderSnippet("Check time on server example 2") {
      val rerenderable = JS.rerenderable(_ => _ => <span>{new Date().toString}</span>)
      rerenderable.render() ++
        BSBtn().BtnPrimary.sm.lbl("Update time")
          .ajax(_ => rerenderable.rerender()).sm.btn.ms_2
    }
    renderSnippet("Check time on server example 3") {
      JS.rerenderableContents(rerenderer => implicit fsc => {
        <span>{new Date().toString}</span> ++
          BSBtn().BtnPrimary.sm.lbl("Update time")
            .ajax(_ => rerenderer.rerender()).sm.btn.ms_2
      }).render()
    }
    renderHtml() {
      <p>
        Lets create an even more complex scenario:
      </p>
    }
    renderSnippet("Variable number of input buttons") {
      var numBtns = 3
      JS.rerenderableContents(rerenderer => implicit fsc => {
        val buttons = (0 until numBtns).map(btnNum => {
          val points = btnNum + 1
          BSBtn().BtnSecondary.lg.lbl(points.toString)
            .onclick(JS.alert(points + " points")).btn.mx_3
        })
        d_flex.justify_content_center.apply(buttons: _*).mb_2 ++
          d_flex.justify_content_center.apply {
            BSBtn().BtnPrimary.sm.lbl("-").ajax(_ => {
              numBtns -= 1
              rerenderer.rerender()
            }).btn ++
              BSBtn().BtnPrimary.sm.lbl("+").ajax(_ => {
                numBtns += 1
                rerenderer.rerender()
              }).btn.ms_2
          }
      }).render()
    }
    renderSnippet("Variable number of input buttons - more elegant/functional approach, with JS.rerenderableContentsP") {
      JS.rerenderableContentsP[Int](rerenderer => implicit fsc => numBtns => {
        val buttons = (0 until numBtns).map(_ + 1).map(points => {
          BSBtn().BtnSecondary.lg.lbl(points.toString)
            .onclick(JS.alert(points + " points")).btn.mx_3
        })
        d_flex.justify_content_center.apply(buttons: _*).mb_2 ++
          d_flex.justify_content_center.apply {
            BSBtn().BtnPrimary.sm.lbl("-").ajax(_ => {
              rerenderer.rerender(math.max(1, numBtns - 1))
            }).btn ++
              BSBtn().BtnPrimary.sm.lbl("+").ajax(_ => {
                rerenderer.rerender(math.min(10, numBtns + 1))
              }).btn.ms_2
          }
      }).render(3)
    }
    renderHtml() {
      <p>
        We continously build on top of abstractions to create even more complex experiences, that are really simple and low-code to develop:
      </p>
    }
    renderSnippet("Easily create advanced forms") {
      import DefaultBSForm6Renderer._
      val nameField = new F6StringField().label("Name").required(true)
      val emailField = new F6StringField().label("Email").inputType("email").required(true)

      new DefaultForm6() {
        override def postSave()(implicit fsc: FSContext): Js =
          BSModal5.verySimple("Your input data", "Done")(modal => implicit fsc => {
            fs_4.apply(s"Your entered the name '${nameField.currentValue}' and email '${emailField.currentValue}'")
          })

        override lazy val rootField: F6Field = F6VerticalField()(
          nameField
          , emailField
          , new F6SaveButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block)
        )
      }.render()
    }
    renderSnippet("Support advanced interactions with a few lines of code") {
      import DefaultBSForm6Renderer._
      case class Definition(definition: Option[String], example: Option[String], synonyms: List[String], antonyms: List[String]) {
        def render(): NodeSeq = definition.map(definition => <li><i>{definition}</i>{example.map(": " + _).getOrElse("")}</li>).getOrElse(NodeSeq.Empty)
      }
      case class Meaning(partOfSpeech: String, definitions: List[Definition]) {
        def render(): NodeSeq = <li><i>{partOfSpeech}</i></li> ++
          <li>Definitions: <ul class="ms-2">{definitions.flatMap(_.render())}</ul></li>.showIf(definitions.nonEmpty)
      }
      case class Response(word: String, phonetic: String, origin: Option[String], meanings: List[Meaning]) {
        def render(): NodeSeq = <h6>{word}</h6> ++
          <ul class="ms-2">
            <li>Phonetic: {phonetic}</li>{origin.map(origin => <li>Origin: {origin}</li>).getOrElse(NodeSeq.Empty)}
            {<li>Meanings: <ul class="ms-2">{meanings.flatMap(_.render())}</ul></li>.showIf(meanings.nonEmpty)}
          </ul>.ms_2
      }
      implicit val definitionDecoder: Decoder[Definition] = semiauto.deriveDecoder[Definition]
      implicit val meaningDecoder: Decoder[Meaning] = semiauto.deriveDecoder[Meaning]
      implicit val responseDecoder: Decoder[Response] = semiauto.deriveDecoder[Response]

      def renderResponses(responses:  List[Response]) = responses.flatMap(_.render())

      val resultsRenderer = JS.rerenderableContentsP[Option[String]](_ => implicit fsc => queryOpt => {
        queryOpt match {
          case Some(query) =>
            val url = new URL(s"https://api.dictionaryapi.dev/api/v2/entries/en/${URLEncoder.encode(query)}")
            val con = url.openConnection().asInstanceOf[HttpURLConnection]
            con.setRequestMethod("GET")
            try {
              Try(Source.fromInputStream(con.getInputStream).mkString).toEither.flatMap(json => {
                io.circe.parser.decode[List[Response]](json)
              }) match {
                case Right(responses) => renderResponses(responses)
                case Left(_: java.io.FileNotFoundException) => span.text_danger.apply("No results found.")
                case Left(value) => span.apply("Error when calling the API: " + value)
              }
            } finally {
              Try(con.getInputStream.close())
            }
          case None => div.apply("...").text_center
        }
      })

      val queryField = new F6StringField().label("Search query").required(true)

      resultsRenderer.render(None) ++
        new DefaultForm6() {
          override def postSave()(implicit fsc: FSContext): Js = resultsRenderer.rerender(Some(queryField.currentValue))

          override lazy val rootField: F6Field = F6VerticalField()(
            queryField
            , new F6SaveButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
    }
    closeSnippet()
  }
}
