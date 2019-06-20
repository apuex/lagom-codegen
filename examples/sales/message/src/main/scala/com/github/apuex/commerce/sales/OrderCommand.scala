package com.github.apuex.commerce.sales


trait OrderCommand extends ShardingEntityCommand {
  def orderId: String
  override def entityId: String = {
    s"order_${orderId}"
  }
}
