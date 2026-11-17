package com.fastscala.components.bootstrap5.table6

import com.fastscala.core.{FSContext, FSPage}
import com.fastscala.js.{Js, js}
import com.fastscala.scala_xml.js.JS

import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8

/**
 * Keeps the table's named values ([[getProp]], [[setProp]]) in the query string of the page's URL, each under
 * `<tableName>-<name>` (e.g. `?people-page=3&people-size=50`): so reloading the page, coming back to it or sharing its
 * link gets them back, and several tables in a page each have their own.
 *
 * A value is read from the request the page was loaded with. A value set is in the URL from the table's next
 * [[rerender]] on, whose JavaScript updates the URL in the browser, without reloading (all that was set since the
 * last one, in one change; the URL's other parameters are left as they are). Until then, [[getProp]] returns it
 * already.
 */
trait Table6WithDataStorageInQueryString extends Table6Base with Table6WithDataStorage with Table6WithName {

  /**
   * Whether each change adds an entry to the browser's history (so "back" undoes it), instead of replacing the
   * current one (so "back" leaves the page).
   */
  def queryStringStoragePushesHistory: Boolean = false

  def queryStringStorageUseTableName: Boolean = true

  /** The name of the query string parameter that keeps the value named `name`. */
  def queryStringParamName(name: String): String = if (queryStringStorageUseTableName) s"$tableName-$name" else name

  private var loadedParams = false
  private var queryParams = Map[String, String]()

  private def loadedQueryParams(implicit fsPage: FSPage): Map[String, String] = {
    if (!loadedParams) {
      loadedParams = true
      val query = Option(fsPage.req).flatMap(req => Option(req.getHttpURI)).flatMap(uri => Option(uri.getQuery)).getOrElse("")
      val params = query.split('&').iterator.filter(_.nonEmpty).flatMap(param => {
        val (name, value) = param.span(_ != '=')
        try Some(URLDecoder.decode(name, UTF_8) -> URLDecoder.decode(value.drop(1), UTF_8))
        catch {
          case _: IllegalArgumentException => None
        } // (a malformed escape: not ours to fail on)
      }).toSeq.reverse.toMap
      queryParams = params
    }
    queryParams
  }

  override def getProp(name: String)(implicit fsPage: FSPage): Option[String] = {
    val param = queryStringParamName(name)
    loadedQueryParams.get(param)
  }

  override def setProp(name: String, value: Option[String])(implicit fsPage: FSPage): Js = {
    val param = queryStringParamName(name)
    // (What's set is what getProp returns from now on: also once it's in the URL, and not pending any more.)
    val current = loadedQueryParams
    value match {
      case Some(value) => queryParams += (param -> value)
      case None => queryParams = queryParams.removed(param)
    }

    val method = if (queryStringStoragePushesHistory) "pushState" else "replaceState"
    val setting = (value match {
      case Some(value) => js"p.set(${JS.asJsStr(param)},${JS.asJsStr(value)});"
      case None => js"p.delete(${JS.asJsStr(param)});"
    })
    js"(function(){if(!window.history||!window.history.$method)return;var u=new URL(window.location.href);var p=u.searchParams;${setting}if(u.href!==window.location.href)window.history.$method(window.history.state,'',u.href);})();"
  }
}
