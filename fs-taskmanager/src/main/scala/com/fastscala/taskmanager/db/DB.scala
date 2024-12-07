package com.fastscala.taskmanager.db

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

@SerialVersionUID(1L)
class DB extends Serializable {

  val tasks = collection.mutable.ListBuffer[Task]()
  val users = collection.mutable.ListBuffer[User]()
}

object DB {

  var instance: DB = new DB()

  def apply(): DB = instance

  val File = "fs_taskmanager.db"

  def save(): Unit = {
    val oos = new ObjectOutputStream(new FileOutputStream(File))
    oos.writeObject(instance)
    oos.close()
  }

  def load(): Unit = {
    val ois = new ObjectInputStream(new FileInputStream(File))
    instance = ois.readObject().asInstanceOf[DB]
    ois.close()
  }
}
