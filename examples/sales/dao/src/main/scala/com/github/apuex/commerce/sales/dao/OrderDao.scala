/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.dao

import com.github.apuex.commerce.sales._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection

trait OrderDao {
  def createOrder(evt: CreateOrderEvent)(implicit conn: Connection): Int

  def retrieveOrder(cmd: RetrieveOrderCmd)(implicit conn: Connection): OrderVo

  def updateOrder(evt: UpdateOrderEvent)(implicit conn: Connection): Int

  def deleteOrder(evt: DeleteOrderEvent)(implicit conn: Connection): Int

  def queryOrder(cmd: QueryCommand)(implicit conn: Connection): Seq[OrderVo]

  def retrieveOrderByRowid(rowid: String)(implicit conn: Connection): OrderVo

  def getOrderLines(cmd: GetOrderLinesCmd)(implicit conn: Connection): OrderLinesVo

  def addOrderLines(evt: AddOrderLinesEvent)(implicit conn: Connection): Int

  def removeOrderLines(evt: RemoveOrderLinesEvent)(implicit conn: Connection): Int

  def getOrderPaymentType(cmd: GetOrderPaymentTypeCmd)(implicit conn: Connection): OrderPaymentTypeVo

  def changeOrderPaymentType(evt: ChangeOrderPaymentTypeEvent)(implicit conn: Connection): Int
}
