package com.github.apuex.commerce.sales.dao

import com.github.apuex.commerce.sales._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection

trait ProductDao {
  def createProduct(cmd: CreateProductCmd)(implicit conn: Connection): Int
  def retrieveProduct(cmd: RetrieveProductCmd)(implicit conn: Connection): ProductVo
  def updateProduct(cmd: UpdateProductCmd)(implicit conn: Connection): Int
  def deleteProduct(cmd: DeleteProductCmd)(implicit conn: Connection): Int
  def queryProduct(cmd: QueryCommand)(implicit conn: Connection): Seq[ProductVo]
  def getProductSales(cmd: GetProductSalesCmd)(implicit conn: Connection): ProductSalesVo
  def updateProductSales(cmd: UpdateProductSalesCmd)(implicit conn: Connection): Int
  def getProductName(cmd: GetProductNameCmd)(implicit conn: Connection): ProductNameVo
  def changeProductName(cmd: ChangeProductNameCmd)(implicit conn: Connection): Int
  def getProductUnit(cmd: GetProductUnitCmd)(implicit conn: Connection): ProductUnitVo
  def changeProductUnit(cmd: ChangeProductUnitCmd)(implicit conn: Connection): Int
  def getUnitPrice(cmd: GetUnitPriceCmd)(implicit conn: Connection): UnitPriceVo
  def changeUnitPrice(cmd: ChangeUnitPriceCmd)(implicit conn: Connection): Int
}
