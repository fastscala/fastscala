package com.fastscala.db

trait RowWithId[K, R <: RowWithId[K, R]] extends RowWithIdBase with Row[R] {
  self: R =>

  def key: K

  def reload(): R

  def save(): R

  def update(): Unit

  def delete(): Unit

  def insert(): Unit
}
