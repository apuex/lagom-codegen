/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.domain

import com.github.apuex.commerce.sales._
import com.google.protobuf.timestamp.Timestamp
import akka.actor._
import akka.event._
import akka.pattern._
import akka.persistence._
import akka.util._
import akka.util.Timeout._

import scala.collection.convert.ImplicitConversions._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util._


object OrderActor {
  def props = Props[OrderActor]
  def name: String = "OrderActor"
}

class OrderActor () extends PersistentActor with ActorLogging {
  override def persistenceId: String = s"${self.path.name}"
  implicit def requestTimeout: Timeout = FiniteDuration(20, SECONDS)
  implicit def executionContext: ExecutionContext = context.dispatcher


  override def receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
    case SnapshotOffer(_, x: OrderVo) =>
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
    true
  }

  private def replyToSender(msg: Any) = {
    if ("deadLetters" != sender().path.name) sender() ! msg
  }
}
