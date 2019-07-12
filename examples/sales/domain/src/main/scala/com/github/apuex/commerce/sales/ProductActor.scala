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
    case x: RecoveryCompleted =>
    case x => log.info("RECOVER: {} {}", this, x)
  }

  override def receiveCommand: Receive = {
    case x => log.info("UNHANDLED: {} {}", this, x)
  }

  private def updateState: (Event => Unit) = {
    case x => log.info("UN-UPDATED: {} {}", this, x)
  }

  private def isValid(): Boolean = {
    productId != ""
  }

  private def replyToSender(msg: Any) = {
    if ("deadLetters" != sender().path.name) sender() ! msg
  }
}
