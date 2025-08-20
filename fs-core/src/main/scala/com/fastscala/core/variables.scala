package com.fastscala.core

import com.fastscala.core.FSContext.logger
import com.fastscala.js.{Js, JsUtils}
import com.fastscala.routing.RoutingHandlerNoSessionHelper
import com.fastscala.routing.req.{Get, Post}
import com.fastscala.routing.resp.*
import com.fastscala.stats.{FSStats, StatEvent}
import com.fastscala.utils.{IdGen, Missing}
import com.typesafe.config.ConfigFactory
import it.unimi.dsi.util.XoRoShiRo128PlusRandom
import org.eclipse.jetty.http.*
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.{Request, Response as JettyServerResponse}
import org.eclipse.jetty.util.Callback
import org.eclipse.jetty.websocket.api.Callback.Completable
import org.eclipse.jetty.websocket.api.Session
import org.slf4j.LoggerFactory

import java.net.URLEncoder
import java.nio.file.{Files, Path}
import java.util.Collections
import scala.jdk.CollectionConverters.{IterableHasAsScala, MapHasAsScala}
import scala.util.chaining.scalaUtilChainingOps


abstract class FSSessionVar[T](default: => T) {

  def apply()(implicit hasSession: FSHasSession): T = hasSession.session.getDataOpt(this).getOrElse({
    hasSession.session.setData(this, default)
    apply()
  })

  def update(value: T)(implicit hasSession: FSHasSession): Unit = hasSession.session.setData(this, value)
}

abstract class FSSessionVarOpt[T]() {

  def apply()(implicit hasSession: FSHasSession): Option[T] = hasSession.session.getDataOpt[T](this)

  def update(value: T)(implicit hasSession: FSHasSession): Unit = hasSession.session.setData(this, value)

  def setOrClear(valueOpt: Option[T])(implicit hasSession: FSHasSession): Unit = valueOpt match {
    case Some(value) => update(value)
    case None        => clear()
  }

  def clear()(implicit hasSession: FSHasSession): Unit = hasSession.session.clear(this)
}
