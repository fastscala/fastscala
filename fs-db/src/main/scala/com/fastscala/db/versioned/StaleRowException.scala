package com.fastscala.db.versioned

/**
 * A versioned row (see [[VersionedRow]]) was saved or deleted from a stale copy: the row in the database isn't at the
 * version the copy was read at, i.e. it was changed (or deleted) in the meantime. Nothing was written.
 *
 * To recover: reload the row, apply the change again and save (or tell the user their data is out of date).
 *
 * @param key             the row's primary key (a UUID, or a Long id)
 * @param expectedVersion the version of the copy that was being saved or deleted
 * @param actualVersion   the version of the row in the database; None if it doesn't exist any more
 */
class StaleRowException(val tableName: String, val key: Any, val expectedVersion: Long, val actualVersion: Option[Long])
  extends RuntimeException(
    s"Stale row of table $tableName with key $key: expected version $expectedVersion, but " +
      actualVersion.map(v => s"the database has version $v").getOrElse("it's not in the database any more")
  )
