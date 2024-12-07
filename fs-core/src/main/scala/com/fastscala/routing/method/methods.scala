package com.fastscala.routing.method

trait Method

object Method {
  def fromString: PartialFunction[String, Method] = {
    case "GET" => GET
    case "HEAD" => HEAD
    case "POST" => POST
    case "PUT" => PUT
    case "DELETE" => DELETE
    case "CONNECT" => CONNECT
    case "OPTIONS" => OPTIONS
    case "TRACE" => TRACE
    case "PATCH" => PATCH
  }
}

object GET extends Method

object HEAD extends Method

object POST extends Method

object PUT extends Method

object DELETE extends Method

object CONNECT extends Method

object OPTIONS extends Method

object TRACE extends Method

object PATCH extends Method
