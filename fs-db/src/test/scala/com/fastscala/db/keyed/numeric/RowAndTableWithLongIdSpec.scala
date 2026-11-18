package com.fastscala.db.keyed.numeric

import com.fastscala.db.keyed.{KeyedSpecBase, KeyedSpecRow, TableWithId}
import scalikejdbc.*

import java.lang.reflect.Field

/** What's specific to Long keys in [[RowWithLongId]] and [[TableWithLongId]], for both ways of giving a row its id. */
abstract class LongIdSpecBase[R <: RowWithLongId[R] & KeyedSpecRow] extends KeyedSpecBase[java.lang.Long, R] {

  override def table: TableWithLongId[R]

  override def absentKey: java.lang.Long = -1L

  override def keyColumn: String = "id"

  "A row with a Long key" should "have its id as its key, given by the database when it's inserted" in {
    val row = newRow()
    assert(row.id == null && row.key == null)
    row.save()
    assert(row.id != null && (row.key eq row.id) && row.id > 0)
    val next = saved()
    assert(next.id == row.id + 1, "ids are sequential")
  }

  "insert()" should "insert the row, without giving it its id" in {
    val before = table.count()
    val row = newRow("inserted", 77)
    row.insert()
    assert(table.count() == before + 1)
    assert(!row.isPersisted_?)
    assert(table.select(sqls"name = 'inserted'").map(_.n) == List(77))
  }

  "The table" should "not update the id column" in {
    assert(table.fieldsList.map(_.getName).contains("id"))
    assert(!table.updateFields.map(_.getName).contains("id"))
    assert(table.updateFields.map(_.getName).toSet == Set("name", "n"))
    assert(table.keyEqualsSQL(5L).value == "id = ?")
  }

  it should "have the id as its primary key" in {
    assert(table.__createTableSQL.map(_.statement).exists(s => s.contains("PRIMARY KEY") && s.contains("\"id\"")))
  }

  it should "leave its sample row without an id" in {
    assert(table.sampleRow.id == null && table.createSampleRowInternal().id == null && table.createEmptyRow().id == null)
  }
}

// ------------------------------- ids from a sequence (TableWithLongIdSeqBacked) -------------------------------

class KeyedLongSeqSpecTable(var name: String = "row", var n: Int = 0) extends RowWithLongId[KeyedLongSeqSpecTable] with KeyedSpecRow {
  override def table: KeyedLongSeqSpecTable.type = KeyedLongSeqSpecTable
}

object KeyedLongSeqSpecTable extends TableWithLongIdSeqBacked[KeyedLongSeqSpecTable] {
  override def createSampleRow(): KeyedLongSeqSpecTable = new KeyedLongSeqSpecTable()
}

/** [[RowWithLongId]] and [[TableWithLongId]], with [[TableWithLongIdSeqBacked]]. */
class RowAndTableWithLongIdSeqBackedSpec extends LongIdSpecBase[KeyedLongSeqSpecTable] {

  override def table: KeyedLongSeqSpecTable.type = KeyedLongSeqSpecTable

  override def newRow(name: String, n: Int): KeyedLongSeqSpecTable = new KeyedLongSeqSpecTable(name, n)

  "A sequence backed table" should "have a sequence named after it, created and dropped with it" in {
    assert(table.sequenceIdName == "s_keyed_long_seq_spec_table_id")
    assert(table.__createTableSQL.head.statement == """CREATE SEQUENCE IF NOT EXISTS "s_keyed_long_seq_spec_table_id";""")
    assert(table.__dropTableSQL.head.statement == """DROP SEQUENCE IF EXISTS s_keyed_long_seq_spec_table_id;""")
    assert(table.__createTableSQL.size == table.__createTableOnlySQL.size + 2, "the sequence, the table and its primary key")
  }

  it should "insert the sequence's next value as the id" in {
    assert(table.insertFields.map(_.getName).toSet == Set("id", "name", "n"))
    val idField = table.fieldsList.find(_.getName == "id").get
    assert(table.valueToFragment(idField, null).value == "nextval('s_keyed_long_seq_spec_table_id')")
    assert(table.valueToFragment(idField, java.lang.Long.valueOf(42L)).parameters == Seq(42L), "a row that has an id keeps it")
    val nameField = table.fieldsList.find(_.getName == "name").get
    assert(table.valueToFragment(nameField, "x").parameters == Seq("x"))

    val row = saved()
    val current = DB.readOnly({ implicit session => SQL(s"select last_value from ${table.sequenceIdName}").map(_.long(1)).single() })
    assert(current.contains(row.id.longValue))
  }

  "upsert()" should "insert a row never saved, with the sequence's next id" in {
    val before = table.count()
    newRow("upserted", 9).upsert()
    assert(table.count() == before + 1)
    assert(table.select(sqls"name = 'upserted'").map(_.n) == List(9))
  }

  dropTableTest()

  "Dropping the table" should "drop its sequence" in {
    val sequences = DB.readOnly({ implicit session =>
      sql"select count(*) from pg_class where relkind = 'S' and relname = ${table.sequenceIdName}".map(_.long(1)).single()
    })
    assert(sequences.contains(0L))
  }
}

// ------------------------------- ids from a serial column (TableWithLongId alone) -------------------------------

class KeyedLongSerialSpecTable(var name: String = "row", var n: Int = 0) extends RowWithLongId[KeyedLongSerialSpecTable] with KeyedSpecRow {
  override def table: KeyedLongSerialSpecTable.type = KeyedLongSerialSpecTable
}

object KeyedLongSerialSpecTable extends TableWithLongId[KeyedLongSerialSpecTable] {
  override def createSampleRow(): KeyedLongSerialSpecTable = new KeyedLongSerialSpecTable()

  // TableWithLongId leaves out the id when it inserts: the column has to give itself a value.
  override def fieldTypeToSQLType(field: Field, clas: Class[?], value: => Any, columnConstrains: Set[String]): String =
    if (field.getName == "id") "bigserial not null" else super.fieldTypeToSQLType(field, clas, value, columnConstrains)
}

/** [[RowWithLongId]] and [[TableWithLongId]], with the id given by the column's default (a serial column). */
class RowAndTableWithLongIdSpec extends LongIdSpecBase[KeyedLongSerialSpecTable] {

  override def table: KeyedLongSerialSpecTable.type = KeyedLongSerialSpecTable

  override def newRow(name: String, n: Int): KeyedLongSerialSpecTable = new KeyedLongSerialSpecTable(name, n)

  "A table with Long keys" should "leave the id out of what it inserts" in {
    assert(table.insertFields.map(_.getName).toSet == Set("name", "n"))
    assert(!table.insertSQL(newRow()).statement.contains("\"id\""))
  }

  it should "be a TableWithId of java.lang.Long keys" in {
    val asKeyed: TableWithId[KeyedLongSerialSpecTable, java.lang.Long] = table
    val row = saved("typed", 1)
    assert(asKeyed.getForId(row.id).name == "typed")
  }

  dropTableTest()
}
