package com.fastscala.components.bootstrap5.table6

trait Table6Persistable extends Table6Base {

  def persistedState: io.circe.JsonObject

  def persistedState_=(persistedState: io.circe.JsonObject): Unit
}
