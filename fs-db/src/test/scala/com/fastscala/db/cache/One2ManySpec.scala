package com.fastscala.db.cache

import com.fastscala.db.PostgresDB
import com.fastscala.db.caching.{DBCompositeObserver, One2ManyCache, TableCache}
import com.fastscala.db.data.Countries
import com.fastscala.db.keyed.{PgTableWithLongId, PgRowWithLongId}
import com.fastscala.db.observable.ObservableRow
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc._

class Teacher(
               val name: String
             ) extends PgRowWithLongId[Teacher] with ObservableRow[java.lang.Long, Teacher] {
  override def table: PgTableWithLongId[Teacher] = Teacher
}

object Teacher extends PgTableWithLongId[Teacher] {
  override def createSampleRow(): Teacher = new Teacher("")
}

class Class(
             val name: String
             , var teacherId: Long
           ) extends PgRowWithLongId[Class] with ObservableRow[java.lang.Long, Class] {
  override def table: PgTableWithLongId[Class] = Class

  override def toString: String = name
}

object Class extends PgTableWithLongId[Class] {
  override def createSampleRow(): Class = new Class("", 0)
}

class One2ManySpec extends AnyFlatSpec with PostgresDB {

  class DBCache extends DBCompositeObserver {
    val teacher = new TableCache[java.lang.Long, Teacher](Teacher)
    val clas = new TableCache[java.lang.Long, Class](Class)
    val class2Teacher = new One2ManyCache[java.lang.Long, Teacher, Class](teacher, clas, _.teacherId, id => sqls"where teacher_id = $id")
  }

  "Create tables" should "succeed" in {
    DB.localTx({ implicit session =>
      Teacher.__createTableSQL.foreach(_.execute())
      Class.__createTableSQL.foreach(_.execute())
    })
  }
  val alice = new Teacher("Alice")
  val bob = new Teacher("Bob")
  val charlie = new Teacher("Charlie")
  "Create data" should "succeed" in {
    alice.save()
    bob.save()
    charlie.save()
  }
  "Cache" should "store mappings when creating rows in 'Many' table" in {
    implicit val cache = new DBCache()
    val grade7 = new Class("7th Grade", alice.id).saveX()
    assert(cache.class2Teacher.getMany(alice) == Seq(grade7))
    assert(cache.class2Teacher.getMany(alice.id) == Seq(grade7))
    assert(cache.class2Teacher.getOne(grade7) == Some(alice))
  }
  "Cache" should "remove mappings when deleting rows in 'Many' table" in {
    implicit val cache = new DBCache()
    val grade8 = new Class("8th Grade", bob.id).saveX()
    assert(cache.class2Teacher.getMany(bob) == Seq(grade8))
    grade8.deleteX()
    assert(cache.class2Teacher.getMany(bob) == Seq())
  }
  "Cache" should "update mappings when changing rows in 'Many' table" in {
    implicit val cache = new DBCache()
    val grade9 = new Class("9th Grade", charlie.id).saveX()
    assert(cache.class2Teacher.getMany(charlie) == Seq(grade9))
    grade9.teacherId = alice.id
    grade9.saveX()
    assert(cache.class2Teacher.getMany(charlie) == Seq())
    assert(cache.class2Teacher.getMany(alice).contains(grade9))
    assert(cache.class2Teacher.getOne(grade9) == Some(alice))
  }
  "Delete tables" should "succeed" in {
    DB.localTx({ implicit session =>
      Teacher.__dropTableSQL.execute()
      Class.__dropTableSQL.execute()
    })
  }
}
