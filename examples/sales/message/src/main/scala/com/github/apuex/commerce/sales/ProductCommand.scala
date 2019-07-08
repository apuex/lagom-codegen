/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales


trait ProductCommand extends Command {
  def productId: String
  override def entityId: String = {
    s"product_${productId}"
  }
}
