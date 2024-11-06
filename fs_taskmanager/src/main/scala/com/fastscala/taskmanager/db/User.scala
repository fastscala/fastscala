package com.fastscala.taskmanager.db

@SerialVersionUID(1L)
class User(
            var firstName: String,
            var lastName: String,
            var email: String,
          ) extends Serializable {

  def fullName = firstName + " " + lastName
}
