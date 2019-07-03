/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.dao

import com.github.apuex.commerce.sales._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection

trait ProductDao {
  def createProduct(evt: CreateProductEvent)(implicit conn: Connection): Int

  def retrieveProduct(cmd: RetrieveProductCmd)(implicit conn: Connection): ProductVo

  def updateProduct(evt: UpdateProductEvent)(implicit conn: Connection): Int

  def deleteProduct(evt: DeleteProductEvent)(implicit conn: Connection): Int

  def queryProduct(cmd: QueryCommand)(implicit conn: Connection): Seq[ProductVo]

  def retrieveProductByRowid(rowid: String)(implicit conn: Connection): ProductVo

  def getProductSales(cmd: GetProductSalesCmd)(implicit conn: Connection): ProductSalesVo

  def updateProductSales(evt: UpdateProductSalesEvent)(implicit conn: Connection): Int

  def getProductName(cmd: GetProductNameCmd)(implicit conn: Connection): ProductNameVo

  def changeProductName(evt: ChangeProductNameEvent)(implicit conn: Connection): Int

  def getProductUnit(cmd: GetProductUnitCmd)(implicit conn: Connection): ProductUnitVo

  def changeProductUnit(evt: ChangeProductUnitEvent)(implicit conn: Connection): Int

  def getUnitPrice(cmd: GetUnitPriceCmd)(implicit conn: Connection): UnitPriceVo

  def changeUnitPrice(evt: ChangeUnitPriceEvent)(implicit conn: Connection): Int
}
