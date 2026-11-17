package com.fastscala.testutils

import com.fastscala.core.*

/**
 * A FastScala system, session and pages for tests that render outside a server (performance tests). FSStats registers
 * its metrics globally, so there must be a single FSSystem per JVM: every test uses this one.
 */
object PerfFS {

  lazy val system: FSSystem = new FSSystemImpl(appName = "perf-tests")
  lazy val session: FSSession = new FSSession("perf-tests-session", system)

  private object NoImpl extends FSPageImpl[Unit] {
    override def render()(implicit fsc: FSContext): Unit = ()
  }

  /** A new page, as FSSession.createPage creates it (the request is only stored, so null is fine here). */
  def newPage(): FSPage = new FSPage(session.nextID(), session, null, NoImpl)

  /** A new page loaded with a GET of `uri` (e.g. `/people?page=2`): for what reads the page's request. */
  def newPage(uri: String): FSPage = new FSPage(session.nextID(), session, TestRequests.request("GET", uri), NoImpl)
}
