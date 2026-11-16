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
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      PrimaryKeyAsLongSeqBackedSpecTable.__dropTableSQL.foreach(_.execute())
    })
  }
}
