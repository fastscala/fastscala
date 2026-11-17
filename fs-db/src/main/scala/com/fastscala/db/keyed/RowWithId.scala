package com.fastscala.db.keyed

import com.fastscala.db.Row
import scalikejdbc.*

/**
 * A row with a primary key. What depends on the kind of key is left to [[com.fastscala.db.keyed.uuid.RowWithUUID]] and
 * [[com.fastscala.db.keyed.numeric.RowWithLongId]]: the key itself, and how a new row gets one
 * ([[insertWithNewKey]]); the rest is here, selecting the row with the table's `keyEqualsSQL`.
 */
trait RowWithId[K, R <: RowWithId[K, R]] extends RowWithIdBase with Row[R] {
  self: R =>

  /** The row's key; null if it was never saved. */
  def key: K

  def keyOpt: Option[K] = Option(key)

  def table: TableWithId[R, K]

  def isPersisted_? : Boolean

  /**
   * Gives this row (never saved) its key, and inserts it. If the insert fails, the row is left without a key (as it
   * was: never saved).
   */
  protected def insertWithNewKey()(implicit session: DBSession): Unit

  /** Takes the row's key away: it's as if never saved (saving it inserts a new row). */
  protected def clearKey(): Unit

  /**
   * Called by the operations that write the row without going through [[save]], [[update]] or [[delete]] (e.g. an
   * upsert, or handing out the SQL to run): a kind of row that has to control every write (as
   * [[com.fastscala.db.versioned.VersionedRow]], which checks the row's version) overrides this to refuse them.
   */
  protected def uncheckedWrite(operation: String): Unit = ()

  /** Inserts the row (if it was never saved), or updates it. */
  def save(): R = DB.localTx({ session => save(session) })

  /** [[save]] in the transaction of `session`. */
  def save(session: DBSession): R = {
    implicit val s: DBSession = session
    if (isPersisted_?) table.updateSQL(this, sqls" where ${table.keyEqualsSQL(key)}").update()
    else insertWithNewKey()
    this
  }

  /** Updates the row (does nothing if it was never saved). */
  def update(): Unit = if (isPersisted_?) {
    DB.localTx({ implicit session =>
      table.updateSQL(this, sqls" where ${table.keyEqualsSQL(key)}").update()
    })
  }

  /**
   * Applies `func` to the current row in the database (to this one, if it was never saved) and saves it; this row
   * becomes that one.
   */
  def update(func: R => Unit): R = {
    val inDB = if (isPersisted_?) table.getForIdOpt(key).get else this
    func(inDB)
    inDB.save()
    if (inDB ne this) copyFrom(inDB)
    this
  }

  /**
   * Inserts a new row with this one's values (and its own key), and returns it, as read back from the database. This
   * row isn't changed.
   */
  def duplicate(): R = {
    val copy = table.createEmptyRowInternal()
    table.copyRow(this, copy)
    copy.clearKey()
    copy.save()
    copy.reload()
  }

  /** Inserts the row or, if there's one with its key already, updates that one (see the table's `upsertSQL`). */
  def upsert(): R = {
    uncheckedWrite("upsert()")
    DB.localTx({ implicit session => table.upsertSQL(this).execute() })
    this
  }

  /** Deletes the row (does nothing if it was never saved). */
  def delete(): Unit = if (isPersisted_?) {
    DB.localTx({ implicit session =>
      table.deleteSQL(this, sqls"where ${table.keyEqualsSQL(key)}").update()
    })
  }

  /** Refreshes this row from the database, in place; a row never saved is left as it is. */
  def reload(): R = {
    if (isPersisted_?) copyFrom(table.getForIdOpt(key).get)
    this
  }

  /**
   * Refreshes this row from the database, in place. Returns None if it doesn't exist in the DB (or was never saved):
   * then this row isn't changed.
   */
  def reloadOpt(): Option[R] = keyOpt.flatMap(key => table.getForIdOpt(key).map(inDB => {
    copyFrom(inDB)
    this
  }))
}
