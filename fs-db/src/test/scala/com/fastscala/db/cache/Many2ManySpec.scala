package com.fastscala.db.cache

import com.fastscala.db.PostgresDB
import com.fastscala.db.caching.*
import com.fastscala.db.data.Countries
import com.fastscala.db.keyed.{PgRowWithLongId, PgTableWithLongId}
import com.fastscala.db.observable.ObservableRow
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
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
    alice.save()
    bob.save()
    charlie.save()
    math.save()
    english.save()

    new Student2Course(alice, math).save()
    new Student2Course(bob, math).save()
    new Student2Course(bob, english).save()
    new Student2Course(charlie, english).save()
  }
  "Cache" should "get the correct rows" in {
    implicit val cache = new DBCache()

    cache.student2Course.getLeftForRight(math).toSet shouldEqual Set(alice, bob)
    cache.student2Course.getRightForLeft(bob).toSet shouldEqual Set(math, english)
    cache.student2Course.getLeftForRight(math, english).toSet shouldEqual Set(alice, bob, charlie)
    cache.student2Course.getRightForLeft(alice, bob, charlie).toSet shouldEqual Set(math, english)
  }
  "Cache" should "work after deleting a row" in {
    implicit val cache = new DBCache()

    // Force hydrate cache:
    cache.student2Course.getLeftForRight(math, english)
    cache.student2Course.getRightForLeft(alice, bob, charlie)

    val alice2Math = cache.student2Course.getJoinForLeftAndRight(alice, math)
    alice2Math.isDefined shouldEqual true
    alice2Math.foreach(_.deleteX())

    cache.student2Course.getLeftForRight(math).toSet shouldEqual Set(bob)
    cache.student2Course.getRightForLeft(bob).toSet shouldEqual Set(math, english)
    cache.student2Course.getLeftForRight(math, english).toSet shouldEqual Set(bob, charlie)
  }
  "Cache" should "work after creating a row" in {
    implicit val cache = new DBCache()

    // Force hydrate cache:
    cache.student2Course.getLeftForRight(math, english)
    cache.student2Course.getRightForLeft(alice, bob, charlie)

    new Student2Course(alice, math).saveX()

    cache.student2Course.getLeftForRight(math).toSet shouldEqual Set(alice, bob)
    cache.student2Course.getRightForLeft(bob).toSet shouldEqual Set(math, english)
    cache.student2Course.getLeftForRight(math, english).toSet shouldEqual Set(alice, bob, charlie)
    cache.student2Course.getRightForLeft(alice, bob, charlie).toSet shouldEqual Set(math, english)
  }

  "Delete tables" should "succeed" in {
    DB.localTx({ implicit session =>
      Student.__dropTableSQL.execute()
      Course.__dropTableSQL.execute()
      Student2Course.__dropTableSQL.execute()
    })
  }
}
