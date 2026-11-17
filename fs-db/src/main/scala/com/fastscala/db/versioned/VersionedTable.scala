package com.fastscala.db.versioned

import com.fastscala.db.keyed.TableWithId
import scalikejdbc.*

/**
 * The table of [[VersionedRow]] rows (rows with a `version` column, saved with optimistic locking), whatever their
 * key: mix it into the keyed table, e.g. `object Account extends TableWithUuidId[Account] with VersionedTable[UUID, Account]`.
 */
trait VersionedTable[K, R <: VersionedRow[K, R]] extends TableWithId[R, K] {

  /** The version of the row in the database; None if there's no such row. */
  def versionOf(key: K): Option[Long] = DB.readOnly({ implicit session => _versionOf(key) })

  def _versionOf(key: K)(implicit session: DBSession): Option[Long] =
    sql"""select "version" from $tableNameSQLSyntaxQuoted where ${keyEqualsSQL(key)}""".map(_.long(1)).single()
}
