package com.fastscala.db.keyed

import com.fastscala.db.DBConn
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

import scala.util.chaining.scalaUtilChainingOps

/** The columns of the rows the keyed specs use (besides the key). */
trait KeyedSpecRow {
  var name: String
  var n: Int
}

/**
 * What [[RowWithId]] and [[TableWithId]] do, whatever the kind of key: run for a table with UUID keys, and for tables
 * with Long keys (from a sequence, and from a serial column). What's specific to a kind of key is in each spec.
 */
abstract class KeyedSpecBase[K, R <: RowWithId[K, R] & KeyedSpecRow] extends AnyFlatSpec with DBConn {

  def table: TableWithId[R, K]

  def newRow(name: String = "row", n: Int = 0): R

  /** A key no row has. */
  def absentKey: K

  /** The name of the key column. */
  def keyColumn: String

  protected def inDB(row: R): R = table.getForId(row.key)

  protected def saved(name: String = "row", n: Int = 0): R = newRow(name, n).save()

  "Create table" should "succeed" in {
    DB.localTx({ implicit session => table.__dropAndCreateTableSQL.foreach(_.execute()) })
    assert(table.count() == 0)
  }

  // ---------------------------------------------- RowWithId ----------------------------------------------

  "A row" should "have no key until it's saved" in {
    val row = newRow()
    assert(row.key == null)
    assert(row.keyOpt.isEmpty)
    assert(!row.isPersisted_?)
    assert(table.keyOf(row).isEmpty)
  }

  "save()" should "insert a row never saved, giving it its key" in {
    val before = table.count()
    val row = newRow("a", 1)
    assert(row.save() eq row)
    assert(row.isPersisted_? && row.key != null && row.keyOpt.contains(row.key))
    assert(table.count() == before + 1)
    val read = inDB(row)
    assert(read.name == "a" && read.n == 1 && read.key == row.key)
  }

  it should "give each row its own key" in {
    assert(saved().key != saved().key)
  }

  it should "update a saved row, keeping its key" in {
    val row = saved("a", 1)
    val (key, before) = (row.key, table.count())
    row.name = "b"
    row.n = 2
    row.save()
    assert(row.key == key && table.count() == before)
    val read = inDB(row)
    assert(read.name == "b" && read.n == 2)
  }

  it should "update only its own row" in {
    val (a, b) = (saved("a", 1), saved("b", 2))
    a.name = "changed"
    a.save()
    assert(inDB(b).name == "b" && inDB(a).name == "changed")
  }

  it should "leave the row without a key if the insert fails" in {
    val row = newRow()
    row.name = null // (the column is not null)
    intercept[Throwable](row.save())
    assert(!row.isPersisted_? && row.key == null)
  }

  "save(session)" should "insert and update in the session's transaction" in {
    val row = newRow("a", 1)
    DB.localTx({ session =>
      assert(row.save(session) eq row)
      row.n = 2
      row.save(session)
    })
    assert(inDB(row).n == 2)
  }

  it should "write nothing if the transaction is rolled back" in {
    val row = newRow()
    val existing = saved("kept", 1)
    intercept[IllegalStateException] {
      DB.localTx({ session =>
        row.save(session)
        existing.name = "changed"
        existing.save(session)
        throw new IllegalStateException("roll back")
      })
    }
    assert(table.getForIdOpt(row.key).isEmpty)
    assert(inDB(existing).name == "kept")
  }

  "update()" should "write a saved row's values" in {
    val row = saved("a", 1)
    row.name = "b"
    row.update()
    assert(inDB(row).name == "b")
  }

  it should "do nothing for a row never saved" in {
    val before = table.count()
    val row = newRow()
    row.update()
    assert(!row.isPersisted_? && table.count() == before)
  }

  "update(func)" should "apply the change to the row in the database, and refresh this copy" in {
    val row = saved("a", 1)
    val other = inDB(row)
    other.n = 50
    other.save()
    assert(row.update(_.name = "b") eq row)
    assert(row.name == "b" && row.n == 50, "this copy has the change, and what was in the database")
    val read = inDB(row)
    assert(read.name == "b" && read.n == 50, "the other columns aren't overwritten with this copy's stale values")
  }

  it should "apply the change to a row never saved, and insert it" in {
    val row = newRow("a", 1)
    row.update(_.name = "b")
    assert(row.isPersisted_? && inDB(row).name == "b")
  }

  "delete()" should "delete a saved row" in {
    val (row, other) = (saved(), saved())
    val before = table.count()
    row.delete()
    assert(table.getForIdOpt(row.key).isEmpty)
    assert(table.count() == before - 1 && table.getForIdOpt(other.key).isDefined)
    row.delete() // not there any more: nothing to do
    assert(table.count() == before - 1)
  }

  it should "do nothing for a row never saved" in {
    val before = table.count()
    newRow().delete()
    assert(table.count() == before)
  }

