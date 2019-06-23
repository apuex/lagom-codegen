package com.github.apuex.commerce.sales.dao

import com.github.apuex.commerce.sales._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection

trait OrderDao {
  def createOrder(cmc: CreateOrderCmd)(implicit conn: Connection): Int
  def retrieveOrder(cmd: RetrieveOrderCmd)(implicit conn: Connection): OrderVo
  def updateOrder(cmd: UpdateOrderCmd)(implicit conn: Connection): Int
  def deleteOrder(cmd: DeleteOrderCmd)(implicit conn: Connection): Int
  def queryOrder(cmd: QueryCommand)(implicit conn: Connection): Seq[OrderVo]
}
