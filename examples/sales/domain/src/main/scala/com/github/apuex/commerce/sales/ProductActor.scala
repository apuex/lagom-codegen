/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.domain

import akka.actor._
import akka.event._
import akka.pattern._
import akka.persistence._
import akka.persistence.journal.Tagged
import akka.util.Timeout._
import akka.util._
import com.github.apuex.commerce.sales._
import com.google.protobuf.timestamp.Timestamp
import com.typesafe.config._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


object ProductActor {
  def props(config: Config) = Props(new ProductActor(config))
  def name: String = "ProductActor"
}

class ProductActor (config: Config) extends PersistentActor with ActorLogging {
  override def persistenceId: String = s"${self.path.name}"
  implicit def requestTimeout: Timeout = Duration(config.getString("db.sales-db.event.query-interval")).asInstanceOf[FiniteDuration]
  implicit def executionContext: ExecutionContext = context.dispatcher

  val tags = Set("all", "product")

  var productId: String = ""
  var productName: String = ""
  var productUnit: String = ""
  var unitPrice: Double = 0
  var recordTime: Option[Timestamp] = None
  var quantitySold: Double = 0
  var productDesc: String = ""

  override def receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
    case Tagged(evt, _)  =>
      updateStateWithTag(evt)
    case SnapshotOffer(_, x: ProductVo) =>
      productId = x.productId
      productName = x.productName
      productUnit = x.productUnit
      unitPrice = x.unitPrice
      recordTime = x.recordTime
      quantitySold = x.quantitySold
      productDesc = x.productDesc
    case _: RecoveryCompleted =>
    case x => log.info("RECOVER: {} {}", this, x)
  }

  override def receiveCommand: Receive = {
    case cmd: CreateProductCmd =>
      val evt = CreateProductEvent(cmd.userId, cmd.productId, cmd.productName, cmd.productUnit, cmd.unitPrice, cmd.productDesc)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case _: RetrieveProductCmd =>
      sender() ! ProductVo(productId, productName, productUnit, unitPrice, recordTime, quantitySold, productDesc)

    case cmd: UpdateProductCmd =>
      val evt = UpdateProductEvent(cmd.userId, cmd.productId, cmd.productName, cmd.productUnit, cmd.unitPrice, cmd.productDesc)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case cmd: DeleteProductCmd =>
      val evt = DeleteProductEvent(cmd.userId, cmd.productId)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case _: GetProductSalesCmd =>
      sender() ! ProductSalesVo(productId, recordTime, quantitySold)

    case cmd: UpdateProductSalesCmd =>
      recordTime = cmd.recordTime
      quantitySold = cmd.quantitySold

    case _: GetProductNameCmd =>
      sender() ! ProductNameVo(productId, productName)

    case cmd: ChangeProductNameCmd =>
      val evt = ChangeProductNameEvent(cmd.userId, cmd.productId, cmd.productName)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case _: GetProductUnitCmd =>
      sender() ! ProductUnitVo(productId, productUnit)

    case cmd: ChangeProductUnitCmd =>
      val evt = ChangeProductUnitEvent(cmd.userId, cmd.productId, cmd.productUnit)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case _: GetUnitPriceCmd =>
      sender() ! UnitPriceVo(productId, unitPrice)

    case cmd: ChangeUnitPriceCmd =>
      val evt = ChangeUnitPriceEvent(cmd.userId, cmd.productId, cmd.unitPrice)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case _: GetProductDescCmd =>
      sender() ! ProductDescVo(productId, productDesc)

    case cmd: ChangeProductDescCmd =>
      val evt = ChangeProductDescEvent(cmd.userId, cmd.productId, cmd.productDesc)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case evt: ProductEvent =>
      persist(Tagged(evt, tags))(updateStateWithTag)

    case x => log.info("UNHANDLED: {} {}", this, x)
  }

  private def updateStateWithTag: (Any => Unit) = {
    case Tagged(x, _) => updateState(x)
    case x => updateState(x)
  }

  private def updateState: (Any => Unit) = {
    case evt: CreateProductEvent =>
      productId = evt.productId
      productName = evt.productName
      productUnit = evt.productUnit
      unitPrice = evt.unitPrice
      productDesc = evt.productDesc

    case evt: UpdateProductEvent =>
      productName = evt.productName
      productUnit = evt.productUnit
      unitPrice = evt.unitPrice
      productDesc = evt.productDesc

    case _: DeleteProductEvent =>

    case evt: ChangeProductNameEvent =>
      productName = evt.productName

    case evt: ChangeProductUnitEvent =>
      productUnit = evt.productUnit

    case evt: ChangeUnitPriceEvent =>
      unitPrice = evt.unitPrice

    case evt: ChangeProductDescEvent =>
      productDesc = evt.productDesc

    case x => log.info("UN-UPDATED: {} {}", this, x)
  }

  private def isValid(): Boolean = {
    productId != ""
  }

  private def replyToSender(msg: Any) = {
    if ("deadLetters" != sender().path.name) sender() ! msg
  }
}
