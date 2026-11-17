package com.fastscala.db

import com.fastscala.db.keyed.numeric.{RowWithLongId, TableWithLongId, TableWithLongIdSeqBacked}
import io.circe.Json
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

class PrimaryKeyAsLongSeqBackedSpecTable(
                                          var myInt: Int = 123
                                        ) extends RowWithLongId[PrimaryKeyAsLongSeqBackedSpecTable] {
  override def table: TableWithLongIdSeqBacked[PrimaryKeyAsLongSeqBackedSpecTable] = PrimaryKeyAsLongSeqBackedSpecTable
}

object PrimaryKeyAsLongSeqBackedSpecTable extends TableWithLongIdSeqBacked[PrimaryKeyAsLongSeqBackedSpecTable] {
  override def createSampleRow(): PrimaryKeyAsLongSeqBackedSpecTable = new PrimaryKeyAsLongSeqBackedSpecTable()
}

class PrimaryKeyAsLongSeqBackedSpec extends AnyFlatSpec with DBConn {

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      PrimaryKeyAsLongSeqBackedSpecTable.__createTableSQL.foreach(_.execute())
    })
  }
  "Save row" should "succeed" in {
    DB.localTx({ implicit session =>
      val saved = new PrimaryKeyAsLongSeqBackedSpecTable().save()
      assert(saved.myInt == 123)
      assert(saved.id == 1)
    })
  }
  "getForIdOpt" should "return the row by id" in {
    DB.localTx({ implicit session =>
      assert(PrimaryKeyAsLongSeqBackedSpecTable.getForIdOpt(1).isDefined)
    })
  }
  "duplicate" should "return the row by id" in {
    DB.localTx({ implicit session =>
      assert(PrimaryKeyAsLongSeqBackedSpecTable.selectAll().size == 1)
      val single = PrimaryKeyAsLongSeqBackedSpecTable.selectAll().head
      val dup = single.duplicate()
      assert(PrimaryKeyAsLongSeqBackedSpecTable.selectAll().size == 2)
    })
  }
  "reload" should "refresh the row in place" in {
    val row = PrimaryKeyAsLongSeqBackedSpecTable.selectAll().head
    val other = PrimaryKeyAsLongSeqBackedSpecTable.getForIdOpt(row.id).get
    other.myInt = 321
    other.save()
    assert(row.myInt == 123)
    assert(row.reload() eq row)
    assert(row.myInt == 321)
    other.myInt = 5
    other.save()
    assert(row.reloadOpt().exists(_ eq row) && row.myInt == 5)
    assert(new PrimaryKeyAsLongSeqBackedSpecTable().reloadOpt().isEmpty)
  }
  "The table" should "get and delete rows by id, as a table with UUID keys does" in {
    val T = PrimaryKeyAsLongSeqBackedSpecTable
    val rows = (1 to 5).map(i => new PrimaryKeyAsLongSeqBackedSpecTable(myInt = i).save())
    val ids = rows.map(_.id)
    assert(T.getForId(ids.head).myInt == 1)
    assert(intercept[Exception](T.getForId(-1L)).getMessage.contains("not found"))
    assert(T.getForIds(ids *).map(_.myInt).sorted == List(1, 2, 3, 4, 5))
    assert(T.selectIds().toSet.intersect(ids.toSet).size == 5)
    assert(T.delete(rows(0)) == 1)
    assert(T.delete(new PrimaryKeyAsLongSeqBackedSpecTable()) == 0)
    assert(T.delete(Seq(ids(1))) == 1)
    assert(T.deleteAll(Seq(rows(2), rows(3))) == 2)
    assert(T.deleteAllById(Seq(ids(4))) == 1)
    assert(T.getForIds(ids *).isEmpty)
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      PrimaryKeyAsLongSeqBackedSpecTable.__dropTableSQL.foreach(_.execute())
    })
  }
}
