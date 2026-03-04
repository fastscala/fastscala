package com.fastscala.db

import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

class CreateDeleteTableSpecTable extends Row[CreateDeleteTableSpecTable] {

  var username: String = ""

  override def table: Table[CreateDeleteTableSpecTable] = CreateDeleteTableSpecTable
}

object CreateDeleteTableSpecTable extends Table[CreateDeleteTableSpecTable] {
  override def createSampleRow(): CreateDeleteTableSpecTable = new CreateDeleteTableSpecTable
}

class CreateDeleteTableSpec extends AnyFlatSpec with PostgresDB {

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      CreateDeleteTableSpecTable.__createTableSQL.foreach(_.execute())
    })
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      CreateDeleteTableSpecTable.__dropTableSQL.foreach(_.execute())
    })
  }
}
