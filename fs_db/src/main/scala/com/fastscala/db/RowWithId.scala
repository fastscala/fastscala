package com.fastscala.db

trait RowWithId[R <: RowWithId[R]] extends RowWithIdBase with Row[R] {
  self: R =>

  def reload(): R

  def save(): R

  def update(): Unit

  def delete(): Unit

  def insert(): Unit
}
