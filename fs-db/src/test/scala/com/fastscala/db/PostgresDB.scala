package com.fastscala.db

import scalikejdbc.{ConnectionPool, ConnectionPoolSettings}

trait PostgresDB {

  Class.forName("org.postgresql.Driver")

  val settings = ConnectionPoolSettings(initialSize = 5, maxSize = 20, connectionTimeoutMillis = 3000L, validationQuery = "select 1")
  ConnectionPool.singleton("jdbc:postgresql://localhost/fastscala_db_test", "fastscala_db_test", "", settings)
}
