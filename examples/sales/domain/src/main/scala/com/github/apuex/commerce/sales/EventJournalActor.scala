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


object EventJournalActor {
  def props = Props[EventJournalActor]
  def name: String = "EventJournalActor"
}

class EventJournalActor (config: Config) extends PersistentActor with ActorLogging {
  override def persistenceId: String = s"${self.path.name}"
  implicit def requestTimeout: Timeout = Duration(config.getString("db.sales-db.event.query-interval")).asInstanceOf[FiniteDuration]
  implicit def executionContext: ExecutionContext = context.dispatcher


  override def receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
    case SnapshotOffer(_, x: EventJournalVo) =>
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
