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

class ProductDaoImpl() extends ProductDao {
  def createProduct(cmd: CreateProductCmd)(implicit conn: Connection): Int = {
    val rowsAffected = SQL(s"""
       |UPDATE sales.product
       |    product.product_id,
       |    product.product_name,
       |    product.product_unit,
       |    product.unit_price,
       |    product.record_time,
       |    product.quantity_sold
       |  SET
       |    product.product_id = {productId},
       |    product.product_name = {productName},
       |    product.product_unit = {productUnit},
       |    product.unit_price = {unitPrice},
       |    product.record_time = {recordTime},
       |    product.quantity_sold = {quantitySold}
       |  WHERE
       |    product.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId,
      "productName" -> cmd.productName,
      "productUnit" -> cmd.productUnit,
      "unitPrice" -> cmd.unitPrice,
      "recordTime" -> scalapbToDate(cmd.recordTime),
      "quantitySold" -> cmd.quantitySold
    ).executeUpdate()
  
    if(rowsAffected == 0)
      SQL(s"""
         |INSERT INTO sales.product(
         |    product.product_id,
         |    product.product_name,
         |    product.product_unit,
         |    product.unit_price,
         |    product.record_time,
         |    product.quantity_sold
         |  ) VALUES (
         |    {productId},
         |    {productName},
         |    {productUnit},
         |    {unitPrice},
         |    {recordTime},
         |    {quantitySold}
         |  )
       """.stripMargin.trim)
      .on(
        "productId" -> cmd.productId,
        "productName" -> cmd.productName,
        "productUnit" -> cmd.productUnit,
        "unitPrice" -> cmd.unitPrice,
        "recordTime" -> scalapbToDate(cmd.recordTime),
        "quantitySold" -> cmd.quantitySold
      ).executeUpdate()
    else rowsAffected
  }

  def retrieveProduct(cmd: RetrieveProductCmd)(implicit conn: Connection): ProductVo = {
    SQL(s"""
       |SELECT
       |    product.product_id,
       |    product.product_name,
       |    product.product_unit,
       |    product.unit_price,
       |    product.record_time,
       |    product.quantity_sold
       |  FROM sales.product
       |  WHERE
       |    product.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId
    ).as(productParser.single)
  }

  def updateProduct(cmd: UpdateProductCmd)(implicit conn: Connection): Int = {
    SQL(s"""
       |UPDATE sales.product
       |    product.product_id,
       |    product.product_name,
       |    product.product_unit,
       |    product.unit_price,
       |    product.record_time,
       |    product.quantity_sold
       |  SET
       |    product.product_id = {productId},
       |    product.product_name = {productName},
       |    product.product_unit = {productUnit},
       |    product.unit_price = {unitPrice},
       |    product.record_time = {recordTime},
       |    product.quantity_sold = {quantitySold}
       |  WHERE
       |    product.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId,
      "productName" -> cmd.productName,
      "productUnit" -> cmd.productUnit,
      "unitPrice" -> cmd.unitPrice,
      "recordTime" -> scalapbToDate(cmd.recordTime),
      "quantitySold" -> cmd.quantitySold
    ).executeUpdate()
  }

  def deleteProduct(cmd: DeleteProductCmd)(implicit conn: Connection): Int = {
    SQL(s"""
       |DELETE
       |  FROM sales.product
       |  WHERE
       |    product.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId
    ).executeUpdate()
  }

  def queryProduct(cmd: QueryCommand)(implicit conn: Connection): Seq[ProductVo] = {
    Seq()
  }

  def retrieveProductByRowid(cmd: RetrieveByRowidCmd)(implicit conn: Connection): ProductVo = {
    SQL(s"""
       |SELECT
       |    product.product_id,
       |    product.product_name,
       |    product.product_unit,
       |    product.unit_price,
       |    product.record_time,
       |    product.quantity_sold
       |  FROM sales.product
       |  WHERE
       |    product.rowid = {rowid}
     """.stripMargin.trim)
    .on(
      "rowid" -> cmd.rowid
    ).as(productParser.single)
  }

  private def productSalesParser(implicit c: Connection): RowParser[ProductSalesVo] = {
    get[String]("product_id") ~ 
    get[Date]("record_time") ~ 
    get[Double]("quantity_sold") map {
      case productId ~ recordTime ~ quantitySold =>
        ProductSalesVo(
          productId,
          Some(toScalapbTimestamp(recordTime)),
          quantitySold
        )
    }
  }
  
