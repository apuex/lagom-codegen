package com.github.apuex.commerce.sales.dao.mysql


import java.sql.Connection
import java.util.Date

import anorm.SqlParser._
import anorm._
import play._
import anorm.ParameterValue._
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._
import com.github.apuex.springbootsolution.runtime.DateFormat.toScalapbTimestamp
import com.github.apuex.springbootsolution.runtime.Parser._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime._

class OrderDaoImpl(orderItemDao: OrderItemDao) extends OrderDao {
  def createOrder(cmc: CreateOrderCmd)(implicit conn: Connection): Int = ???
  def retrieveOrder(cmd: RetrieveOrderCmd)(implicit conn: Connection): OrderVo = ???
  def updateOrder(cmd: UpdateOrderCmd)(implicit conn: Connection): Int = ???
  def deleteOrder(cmd: DeleteOrderCmd)(implicit conn: Connection): Int = ???
  def queryOrder(cmd: QueryCommand)(implicit conn: Connection): Seq[OrderVo] = ???
  def retrieveOrderByRowid(cmd: RetrieveByRowidCmd)(implicit conn: Connection): Seq[OrderVo] = ???
  def getOrderLines(cmd: GetOrderLinesCmd)(implicit conn: Connection): OrderLinesVo = ???
  def addOrderLines(cmd: AddOrderLinesCmd)(implicit conn: Connection): Int = ???
  def removeOrderLines(cmd: RemoveOrderLinesCmd)(implicit conn: Connection): Int = ???
  def getOrderPaymentType(cmd: GetOrderPaymentTypeCmd)(implicit conn: Connection): OrderPaymentTypeVo = ???
  def changeOrderPaymentType(cmd: ChangeOrderPaymentTypeCmd)(implicit conn: Connection): Int = ???


  private val sql =
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

  private def rowParser(implicit c: Connection): RowParser[OrderVo] = {
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
