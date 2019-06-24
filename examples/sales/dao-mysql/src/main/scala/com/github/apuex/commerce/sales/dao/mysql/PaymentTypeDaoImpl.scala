package com.github.apuex.commerce.sales.dao.mysql

import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection
import play._
import anorm.SqlParser._
import anorm.ParameterValue._
import anorm._

class PaymentTypeDaoImpl extends PaymentTypeDao {
  def createPaymentType(cmc: CreatePaymentTypeCmd)(implicit conn: Connection): Int = ???
  def retrievePaymentType(cmd: RetrievePaymentTypeCmd)(implicit conn: Connection): PaymentTypeVo = ???
  def updatePaymentType(cmd: UpdatePaymentTypeCmd)(implicit conn: Connection): Int = ???
  def deletePaymentType(cmd: DeletePaymentTypeCmd)(implicit conn: Connection): Int = ???
  def queryPaymentType(cmd: QueryCommand)(implicit conn: Connection): Seq[PaymentTypeVo] = ???
  def retrievePaymentTypeByRowid(cmd: RetrieveByRowidCmd)(implicit conn: Connection): Seq[PaymentTypeVo] = ???
}