  "reload()" should "refresh the row in place" in {
    val row = saved("a", 1)
    val other = inDB(row)
    other.name = "b"
    other.n = 2
    other.save()
    assert(row.name == "a")
    assert(row.reload() eq row)
    assert(row.name == "b" && row.n == 2)
  }

  it should "leave a row never saved as it is" in {
    val row = newRow("a", 1)
    assert(row.reload() eq row)
    assert(row.name == "a" && row.n == 1 && !row.isPersisted_?)
  }

  it should "fail if the row isn't in the database any more" in {
    val row = saved()
    inDB(row).delete()
    intercept[NoSuchElementException](row.reload())
  }

  "reloadOpt()" should "refresh the row in place" in {
    val row = saved("a", 1)
    val other = inDB(row)
    other.name = "b"
    other.save()
    assert(row.reloadOpt().exists(_ eq row))
    assert(row.name == "b")
  }

  it should "return None, leaving the row as it is, if it isn't in the database" in {
    val row = saved("a", 1)
    inDB(row).delete()
    assert(row.reloadOpt().isEmpty)
    assert(row.name == "a" && row.n == 1)
    assert(newRow().reloadOpt().isEmpty)
  }

  "duplicate()" should "insert a new row with the same values, leaving the row as it is" in {
    val row = saved("a", 7)
    val (key, before) = (row.key, table.count())
    val dup = row.duplicate()
    assert(dup ne row)
    assert(dup.isPersisted_? && dup.key != key && dup.name == "a" && dup.n == 7)
    assert(row.key == key && row.name == "a")
    assert(table.count() == before + 1)
    dup.name = "b"
    dup.save()
    assert(inDB(row).name == "a" && inDB(dup).name == "b")
  }

  it should "insert a row with the values of one never saved, which stays so" in {
    val row = newRow("a", 7)
    val dup = row.duplicate()
    assert(!row.isPersisted_? && dup.isPersisted_? && inDB(dup).n == 7)
  }

  "upsert()" should "update the row with the same key" in {
    val row = saved("a", 1)
    val before = table.count()
    val copy = inDB(row)
    copy.name = "b"
    assert(copy.upsert() eq copy)
    assert(table.count() == before)
    assert(inDB(row).name == "b")
  }

  "equals and hashCode" should "go by the key, for saved rows" in {
    val (a, b) = (saved("a"), saved("b"))
    val a2 = inDB(a)
    a2.name = "other values"
    assert(a == a2 && a.hashCode == a2.hashCode)
    assert(a != b)
    assert(Set(a, a2, b).size == 2)
  }

  they should "go by identity, for rows never saved" in {
    val (u1, u2) = (newRow("a"), newRow("a"))
    assert(u1 == u1)
    assert(u1 != u2)
    assert(u1 != saved("a") && saved("a") != u1)
    assert(u1.hashCode == u1.hashCode)
    assert(Set(u1, u2, u1).size == 2, "rows never saved can be in a Set")
    assert(Map(u1 -> 1, u2 -> 2)(u2) == 2)
  }

  they should "not equal anything else" in {
    val row = saved()
    assert(!row.equals(row.key) && !row.equals("row") && !row.equals(null))
  }

  "RowWithIdBase" should "save, update, reload and delete a row, whatever its type" in {
    val row = newRow("a", 1)
    val base: RowWithIdBase = row
    base.save()
    assert(base.table eq table)
    assert(inDB(row).name == "a")
    row.name = "b"
    base.update()
    assert(inDB(row).name == "b")
    inDB(row).tap(_.name = "c").save()
    base.reload()
    assert(row.name == "c")
    base.delete()
    assert(table.getForIdOpt(row.key).isEmpty)
  }

  // ---------------------------------------------- TableWithId ----------------------------------------------

  "keyOf" should "be the row's key, if it was saved" in {
    val row = saved()
    assert(table.keyOf(row).contains(row.key))
    assert(table.keyOf(newRow()).isEmpty)
  }

