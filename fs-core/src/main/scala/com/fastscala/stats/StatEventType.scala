package com.fastscala.stats

object StatEventType extends Enumeration {

  val CREATE = Value
  val USE = Value
  val GC = Value
  val NOT_FOUND = Value
}

sealed abstract class StatEvent(val name: String, val eventType: StatEventType.Value) {
  override def toString: String = name
}

object StatEvent {
  object CREATE_SESSION extends StatEvent("CREATE_SESSION", StatEventType.CREATE)

  object CREATE_PAGE extends StatEvent("CREATE_PAGE", StatEventType.CREATE)

  object CREATE_ANON_PAGE extends StatEvent("CREATE_ANON_PAGE", StatEventType.CREATE)

  object CREATE_FILE_DOWNLOAD extends StatEvent("CREATE_FILE_DOWNLOAD", StatEventType.CREATE)

  object CREATE_FILE_UPLOAD extends StatEvent("CREATE_FILE_UPLOAD", StatEventType.CREATE)

  object CREATE_CALLBACK extends StatEvent("CREATE_CALLBACK", StatEventType.CREATE)

  object USE_CALLBACK extends StatEvent("USE_CALLBACK", StatEventType.USE)

  object GC_SESSION extends StatEvent("GC_SESSION", StatEventType.GC)

  object GC_PAGE extends StatEvent("GC_PAGE", StatEventType.GC)

  object GC_ANON_PAGE extends StatEvent("GC_ANON_PAGE", StatEventType.GC)

  object GC_FILE_DOWNLOAD extends StatEvent("GC_FILE_DOWNLOAD", StatEventType.GC)

  object GC_CALLBACK extends StatEvent("GC_CALLBACK", StatEventType.GC)

  object GC_FILE_UPLOAD extends StatEvent("GC_FILE_UPLOAD", StatEventType.GC)

  object NOT_FOUND_SESSION extends StatEvent("NOT_FOUND_SESSION", StatEventType.NOT_FOUND)

  object NOT_FOUND_PAGE extends StatEvent("NOT_FOUND_PAGE", StatEventType.NOT_FOUND)

  object NOT_FOUND_ANON_PAGE extends StatEvent("NOT_FOUND_ANON_PAGE", StatEventType.NOT_FOUND)

  object NOT_FOUND_FILE_DOWNLOAD extends StatEvent("NOT_FOUND_FILE_DOWNLOAD", StatEventType.NOT_FOUND)

  object NOT_FOUND_FILE_UPLOAD extends StatEvent("NOT_FOUND_FILE_UPLOAD", StatEventType.NOT_FOUND)

  object NOT_FOUND_CALLBACK extends StatEvent("NOT_FOUND_CALLBACK", StatEventType.NOT_FOUND)

  val all = Vector(
    CREATE_SESSION
    , CREATE_PAGE
    , CREATE_ANON_PAGE
    , CREATE_FILE_DOWNLOAD
    , CREATE_FILE_UPLOAD
    , CREATE_CALLBACK
    , USE_CALLBACK
    , GC_SESSION
    , GC_PAGE
    , GC_ANON_PAGE
    , GC_FILE_DOWNLOAD
    , GC_CALLBACK
    , GC_FILE_UPLOAD
    , NOT_FOUND_SESSION
    , NOT_FOUND_PAGE
    , NOT_FOUND_ANON_PAGE
    , NOT_FOUND_FILE_DOWNLOAD
    , NOT_FOUND_FILE_UPLOAD
    , NOT_FOUND_CALLBACK
  )
}