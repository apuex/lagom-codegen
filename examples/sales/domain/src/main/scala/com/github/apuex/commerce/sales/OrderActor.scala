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


object OrderActor {
  def props(config: Config) = Props(new OrderActor(config))
  def name: String = "OrderActor"
}

class OrderActor (config: Config) extends PersistentActor with ActorLogging {
  override def persistenceId: String = s"${self.path.name}"
  implicit def requestTimeout: Timeout = Duration(config.getString("db.sales-db.event.query-interval")).asInstanceOf[FiniteDuration]
  implicit def executionContext: ExecutionContext = context.dispatcher

  val tags = Set("all", "order")

  var orderId: String = ""
  var orderTime: Option[Timestamp] = None
  var orderLines: Seq[OrderItemVo] = Seq()
  var orderPaymentType: PaymentType = PaymentType.fromValue(0)

  override def receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
    case Tagged(evt, _)  =>
      updateStateWithTag(evt)
    case SnapshotOffer(_, x: OrderVo) =>
      orderId = x.orderId
      orderTime = x.orderTime
      orderLines = x.orderLines
      orderPaymentType = x.orderPaymentType
    case _: RecoveryCompleted =>
    case x => log.info("RECOVER: {} {}", this, x)
  }

  override def receiveCommand: Receive = {
    case cmd: CreateOrderCmd =>
      val evt = CreateOrderEvent(cmd.userId, cmd.orderId, cmd.orderTime, cmd.orderLines, cmd.orderPaymentType)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case _: RetrieveOrderCmd =>
      sender() ! OrderVo(orderId, orderTime, orderLines, orderPaymentType)

    case cmd: UpdateOrderCmd =>
      val evt = UpdateOrderEvent(cmd.userId, cmd.orderId, cmd.orderTime, cmd.orderLines, cmd.orderPaymentType)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case cmd: DeleteOrderCmd =>
      val evt = DeleteOrderEvent(cmd.userId, cmd.orderId)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case _: GetOrderLinesCmd =>
      sender() ! OrderLinesVo(orderId, orderLines)

    case cmd: AddOrderLinesCmd =>
      orderLines = orderLines ++ cmd.orderLines
      val evt = AddOrderLinesEvent(cmd.userId, cmd.orderId, cmd.orderLines)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case cmd: RemoveOrderLinesCmd =>
      orderLines = orderLines.filter(cmd.orderLines.contains(_))
      val evt = RemoveOrderLinesEvent(cmd.userId, cmd.orderId, cmd.orderLines)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case _: GetOrderPaymentTypeCmd =>
      sender() ! OrderPaymentTypeVo(orderId, orderPaymentType)

    case cmd: ChangeOrderPaymentTypeCmd =>
      val evt = ChangeOrderPaymentTypeEvent(cmd.userId, cmd.orderId, cmd.orderPaymentType)
      persist(Tagged(evt, tags))(updateStateWithTag)

    case evt: OrderEvent =>
      persist(Tagged(evt, tags))(updateStateWithTag)

    case x => log.info("UNHANDLED: {} {}", this, x)
  }

  private def updateStateWithTag: (Any => Unit) = {
    case Tagged(x, _) => updateState(x)
    case x => updateState(x)
  }

  private def updateState: (Any => Unit) = {
    case evt: CreateOrderEvent =>
      orderId = evt.orderId
      orderTime = evt.orderTime
      orderLines = evt.orderLines
      orderPaymentType = evt.orderPaymentType

    case evt: UpdateOrderEvent =>
      orderTime = evt.orderTime
      orderLines = evt.orderLines
      orderPaymentType = evt.orderPaymentType

    case _: DeleteOrderEvent =>

    case evt: AddOrderLinesEvent =>
      orderLines = orderLines ++ evt.orderLines

    case evt: RemoveOrderLinesEvent =>
      orderLines = orderLines.filter(evt.orderLines.contains(_))

    case evt: ChangeOrderPaymentTypeEvent =>
      orderPaymentType = evt.orderPaymentType

    case x => log.info("UN-UPDATED: {} {}", this, x)
  }

  private def isValid(): Boolean = {
    orderId != ""
  }

  private def replyToSender(msg: Any) = {
    if ("deadLetters" != sender().path.name) sender() ! msg
  }
}
