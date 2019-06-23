package com.github.apuex.commerce.sales


trait PaymentTypeEvent extends Event {
  def paymentTypeId: Int
}
