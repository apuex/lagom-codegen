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

class PaymentTypeDaoImpl() extends PaymentTypeDao {
  def createPaymentType(cmd: CreatePaymentTypeCmd)(implicit conn: Connection): Int = {
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
      "paymentTypeId" -> cmd.paymentTypeId,
      "paymentTypeName" -> cmd.paymentTypeName,
      "paymentTypeLabel" -> cmd.paymentTypeLabel
    ).executeUpdate()
  }

  def retrievePaymentType(cmd: RetrievePaymentTypeCmd)(implicit conn: Connection): PaymentTypeVo = {
    SQL(s"""
       |SELECT
       |    payment_type.payment_type_id,
       |    payment_type.payment_type_name,
       |    payment_type.payment_type_label
       |  FROM sales.payment_type
       |  WHERE
       |    payment_type.payment_type_id = {paymentTypeId}
     """.stripMargin.trim)
    .on(
      "paymentTypeId" -> cmd.paymentTypeId
    ).as(rowParser.single)
  }

  def updatePaymentType(cmd: UpdatePaymentTypeCmd)(implicit conn: Connection): Int = {
    SQL(s"""
       |UPDATE sales.payment_type
       |    payment_type.payment_type_id,
       |    payment_type.payment_type_name,
       |    payment_type.payment_type_label
       |  SET
       |    payment_type.payment_type_id = {paymentTypeId},
       |    payment_type.payment_type_name = {paymentTypeName},
       |    payment_type.payment_type_label = {paymentTypeLabel}
       |  WHERE
       |    payment_type.payment_type_id = {paymentTypeId}
     """.stripMargin.trim)
    .on(
      "paymentTypeId" -> cmd.paymentTypeId,
      "paymentTypeName" -> cmd.paymentTypeName,
      "paymentTypeLabel" -> cmd.paymentTypeLabel
    ).executeUpdate()
  }

  def deletePaymentType(cmd: DeletePaymentTypeCmd)(implicit conn: Connection): Int = {
    SQL(s"""
       |DELETE
       |  FROM sales.payment_type
       |  WHERE
       |    payment_type.payment_type_id = {paymentTypeId}
     """.stripMargin.trim)
    .on(
      "paymentTypeId" -> cmd.paymentTypeId
    ).executeUpdate()
  }

  def queryPaymentType(cmd: QueryCommand)(implicit conn: Connection): Seq[PaymentTypeVo] = {
    Seq()
  }

  def retrievePaymentTypeByRowid(cmd: RetrieveByRowidCmd)(implicit conn: Connection): PaymentTypeVo = {
    SQL(s"""
       |SELECT
       |    payment_type.payment_type_id,
       |    payment_type.payment_type_name,
       |    payment_type.payment_type_label
       |  FROM sales.payment_type
       |  WHERE
       |    payment_type.rowid = {rowid}
     """.stripMargin.trim)
    .on(
      "rowid" -> cmd.rowid
    ).as(rowParser.single)
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

  private def rowParser(implicit c: Connection): RowParser[PaymentTypeVo] = {
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
