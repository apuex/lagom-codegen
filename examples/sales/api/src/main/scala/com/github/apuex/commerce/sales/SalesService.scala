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

  def selectOrderItemByOrderId(orderId: String): ServiceCall[NotUsed, OrderItemListVo]

  def deleteOrderItemByOrderId(orderId: String): ServiceCall[NotUsed, Int]

  def selectOrderItemByProductId(productId: String): ServiceCall[NotUsed, OrderItemListVo]

  def deleteOrderItemByProductId(productId: String): ServiceCall[NotUsed, Int]

  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override def descriptor: Descriptor = {
    import Service._
    import ScalapbJson._

    

    named("sales")
      .withCalls(
        pathCall("/api/events?offset", events _),
        pathCall("/api/alarm/create-alarm", createAlarm _),
        pathCall("/api/alarm/retrieve-alarm", retrieveAlarm _),
        pathCall("/api/alarm/update-alarm", updateAlarm _),
        pathCall("/api/alarm/delete-alarm", deleteAlarm _),
        pathCall("/api/alarm/query-alarm", queryAlarm _),
        pathCall("/api/alarm/retrieve-alarm-by-rowid/:rowid", retrieveAlarmByRowid _),
        pathCall("/api/alarm/begin-alarm", beginAlarm _),
        pathCall("/api/alarm/end-alarm", endAlarm _),
        pathCall("/api/payment-type/create-payment-type", createPaymentType _),
        pathCall("/api/payment-type/retrieve-payment-type", retrievePaymentType _),
        pathCall("/api/payment-type/update-payment-type", updatePaymentType _),
        pathCall("/api/payment-type/delete-payment-type", deletePaymentType _),
        pathCall("/api/payment-type/query-payment-type", queryPaymentType _),
        pathCall("/api/payment-type/retrieve-payment-type-by-rowid/:rowid", retrievePaymentTypeByRowid _),
        pathCall("/api/product/create-product", createProduct _),
        pathCall("/api/product/retrieve-product", retrieveProduct _),
        pathCall("/api/product/update-product", updateProduct _),
        pathCall("/api/product/delete-product", deleteProduct _),
        pathCall("/api/product/query-product", queryProduct _),
        pathCall("/api/product/retrieve-product-by-rowid/:rowid", retrieveProductByRowid _),
        pathCall("/api/product-sales/get-product-sales", getProductSales _),
        pathCall("/api/product-sales/update-product-sales", updateProductSales _),
        pathCall("/api/product-name/get-product-name", getProductName _),
        pathCall("/api/product-name/change-product-name", changeProductName _),
        pathCall("/api/product-unit/get-product-unit", getProductUnit _),
        pathCall("/api/product-unit/change-product-unit", changeProductUnit _),
        pathCall("/api/unit-price/get-unit-price", getUnitPrice _),
        pathCall("/api/unit-price/change-unit-price", changeUnitPrice _),
        pathCall("/api/order/create-order", createOrder _),
        pathCall("/api/order/retrieve-order", retrieveOrder _),
        pathCall("/api/order/update-order", updateOrder _),
        pathCall("/api/order/delete-order", deleteOrder _),
        pathCall("/api/order/query-order", queryOrder _),
        pathCall("/api/order/retrieve-order-by-rowid/:rowid", retrieveOrderByRowid _),
        pathCall("/api/order-lines/get-order-lines", getOrderLines _),
        pathCall("/api/order-lines/add-order-lines", addOrderLines _),
        pathCall("/api/order-lines/remove-order-lines", removeOrderLines _),
        pathCall("/api/order-payment-type/get-order-payment-type", getOrderPaymentType _),
        pathCall("/api/order-payment-type/change-order-payment-type", changeOrderPaymentType _),
        pathCall("/api/order-item/create-order-item", createOrderItem _),
        pathCall("/api/order-item/retrieve-order-item", retrieveOrderItem _),
        pathCall("/api/order-item/update-order-item", updateOrderItem _),
        pathCall("/api/order-item/delete-order-item", deleteOrderItem _),
        pathCall("/api/order-item/query-order-item", queryOrderItem _),
        pathCall("/api/order-item/retrieve-order-item-by-rowid/:rowid", retrieveOrderItemByRowid _),
        pathCall("/api/order-item/select-order-item-by-order-id?orderId", selectOrderItemByOrderId _),
        pathCall("/api/order-item/delete-order-item-by-order-id?orderId", deleteOrderItemByOrderId _),
        pathCall("/api/order-item/select-order-item-by-product-id?productId", selectOrderItemByProductId _),
        pathCall("/api/order-item/delete-order-item-by-product-id?productId", deleteOrderItemByProductId _)
      ).withAutoAcl(true)
  }
}
