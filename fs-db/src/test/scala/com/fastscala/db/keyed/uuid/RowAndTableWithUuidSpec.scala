package com.fastscala.db.keyed.uuid

import com.fastscala.db.TableWithUuidId
import com.fastscala.db.keyed.{KeyedSpecBase, KeyedSpecRow}
import scalikejdbc.*

import java.util.UUID

class KeyedUuidSpecTable(var name: String = "row", var n: Int = 0) extends RowWithUUID[KeyedUuidSpecTable] with KeyedSpecRow {
  override def table: KeyedUuidSpecTable.type = KeyedUuidSpecTable
}

object KeyedUuidSpecTable extends TableWithUuidId[KeyedUuidSpecTable] {
  override def createSampleRow(): KeyedUuidSpecTable = new KeyedUuidSpecTable()
}

/** [[RowWithUUID]], [[RowWithUUIDBase]], [[TableWithUUIDBase]] and [[TableWithUuidId]]. */
class RowAndTableWithUuidSpec extends KeyedSpecBase[UUID, KeyedUuidSpecTable] {

  override def table: KeyedUuidSpecTable.type = KeyedUuidSpecTable

  override def newRow(name: String, n: Int): KeyedUuidSpecTable = new KeyedUuidSpecTable(name, n)

  override def absentKey: UUID = UUID.randomUUID()

  override def keyColumn: String = "uuid"

  "A row with a UUID key" should "have its uuid as its key and id, once saved" in {
    val row = newRow()
    assert(row.uuid.isEmpty && row.key == null)
    assert(intercept[Exception](row.id).getMessage.contains(table.tableName))
    row.save()
    assert(row.uuid.isDefined && row.key == row.uuid.get && row.id == row.uuid.get)
    assert(row.uuid.get.version == 4, "a random UUID")
  }

  "saveSQL()" should "give a row never saved its uuid, and the SQL that inserts it" in {
    val row = newRow("sql", 1)
    val sql = row.saveSQL()
    assert(row.uuid.isDefined)
    assert(sql.statement.trim.toLowerCase.startsWith("insert into"))
    assert(table.getForIdOpt(row.id).isEmpty, "not run yet")
    DB.localTx({ implicit session => assert(sql.update() == 1) })
    assert(inDB(row).name == "sql")
  }

  it should "be the SQL that updates a saved row" in {
    val row = saved("a", 1)
    row.name = "b"
    val sql = row.saveSQL()
    assert(sql.statement.trim.toLowerCase.startsWith("update"))
    assert(inDB(row).name == "a", "not run yet")
    DB.localTx({ implicit session => assert(sql.update() == 1) })
    assert(inDB(row).name == "b")
  }

  "_reload()" should "refresh the row in place, in a session (seeing what its transaction wrote)" in {
    val row = saved("a", 1)
    val other = inDB(row)
    other.name = "b"
    DB.localTx({ implicit session =>
      other.save(session)
      assert(row._reload() eq row)
      assert(row.name == "b")
    })
    val unsaved = newRow("u")
    DB.readOnly({ implicit session => assert(unsaved._reload() eq unsaved) })
    assert(unsaved.name == "u")
  }

  "insert()" should "fail for a row that's saved already" in {
    val row = saved()
    val before = table.count()
    assert(intercept[Exception](row.insert()).getMessage.contains("already inserted"))
    assert(table.count() == before)
  }

  "upsert()" should "insert a row whose uuid no row has" in {
    val row = newRow("new", 5)
    row.uuid = Some(UUID.randomUUID())
    row.upsert()
    assert(inDB(row).name == "new")
  }

  "equals" should "not hold between rows of different tables with the same uuid" in {
    val row = saved()
    val other = new OtherUuidSpecTable()
    other.uuid = row.uuid
    assert(row != other && other != row)
  }

  "The table" should "give its sample row a placeholder uuid" in {
    assert(table.sampleRow.uuid.contains(TableWithUuidId.PlaceholderUUID))
    assert(table.createSampleRowInternal().uuid.contains(TableWithUuidId.PlaceholderUUID))
    assert(table.createSampleRow().uuid.isEmpty)
    assert(table.createEmptyRow().uuid.isEmpty)
  }

  it should "have placeholder values" in {
    assert(table.PlaceholderUUID == UUID.fromString("00000000-0000-0000-0000-000000000000"))
    assert(table.PlaceholderUUID eq TableWithUuidId.PlaceholderUUID)
    assert(table.PlaceholderOptionalUUID.contains(TableWithUuidId.PlaceholderUUID))
    assert(table.PlaceholderTimestamp.getTime == 0L)
    assert(table.PlaceholderOptionalTimestamp.exists(_.getTime == 0L))
  }

  it should "have a uuid column, which is its primary key and is written like any other column" in {
    assert(table.fieldsList.map(_.getName).contains("uuid"))
    assert(table.insertFields.map(_.getName).contains("uuid"))
    assert(table.__createTableSQL.map(_.statement).exists(s => s.contains("PRIMARY KEY") && s.contains("\"uuid\"")))
    assert(table.keyEqualsSQL(TableWithUuidId.PlaceholderUUID).value == "uuid = ?::UUID")
  }

  dropTableTest()
}

class OtherUuidSpecTable(var name: String = "other") extends RowWithUUID[OtherUuidSpecTable] {
  override def table: OtherUuidSpecTable.type = OtherUuidSpecTable
}

object OtherUuidSpecTable extends TableWithUuidId[OtherUuidSpecTable] {
  override def createSampleRow(): OtherUuidSpecTable = new OtherUuidSpecTable()
}
