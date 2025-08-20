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

object FSSession {
  val logger = LoggerFactory.getLogger(getClass.getName)
}

trait FSHasSession {
  def session: FSSession
}

class FSSession(
                 val id: String,
                 val fsSystem: FSSystem,
                 data: collection.mutable.Map[Any, Any] = collection.mutable.Map.empty[Any, Any],
                 val createdAt: Long = System.currentTimeMillis(),
                 var keepAliveAt: Long = System.currentTimeMillis(),
                 val debugLbl: Option[String] = None
               ) extends FSHasSession {

  val config = ConfigFactory.load()

  def useRandomIds: Boolean = Option(config.getBoolean("com.fastscala.core.random-ids")).getOrElse(false)

  val logger = LoggerFactory.getLogger(getClass.getName)

  private var idSeq = 0L
  private lazy val xoRoShiRo128PlusRandom = new XoRoShiRo128PlusRandom()

  private implicit def __fsContextOpt: Option[FSContext] = None

  private implicit def __fsPageOpt: Option[FSPage] = None

  private implicit def __fsSessionOpt: Option[FSSession] = Some(this)

  private implicit def __fsSystem: FSSystem = fsSystem

  def nextID(prefix: String = ""): String = {
    if (useRandomIds) {
      prefix + java.lang.Long.toHexString(xoRoShiRo128PlusRandom.nextLong())
    } else {
      session.synchronized {
        session.idSeq += 1
        prefix + session.idSeq.toString
      }
    }
  }

  override def session: FSSession = this

  def keepAlive(): Unit = {
    keepAliveAt = System.currentTimeMillis()
  }

  def allKeepAlivesIterable: Iterable[Long] = pages.values.flatMap(_.allKeepAlivesIterable) ++ Iterable(keepAliveAt)

  //  override def finalize(): Unit = {
  //    super.finalize()
  //    FSSession.logger.trace(s"GC SESSION lived ${((System.currentTimeMillis() - createdAt) / 1000d).formatted("%.2f")}s session_id=$id #pages=${pages.keys.size} #anonymous_pages=${anonymousPages.keys.size}, evt_type=gc_session")
  //  }

  val pages = collection.mutable.Map[String, FSPage]()
  val anonymousPages = collection.mutable.Map[String, FSAnonymousPage]()

  def nPages(): Int = pages.size

  def getData[T](key: Any): T = getDataOpt(key).get

  def getDataOpt[T](key: Any): Option[T] = data.get(key).map(_.asInstanceOf[T])

  def setData[T](key: Any, value: T): Unit = data(key) = value

  def clear[T](key: Any): Unit = data.remove(key)

  def createPage[T](generatePage: FSContext => T, debugLbl: Option[String] = None, onPageUnload: () => Js = () => JS.void)(implicit req: Request): T = createAndRenderPageWithImpl(
    new NoFSPageImplProvided[T] {
      override def render()(implicit fsc: FSContext): T = generatePage(fsc)
    }
  )

  /** Allows you to store your own page implementation in the page
   *
   * @return
   */
  def createAndRenderPageWithImpl[T](impl: FSPageImpl[T], debugLbl: Option[String] = None, onPageUnload: () => Js = () => JS.void)(implicit req: Request): T = try {
    session.fsSystem.gc()
    val copy = new Request.Wrapper(req)
    val page = new FSPage(nextID(), this, copy, impl, onPageUnload = onPageUnload, debugLbl = debugLbl)
    if (logger.isTraceEnabled) logger.trace(s"Created page ${page.id}")
    fsSystem.stats.event(StatEvent.CREATE_PAGE)
    session.fsSystem.stats.pagesTotal.inc()
    session.fsSystem.stats.currentPages.inc()
    this.synchronized {
      pages += (page.id -> page)
    }
    // FSSession.logger.trace(s"Created page: page_id=${page.id}, evt_type=create_page")
    impl.render()(page.rootFSContext)
  } catch {
    case ex: Exception =>
      ex.printStackTrace()
      throw ex
  }

  def deletePages(toDelete: Set[FSPage]): Unit = {
    val currentPages = pages.values.toSet
    val pagesToRemove = currentPages.intersect(toDelete)
    pagesToRemove.foreach { page =>
      pages -= page.id
      page.delete()
    }
    fsSystem.stats.currentPages.dec(pagesToRemove.size)
  }

  def deletePagesOlderThan(ts: Long): Unit = {
    val pagesToRemove = pages.filter(_._2.keepAliveAt < ts)
    pagesToRemove.foreach { case (id, page) =>
      pages -= id
      page.delete()
    }
    fsSystem.stats.currentPages.dec(pagesToRemove.size)

    val anonymousPagesToRemove = anonymousPages.filter(_._2.keepAliveAt < ts)
    anonymousPagesToRemove.foreach(anonymousPages -= _._1)
    fsSystem.stats.currentAnonPages.dec(anonymousPagesToRemove.size)

    pages.values.foreach(_.deleteOlderThan(ts))
  }

  def delete(): Unit = {
    fsSystem.sessions -= this.id
    fsSystem.stats.currentSessions.dec()

    pages.filterInPlace { (_, page) =>
      page.delete()
      false
    }
    fsSystem.stats.currentPages.dec(pages.size)

    anonymousPages.clear()
    fsSystem.stats.currentAnonPages.dec(anonymousPages.size)
  }
}
