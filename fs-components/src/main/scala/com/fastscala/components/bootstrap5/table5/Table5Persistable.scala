package com.fastscala.components.bootstrap5.table5

trait Table5Persistable extends Table5Base {

  def persistedState: io.circe.JsonObject

  def persistedState_=(persistedState: io.circe.JsonObject): Unit
}
