/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.impl

import java.util.{Date, UUID}

import akka._
import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator._
import akka.pattern.ask
import akka.persistence.query._
import akka.persistence.query.scaladsl.EventsByTagQuery
import akka.stream.scaladsl._
import akka.stream.{OverflowStrategy, SourceShape}
import akka.util.Timeout
import com.github.apuex.commerce.sales.ScalapbJson._
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao.mysql._
import com.github.apuex.commerce.sales.sharding._
import com.github.apuex.events.play.EventEnvelope
import com.github.apuex.springbootsolution.runtime.DateFormat._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.any.Any
import com.lightbend.lagom.scaladsl.api._
import com.typesafe.config.Config
import play.api.db.Database
import scalapb.GeneratedMessage

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}

class SalesServiceImpl (config: Config,
  clusterShardingModule: ClusterShardingModule,
  daoModule: DaoModule,
  eventApply: SalesDomainEventApply,
  mediator: ActorRef,
  readJournal: EventsByTagQuery,
  db: Database)
  extends SalesService {

  import clusterShardingModule._
  import daoModule._

  val publishQueue = config.getString("sales.instant-event-publish-queue")
  implicit val duration = Duration(config.getString("db.sales-db.event.query-interval")).asInstanceOf[FiniteDuration]
  implicit val timeout = Timeout(Duration(config.getString("sales.request-timeout")).asInstanceOf[FiniteDuration])

  def createAlarm(): ServiceCall[CreateAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingAlarms ! cmd
      0
    })
  }

  def retrieveAlarm(): ServiceCall[RetrieveAlarmCmd, AlarmVo] = ServiceCall { cmd =>
    shardingAlarms.ask(cmd).mapTo[AlarmVo]
  }

  def updateAlarm(): ServiceCall[UpdateAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingAlarms ! cmd
      0
    })
  }

  def deleteAlarm(): ServiceCall[DeleteAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingAlarms ! cmd
      0
    })
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
    Future.successful({
      shardingAlarms ! cmd
      0
    })
  }

  def endAlarm(): ServiceCall[EndAlarmCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingAlarms ! cmd
      0
    })
  }

  def createPaymentType(): ServiceCall[CreatePaymentTypeCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = CreatePaymentTypeEvent(cmd.userId, cmd.paymentTypeId, cmd.paymentTypeName, cmd.paymentTypeLabel)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(cmd.userId, 0L, cmd.entityId, Some(toScalapbTimestamp(new Date())), evt.getClass.getName, evt.toByteString)
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
          CreateEventJournalEvent(cmd.userId, 0L, cmd.entityId, Some(toScalapbTimestamp(new Date())), evt.getClass.getName, evt.toByteString)
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
          CreateEventJournalEvent(cmd.userId, 0L, cmd.entityId, Some(toScalapbTimestamp(new Date())), evt.getClass.getName, evt.toByteString)
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
    Future.successful({
      shardingProducts ! cmd
      0
    })
  }

  def retrieveProduct(): ServiceCall[RetrieveProductCmd, ProductVo] = ServiceCall { cmd =>
    shardingProducts.ask(cmd).mapTo[ProductVo]
  }

  def updateProduct(): ServiceCall[UpdateProductCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingProducts ! cmd
      0
    })
  }

  def deleteProduct(): ServiceCall[DeleteProductCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingProducts ! cmd
      0
    })
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
    shardingProducts.ask(cmd).mapTo[ProductSalesVo]
  }

  def updateProductSales(): ServiceCall[UpdateProductSalesCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingProducts ! cmd
      0
    })
  }

  def getProductName(): ServiceCall[GetProductNameCmd, ProductNameVo] = ServiceCall { cmd =>
    shardingProducts.ask(cmd).mapTo[ProductNameVo]
  }

  def changeProductName(): ServiceCall[ChangeProductNameCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingProducts ! cmd
      0
    })
  }

  def getProductUnit(): ServiceCall[GetProductUnitCmd, ProductUnitVo] = ServiceCall { cmd =>
    shardingProducts.ask(cmd).mapTo[ProductUnitVo]
  }

  def changeProductUnit(): ServiceCall[ChangeProductUnitCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingProducts ! cmd
      0
    })
  }

  def getUnitPrice(): ServiceCall[GetUnitPriceCmd, UnitPriceVo] = ServiceCall { cmd =>
    shardingProducts.ask(cmd).mapTo[UnitPriceVo]
  }

  def changeUnitPrice(): ServiceCall[ChangeUnitPriceCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingProducts ! cmd
      0
    })
  }

  def getProductDesc(): ServiceCall[GetProductDescCmd, ProductDescVo] = ServiceCall { cmd =>
    shardingProducts.ask(cmd).mapTo[ProductDescVo]
  }

  def changeProductDesc(): ServiceCall[ChangeProductDescCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingProducts ! cmd
      0
    })
  }

  def createOrder(): ServiceCall[CreateOrderCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingOrders ! cmd
      0
    })
  }

  def retrieveOrder(): ServiceCall[RetrieveOrderCmd, OrderVo] = ServiceCall { cmd =>
    shardingOrders.ask(cmd).mapTo[OrderVo]
  }

  def updateOrder(): ServiceCall[UpdateOrderCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingOrders ! cmd
      0
    })
  }

  def deleteOrder(): ServiceCall[DeleteOrderCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingOrders ! cmd
      0
    })
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
    shardingOrders.ask(cmd).mapTo[OrderLinesVo]
  }

  def addOrderLines(): ServiceCall[AddOrderLinesCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingOrders ! cmd
      0
    })
  }

  def removeOrderLines(): ServiceCall[RemoveOrderLinesCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingOrders ! cmd
      0
    })
  }

  def getOrderPaymentType(): ServiceCall[GetOrderPaymentTypeCmd, OrderPaymentTypeVo] = ServiceCall { cmd =>
    shardingOrders.ask(cmd).mapTo[OrderPaymentTypeVo]
  }

  def changeOrderPaymentType(): ServiceCall[ChangeOrderPaymentTypeCmd, Int] = ServiceCall { cmd =>
    Future.successful({
      shardingOrders ! cmd
      0
    })
  }

  def createOrderItem(): ServiceCall[CreateOrderItemCmd, Int] = ServiceCall { cmd =>
    Future.successful(
      db.withTransaction { implicit c =>
        val evt = CreateOrderItemEvent(cmd.userId, cmd.orderId, cmd.productId, cmd.productName, cmd.itemUnit, cmd.unitPrice, cmd.orderQuantity)
        eventJournalDao.createEventJournal(
          CreateEventJournalEvent(cmd.userId, 0L, cmd.entityId, Some(toScalapbTimestamp(new Date())), evt.getClass.getName, evt.toByteString)
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
          CreateEventJournalEvent(cmd.userId, 0L, cmd.entityId, Some(toScalapbTimestamp(new Date())), evt.getClass.getName, evt.toByteString)
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
          CreateEventJournalEvent(cmd.userId, 0L, cmd.entityId, Some(toScalapbTimestamp(new Date())), evt.getClass.getName, evt.toByteString)
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

        val eventSource = readJournal
          .eventsByTag(
            "all",
            offset
              .map(x => {
                if (x.matches("^[\\+\\-]{0,1}[0-9]+$")) Offset.sequence(x.toLong)
                else Offset.timeBasedUUID(UUID.fromString(x))
              })
              .getOrElse(Offset.noOffset)
          )
          .filter(ee => ee.event.isInstanceOf[GeneratedMessage])
          .map(ee => EventEnvelope(
            ee.offset match {
              case Sequence(value) => value.toString
              case TimeBasedUUID(value) => value.toString
              case x => x.toString
            },
            ee.persistenceId,
            ee.sequenceNr,
            Some(pack(ee.event.getClass.getName, ee.event.asInstanceOf[GeneratedMessage].toByteString)))
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
}
