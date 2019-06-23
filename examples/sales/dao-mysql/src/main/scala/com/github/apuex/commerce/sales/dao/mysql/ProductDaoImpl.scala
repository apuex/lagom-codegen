package com.github.apuex.commerce.sales.dao.mysql

import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection
import play._
import anorm.SqlParser._
import anorm.ParameterValue._
import anorm._

class ProductDaoImpl extends ProductDao {
  def createProduct(cmc: CreateProductCmd)(implicit conn: Connection): Int = ???
  def retrieveProduct(cmd: RetrieveProductCmd)(implicit conn: Connection): ProductVo = ???
  def updateProduct(cmd: UpdateProductCmd)(implicit conn: Connection): Int = ???
  def deleteProduct(cmd: DeleteProductCmd)(implicit conn: Connection): Int = ???
  def queryProduct(cmd: QueryCommand)(implicit conn: Connection): Seq[ProductVo] = ???
}
