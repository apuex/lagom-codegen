package com.github.apuex.commerce.sales.dao.mysql

import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection
import play._
import anorm.SqlParser._
import anorm.ParameterValue._
import anorm._

class OrderDaoImpl extends OrderDao {
  def createOrder(cmc: CreateOrderCmd)(implicit conn: Connection): Int = ???
  def retrieveOrder(cmd: RetrieveOrderCmd)(implicit conn: Connection): OrderVo = ???
  def updateOrder(cmd: UpdateOrderCmd)(implicit conn: Connection): Int = ???
  def deleteOrder(cmd: DeleteOrderCmd)(implicit conn: Connection): Int = ???
  def queryOrder(cmd: QueryCommand)(implicit conn: Connection): Seq[OrderVo] = ???
  def getOrderLines(cmd: GetOrderLinesCmd)(implicit conn: Connection): OrderLinesVo = ???
  def addOrderLines(cmd: AddOrderLinesCmd)(implicit conn: Connection): Int = ???
  def removeOrderLines(cmd: RemoveOrderLinesCmd)(implicit conn: Connection): Int = ???
  def getOrderPaymentType(cmd: GetOrderPaymentTypeCmd)(implicit conn: Connection): OrderPaymentTypeVo = ???
  def changeOrderPaymentType(cmd: ChangeOrderPaymentTypeCmd)(implicit conn: Connection): Int = ???
}
