/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.dao.mysql

import java.io.InputStream
import java.sql.Connection
import java.util.Date

import anorm.ParameterValue._
import anorm.SqlParser._
import anorm._
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

class PaymentTypeDaoImpl() extends PaymentTypeDao {
  def createPaymentType(evt: CreatePaymentTypeEvent)(implicit conn: Connection): Int = {
    val rowsAffected = SQL(s"""
      |UPDATE sales.payment_type
      |  SET
      |    payment_type.payment_type_name = {paymentTypeName},
      |    payment_type.payment_type_label = {paymentTypeLabel}
      |  WHERE payment_type_id = {paymentTypeId}
     """.stripMargin.trim)
    .on(
      "paymentTypeId" -> evt.paymentTypeId,
      "paymentTypeName" -> evt.paymentTypeName,
      "paymentTypeLabel" -> evt.paymentTypeLabel
    ).executeUpdate()
  
    if(rowsAffected == 0)
      SQL(s"""
        |INSERT INTO sales.payment_type(
        |    payment_type.payment_type_id,
        |    payment_type.payment_type_name,
        |    payment_type.payment_type_label
        |  ) VALUES (
        |    {paymentTypeId},
        |    {paymentTypeName},
        |    {paymentTypeLabel}
        |  )
       """.stripMargin.trim)
      .on(
        "paymentTypeId" -> evt.paymentTypeId,
        "paymentTypeName" -> evt.paymentTypeName,
        "paymentTypeLabel" -> evt.paymentTypeLabel
      ).executeUpdate()
    else rowsAffected
  }

  def retrievePaymentType(cmd: RetrievePaymentTypeCmd)(implicit conn: Connection): PaymentTypeVo = {
    SQL(s"""
      |SELECT
      |    payment_type.payment_type_id,
      |    payment_type.payment_type_name,
      |    payment_type.payment_type_label
      |  FROM sales.payment_type
      |  WHERE payment_type_id = {paymentTypeId}
     """.stripMargin.trim)
    .on(
      "paymentTypeId" -> cmd.paymentTypeId
    ).as(paymentTypeParser.single)
  }

  def updatePaymentType(evt: UpdatePaymentTypeEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |UPDATE sales.payment_type
      |  SET
      |    payment_type.payment_type_name = {paymentTypeName},
      |    payment_type.payment_type_label = {paymentTypeLabel}
      |  WHERE payment_type_id = {paymentTypeId}
     """.stripMargin.trim)
    .on(
      "paymentTypeId" -> evt.paymentTypeId,
      "paymentTypeName" -> evt.paymentTypeName,
      "paymentTypeLabel" -> evt.paymentTypeLabel
    ).executeUpdate()
  }

  def deletePaymentType(evt: DeletePaymentTypeEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |DELETE
      |  FROM sales.payment_type
      |  WHERE payment_type_id = {paymentTypeId}
     """.stripMargin.trim)
    .on(
      "paymentTypeId" -> evt.paymentTypeId
    ).executeUpdate()
  }

  def queryPaymentType(cmd: QueryCommand)(implicit conn: Connection): Seq[PaymentTypeVo] = {
    val sqlStr = s"""
      |${selectPaymentTypeSql}
      |  ${whereClause.toWhereClause(cmd, 4)}
     """.stripMargin.trim
    val stmt = SQL(sqlStr)
    val params = namedParams(cmd)
  
    Logger.of(getClass).info(
      s"""
      |[SQL statement] =>
      |  ${indent(sqlStr, 2)}
      |[params for substitution] =>
      |  {}
     """.stripMargin.trim,
      params
    )
  
    if (params.isEmpty) {
      stmt.as(paymentTypeParser.*)
    } else {
      stmt.on(
        params: _*
      ).as(paymentTypeParser.*)
    }
  }

  def retrievePaymentTypeByRowid(rowid: String)(implicit conn: Connection): PaymentTypeVo = {
    SQL(s"""
      |SELECT
      |    payment_type.payment_type_id,
      |    payment_type.payment_type_name,
      |    payment_type.payment_type_label
      |  FROM sales.payment_type
      |  WHERE rowid = {rowid}
     """.stripMargin.trim)
    .on(
      "rowid" -> rowid
    ).as(paymentTypeParser.single)
  }

  private val selectPaymentTypeSql =
    s"""
      |SELECT
      |    t.payment_type_id,
      |    t.payment_type_name,
      |    t.payment_type_label
      |  FROM sales.payment_type t
     """.stripMargin.trim

  private val fieldConverter: SymbolConverter = {
    case "paymentTypeId" => "payment_type_id"
    case "paymentTypeName" => "payment_type_name"
    case "paymentTypeLabel" => "payment_type_label"
    case x: String => camelToC(x)
  }

  private val whereClause = WhereClauseWithNamedParams(fieldConverter)

  private def parseParam(fieldName: String, paramName:String, paramValue: scala.Any): NamedParameter = paramValue match {
    case x: String => parseParam(fieldName, paramName, x)
    case x: Array[String] => parseParam(fieldName, paramName, x.toSeq)
    case x: scala.Any => throw new RuntimeException(x.toString)
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: String): NamedParameter = fieldName match {
    case "paymentTypeId" => paramName -> IntParser.parse(paramValue)
    case "paymentTypeName" => paramName -> paramValue
    case "paymentTypeLabel" => paramName -> paramValue
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: Seq[String]): NamedParameter = fieldName match {
    case "paymentTypeId" => paramName -> paramValue.map(IntParser.parse(_))
    case "paymentTypeName" => paramName -> paramValue
    case "paymentTypeLabel" => paramName -> paramValue
  }

  private def paymentTypeParser(implicit c: Connection): RowParser[PaymentTypeVo] = {
    get[Int]("payment_type_id") ~ 
    get[String]("payment_type_name") ~ 
    get[String]("payment_type_label") map {
      case paymentTypeId ~ paymentTypeName ~ paymentTypeLabel =>
        PaymentTypeVo(
          paymentTypeId,
          paymentTypeName,
          paymentTypeLabel
        )
    }
  }

  private def namedParams(q: QueryCommand): Seq[NamedParameter] = {
    whereClause.toNamedParams(q.getPredicate, q.params)
      .map(x => parseParam(x._1, x._2, x._3))
      .asInstanceOf[Seq[NamedParameter]]
  }
}
