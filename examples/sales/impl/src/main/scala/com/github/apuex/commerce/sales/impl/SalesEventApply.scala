/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.impl

import java.sql.Connection
import java.util.Date

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator._
import com.github.apuex.commerce.sales.ScalapbJson._
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._
import com.github.apuex.events.play.EventEnvelope
import com.github.apuex.springbootsolution.runtime.DateFormat._
import play.api.db.Database

class SalesEventApply(alarmDao: AlarmDao,
  paymentTypeDao: PaymentTypeDao,
  productDao: ProductDao,
  orderDao: OrderDao,
  orderItemDao: OrderItemDao,
  eventJournalDao: EventJournalDao,
  publishQueue: String,
  mediator: ActorRef,
  db: Database) {

  def on(ee: EventEnvelope): Any = {
    db.withTransaction { implicit c =>
      ee.event
        .map(unpack)
        .map({
          case x: Event =>
            eventJournalDao.createEventJournal(
              CreateEventJournalEvent(x.userId, 0L, x.entityId, Some(toScalapbTimestamp(new Date())), x.getClass.getName, x.toByteString)
            )
            dispatch(x)
          case x: ValueObject =>
            mediator ! Publish(publishQueue, x)
        })
    }
  }

  def dispatch(msg: Any)(implicit conn: Connection): Any = msg match {
    case evt: CreateAlarmEvent =>
      alarmDao.createAlarm(evt)

    case evt: UpdateAlarmEvent =>
      alarmDao.updateAlarm(evt)

    case evt: DeleteAlarmEvent =>
      alarmDao.deleteAlarm(evt)

    case evt: BeginAlarmEvent =>
      alarmDao.beginAlarm(evt)

    case evt: EndAlarmEvent =>
      alarmDao.endAlarm(evt)

    case evt: CreatePaymentTypeEvent =>
      paymentTypeDao.createPaymentType(evt)

    case evt: UpdatePaymentTypeEvent =>
      paymentTypeDao.updatePaymentType(evt)

    case evt: DeletePaymentTypeEvent =>
      paymentTypeDao.deletePaymentType(evt)

    case evt: CreateProductEvent =>
      productDao.createProduct(evt)

    case evt: UpdateProductEvent =>
      productDao.updateProduct(evt)

    case evt: DeleteProductEvent =>
      productDao.deleteProduct(evt)

    case evt: ChangeProductNameEvent =>
      productDao.changeProductName(evt)

    case evt: ChangeProductUnitEvent =>
      productDao.changeProductUnit(evt)

    case evt: ChangeUnitPriceEvent =>
      productDao.changeUnitPrice(evt)

    case evt: ChangeProductDescEvent =>
      productDao.changeProductDesc(evt)

    case evt: CreateOrderEvent =>
      orderDao.createOrder(evt)

    case evt: UpdateOrderEvent =>
      orderDao.updateOrder(evt)

    case evt: DeleteOrderEvent =>
      orderDao.deleteOrder(evt)

    case evt: AddOrderLinesEvent =>
      orderDao.addOrderLines(evt)

    case evt: RemoveOrderLinesEvent =>
      orderDao.removeOrderLines(evt)

    case evt: ChangeOrderPaymentTypeEvent =>
      orderDao.changeOrderPaymentType(evt)

    case evt: CreateOrderItemEvent =>
      orderItemDao.createOrderItem(evt)

    case evt: UpdateOrderItemEvent =>
      orderItemDao.updateOrderItem(evt)

    case evt: DeleteOrderItemEvent =>
      orderItemDao.deleteOrderItem(evt)

    case evt: CreateEventJournalEvent =>
      eventJournalDao.createEventJournal(evt)

    case evt: UpdateEventJournalEvent =>
      eventJournalDao.updateEventJournal(evt)

    case evt: DeleteEventJournalEvent =>
      eventJournalDao.deleteEventJournal(evt)
  }
}
