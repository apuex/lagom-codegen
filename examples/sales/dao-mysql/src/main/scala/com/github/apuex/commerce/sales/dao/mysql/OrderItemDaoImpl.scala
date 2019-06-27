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

class OrderItemDaoImpl() extends OrderItemDao {
  def createOrderItem(cmd: CreateOrderItemCmd)(implicit conn: Connection): Int = {
    SQL(s"""
      INSERT INTO sales.order_item(
        order_item.order_id,
        order_item.product_id,
        order_item.product_name,
        order_item.item_unit,
        order_item.unit_price,
        order_item.order_quantity
      ) VALUES (
        {orderId},
        {productId},
        {productName},
        {itemUnit},
        {unitPrice},
        {orderQuantity}
      )
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId,
      "productId" -> cmd.productId,
      "productName" -> cmd.productName,
      "itemUnit" -> cmd.itemUnit,
      "unitPrice" -> cmd.unitPrice,
      "orderQuantity" -> cmd.orderQuantity
    ).executeUpdate()
  }

  def retrieveOrderItem(cmd: RetrieveOrderItemCmd)(implicit conn: Connection): OrderItemVo = {
    SQL(s"""
      SELECT
        order_item.order_id,
        order_item.product_id,
        order_item.product_name,
        order_item.item_unit,
        order_item.unit_price,
        order_item.order_quantity
      FROM sales.order_item
      WHERE
        order_item.order_id = {orderId},
        order_item.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId,
      "productId" -> cmd.productId
    ).as(rowParser.single)
  }

  def updateOrderItem(cmd: UpdateOrderItemCmd)(implicit conn: Connection): Int = {
    SQL(s"""
      UPDATE sales.order_item
        order_item.order_id,
        order_item.product_id,
        order_item.product_name,
        order_item.item_unit,
        order_item.unit_price,
        order_item.order_quantity
      SET
        order_item.order_id = {orderId},
        order_item.product_id = {productId},
        order_item.product_name = {productName},
        order_item.item_unit = {itemUnit},
        order_item.unit_price = {unitPrice},
        order_item.order_quantity = {orderQuantity}
      WHERE
        order_item.order_id = {orderId},
        order_item.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId,
      "productId" -> cmd.productId,
      "productName" -> cmd.productName,
      "itemUnit" -> cmd.itemUnit,
      "unitPrice" -> cmd.unitPrice,
      "orderQuantity" -> cmd.orderQuantity
    ).executeUpdate()
  }

  def deleteOrderItem(cmd: DeleteOrderItemCmd)(implicit conn: Connection): Int = {
    SQL(s"""
      DELETE
      FROM sales.order_item
      WHERE
        order_item.order_id = {orderId},
        order_item.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "orderId" -> cmd.orderId,
      "productId" -> cmd.productId
    ).executeUpdate()
  }

  def queryOrderItem(cmd: QueryCommand)(implicit conn: Connection): Seq[OrderItemVo] = {
    Seq()
  }

  def retrieveOrderItemByRowid(cmd: RetrieveByRowidCmd)(implicit conn: Connection): OrderItemVo = {
    SQL(s"""
      SELECT
        order_item.order_id,
        order_item.product_id,
        order_item.product_name,
        order_item.item_unit,
        order_item.unit_price,
        order_item.order_quantity
      FROM sales.order_item
      WHERE
        order_item.rowid = {rowid}
     """.stripMargin.trim)
    .on(
      "rowid" -> cmd.rowid
    ).as(rowParser.single)
  }

  def selectByOrderId(orderId: String)(implicit conn: Connection): Seq[OrderItemVo] = {
    SQL(s"""
      SELECT
        order_item.order_id,
        order_item.product_id,
        order_item.product_name,
        order_item.item_unit,
        order_item.unit_price,
        order_item.order_quantity
      FROM sales.order_item
      WHERE
        order_item.order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> orderId
    ).as(rowParser.*)
  }

  def selectByProductId(productId: String)(implicit conn: Connection): Seq[OrderItemVo] = {
    SQL(s"""
      SELECT
        order_item.order_id,
        order_item.product_id,
        order_item.product_name,
        order_item.item_unit,
        order_item.unit_price,
        order_item.order_quantity
      FROM sales.order_item
      WHERE
        order_item.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> productId
    ).as(rowParser.*)
  }

  def deleteByOrderId(orderId: String)(implicit conn: Connection): Int = {
    SQL(s"""
      DELETE
      FROM sales.order_item
      WHERE
        order_item.order_id = {orderId}
     """.stripMargin.trim)
    .on(
      "orderId" -> orderId
    ).executeUpdate()
  }

  def deleteByProductId(productId: String)(implicit conn: Connection): Int = {
    SQL(s"""
      DELETE
      FROM sales.order_item
      WHERE
        order_item.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> productId
    ).executeUpdate()
  }

  private val selectOrderItemSql =
    s"""
       |SELECT
       |    t.order_id,
       |    t.product_id,
       |    t.product_name,
       |    t.item_unit,
       |    t.unit_price,
       |    t.order_quantity
       |  FROM sales.order_item t
     """.stripMargin.trim

  private val fieldConverter: SymbolConverter = {
    case "orderId" => "order_id"
    case "productId" => "product_id"
    case "productName" => "product_name"
    case "itemUnit" => "item_unit"
    case "unitPrice" => "unit_price"
    case "orderQuantity" => "order_quantity"
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
    case "productId" => paramName -> paramValue
    case "productName" => paramName -> paramValue
    case "itemUnit" => paramName -> paramValue
    case "unitPrice" => paramName -> DoubleParser.parse(paramValue)
    case "orderQuantity" => paramName -> DoubleParser.parse(paramValue)
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: Seq[String]): NamedParameter = fieldName match {
    case "orderId" => paramName -> paramValue
    case "productId" => paramName -> paramValue
    case "productName" => paramName -> paramValue
    case "itemUnit" => paramName -> paramValue
    case "unitPrice" => paramName -> paramValue.map(DoubleParser.parse(_))
    case "orderQuantity" => paramName -> paramValue.map(DoubleParser.parse(_))
  }

  private def rowParser(implicit c: Connection): RowParser[OrderItemVo] = {
    get[String]("order_id") ~ 
    get[String]("product_id") ~ 
    get[String]("product_name") ~ 
    get[String]("item_unit") ~ 
    get[Double]("unit_price") ~ 
    get[Double]("order_quantity") map {
      case orderId ~ productId ~ productName ~ itemUnit ~ unitPrice ~ orderQuantity =>
        OrderItemVo(
          orderId,
          productId,
          productName,
          itemUnit,
          unitPrice,
          orderQuantity
        )
    }
  }

  private def namedParams(q: QueryCommand): Seq[NamedParameter] = {
    whereClause.toNamedParams(q.getPredicate, q.params)
      .map(x => parseParam(x._1, x._2, x._3))
      .asInstanceOf[Seq[NamedParameter]]
  }
}
