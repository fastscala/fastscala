package com.fastscala.db.cache

import com.fastscala.db.caching.{DBCompositeObserver, TableCache}
import com.fastscala.db.data.Countries
import com.fastscala.db.keyed.{PgRowWithLongId, PgTableWithLongId}
import com.fastscala.db.observable.ObservableRow
import com.fastscala.db.{PostgresDB, TestEntity2}
import org.scalatest.flatspec.AnyFlatSpec
import scalikejdbc.*

class Country(
               val name: String
             ) extends PgRowWithLongId[Country] with ObservableRow[java.lang.Long, Country] {

  override def table: PgTableWithLongId[Country] = Country
}

object Country extends PgTableWithLongId[Country] {
  override def createSampleRow(): Country = new Country("")
}

class TableCacheSpec extends AnyFlatSpec with PostgresDB {

  class DBCache extends DBCompositeObserver {
    val country = new TableCache[java.lang.Long, Country](Country)
  }

  "Create table" should "succeed" in {
    DB.localTx({ implicit session =>
      Country.__createTableSQL.foreach(_.execute())
    })
  }
  "Cache" should "store created row" in {
    implicit val cache = new DBCache()
    val saved = new Country(Countries.all.head).saveX()
    assert(cache.country.entries.contains(saved.id))
    assert(cache.country.entries(saved.id) == saved)
  }
  "Cache" should "remove deleted row" in {
    implicit val cache = new DBCache()
    val saved = new Country(Countries.all.head).saveX()
    assert(cache.country.entries.contains(saved.id))
    saved.deleteX()
    assert(!cache.country.entries.contains(saved.id))
  }
  "Cache" should "load missing rows on select" in {
    DB.localTx({ implicit session =>
      Country.__truncateSQL.execute()
    })
    Countries.all.map(new Country(_)).foreach(_.save())

    implicit val cache = new DBCache()

    val one = cache.country.select(sqls"""name = ${"Abkhazia"}""")
    assert(one.head.name == "Abkhazia")

    val loaded = cache.country.select(sqls"""name ilike ${"Y%"}""")
    assert(loaded.head.name == "Yemen")
  }
  "Delete table" should "succeed" in {
    DB.localTx({ implicit session =>
      Country.__dropTableSQL.execute()
    })
  }
}
