/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales


trait PaymentTypeCommand extends Command {
  def paymentTypeId: Int
  override def entityId: String = {
    s"paymentType_${paymentTypeId}"
  }
}
