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


object AlarmActor {
  def props = Props[AlarmActor]
  def name: String = "AlarmActor"
}

class AlarmActor (config: Config) extends PersistentActor with ActorLogging {
  override def persistenceId: String = s"${self.path.name}"
  implicit def requestTimeout: Timeout = Duration(config.getString("db.sales-db.event.query-interval")).asInstanceOf[FiniteDuration]
  implicit def executionContext: ExecutionContext = context.dispatcher

  var alarmId: String = ""
  var alarmBegin: Option[Timestamp] = None
  var alarmEnd: Option[Timestamp] = None
  var alarmDesc: String = ""

  override def receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
    case SnapshotOffer(_, x: AlarmVo) =>
      alarmId = x.alarmId
      alarmBegin = x.alarmBegin
      alarmEnd = x.alarmEnd
      alarmDesc = x.alarmDesc
    case _: RecoveryCompleted =>
    case x => log.info("RECOVER: {} {}", this, x)
  }

  override def receiveCommand: Receive = {
    case cmd: CreateAlarmCmd =>

    case cmd: RetrieveAlarmCmd =>

    case cmd: UpdateAlarmCmd =>

    case cmd: DeleteAlarmCmd =>

    case cmd: BeginAlarmCmd =>

    case cmd: EndAlarmCmd =>

    case x => log.info("UNHANDLED: {} {}", this, x)
  }

  private def updateState: (Event => Unit) = {
    case evt: CreateAlarmEvent =>
      alarmId = evt.alarmId
      alarmBegin = evt.alarmBegin
      alarmEnd = evt.alarmEnd
      alarmDesc = evt.alarmDesc

    case evt: UpdateAlarmEvent =>
      alarmEnd = evt.alarmEnd
      alarmDesc = evt.alarmDesc

    case evt: DeleteAlarmEvent =>

    case evt: BeginAlarmEvent =>
      alarmDesc = evt.alarmDesc

    case evt: EndAlarmEvent =>
      alarmEnd = evt.alarmEnd
      alarmDesc = evt.alarmDesc

    case x => log.info("UN-UPDATED: {} {}", this, x)
  }

  private def isValid(): Boolean = {
    alarmId != "" && alarmBegin != None
  }

  private def replyToSender(msg: Any) = {
    if ("deadLetters" != sender().path.name) sender() ! msg
  }
}
