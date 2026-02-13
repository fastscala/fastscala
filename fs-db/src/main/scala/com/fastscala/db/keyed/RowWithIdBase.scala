package com.fastscala.db.keyed

import com.fastscala.db.RowBase
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.sqls

trait RowWithIdBase extends RowBase {

  def reload(): Any

  def save(): Any

  def update(): Unit

  def delete(): Unit

  def insert(): Unit
}
