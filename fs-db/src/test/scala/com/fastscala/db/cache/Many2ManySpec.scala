package com.fastscala.db.cache

import com.fastscala.db.PostgresDB
import com.fastscala.db.caching.{DBCompositeObserver, JoinTableCache, Many2ManyCache, One2ManyCache, TableCache}
import com.fastscala.db.data.Countries
import com.fastscala.db.keyed.{PgTableWithLongId, PgRowWithLongId}
import com.fastscala.db.observable.ObservableRow
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

import java.util.UUID


class Many2ManySpec extends AnyFlatSpec with PostgresDB {

  class DBCache extends DBCompositeObserver {
    val student = new TableCache[java.lang.Long, Student](Student)
    val course = new TableCache[java.lang.Long, Course](Course)
    val student2CourseCache = new TableCache[java.lang.Long, Student2Course](Student2Course)
    val student2Course = new Many2ManyCache[java.lang.Long, Student, Student2Course, Course](student, student2CourseCache, course, _.studentId, _.courseId, ids => sqls"student_id in ($ids)", ids => sqls"course_id in ($ids)")
  }

  "Create tables" should "succeed" in {
    DB.localTx({ implicit session =>
      Student.__createTableSQL.foreach(_.execute())
      Course.__createTableSQL.foreach(_.execute())
      Student2Course.__createTableSQL.foreach(_.execute())
    })
  }

  val alice = new Student("Alice")
  val bob = new Student("Bob")
  val charlie = new Student("Charlie")
  val math = new Course("Math")
  val english = new Course("English")

  "Create data" should "succeed" in {
    alice.saveX()
    bob.saveX()
    charlie.saveX()
    math.saveX()
    english.saveX()

    new Student2Course(alice, math).saveX()
    new Student2Course(bob, math).saveX()
    new Student2Course(bob, english).saveX()
    new Student2Course(charlie, english).saveX()
  }
  "Cache" should "get the correct rows" in {
    implicit val cache = new DBCache()

    assert(cache.student2Course.getLeftForRight(math).toSet == Set(alice, bob))
    assert(cache.student2Course.getRightForLeft(bob).toSet == Set(math, english))
    assert(cache.student2Course.getLeftForRight(math, english).toSet == Set(alice, bob, charlie))
    assert(cache.student2Course.getRightForLeft(alice, bob, charlie).toSet == Set(math, english))
  }

  "Delete tables" should "succeed" in {
    DB.localTx({ implicit session =>
      Student.__dropTableSQL.execute()
      Course.__dropTableSQL.execute()
      Student2Course.__dropTableSQL.execute()
    })
  }
}
