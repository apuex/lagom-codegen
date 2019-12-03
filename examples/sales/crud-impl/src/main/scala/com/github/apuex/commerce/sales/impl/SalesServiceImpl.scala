/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.impl

import java.util.Date

import akka._
import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator._
import akka.pattern.ask
import akka.stream.scaladsl._
import akka.stream.{OverflowStrategy, SourceShape}
import akka.util.Timeout
import com.datastax.driver.core.utils.UUIDs
import com.github.apuex.commerce.sales.ScalapbJson._
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._
import com.github.apuex.events.play.EventEnvelope
import com.github.apuex.springbootsolution.runtime.DateFormat._
import com.github.apuex.springbootsolution.runtime.FilterPredicate.Clause.{Connection, Predicate}
import com.github.apuex.springbootsolution.runtime.LogicalConnectionType.AND
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.any.Any
import com.google.protobuf.timestamp.Timestamp
import com.lightbend.lagom.scaladsl.api._
import play.api.db.Database
import scalapb.GeneratedMessage

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class SalesServiceImpl (alarmDao: AlarmDao,
  paymentTypeDao: PaymentTypeDao,
  productDao: ProductDao,
  orderDao: OrderDao,
  orderItemDao: OrderItemDao,
  eventJournalDao: EventJournalDao,
  eventApply: SalesQueryEventApply,
  publishQueue: String,
  mediator: ActorRef,
  duration: FiniteDuration,
  db: Database)
  extends SalesService {

  def createAlarm(): ServiceCall[CreateAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = CreateAlarmEvent(cmd.userId, cmd.alarmId, cmd.alarmBegin, cmd.alarmEnd, cmd.alarmDesc)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        alarmDao.createAlarm(evt)
      }
    )
  }

  def retrieveAlarm(): ServiceCall[RetrieveAlarmCmd, AlarmVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        alarmDao.retrieveAlarm(cmd)
      }
    )
  }

  def updateAlarm(): ServiceCall[UpdateAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = UpdateAlarmEvent(cmd.userId, cmd.alarmId, cmd.alarmBegin, cmd.alarmEnd, cmd.alarmDesc)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        alarmDao.updateAlarm(evt)
      }
    )
  }

  def deleteAlarm(): ServiceCall[DeleteAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = DeleteAlarmEvent(cmd.userId, cmd.alarmId, cmd.alarmBegin)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        alarmDao.deleteAlarm(evt)
      }
    )
  }

  def queryAlarm(): ServiceCall[QueryCommand, AlarmListVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         AlarmListVo(alarmDao.queryAlarm(cmd))
      }
    )
  }

  def retrieveAlarmByRowid(rowid: String): ServiceCall[NotUsed, AlarmVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         alarmDao.retrieveAlarmByRowid(rowid)
      }
    )
  }

  def beginAlarm(): ServiceCall[BeginAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = BeginAlarmEvent(cmd.userId, cmd.alarmId, cmd.alarmBegin, cmd.alarmDesc)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(cmd.userId, 0L, cmd.entityId, UUIDs.timeBased().toString, cmd.getClass.getName, cmd.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        alarmDao.beginAlarm(evt)
      }
    )
  }

  def endAlarm(): ServiceCall[EndAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = EndAlarmEvent(cmd.userId, cmd.alarmId, cmd.alarmBegin, cmd.alarmEnd, cmd.alarmDesc)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(cmd.userId, 0L, cmd.entityId, UUIDs.timeBased().toString, cmd.getClass.getName, cmd.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        alarmDao.endAlarm(evt)
      }
    )
  }

  def createPaymentType(): ServiceCall[CreatePaymentTypeCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = CreatePaymentTypeEvent(cmd.userId, cmd.paymentTypeId, cmd.paymentTypeName, cmd.paymentTypeLabel)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        paymentTypeDao.createPaymentType(evt)
      }
    )
  }

  def retrievePaymentType(): ServiceCall[RetrievePaymentTypeCmd, PaymentTypeVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        paymentTypeDao.retrievePaymentType(cmd)
      }
    )
  }

  def updatePaymentType(): ServiceCall[UpdatePaymentTypeCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = UpdatePaymentTypeEvent(cmd.userId, cmd.paymentTypeId, cmd.paymentTypeName, cmd.paymentTypeLabel)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        paymentTypeDao.updatePaymentType(evt)
      }
    )
  }

  def deletePaymentType(): ServiceCall[DeletePaymentTypeCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = DeletePaymentTypeEvent(cmd.userId, cmd.paymentTypeId)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        paymentTypeDao.deletePaymentType(evt)
      }
    )
  }

  def queryPaymentType(): ServiceCall[QueryCommand, PaymentTypeListVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         PaymentTypeListVo(paymentTypeDao.queryPaymentType(cmd))
      }
    )
  }

  def retrievePaymentTypeByRowid(rowid: String): ServiceCall[NotUsed, PaymentTypeVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         paymentTypeDao.retrievePaymentTypeByRowid(rowid)
      }
    )
  }

  def createProduct(): ServiceCall[CreateProductCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = CreateProductEvent(cmd.userId, cmd.productId, cmd.productName, cmd.productUnit, cmd.unitPrice, cmd.productDesc)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        productDao.createProduct(evt)
      }
    )
  }

  def retrieveProduct(): ServiceCall[RetrieveProductCmd, ProductVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        productDao.retrieveProduct(cmd)
      }
    )
  }

  def updateProduct(): ServiceCall[UpdateProductCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = UpdateProductEvent(cmd.userId, cmd.productId, cmd.productName, cmd.productUnit, cmd.unitPrice, cmd.productDesc)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        productDao.updateProduct(evt)
      }
    )
  }

  def deleteProduct(): ServiceCall[DeleteProductCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = DeleteProductEvent(cmd.userId, cmd.productId)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        productDao.deleteProduct(evt)
      }
    )
  }

  def queryProduct(): ServiceCall[QueryCommand, ProductListVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         ProductListVo(productDao.queryProduct(cmd))
      }
    )
  }

  def retrieveProductByRowid(rowid: String): ServiceCall[NotUsed, ProductVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.retrieveProductByRowid(rowid)
      }
    )
  }

  def getProductSales(): ServiceCall[GetProductSalesCmd, ProductSalesVo] = ServiceCall { cmd =>
    mediator.ask(Publish(publishQueue, cmd))(Timeout(duration))
      .mapTo[ProductSalesVo]
  }

  def updateProductSales(): ServiceCall[UpdateProductSalesCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      mediator ! Publish(publishQueue, cmd)
      0
    })
  }

  def getProductName(): ServiceCall[GetProductNameCmd, ProductNameVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.getProductName(cmd)
      }
    )
  }

  def changeProductName(): ServiceCall[ChangeProductNameCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = ChangeProductNameEvent(cmd.userId, cmd.productId, cmd.productName)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        productDao.changeProductName(evt)
      }
    )
  }

  def getProductUnit(): ServiceCall[GetProductUnitCmd, ProductUnitVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.getProductUnit(cmd)
      }
    )
  }

  def changeProductUnit(): ServiceCall[ChangeProductUnitCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = ChangeProductUnitEvent(cmd.userId, cmd.productId, cmd.productUnit)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        productDao.changeProductUnit(evt)
      }
    )
  }

  def getUnitPrice(): ServiceCall[GetUnitPriceCmd, UnitPriceVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.getUnitPrice(cmd)
      }
    )
  }

  def changeUnitPrice(): ServiceCall[ChangeUnitPriceCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = ChangeUnitPriceEvent(cmd.userId, cmd.productId, cmd.unitPrice)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        productDao.changeUnitPrice(evt)
      }
    )
  }

  def getProductDesc(): ServiceCall[GetProductDescCmd, ProductDescVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         productDao.getProductDesc(cmd)
      }
    )
  }

  def changeProductDesc(): ServiceCall[ChangeProductDescCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = ChangeProductDescEvent(cmd.userId, cmd.productId, cmd.productDesc)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        productDao.changeProductDesc(evt)
      }
    )
  }

  def createOrder(): ServiceCall[CreateOrderCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = CreateOrderEvent(cmd.userId, cmd.orderId, cmd.orderTime, cmd.orderLines, cmd.orderPaymentType)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        orderDao.createOrder(evt)
      }
    )
  }

  def retrieveOrder(): ServiceCall[RetrieveOrderCmd, OrderVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        orderDao.retrieveOrder(cmd)
      }
    )
  }

  def updateOrder(): ServiceCall[UpdateOrderCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = UpdateOrderEvent(cmd.userId, cmd.orderId, cmd.orderTime, cmd.orderLines, cmd.orderPaymentType)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        orderDao.updateOrder(evt)
      }
    )
  }

  def deleteOrder(): ServiceCall[DeleteOrderCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = DeleteOrderEvent(cmd.userId, cmd.orderId)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        orderDao.deleteOrder(evt)
      }
    )
  }

  def queryOrder(): ServiceCall[QueryCommand, OrderListVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         OrderListVo(orderDao.queryOrder(cmd))
      }
    )
  }

  def retrieveOrderByRowid(rowid: String): ServiceCall[NotUsed, OrderVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.retrieveOrderByRowid(rowid)
      }
    )
  }

  def getOrderLines(): ServiceCall[GetOrderLinesCmd, OrderLinesVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.getOrderLines(cmd)
      }
    )
  }

  def addOrderLines(): ServiceCall[AddOrderLinesCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = AddOrderLinesEvent(cmd.userId, cmd.orderId, cmd.orderLines)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        orderDao.addOrderLines(evt)
      }
    )
  }

  def removeOrderLines(): ServiceCall[RemoveOrderLinesCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = RemoveOrderLinesEvent(cmd.userId, cmd.orderId)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        orderDao.removeOrderLines(evt)
      }
    )
  }

  def getOrderPaymentType(): ServiceCall[GetOrderPaymentTypeCmd, OrderPaymentTypeVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderDao.getOrderPaymentType(cmd)
      }
    )
  }

  def changeOrderPaymentType(): ServiceCall[ChangeOrderPaymentTypeCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = ChangeOrderPaymentTypeEvent(cmd.userId, cmd.orderId, cmd.orderPaymentType)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        orderDao.changeOrderPaymentType(evt)
      }
    )
  }

  def createOrderItem(): ServiceCall[CreateOrderItemCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = CreateOrderItemEvent(cmd.userId, cmd.orderId, cmd.productId, cmd.productName, cmd.itemUnit, cmd.unitPrice, cmd.orderQuantity)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        orderItemDao.createOrderItem(evt)
      }
    )
  }

  def retrieveOrderItem(): ServiceCall[RetrieveOrderItemCmd, OrderItemVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        orderItemDao.retrieveOrderItem(cmd)
      }
    )
  }

  def updateOrderItem(): ServiceCall[UpdateOrderItemCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = UpdateOrderItemEvent(cmd.userId, cmd.orderId, cmd.productId, cmd.productName, cmd.itemUnit, cmd.unitPrice, cmd.orderQuantity)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        orderItemDao.updateOrderItem(evt)
      }
    )
  }

  def deleteOrderItem(): ServiceCall[DeleteOrderItemCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = DeleteOrderItemEvent(cmd.userId, cmd.orderId, cmd.productId)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(evt.userId, 0L, evt.entityId, UUIDs.timeBased().toString, evt.getClass.getName, evt.toByteString)
        )
        mediator ! Publish(publishQueue, evt)
        orderItemDao.deleteOrderItem(evt)
      }
    )
  }

  def queryOrderItem(): ServiceCall[QueryCommand, OrderItemListVo] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
         OrderItemListVo(orderItemDao.queryOrderItem(cmd))
      }
    )
  }

  def retrieveOrderItemByRowid(rowid: String): ServiceCall[NotUsed, OrderItemVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderItemDao.retrieveOrderItemByRowid(rowid)
      }
    )
  }

  def selectOrderItemByOrderId(orderId: String): ServiceCall[NotUsed, OrderItemListVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         OrderItemListVo(orderItemDao.selectByOrderId(orderId))
      }
    )
  }

  def deleteOrderItemByOrderId(orderId: String): ServiceCall[NotUsed, Int] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderItemDao.deleteByOrderId(orderId)
      }
    )
  }

  def selectOrderItemByProductId(productId: String): ServiceCall[NotUsed, OrderItemListVo] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         OrderItemListVo(orderItemDao.selectByProductId(productId))
      }
    )
  }

  def deleteOrderItemByProductId(productId: String): ServiceCall[NotUsed, Int] = ServiceCall { _ =>
    Future.successful(
      db.withTransaction { implicit c =>
         orderItemDao.deleteByProductId(productId)
      }
    )
  }

  def currentEvents(): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] = {
    ServiceCall { is =>
      Future.successful({
        val replySource = is
          .map(parseJson)
          .filter(x => x.event.isDefined)
          .map(x => unpack(x.event.get))
          .map(eventApply.on)
          .filter(_ => false) // to drainage
          .map(x => printer.print(x.asInstanceOf[GeneratedMessage]))

        val commandSource: Source[String, ActorRef] = Source.actorRef[scala.Any](
          512,
          OverflowStrategy.dropHead)
          .filter(x => x.isInstanceOf[Event] || x.isInstanceOf[ValueObject])
          .map({
            case x: Event =>
              EventEnvelope(
                "",          // offset
                x.entityId,  // persistence_id
                0L,          // sequence_nr
                Some(pack(x.asInstanceOf[GeneratedMessage])))
            case x: ValueObject =>
              EventEnvelope(
                "",          // offset
                "",          // persistence_id
                0L,          // sequence_nr
                Some(pack(x.asInstanceOf[GeneratedMessage])))
          })
          .map(printer.print(_))

        Source.fromGraph(GraphDSL.create() { implicit builder =>
          import akka.stream.scaladsl.GraphDSL.Implicits._
          val replyShape = builder.add(replySource)
          val materializedCommandSource = commandSource.mapMaterializedValue(actorRef => mediator ! Subscribe(publishQueue, actorRef))
          val commandShape = builder.add(materializedCommandSource)

          val merge = builder.add(Merge[String](2))

          replyShape ~> merge
          commandShape ~> merge

          SourceShape(merge.out)
        })
      })
    }
  }

  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] = {
    ServiceCall { is =>
      Future.successful({
        // reply/confirm to inbound message...
        val replySource = is
          .map(parseJson)
          .filter(x => x.event.isDefined)
          .map(x => unpack(x.event.get))
          .map(eventApply.on)
          .filter(_ => false) // to drainage
          .map(x => printer.print(x.asInstanceOf[GeneratedMessage]))

        val commandSource: Source[String, ActorRef] = Source.actorRef[scala.Any](
          512,
          OverflowStrategy.dropHead)
          .filter(x => x.isInstanceOf[Command])
          .map({
            case x: Command =>
              EventEnvelope(
                "",          // offset
                x.entityId,  // persistence_id
                0L,          // sequence_nr
                Some(pack(x.asInstanceOf[GeneratedMessage])))
          })
          .map(printer.print(_))

        val eventSource = Source.fromIterator(() => new Iterator[Seq[EventJournalVo]] {
          var lastOffset: Option[String] = Some(offset.getOrElse("0"))

          override def hasNext: Boolean = true

          override def next(): Seq[EventJournalVo] = db.withTransaction { implicit c =>
            val result = eventJournalDao.queryEventJournal(queryForEventsCmd(lastOffset).withOrderBy(Seq(OrderBy("offset", OrderType.ASC))))
            if (!result.isEmpty) {
              lastOffset = Some(result.last.offset.toString)
            }
            result
          }
        })
          .throttle(1, duration)
          .flatMapMerge(2, x => Source(x.toList))
          .map(x => EventEnvelope(
            x.offset.toString,
            x.persistenceId,
            0,
            Some(pack(x.metaData, x.content)))
          )
          .map(printer.print(_))

        Source.fromGraph(GraphDSL.create() { implicit builder =>
          import akka.stream.scaladsl.GraphDSL.Implicits._
          val replyShape = builder.add(replySource)
          val eventShape = builder.add(eventSource)
          val materializedCommandSource = commandSource.mapMaterializedValue(actorRef => mediator ! Subscribe(publishQueue, actorRef))
          val commandShape = builder.add(materializedCommandSource)

          val merge = builder.add(Merge[String](3))

          replyShape ~> merge
          eventShape ~> merge
          commandShape ~> merge

          SourceShape(merge.out)
        })
      })
    }
  }

  private def queryForEventsCmd(offset: Option[String]): QueryCommand = {
    QueryCommand(
      Some(
        FilterPredicate(
          Connection(
            LogicalConnectionVo(
              AND,
              Seq(
                FilterPredicate(
                  Predicate(
                    LogicalPredicateVo(
                      PredicateType.GT,
                      "offset",
                      Seq("offset")
                    )
                  )
                )
              )
            )
          )
        )
      ),
      Map(
        "offset" -> offset.getOrElse("")
      )
    )
  }
}
