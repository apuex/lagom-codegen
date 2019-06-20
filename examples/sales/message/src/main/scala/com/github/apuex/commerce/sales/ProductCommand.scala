package com.github.apuex.commerce.sales


trait ProductCommand extends ShardingEntityCommand {
  def productId: String
  override def entityId: String = {
    s"product_${productId}"
  }
}
