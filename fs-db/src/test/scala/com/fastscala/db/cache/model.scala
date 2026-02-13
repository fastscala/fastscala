package com.fastscala.db.cache

import com.fastscala.db.keyed.numeric.{RowWithLongId, TableWithLongId}
import com.fastscala.db.observable.ObservableRow

class Student(
               val name: String
             ) extends RowWithLongId[Student] with ObservableRow[java.lang.Long, Student] {
  override def table: TableWithLongId[Student] = Student

  override def toString: String = s"Student(id=$id,name=$name)"
}

object Student extends TableWithLongId[Student] {
  override def createSampleRow(): Student = new Student("")
}

class Course(
              val name: String
            ) extends RowWithLongId[Course] with ObservableRow[java.lang.Long, Course] {
  override def table: TableWithLongId[Course] = Course

  override def toString: String = s"Course(id=$id,name=$name)"
}

object Course extends TableWithLongId[Course] {
  override def createSampleRow(): Course = new Course("")
}

class Student2Course(
                      val studentId: Long,
                      val courseId: Long
                    ) extends RowWithLongId[Student2Course] with ObservableRow[java.lang.Long, Student2Course] {

  def this(student: Student, course: Course) = this(student.id, course.id)

  override def table: TableWithLongId[Student2Course] = Student2Course

  override def toString: String = s"Student($studentId) => Course($courseId)"
}

object Student2Course extends TableWithLongId[Student2Course] {
  override def createSampleRow(): Student2Course = new Student2Course(0, 0)
}
