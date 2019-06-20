package com.github.apuex.commerce.sales


trait OrderEvent extends Event {
  def orderId: String
}
