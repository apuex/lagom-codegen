/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.dao.mysql

import java.io.InputStream
import java.sql.Connection
import java.util.{Date, UUID}

import anorm.ParameterValue._
import anorm.SqlParser._
import anorm._
import com.datastax.driver.core.utils.UUIDs
import play._
import com.github.apuex.springbootsolution.runtime.DateFormat.{scalapbToDate, toScalapbTimestamp}
import com.github.apuex.springbootsolution.runtime.EnumConvert._
import com.github.apuex.springbootsolution.runtime.Parser._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.ByteString
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._

class OrderDaoImpl(orderItemDao: OrderItemDao) extends OrderDao {
  val log = Logger.of(getClass)

  def createOrder(evt: CreateOrderEvent)(implicit conn: Connection): Int = {
    val rowsAffected = SQL(s"""
      |UPDATE sales.order
      |  SET
      |    order.order_time = {orderTime},
      |    order.order_payment_type = {orderPaymentType}
      |  WHERE order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> evt.orderId,
      "orderTime" -> scalapbToDate(evt.orderTime),
      "orderPaymentType" -> toValue(evt.orderPaymentType)
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
        "orderId" -> evt.orderId,
        "orderTime" -> scalapbToDate(evt.orderTime),
        "orderPaymentType" -> toValue(evt.orderPaymentType)
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
      |  WHERE order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId
    ).as(orderParser.single)
  }

  def updateOrder(evt: UpdateOrderEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |UPDATE sales.order
      |  SET
      |    order.order_time = {orderTime},
      |    order.order_payment_type = {orderPaymentType}
      |  WHERE order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> evt.orderId,
      "orderTime" -> scalapbToDate(evt.orderTime),
      "orderPaymentType" -> toValue(evt.orderPaymentType)
    ).executeUpdate()
  }

  def deleteOrder(evt: DeleteOrderEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |DELETE
      |  FROM sales.order
      |  WHERE order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> evt.orderId
    ).executeUpdate()
  }

  def queryOrder(cmd: QueryCommand)(implicit conn: Connection): Seq[OrderVo] = {
    val sqlStr = s"""
      |${selectOrderSql}
      |  ${whereClause.toWhereClause(cmd, 4)}
      |  ${indent(if(!cmd.orderBy.isEmpty) "ORDER BY " + whereClause.orderBy(cmd.orderBy, "t") else "", 4)}
     """.stripMargin.trim
    val stmt = SQL(sqlStr)
    val params = namedParams(cmd)
  
    if(log.isDebugEnabled) log.debug(
      s"""
      |[SQL statement] =>
      |  ${indent(sqlStr, 2)}
      |  [params for substitution] =>
      |    {}
     """.stripMargin.trim,
      params
    )
  
    if (params.isEmpty) {
      stmt.as(orderParser.*)
    } else {
      stmt.on(
        params: _*
      ).as(orderParser.*)
    }
  }

  def retrieveOrderByRowid(rowid: String)(implicit conn: Connection): OrderVo = {
    SQL(s"""
      |SELECT
      |    order.order_id,
      |    order.order_time,
      |    order.order_payment_type
      |  FROM sales.order
      |  WHERE rowid = {rowid}
     """.stripMargin.trim)
    .on(
      "rowid" -> rowid
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
    OrderLinesVo(cmd.orderId, orderItemDao.selectByOrderId(cmd.orderId))
  }
  
  def addOrderLines(evt: AddOrderLinesEvent)(implicit conn: Connection): Int = {
    evt.orderLines
      .map(x => CreateOrderItemEvent(
          evt.userId, evt.orderId, x.productId, x.productName, x.itemUnit, x.unitPrice, x.orderQuantity
        )
       )
      .map(orderItemDao.createOrderItem(_))
      .foldLeft(0)((t, u) => t + u)
  }
  
  def removeOrderLines(evt: RemoveOrderLinesEvent)(implicit conn: Connection): Int = {
    evt.orderLines
      .map(x => DeleteOrderItemEvent(
          evt.userId, evt.orderId, x.productId
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
      |    order.order_id,
      |    order.order_payment_type
      |  FROM sales.order
      |  WHERE order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId
    ).as(orderPaymentTypeParser.single)
  }
  
  def changeOrderPaymentType(evt: ChangeOrderPaymentTypeEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |UPDATE sales.order
      |  SET
      |    order.order_payment_type = {orderPaymentType}
      |  WHERE order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> evt.orderId,
      "orderPaymentType" -> toValue(evt.orderPaymentType)
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
    q.predicate.map(p => whereClause.toNamedParams(p, q.params)
      .map(x => parseParam(x._1, x._2, x._3))
      .asInstanceOf[Seq[NamedParameter]])
      .getOrElse(Seq())
  }
}
