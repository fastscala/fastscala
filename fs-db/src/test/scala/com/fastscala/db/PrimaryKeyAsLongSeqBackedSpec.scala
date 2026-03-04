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
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      PrimaryKeyAsLongSeqBackedSpecTable.__dropTableSQL.foreach(_.execute())
    })
  }
}
