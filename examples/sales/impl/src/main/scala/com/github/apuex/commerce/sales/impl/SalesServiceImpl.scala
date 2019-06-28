/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.impl

import akka._
import akka.stream.scaladsl._
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.timestamp.Timestamp
import com.lightbend.lagom.scaladsl.api._
import play.api.db.Database
import play.api.libs.json.Json

import scala.concurrent.Future

class SalesServiceImpl (alarmDao: AlarmDao,
  paymentTypeDao: PaymentTypeDao,
  productDao: ProductDao,
  orderDao: OrderDao,
  orderItemDao: OrderItemDao,
  db: Database)
  extends SalesService {

  def createAlarm(): ServiceCall[CreateAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         alarmDao.createAlarm(cmd)
      }
    )
  }

  def retrieveAlarm(): ServiceCall[RetrieveAlarmCmd, AlarmVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         alarmDao.retrieveAlarm(cmd)
      }
    )
  }

  def updateAlarm(): ServiceCall[UpdateAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         alarmDao.updateAlarm(cmd)
      }
    )
  }

  def deleteAlarm(): ServiceCall[DeleteAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         alarmDao.deleteAlarm(cmd)
      }
    )
  }

  def queryAlarm(): ServiceCall[QueryCommand, AlarmListVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         AlarmListVo(alarmDao.queryAlarm(cmd))
      }
    )
  }

  def retrieveAlarmByRowid(rowid: String): ServiceCall[NotUsed, AlarmVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         alarmDao.retrieveAlarmByRowid(rowid)
      }
    )
  }

  def beginAlarm(): ServiceCall[BeginAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         alarmDao.beginAlarm(cmd)
      }
    )
  }

  def endAlarm(): ServiceCall[EndAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         alarmDao.endAlarm(cmd)
      }
    )
  }

  def createPaymentType(): ServiceCall[CreatePaymentTypeCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         paymentTypeDao.createPaymentType(cmd)
      }
    )
  }

  def retrievePaymentType(): ServiceCall[RetrievePaymentTypeCmd, PaymentTypeVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         paymentTypeDao.retrievePaymentType(cmd)
      }
    )
  }

  def updatePaymentType(): ServiceCall[UpdatePaymentTypeCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         paymentTypeDao.updatePaymentType(cmd)
      }
    )
  }

  def deletePaymentType(): ServiceCall[DeletePaymentTypeCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         paymentTypeDao.deletePaymentType(cmd)
      }
    )
  }

  def queryPaymentType(): ServiceCall[QueryCommand, PaymentTypeListVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         PaymentTypeListVo(paymentTypeDao.queryPaymentType(cmd))
      }
    )
  }

  def retrievePaymentTypeByRowid(rowid: String): ServiceCall[NotUsed, PaymentTypeVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         paymentTypeDao.retrievePaymentTypeByRowid(rowid)
      }
    )
  }

  def createProduct(): ServiceCall[CreateProductCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.createProduct(cmd)
      }
    )
  }

  def retrieveProduct(): ServiceCall[RetrieveProductCmd, ProductVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.retrieveProduct(cmd)
      }
    )
  }

  def updateProduct(): ServiceCall[UpdateProductCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.updateProduct(cmd)
      }
    )
  }

  def deleteProduct(): ServiceCall[DeleteProductCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.deleteProduct(cmd)
      }
    )
  }

  def queryProduct(): ServiceCall[QueryCommand, ProductListVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         ProductListVo(productDao.queryProduct(cmd))
      }
    )
  }

  def retrieveProductByRowid(rowid: String): ServiceCall[NotUsed, ProductVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.retrieveProductByRowid(rowid)
      }
    )
  }

  def getProductSales(): ServiceCall[GetProductSalesCmd, ProductSalesVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.getProductSales(cmd)
      }
    )
  }

  def updateProductSales(): ServiceCall[UpdateProductSalesCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.updateProductSales(cmd)
      }
    )
  }

  def getProductName(): ServiceCall[GetProductNameCmd, ProductNameVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.getProductName(cmd)
      }
    )
  }

  def changeProductName(): ServiceCall[ChangeProductNameCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.changeProductName(cmd)
      }
    )
  }

  def getProductUnit(): ServiceCall[GetProductUnitCmd, ProductUnitVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.getProductUnit(cmd)
      }
    )
  }

  def changeProductUnit(): ServiceCall[ChangeProductUnitCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.changeProductUnit(cmd)
      }
    )
  }

  def getUnitPrice(): ServiceCall[GetUnitPriceCmd, UnitPriceVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.getUnitPrice(cmd)
      }
    )
  }

  def changeUnitPrice(): ServiceCall[ChangeUnitPriceCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.changeUnitPrice(cmd)
      }
    )
  }

  def createOrder(): ServiceCall[CreateOrderCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.createOrder(cmd)
      }
    )
  }

  def retrieveOrder(): ServiceCall[RetrieveOrderCmd, OrderVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.retrieveOrder(cmd)
      }
    )
  }

  def updateOrder(): ServiceCall[UpdateOrderCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.updateOrder(cmd)
      }
    )
  }

  def deleteOrder(): ServiceCall[DeleteOrderCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.deleteOrder(cmd)
      }
    )
  }

  def queryOrder(): ServiceCall[QueryCommand, OrderListVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         OrderListVo(orderDao.queryOrder(cmd))
      }
    )
  }

  def retrieveOrderByRowid(rowid: String): ServiceCall[NotUsed, OrderVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.retrieveOrderByRowid(rowid)
      }
    )
  }

  def getOrderLines(): ServiceCall[GetOrderLinesCmd, OrderLinesVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.getOrderLines(cmd)
      }
    )
  }

  def addOrderLines(): ServiceCall[AddOrderLinesCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.addOrderLines(cmd)
      }
    )
  }

  def removeOrderLines(): ServiceCall[RemoveOrderLinesCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.removeOrderLines(cmd)
      }
    )
  }

  def getOrderPaymentType(): ServiceCall[GetOrderPaymentTypeCmd, OrderPaymentTypeVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.getOrderPaymentType(cmd)
      }
    )
  }

  def changeOrderPaymentType(): ServiceCall[ChangeOrderPaymentTypeCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.changeOrderPaymentType(cmd)
      }
    )
  }

  def createOrderItem(): ServiceCall[CreateOrderItemCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderItemDao.createOrderItem(cmd)
      }
    )
  }

  def retrieveOrderItem(): ServiceCall[RetrieveOrderItemCmd, OrderItemVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderItemDao.retrieveOrderItem(cmd)
      }
    )
  }

  def updateOrderItem(): ServiceCall[UpdateOrderItemCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderItemDao.updateOrderItem(cmd)
      }
    )
  }

  def deleteOrderItem(): ServiceCall[DeleteOrderItemCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderItemDao.deleteOrderItem(cmd)
      }
    )
  }

  def queryOrderItem(): ServiceCall[QueryCommand, OrderItemListVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         OrderItemListVo(orderItemDao.queryOrderItem(cmd))
      }
    )
  }

  def retrieveOrderItemByRowid(rowid: String): ServiceCall[NotUsed, OrderItemVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderItemDao.retrieveOrderItemByRowid(rowid)
      }
    )
  }

  def selectOrderItemByOrderId(orderId: String): ServiceCall[NotUsed, OrderItemListVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         OrderItemListVo(orderItemDao.selectByOrderId(orderId))
      }
    )
  }

  def deleteOrderItemByOrderId(orderId: String): ServiceCall[NotUsed, Int] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderItemDao.deleteByOrderId(orderId)
      }
    )
  }

  def selectOrderItemByProductId(productId: String): ServiceCall[NotUsed, OrderItemListVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         OrderItemListVo(orderItemDao.selectByProductId(productId))
      }
    )
  }

  def deleteOrderItemByProductId(productId: String): ServiceCall[NotUsed, Int] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderItemDao.deleteByProductId(productId)
      }
    )
  }

  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] = {
    ServiceCall { is =>
      Future.successful(is.map(x => x))
    }
  }
}
