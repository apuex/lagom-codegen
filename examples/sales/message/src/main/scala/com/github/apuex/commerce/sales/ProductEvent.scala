package com.github.apuex.commerce.sales


trait ProductEvent extends Event {
  def productId: String
}
