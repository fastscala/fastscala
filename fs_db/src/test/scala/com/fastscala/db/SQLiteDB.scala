package com.fastscala.db

import org.sqlite.{SQLiteConfig, SQLiteDataSource}
import scalikejdbc.{ConnectionPool, ConnectionPoolSettings, DataSourceConnectionPool}

trait SQLiteDB {

  //  val settings = ConnectionPoolSettings(initialSize = 5, maxSize = 20, connectionTimeoutMillis = 3000L, validationQuery = "select 1")
  //  ConnectionPool.singleton("jdbc:sqlite:fastscala_db_test.db", "fastscala_db_test", "", settings)

  Class.forName("org.sqlite.JDBC")

  //  ConnectionPool.add("db", "jdbc:sqlite:fastscala_db_test.db", "fastscala_db_test", "")

  scalikejdbc.GlobalSettings.jtaDataSourceCompatible = true
  ConnectionPool.singleton(
    {
      val conf = new SQLiteConfig()
      conf.setReadOnly(false)
      val source = new SQLiteDataSource(conf)
      source.setUrl(s"jdbc:sqlite:fastscala_db_test.db")
      new DataSourceConnectionPool(source)
    }
  )
}
