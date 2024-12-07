package com.fastscala.db.caching

object CacheStatus extends Enumeration {
  val ALL_LOADED = Value(10)
  val SOME_LOADED = Value(20)
  val NONE_LOADED = Value(30)
}
