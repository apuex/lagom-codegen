/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales


trait PaymentTypeEvent extends Event {
  def paymentTypeId: Int
  override def entityId: String = {
    s"paymentType_${paymentTypeId}"
  }
}
