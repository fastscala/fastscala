package com.fastscala.stats

import com.fastscala.core.{FSContext, FSPage, FSSession, FSSystem}
import com.github.loki4j.slf4j.marker.LabelMarker
import io.circe.syntax.EncoderOps
import io.prometheus.client.Counter
import org.slf4j.{Logger, LoggerFactory}
import org.slf4j.event.Level

import java.util.concurrent.atomic.AtomicLong

class FSStats(
               logger: Logger = LoggerFactory.getLogger("com.fastscala.stats.FSStats")
             ) {

  import StatEvent._

  val data: Map[StatEvent, Counter] = StatEvent.all.map(evt =>
    evt -> Counter.build().name(evt.name).help(evt.name).register()
  ).toMap

  def eventLevel(event: StatEvent): Level = event match {
    case CREATE_CALLBACK | USE_CALLBACK | GC_CALLBACK => Level.TRACE
    case _ => Level.DEBUG
  }

  def event(
             event: StatEvent,
             n: Int = 1,
             additionalFields: Seq[(String, String)] = Nil
           )(implicit
             fsSystem: FSSystem
             , fsSessionOpt: Option[FSSession] = None
             , fsPageOpt: Option[FSPage] = None
             , fsContextOpt: Option[FSContext] = None
           ): Unit = {
    data(event).synchronized {
      data(event).inc(n)
    }
    logger.atLevel(eventLevel(event))
      .addMarker(LabelMarker.of("app", () => fsSystem.appName))
      .log(List(
        List("event" -> event.name)
        , fsSessionOpt.toList.map(session => "session_id" -> session.id)
        , fsSessionOpt.toList.flatMap(_.debugLbl.map("session_lbl" -> _))
        , fsPageOpt.toList.map(page => "page_id" -> page.id)
        , fsPageOpt.toList.flatMap(_.debugLbl.map("page_lbl" -> _))
        , fsContextOpt.toList.map("context_lbl" -> _.fullPath)
        , additionalFields
      ).flatten.toMap.asJson.noSpaces)
  }
}
