package com.github.apuex.commerce.sales.dao

import com.github.apuex.commerce.sales._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection

trait PaymentTypeDao {
  def createPaymentType(cmc: CreatePaymentTypeCmd)(implicit conn: Connection): Int
  def retrievePaymentType(cmd: RetrievePaymentTypeCmd)(implicit conn: Connection): PaymentTypeVo
  def updatePaymentType(cmd: UpdatePaymentTypeCmd)(implicit conn: Connection): Int
  def deletePaymentType(cmd: DeletePaymentTypeCmd)(implicit conn: Connection): Int
  def queryPaymentType(cmd: QueryCommand)(implicit conn: Connection): Seq[PaymentTypeVo]
}
