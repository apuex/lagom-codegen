/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.dao.mysql

import java.sql.Connection
import java.util.Date

import anorm.SqlParser._
import anorm._
import play._
import anorm.ParameterValue._
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._
import com.github.apuex.springbootsolution.runtime.DateFormat.{toScalapbTimestamp, scalapbToDate}
import com.github.apuex.springbootsolution.runtime.EnumConvert._
import com.github.apuex.springbootsolution.runtime.Parser._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime._

class OrderDaoImpl(orderItemDao: OrderItemDao) extends OrderDao {
  def createOrder(cmd: CreateOrderCmd)(implicit conn: Connection): Int = {
    val rowsAffected = SQL(s"""
       |UPDATE sales.order
       |    order.order_id,
       |    order.order_time,
       |    order.order_payment_type
       |  SET
       |    order.order_id = {orderId},
       |    order.order_time = {orderTime},
       |    order.order_payment_type = {orderPaymentType}
       |  WHERE
       |    order.order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId,
      "orderTime" -> scalapbToDate(cmd.orderTime),
      "orderPaymentType" -> toValue(cmd.orderPaymentType)
    ).executeUpdate()
  
    if(rowsAffected == 0)
      SQL(s"""
         |INSERT INTO sales.order(
         |    order.order_id,
         |    order.order_time,
         |    order.order_payment_type
         |  ) VALUES (
         |    {orderId},
         |    {orderTime},
         |    {orderPaymentType}
         |  )
       """.stripMargin.trim)
      .on(
        "orderId" -> cmd.orderId,
        "orderTime" -> scalapbToDate(cmd.orderTime),
        "orderPaymentType" -> toValue(cmd.orderPaymentType)
      ).executeUpdate()
    else rowsAffected
  }

  def retrieveOrder(cmd: RetrieveOrderCmd)(implicit conn: Connection): OrderVo = {
    SQL(s"""
       |SELECT
       |    order.order_id,
       |    order.order_time,
       |    order.order_payment_type
       |  FROM sales.order
       |  WHERE
       |    order.order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId
    ).as(orderParser.single)
  }

  def updateOrder(cmd: UpdateOrderCmd)(implicit conn: Connection): Int = {
    SQL(s"""
       |UPDATE sales.order
       |    order.order_id,
       |    order.order_time,
       |    order.order_payment_type
       |  SET
       |    order.order_id = {orderId},
       |    order.order_time = {orderTime},
       |    order.order_payment_type = {orderPaymentType}
       |  WHERE
       |    order.order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId,
      "orderTime" -> scalapbToDate(cmd.orderTime),
      "orderPaymentType" -> toValue(cmd.orderPaymentType)
    ).executeUpdate()
  }

  def deleteOrder(cmd: DeleteOrderCmd)(implicit conn: Connection): Int = {
    SQL(s"""
       |DELETE
       |  FROM sales.order
       |  WHERE
       |    order.order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId
    ).executeUpdate()
  }

  def queryOrder(cmd: QueryCommand)(implicit conn: Connection): Seq[OrderVo] = {
    Seq()
  }

  def retrieveOrderByRowid(cmd: RetrieveByRowidCmd)(implicit conn: Connection): OrderVo = {
    SQL(s"""
       |SELECT
       |    order.order_id,
       |    order.order_time,
       |    order.order_payment_type
       |  FROM sales.order
       |  WHERE
       |    order.rowid = {rowid}
     """.stripMargin.trim)
    .on(
      "rowid" -> cmd.rowid
    ).as(orderParser.single)
  }

  private def orderLinesParser(implicit c: Connection): RowParser[OrderLinesVo] = {
    get[String]("order_id") map {
      case orderId =>
        OrderLinesVo(
          orderId,
          orderItemDao.selectByOrderId(orderId)
        )
    }
  }
  
