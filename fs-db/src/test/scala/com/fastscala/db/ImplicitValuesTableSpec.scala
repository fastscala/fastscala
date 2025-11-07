package com.fastscala.db

import com.fastscala.db.Department.DepartmentId
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

object Department {
  opaque type DepartmentId = Long

  object DepartmentId {
    def apply(id: Long): DepartmentId = id
  }
}

class ImplicitValuesTestEntity(var name: String)(implicit val departmentId: DepartmentId) extends Row[ImplicitValuesTestEntity] {
  override def table: PgTable[ImplicitValuesTestEntity] = ImplicitValuesTestEntity
}

object ImplicitValuesTestEntity extends PgTable[ImplicitValuesTestEntity] {
  override def createSampleRow(): ImplicitValuesTestEntity = new ImplicitValuesTestEntity("John Doe")(using DepartmentId(1234))
}

trait ImplicitValuesSpecBase extends AnyFlatSpec {

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      ImplicitValuesTestEntity.__createTableSQL.foreach(_.execute())
    })
  }
  "Insert row" should "succeed" in {
    DB.localTx({ implicit session =>
      new ImplicitValuesTestEntity("Johnny")(using DepartmentId(3210)).insert()
    })
  }
  "Read light row" should "succeed" in {
    DB.localTx({ implicit session =>
      val single = ImplicitValuesTestEntity.selectAll().head

      assert(single.name == "Johnny")
      assert(single.departmentId == DepartmentId(3210))
    })
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      ImplicitValuesTestEntity.__dropTableSQL.execute()
    })
  }
}

class SQLiteImplicitValuesSpec extends ImplicitValuesSpecBase with SQLiteDB

class PostgresImplicitValuesSpec extends ImplicitValuesSpecBase with PostgresDB
