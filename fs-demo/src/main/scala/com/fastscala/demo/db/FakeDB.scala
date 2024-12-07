package com.fastscala.demo.db

object FakeDB {

  val users = collection.mutable.ListBuffer[User](
    // Hardcoded user for demo:
    new User("John", "Doe", "admin", loginToken = "f5938e4f-caf7-4b6a-a0d2-46377f22e055") {
      override def setPassword(password: String): this.type = super.setPassword("admin")
    }.setPassword("admin")
  )
}
