/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales

import com.github.apuex.commerce.sales._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.timestamp.Timestamp
import akka._
import akka.stream.scaladsl._
import com.lightbend.lagom.scaladsl.api._
import play.api.libs.json.Json

trait SalesService extends Service {

  def createAlarm(): ServiceCall[CreateAlarmCmd, Int]

  def retrieveAlarm(): ServiceCall[RetrieveAlarmCmd, AlarmVo]

  def updateAlarm(): ServiceCall[UpdateAlarmCmd, Int]

  def deleteAlarm(): ServiceCall[DeleteAlarmCmd, Int]

  def queryAlarm(): ServiceCall[QueryCommand, AlarmListVo]

  def retrieveAlarmByRowid(rowid: String): ServiceCall[NotUsed, AlarmVo]

  def beginAlarm(): ServiceCall[BeginAlarmCmd, Int]

  def endAlarm(): ServiceCall[EndAlarmCmd, Int]

  def createPaymentType(): ServiceCall[CreatePaymentTypeCmd, Int]

  def retrievePaymentType(): ServiceCall[RetrievePaymentTypeCmd, PaymentTypeVo]

  def updatePaymentType(): ServiceCall[UpdatePaymentTypeCmd, Int]

  def deletePaymentType(): ServiceCall[DeletePaymentTypeCmd, Int]

  def queryPaymentType(): ServiceCall[QueryCommand, PaymentTypeListVo]

  def retrievePaymentTypeByRowid(rowid: String): ServiceCall[NotUsed, PaymentTypeVo]

  def createProduct(): ServiceCall[CreateProductCmd, Int]

  def retrieveProduct(): ServiceCall[RetrieveProductCmd, ProductVo]

  def updateProduct(): ServiceCall[UpdateProductCmd, Int]

  def deleteProduct(): ServiceCall[DeleteProductCmd, Int]

  def queryProduct(): ServiceCall[QueryCommand, ProductListVo]

  def retrieveProductByRowid(rowid: String): ServiceCall[NotUsed, ProductVo]

  def getProductSales(): ServiceCall[GetProductSalesCmd, ProductSalesVo]

  def updateProductSales(): ServiceCall[UpdateProductSalesCmd, Int]

  def getProductName(): ServiceCall[GetProductNameCmd, ProductNameVo]

  def changeProductName(): ServiceCall[ChangeProductNameCmd, Int]

  def getProductUnit(): ServiceCall[GetProductUnitCmd, ProductUnitVo]

  def changeProductUnit(): ServiceCall[ChangeProductUnitCmd, Int]

  def getUnitPrice(): ServiceCall[GetUnitPriceCmd, UnitPriceVo]

  def changeUnitPrice(): ServiceCall[ChangeUnitPriceCmd, Int]

  def createOrder(): ServiceCall[CreateOrderCmd, Int]

  def retrieveOrder(): ServiceCall[RetrieveOrderCmd, OrderVo]

  def updateOrder(): ServiceCall[UpdateOrderCmd, Int]

  def deleteOrder(): ServiceCall[DeleteOrderCmd, Int]

  def queryOrder(): ServiceCall[QueryCommand, OrderListVo]

  def retrieveOrderByRowid(rowid: String): ServiceCall[NotUsed, OrderVo]

  def getOrderLines(): ServiceCall[GetOrderLinesCmd, OrderLinesVo]

  def addOrderLines(): ServiceCall[AddOrderLinesCmd, Int]

  def removeOrderLines(): ServiceCall[RemoveOrderLinesCmd, Int]

  def getOrderPaymentType(): ServiceCall[GetOrderPaymentTypeCmd, OrderPaymentTypeVo]

  def changeOrderPaymentType(): ServiceCall[ChangeOrderPaymentTypeCmd, Int]

  def createOrderItem(): ServiceCall[CreateOrderItemCmd, Int]

  def retrieveOrderItem(): ServiceCall[RetrieveOrderItemCmd, OrderItemVo]

  def updateOrderItem(): ServiceCall[UpdateOrderItemCmd, Int]

  def deleteOrderItem(): ServiceCall[DeleteOrderItemCmd, Int]

  def queryOrderItem(): ServiceCall[QueryCommand, OrderItemListVo]

  def retrieveOrderItemByRowid(rowid: String): ServiceCall[NotUsed, OrderItemVo]

  def selectByOrderId(orderId: String): ServiceCall[NotUsed, OrderItemListVo]

  def deleteByOrderId(orderId: String): ServiceCall[NotUsed, Int]

  def selectByProductId(productId: String): ServiceCall[NotUsed, OrderItemListVo]

  def deleteByProductId(productId: String): ServiceCall[NotUsed, Int]

  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override def descriptor: Descriptor = {
    import Service._
    import ScalapbJson._

    

    named("sales")
      .withCalls(
        
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
}
