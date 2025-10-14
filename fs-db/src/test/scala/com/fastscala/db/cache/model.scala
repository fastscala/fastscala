package com.fastscala.db.cache

import com.fastscala.db.keyed.{PgTableWithLongId, PgRowWithLongId}
import com.fastscala.db.observable.ObservableRow

class Student(
               val name: String
             ) extends PgRowWithLongId[Student] with ObservableRow[java.lang.Long, Student] {
  override def table: PgTableWithLongId[Student] = Student
}

object Student extends PgTableWithLongId[Student] {
  override def createSampleRow(): Student = new Student("")
}

class Course(
              val name: String
            ) extends PgRowWithLongId[Course] with ObservableRow[java.lang.Long, Course] {
  override def table: PgTableWithLongId[Course] = Course

  override def toString: String = name
}

object Course extends PgTableWithLongId[Course] {
  override def createSampleRow(): Course = new Course("")
}

class Student2Course(
                      val studentId: Long,
                      val courseId: Long
                    ) extends PgRowWithLongId[Student2Course] with ObservableRow[java.lang.Long, Student2Course] {

  def this(student: Student, course: Course) = this(student.id, course.id)

  override def table: PgTableWithLongId[Student2Course] = Student2Course

  override def toString: String = s"Student($studentId) => Course($courseId)"
}

object Student2Course extends PgTableWithLongId[Student2Course] {
  override def createSampleRow(): Student2Course = new Student2Course(0, 0)
}
