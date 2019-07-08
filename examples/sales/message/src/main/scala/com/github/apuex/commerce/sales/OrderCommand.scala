/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales


trait OrderCommand extends Command {
  def orderId: String
  override def entityId: String = {
    s"order_${orderId}"
  }
}
