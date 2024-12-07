package com.fastscala.db

trait RowWithIdBase extends RowBase {

  def reload(): Any

  def save(): Any

  def update(): Unit

  def delete(): Unit

  def insert(): Unit
}
