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

class ProductDaoImpl() extends ProductDao {
  val log = Logger.of(getClass)

  def createProduct(evt: CreateProductEvent)(implicit conn: Connection): Int = {
    val rowsAffected0 = SQL(s"""
      |UPDATE sales.product
      |  SET
      |    product.product_name = {productName},
      |    product.product_unit = {productUnit},
      |    product.unit_price = {unitPrice},
      |    product.product_desc = {productDesc}
      |  WHERE product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> evt.productId,
      "productName" -> evt.productName,
      "productUnit" -> evt.productUnit,
      "unitPrice" -> evt.unitPrice,
      "productDesc" -> evt.productDesc
    ).executeUpdate()
  
    val rowsAffected = if(rowsAffected0 == 0) {
      val rowsAffected1 = SQL(s"""
        |INSERT INTO sales.product(
        |    product.product_id,
        |    product.product_name,
        |    product.product_unit,
        |    product.unit_price,
        |    product.product_desc
        |  ) VALUES (
        |    {productId},
        |    {productName},
        |    {productUnit},
        |    {unitPrice},
        |    {productDesc}
        |  )
       """.stripMargin.trim)
      .on(
        "productId" -> evt.productId,
        "productName" -> evt.productName,
        "productUnit" -> evt.productUnit,
        "unitPrice" -> evt.unitPrice,
        "productDesc" -> evt.productDesc
      ).executeUpdate()
      
      rowsAffected1
    } else rowsAffected0
  
    rowsAffected
  }

  def retrieveProduct(cmd: RetrieveProductCmd)(implicit conn: Connection): ProductVo = {
    SQL(s"""
      |SELECT
      |    product.product_id,
      |    product.product_name,
      |    product.product_unit,
      |    product.unit_price,
      |    product.product_desc
      |  FROM sales.product
      |  WHERE product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId
    ).as(productParser.single)
  }

  def updateProduct(evt: UpdateProductEvent)(implicit conn: Connection): Int = {
    val rowsAffected = SQL(s"""
      |UPDATE sales.product
      |  SET
      |    product.product_name = {productName},
      |    product.product_unit = {productUnit},
      |    product.unit_price = {unitPrice},
      |    product.product_desc = {productDesc}
      |  WHERE product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> evt.productId,
      "productName" -> evt.productName,
      "productUnit" -> evt.productUnit,
      "unitPrice" -> evt.unitPrice,
      "productDesc" -> evt.productDesc
    ).executeUpdate()
    
    rowsAffected
  }

  def deleteProduct(evt: DeleteProductEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |DELETE
      |  FROM sales.product
      |  WHERE product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> evt.productId
    ).executeUpdate()
  }

  def queryProduct(cmd: QueryCommand)(implicit conn: Connection): Seq[ProductVo] = {
    val sqlStr = s"""
      |${selectProductSql}
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
      stmt.as(productParser.*)
    } else {
      stmt.on(
        params: _*
      ).as(productParser.*)
    }
  }

  def retrieveProductByRowid(rowid: String)(implicit conn: Connection): ProductVo = {
    SQL(s"""
      |SELECT
      |    product.product_id,
      |    product.product_name,
      |    product.product_unit,
      |    product.unit_price,
      |    product.product_desc
      |  FROM sales.product
      |  WHERE rowid = {rowid}
     """.stripMargin.trim)
    .on(
      "rowid" -> rowid
    ).as(productParser.single)
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
      |    product.product_id,
      |    product.product_name
      |  FROM sales.product
      |  WHERE product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId
    ).as(productNameParser.single)
  }
  
  def changeProductName(evt: ChangeProductNameEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |UPDATE sales.product
      |  SET
      |    product.product_name = {productName}
      |  WHERE product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> evt.productId,
      "productName" -> evt.productName
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
      |    product.product_id,
      |    product.product_unit
      |  FROM sales.product
      |  WHERE product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId
    ).as(productUnitParser.single)
  }
  
  def changeProductUnit(evt: ChangeProductUnitEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |UPDATE sales.product
      |  SET
      |    product.product_unit = {productUnit}
      |  WHERE product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> evt.productId,
      "productUnit" -> evt.productUnit
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
      |    product.product_id,
      |    product.unit_price
      |  FROM sales.product
      |  WHERE product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId
    ).as(unitPriceParser.single)
  }
  
  def changeUnitPrice(evt: ChangeUnitPriceEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |UPDATE sales.product
      |  SET
      |    product.unit_price = {unitPrice}
      |  WHERE product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> evt.productId,
      "unitPrice" -> evt.unitPrice
    ).executeUpdate()
  }

  private def productDescParser(implicit c: Connection): RowParser[ProductDescVo] = {
    get[String]("product_id") ~ 
    get[String]("product_desc") map {
      case productId ~ productDesc =>
        ProductDescVo(
          productId,
          productDesc
        )
    }
  }
  
  def getProductDesc(cmd: GetProductDescCmd)(implicit conn: Connection): ProductDescVo = {
    SQL(s"""
      |SELECT
      |    product.product_id,
      |    product.product_desc
      |  FROM sales.product
      |  WHERE product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> cmd.productId
    ).as(productDescParser.single)
  }
  
  def changeProductDesc(evt: ChangeProductDescEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |UPDATE sales.product
      |  SET
      |    product.product_desc = {productDesc}
      |  WHERE product_id = {productId}
     """.stripMargin.trim)
    .on(
      "productId" -> evt.productId,
      "productDesc" -> evt.productDesc
    ).executeUpdate()
  }

  private val selectProductSql =
    s"""
      |SELECT
      |    t.product_id,
      |    t.product_name,
      |    t.product_unit,
      |    t.unit_price,
      |    t.product_desc
      |  FROM sales.product t
     """.stripMargin.trim

  private val fieldConverter: SymbolConverter = {
    case "productId" => "product_id"
    case "productName" => "product_name"
    case "productUnit" => "product_unit"
    case "unitPrice" => "unit_price"
    case "productDesc" => "product_desc"
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
    case "productDesc" => paramName -> paramValue
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: Seq[String]): NamedParameter = fieldName match {
    case "productId" => paramName -> paramValue
    case "productName" => paramName -> paramValue
    case "productUnit" => paramName -> paramValue
    case "unitPrice" => paramName -> paramValue.map(DoubleParser.parse(_))
    case "productDesc" => paramName -> paramValue
  }

  private def productParser(implicit c: Connection): RowParser[ProductVo] = {
    get[String]("product_id") ~ 
    get[String]("product_name") ~ 
    get[String]("product_unit") ~ 
    get[Double]("unit_price") ~ 
    get[String]("product_desc") map {
      case productId ~ productName ~ productUnit ~ unitPrice ~ productDesc =>
        ProductVo(
          productId,
          productName,
          productUnit,
          unitPrice,
          None,
          0,
          productDesc
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
