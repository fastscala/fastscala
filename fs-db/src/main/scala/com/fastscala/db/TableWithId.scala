package com.fastscala.db

trait TableWithId[R, K] extends Table[R] {

  def getForIdOpt(key: K): Option[R]

  def getForIds(key: K*): List[R]
}



