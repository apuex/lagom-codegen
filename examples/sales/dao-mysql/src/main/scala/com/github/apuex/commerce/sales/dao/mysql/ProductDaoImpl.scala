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

class ProductDaoImpl() extends ProductDao {
  def createProduct(cmc: CreateProductCmd)(implicit conn: Connection): Int = ???
  def retrieveProduct(cmd: RetrieveProductCmd)(implicit conn: Connection): ProductVo = ???
  def updateProduct(cmd: UpdateProductCmd)(implicit conn: Connection): Int = ???
  def deleteProduct(cmd: DeleteProductCmd)(implicit conn: Connection): Int = ???
  def queryProduct(cmd: QueryCommand)(implicit conn: Connection): Seq[ProductVo] = ???
  def retrieveProductByRowid(cmd: RetrieveByRowidCmd)(implicit conn: Connection): Seq[ProductVo] = ???
  def getProductSales(cmd: GetProductSalesCmd)(implicit conn: Connection): ProductSalesVo = ???
  def updateProductSales(cmd: UpdateProductSalesCmd)(implicit conn: Connection): Int = ???
  def getProductName(cmd: GetProductNameCmd)(implicit conn: Connection): ProductNameVo = ???
  def changeProductName(cmd: ChangeProductNameCmd)(implicit conn: Connection): Int = ???
  def getProductUnit(cmd: GetProductUnitCmd)(implicit conn: Connection): ProductUnitVo = ???
  def changeProductUnit(cmd: ChangeProductUnitCmd)(implicit conn: Connection): Int = ???
  def getUnitPrice(cmd: GetUnitPriceCmd)(implicit conn: Connection): UnitPriceVo = ???
  def changeUnitPrice(cmd: ChangeUnitPriceCmd)(implicit conn: Connection): Int = ???


  private val sql =
    s"""
       |SELECT
       |    t.product_id,
       |    t.product_name,
       |    t.product_unit,
       |    t.unit_price,
       |    t.record_time,
       |    t.quantity_sold
       |  FROM sales.product t
     """.stripMargin.trim

  private val fieldConverter: SymbolConverter = {
    case "productId" => "product_id"
    case "productName" => "product_name"
    case "productUnit" => "product_unit"
    case "unitPrice" => "unit_price"
    case "recordTime" => "record_time"
    case "quantitySold" => "quantity_sold"
    case x: String => camelToC(x)
  }

  private val whereClause = WhereClauseWithNamedParams(fieldConverter)

  private def parseParam(fieldName: String, paramName:String, paramValue: scala.Any): NamedParameter = paramValue match {
    case x: String => parseParam(fieldName, paramName, x)
    case x: Array[String] => parseParam(fieldName, paramName, x.toSeq)
    case x: scala.Any => throw new RuntimeException(x.toString)
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: String): NamedParameter = fieldName match {
    case "productId" => paramName -> paramValue
    case "productName" => paramName -> paramValue
    case "productUnit" => paramName -> paramValue
    case "unitPrice" => paramName -> DoubleParser.parse(paramValue)
    case "recordTime" => paramName -> DateParser.parse(paramValue)
    case "quantitySold" => paramName -> DoubleParser.parse(paramValue)
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: Seq[String]): NamedParameter = fieldName match {
    case "productId" => paramName -> paramValue
    case "productName" => paramName -> paramValue
    case "productUnit" => paramName -> paramValue
    case "unitPrice" => paramName -> paramValue.map(DoubleParser.parse(_))
    case "recordTime" => paramName -> paramValue.map(DateParser.parse(_))
    case "quantitySold" => paramName -> paramValue.map(DoubleParser.parse(_))
  }

  private def rowParser(implicit c: Connection): RowParser[ProductVo] = {
    get[String]("product_id") ~ 
    get[String]("product_name") ~ 
    get[String]("product_unit") ~ 
    get[Double]("unit_price") ~ 
    get[Date]("record_time") ~ 
    get[Double]("quantity_sold") map {
      case productId ~ productName ~ productUnit ~ unitPrice ~ recordTime ~ quantitySold =>
        ProductVo(
          productId,
          productName,
          productUnit,
          unitPrice,
          Some(toScalapbTimestamp(recordTime)),
          quantitySold
        )
    }
  }

  private def namedParams(q: QueryCommand): Seq[NamedParameter] = {
    whereClause.toNamedParams(q.getPredicate, q.params)
      .map(x => parseParam(x._1, x._2, x._3))
      .asInstanceOf[Seq[NamedParameter]]
  }
}
