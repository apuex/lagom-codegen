/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.dao

import com.github.apuex.commerce.sales._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection

trait PaymentTypeDao {
  def createPaymentType(evt: CreatePaymentTypeEvent)(implicit conn: Connection): Int

  def retrievePaymentType(cmd: RetrievePaymentTypeCmd)(implicit conn: Connection): PaymentTypeVo

  def updatePaymentType(evt: UpdatePaymentTypeEvent)(implicit conn: Connection): Int

  def deletePaymentType(evt: DeletePaymentTypeEvent)(implicit conn: Connection): Int

  def queryPaymentType(cmd: QueryCommand)(implicit conn: Connection): Seq[PaymentTypeVo]

  def retrievePaymentTypeByRowid(rowid: String)(implicit conn: Connection): PaymentTypeVo
}
