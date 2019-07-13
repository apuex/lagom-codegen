/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.domain

import akka.actor._
import akka.event._
import akka.pattern._
import akka.persistence._
import akka.util.Timeout._
import akka.util._
import com.github.apuex.commerce.sales._
import com.google.protobuf.timestamp.Timestamp
import com.typesafe.config._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


object ProductActor {
  def props = Props[ProductActor]
  def name: String = "ProductActor"
}

class ProductActor (config: Config) extends PersistentActor with ActorLogging {
  override def persistenceId: String = s"${self.path.name}"
  implicit def requestTimeout: Timeout = Duration(config.getString("db.sales-db.event.query-interval")).asInstanceOf[FiniteDuration]
  implicit def executionContext: ExecutionContext = context.dispatcher

  var productId: String = ""
  var productName: String = ""
  var productUnit: String = ""
  var unitPrice: Double = 0
  var recordTime: Option[Timestamp] = None
  var quantitySold: Option[Double] = None
  var productDesc: String = ""

  override def receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
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

    case cmd: RetrieveProductCmd =>

    case cmd: UpdateProductCmd =>

    case cmd: DeleteProductCmd =>

    case cmd: ChangeProductNameCmd =>

    case cmd: ChangeProductUnitCmd =>

    case cmd: ChangeUnitPriceCmd =>

    case cmd: ChangeProductDescCmd =>

    case x => log.info("UNHANDLED: {} {}", this, x)
  }

  private def updateState: (Event => Unit) = {
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

    case evt: DeleteProductEvent =>

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