  "idSQL" should "be the key column" in {
    assert(table.idSQL.value == keyColumn)
    assert(table.selectIdFromSQL.value.replaceAll("\\s+", " ").trim == s"""select $keyColumn from "${table.tableName}"""")
  }

  "getForIdOpt" should "return the row with the key, if there is one" in {
    val row = saved("a", 3)
    assert(table.getForIdOpt(row.key).exists(r => r.key == row.key && r.name == "a" && r.n == 3))
    assert(table.getForIdOpt(absentKey).isEmpty)
  }

  "getForId" should "return the row with the key, or fail" in {
    val row = saved("a", 3)
    assert(table.getForId(row.key).n == 3)
    val ex = intercept[Exception](table.getForId(absentKey))
    assert(ex.getMessage.contains("not found") && ex.getMessage.contains(table.tableName))
  }

  "getForIds" should "return the rows with the keys" in {
    val rows = (1 to 4).map(i => saved("many", i))
    assert(table.getForIds(rows.map(_.key) *).map(_.n).sorted == List(1, 2, 3, 4))
    assert(table.getForIds(rows(1).key, absentKey).map(_.n) == List(2))
    assert(table.getForIds().isEmpty)
    assert(table.getForIds(absentKey).isEmpty)
  }

  "_getForIds" should "return the rows with the keys, in a session (seeing what its transaction wrote)" in {
    val row = newRow("tx", 1)
    DB.localTx({ implicit session =>
      row.save(session)
      assert(table._getForIds(row.key).map(_.name) == List("tx"))
      assert(table._getForIds(absentKey).isEmpty)
    })
  }

  "selectIds" should "return the keys of the rows selected" in {
    val rows = (1 to 3).map(i => saved("ids", 1000 + i))
    assert(table.selectIds(sqls"name = 'ids' and n >= 1000").toSet == rows.map(_.key).toSet)
    assert(table.selectIds(sqls"name = 'ids' and n >= 1000", sqls"order by n desc limit 1") == List(rows.last.key))
    assert(table.selectIds().size == table.count())
    DB.readOnly({ implicit session =>
      assert(table._selectIds(sqls"name = 'ids' and n = 1001") == List(rows.head.key))
    })
  }

  "idFromWrappedResultSet" should "read the key from a result set, with and without a prefix" in {
    val row = saved()
    DB.readOnly({ implicit session =>
      val where = table.keyEqualsSQL(row.key)
      assert(sql"select ${table.selectSQL(None)} from ${table.tableNameSQLSyntaxQuoted} where $where".map(rs => table.idFromWrappedResultSet(rs)).single().contains(row.key))
      assert(sql"select ${table.selectSQL(Some("p"))} from ${table.tableNameSQLSyntaxQuoted} where $where".map(rs => table.idFromWrappedResultSet(rs, Some("p"))).single().contains(row.key))
      assert(sql"select ${table.selectSQL(Some("p"))} from ${table.tableNameSQLSyntaxQuoted} where $where".map(rs => table.idFromWrappedResultSetOpt(rs, Some("p"))).single().contains(Some(row.key)))
    })
  }

  "idFromWrappedResultSetOpt" should "be None for a null key (e.g. no row on that side of a left join)" in {
    DB.readOnly({ implicit session =>
      val read = sql"select t.${table.idSQL} from (select 1) x left join ${table.tableNameSQLSyntaxQuoted} t on false".map(rs => table.idFromWrappedResultSetOpt(rs)).single()
      assert(read.contains(None))
    })
  }

  "keyEqualsSQL and keyInSQL" should "select the rows with the keys" in {
    val rows = (1 to 3).map(i => saved("sql", i))
    assert(table.select(table.keyEqualsSQL(rows.head.key)).map(_.n) == List(1))
    assert(table.select(table.keyInSQL(rows.take(2).map(_.key))).map(_.n).sorted == List(1, 2))
    assert(table.select(table.keyInSQL(Nil)).isEmpty)
    assert(table.count(table.keyInSQL(rows.map(_.key))) == 3)
  }

  "delete(row)" should "delete the row, if it was saved" in {
    val (row, other) = (saved(), saved())
    assert(table.delete(row) == 1)
    assert(table.delete(row) == 0)
    assert(table.delete(newRow()) == 0)
    assert(table.getForIdOpt(row.key).isEmpty && table.getForIdOpt(other.key).isDefined)
  }

  "delete(keys), deleteAll and deleteAllById" should "delete the rows with those keys only" in {
    val rows = (1 to 7).map(i => saved("del", i))
    val before = table.count()
    assert(table.delete(Seq(rows(0).key, rows(1).key, absentKey)) == 2)
    assert(table.deleteAll(Seq(rows(2), rows(3), newRow())) == 2)
    assert(table.deleteAllById(Seq(rows(4).key)) == 1)
    assert(table.delete(Seq.empty[K]) == 0 && table.deleteAll(Nil) == 0 && table.deleteAllById(Nil) == 0)
    assert(table.count() == before - 5)
    assert(table.getForIds(rows.map(_.key) *).map(_.n).sorted == List(6, 7))
  }

  "upsertSQL" should "insert the row, or update the one with its key" in {
    val row = saved("a", 1)
    val copy = inDB(row)
    copy.n = 2
    DB.localTx({ implicit session =>
      assert(table.upsertSQL(copy).statement.contains(s"ON CONFLICT ($keyColumn) DO UPDATE SET"))
      table.upsertSQL(copy).update()
    })
    assert(inDB(row).n == 2)
  }

  protected def dropTableTest(): Unit = "Drop table" should "succeed" in {
    DB.localTx({ implicit session => table.__dropTableSQL.foreach(_.execute()) })
  }
}