  def getOrderLines(cmd: GetOrderLinesCmd)(implicit conn: Connection): OrderLinesVo = {
    SQL(s"""
       |SELECT
       |    order_lines.order_id
       |  FROM sales.order_lines
       |  WHERE
       |    order_lines.order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId
    ).as(orderLinesParser.single)
  }
  
  def addOrderLines(cmd: AddOrderLinesCmd)(implicit conn: Connection): Int = {
    cmd.orderLines
      .map(x => CreateOrderItemCmd(
          cmd.userId, cmd.orderId, x.productId, x.productName, x.itemUnit, x.unitPrice, x.orderQuantity
        )
       )
      .map(orderItemDao.createOrderItem(_))
      .foldLeft(0)((t, u) => t + u)
  }
  
  def removeOrderLines(cmd: RemoveOrderLinesCmd)(implicit conn: Connection): Int = {
    cmd.orderLines
      .map(x => DeleteOrderItemCmd(
          cmd.userId, cmd.orderId, x.productId
        )
       )
      .map(orderItemDao.deleteOrderItem(_))
      .foldLeft(0)((t, u) => t + u)
  }

  private def orderPaymentTypeParser(implicit c: Connection): RowParser[OrderPaymentTypeVo] = {
    get[String]("order_id") ~ 
    get[Int]("order_payment_type") map {
      case orderId ~ orderPaymentType =>
        OrderPaymentTypeVo(
          orderId,
          PaymentType.fromValue(orderPaymentType)
        )
    }
  }
  
  def getOrderPaymentType(cmd: GetOrderPaymentTypeCmd)(implicit conn: Connection): OrderPaymentTypeVo = {
    SQL(s"""
       |SELECT
       |    order_payment_type.order_id,
       |    order_payment_type.order_payment_type
       |  FROM sales.order_payment_type
       |  WHERE
       |    order_payment_type.order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId
    ).as(orderPaymentTypeParser.single)
  }
  
  def changeOrderPaymentType(cmd: ChangeOrderPaymentTypeCmd)(implicit conn: Connection): Int = {
    SQL(s"""
       |UPDATE sales.order_payment_type
       |    order_payment_type.order_id,
       |    order_payment_type.order_payment_type
       |  SET
       |    order_payment_type.order_id = {orderId},
       |    order_payment_type.order_payment_type = {orderPaymentType}
       |  WHERE
       |    order_payment_type.order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId,
      "orderPaymentType" -> toValue(cmd.orderPaymentType)
    ).executeUpdate()
  }

  private val selectOrderSql =
    s"""
       |SELECT
       |    t.order_id,
       |    t.order_time,
       |    t.order_payment_type
       |  FROM sales.order t
     """.stripMargin.trim

  private val fieldConverter: SymbolConverter = {
    case "orderId" => "order_id"
    case "orderTime" => "order_time"
    case "orderPaymentType" => "order_payment_type"
    case x: String => camelToC(x)
  }

  private val whereClause = WhereClauseWithNamedParams(fieldConverter)

  private def parseParam(fieldName: String, paramName:String, paramValue: scala.Any): NamedParameter = paramValue match {
    case x: String => parseParam(fieldName, paramName, x)
    case x: Array[String] => parseParam(fieldName, paramName, x.toSeq)
    case x: scala.Any => throw new RuntimeException(x.toString)
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: String): NamedParameter = fieldName match {
    case "orderId" => paramName -> paramValue
    case "orderTime" => paramName -> DateParser.parse(paramValue)
    case "orderPaymentType" => paramName -> EnumParser(PaymentType).parse(paramValue).value
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: Seq[String]): NamedParameter = fieldName match {
    case "orderId" => paramName -> paramValue
    case "orderTime" => paramName -> paramValue.map(DateParser.parse(_))
    case "orderPaymentType" => paramName -> paramValue.map(EnumParser(PaymentType).parse(_).value)
  }

  private def orderParser(implicit c: Connection): RowParser[OrderVo] = {
    get[String]("order_id") ~ 
    get[Date]("order_time") ~ 
    get[Int]("order_payment_type") map {
      case orderId ~ orderTime ~ orderPaymentType =>
        OrderVo(
          orderId,
          Some(toScalapbTimestamp(orderTime)),
          orderItemDao.selectByOrderId(orderId),
          PaymentType.fromValue(orderPaymentType)
        )
    }
  }

  private def namedParams(q: QueryCommand): Seq[NamedParameter] = {
    whereClause.toNamedParams(q.getPredicate, q.params)
      .map(x => parseParam(x._1, x._2, x._3))
      .asInstanceOf[Seq[NamedParameter]]
  }
}
