package com.fastscala.db.versioned

import com.fastscala.db.keyed.RowWithId
import scalikejdbc.*

/**
 * A row with optimistic locking, whatever its key: mix it in after the keyed row trait (and [[VersionedTable]] into the
 * table), e.g. `class Account(...) extends RowWithUUID[Account] with VersionedRow[UUID, Account]`, or
 * `... extends RowWithLongId[Order] with VersionedRow[java.lang.Long, Order]`.
 *
 * It has a `version` column, a sequential number that every save increments, and a save (or delete) only goes through
 * if the row in the database is still at the version this copy was read at. Otherwise someone else changed it in the
 * meantime, and a [[StaleRowException]] is thrown: nothing is written, so a change is never silently overwritten (no
 * lost updates).
 *
 * The check and the write are a single statement (`update ... set version = <version + 1> where <key> and version =
 * <version>`), so it's atomic in the database, with no locks held between reading a row and saving it, and it's safe
 * across threads, transactions and servers.
 *
 * A row never saved has version 0; inserting it stores version 1.
 *
 * Bulk operations of the table (`delete(where)`, hand-written SQL) don't check versions: hand-written updates should
 * increment `version` themselves. The row's operations that would write without the check (`saveSQL()`, `upsert()`)
 * throw an UnsupportedOperationException.
 */
trait VersionedRow[K, R <: VersionedRow[K, R]] extends RowWithId[K, R] {
  self: R =>

  def table: VersionedTable[K, R]

  /** The version of the row in the database this copy was read at (or saved as); 0 if it was never saved. */
  var version: Long = 0L

  override protected def uncheckedWrite(operation: String): Unit =
    throw new UnsupportedOperationException(s"$operation isn't supported for versioned rows (table ${table.tableName}): it would write without checking the version. Use save()")

  /**
   * Inserts the row (if it was never saved), or updates it and increments its version: what `save()` does too. If the
   * transaction of `session` is rolled back afterwards, this copy keeps the new version (and key), so reload it.
   *
   * @throws StaleRowException if the row in the database isn't at this copy's version (this copy isn't changed).
   */
  override def save(session: DBSession): R = {
    implicit val s: DBSession = session
    if (isPersisted_?) updateVersioned()
    else {
      val previousVersion = version
      version = 1L
      try insertWithNewKey()
      catch {
        case t: Throwable =>
          version = previousVersion
          throw t
      }
    }
    this
  }

  /**
   * Updates the row (does nothing if it was never saved), incrementing its version.
   *
   * @throws StaleRowException if the row in the database isn't at this copy's version (this copy isn't changed).
   */
  override def update(): Unit = DB.localTx({ implicit session => _update() })

  def _update()(implicit session: DBSession): Unit = if (isPersisted_?) updateVersioned()

  /**
   * Deletes the row (does nothing if it was never saved, or if it's not in the database any more).
   *
   * @throws StaleRowException if the row in the database isn't at this copy's version (it was changed since it was read).
   */
  override def delete(): Unit = DB.localTx({ implicit session => _delete() })

  def _delete()(implicit session: DBSession): Unit = if (isPersisted_?) {
    val deleted = table.deleteSQL(this, sqls"""where ${table.keyEqualsSQL(key)} and "version" = $version""").update()
    // (None deleted and no such row: it was deleted already, which is what was asked for.)
    if (deleted == 0) table._versionOf(key).foreach(actual => throw new StaleRowException(table.tableName, key, version, Some(actual)))
  }

  private def updateVersioned()(implicit session: DBSession): Unit = {
    val expected = version
    // The new version is written with the other columns; restored if the row wasn't updated.
    version = expected + 1
    val updated =
      try table.updateSQL(this, sqls""" where ${table.keyEqualsSQL(key)} and "version" = $expected""").update()
      catch {
        case t: Throwable =>
          version = expected
          throw t
      }
    if (updated == 0) {
      version = expected
      throw new StaleRowException(table.tableName, key, expected, table._versionOf(key))
    }
  }
}