  def getProductSales(cmd: GetProductSalesCmd)(implicit conn: Connection): ProductSalesVo = {
    SQL(s"""
       |SELECT
       |    product_sales.product_id,
       |    product_sales.record_time,
       |    product_sales.quantity_sold
       |  FROM sales.product_sales
       |  WHERE
       |    product_sales.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId
    ).as(productSalesParser.single)
  }
  
  def updateProductSales(cmd: UpdateProductSalesCmd)(implicit conn: Connection): Int = {
    SQL(s"""
       |UPDATE sales.product_sales
       |    product_sales.product_id,
       |    product_sales.record_time,
       |    product_sales.quantity_sold
       |  SET
       |    product_sales.product_id = {productId},
       |    product_sales.record_time = {recordTime},
       |    product_sales.quantity_sold = {quantitySold}
       |  WHERE
       |    product_sales.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId,
      "recordTime" -> scalapbToDate(cmd.recordTime),
      "quantitySold" -> cmd.quantitySold
    ).executeUpdate()
  }

  private def productNameParser(implicit c: Connection): RowParser[ProductNameVo] = {
    get[String]("product_id") ~ 
    get[String]("product_name") map {
      case productId ~ productName =>
        ProductNameVo(
          productId,
          productName
        )
    }
  }
  
  def getProductName(cmd: GetProductNameCmd)(implicit conn: Connection): ProductNameVo = {
    SQL(s"""
       |SELECT
       |    product_name.product_id,
       |    product_name.product_name
       |  FROM sales.product_name
       |  WHERE
       |    product_name.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId
    ).as(productNameParser.single)
  }
  
  def changeProductName(cmd: ChangeProductNameCmd)(implicit conn: Connection): Int = {
    SQL(s"""
       |UPDATE sales.product_name
       |    product_name.product_id,
       |    product_name.product_name
       |  SET
       |    product_name.product_id = {productId},
       |    product_name.product_name = {productName}
       |  WHERE
       |    product_name.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId,
      "productName" -> cmd.productName
    ).executeUpdate()
  }

  private def productUnitParser(implicit c: Connection): RowParser[ProductUnitVo] = {
    get[String]("product_id") ~ 
    get[String]("product_unit") map {
      case productId ~ productUnit =>
        ProductUnitVo(
          productId,
          productUnit
        )
    }
  }
  
  def getProductUnit(cmd: GetProductUnitCmd)(implicit conn: Connection): ProductUnitVo = {
    SQL(s"""
       |SELECT
       |    product_unit.product_id,
       |    product_unit.product_unit
       |  FROM sales.product_unit
       |  WHERE
       |    product_unit.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId
    ).as(productUnitParser.single)
  }
  
  def changeProductUnit(cmd: ChangeProductUnitCmd)(implicit conn: Connection): Int = {
    SQL(s"""
       |UPDATE sales.product_unit
       |    product_unit.product_id,
       |    product_unit.product_unit
       |  SET
       |    product_unit.product_id = {productId},
       |    product_unit.product_unit = {productUnit}
       |  WHERE
       |    product_unit.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId,
      "productUnit" -> cmd.productUnit
    ).executeUpdate()
  }

  private def unitPriceParser(implicit c: Connection): RowParser[UnitPriceVo] = {
    get[String]("product_id") ~ 
    get[Double]("unit_price") map {
      case productId ~ unitPrice =>
        UnitPriceVo(
          productId,
          unitPrice
        )
    }
  }
  
  def getUnitPrice(cmd: GetUnitPriceCmd)(implicit conn: Connection): UnitPriceVo = {
    SQL(s"""
       |SELECT
       |    unit_price.product_id,
       |    unit_price.unit_price
       |  FROM sales.unit_price
       |  WHERE
       |    unit_price.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId
    ).as(unitPriceParser.single)
  }
  
  def changeUnitPrice(cmd: ChangeUnitPriceCmd)(implicit conn: Connection): Int = {
    SQL(s"""
       |UPDATE sales.unit_price
       |    unit_price.product_id,
       |    unit_price.unit_price
       |  SET
       |    unit_price.product_id = {productId},
       |    unit_price.unit_price = {unitPrice}
       |  WHERE
       |    unit_price.product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId,
      "unitPrice" -> cmd.unitPrice
    ).executeUpdate()
  }

  private val selectProductSql =
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

  private def productParser(implicit c: Connection): RowParser[ProductVo] = {
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
