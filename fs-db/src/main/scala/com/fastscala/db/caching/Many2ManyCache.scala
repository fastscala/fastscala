package com.fastscala.db.caching

import com.fastscala.db.*
import com.fastscala.db.observable.{DBObserver, ObservableRowBase}
import org.slf4j.LoggerFactory
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

import java.util.UUID
import scala.collection.mutable.ListBuffer

class Many2ManyCache[K, L <: Row[L] & ObservableRowBase & RowWithId[K, L], J <: Row[J] & ObservableRowBase & RowWithId[K, J], R <: Row[R] & ObservableRowBase & RowWithId[K, R]](
  cacheL: TableCache[K, L],
  cacheJ: TableCache[K, J],
  cacheR: TableCache[K, R],
  getLeft: J => K,
  getRight: J => K,
  filterLeftOnJoinTable: Seq[K] => SQLSyntax,
  filterRightOnJoinTable: Seq[K] => SQLSyntax,
  left2Right: collection.mutable.Map[L, ListBuffer[R]] = collection.mutable.Map[L, ListBuffer[R]](),
  right2Left: collection.mutable.Map[R, ListBuffer[L]] = collection.mutable.Map[R, ListBuffer[L]]()
) extends Many2ManyDiffKeysCache[K, K, K, L, J, R](cacheL = cacheL, cacheJ = cacheJ, cacheR = cacheR, getLeft = getLeft, getRight = getRight, filterLeftOnJoinTable = filterLeftOnJoinTable, filterRightOnJoinTable = filterRightOnJoinTable, left2Right = left2Right, right2Left = right2Left)
