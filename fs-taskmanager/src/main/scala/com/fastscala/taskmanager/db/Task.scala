package com.fastscala.taskmanager.db

import java.time.LocalDate

@SerialVersionUID(1L)
class Task(
            var name: String = "",
            var completed: Boolean = false,
            var dueDate: Option[LocalDate] = None,
            var assignedTo: Option[User] = None,
          ) extends Serializable {

}
