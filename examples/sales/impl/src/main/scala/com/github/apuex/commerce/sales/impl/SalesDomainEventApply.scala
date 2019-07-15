/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.impl

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator._
import com.github.apuex.commerce.sales.ScalapbJson._
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.sharding._
import com.github.apuex.events.play.EventEnvelope


class SalesDomainEventApply(clusterShardingModule: ClusterShardingModule,
  publishQueue: String,
  mediator: ActorRef) {

  import clusterShardingModule._

  def on(ee: EventEnvelope): Any = {
    ee.event
      .map(unpack)
      .map({
        case x: Event =>
          dispatch(x)
        case x: ValueObject =>
          mediator ! Publish(publishQueue, x)
      })
  }

  def dispatch(msg: Any): Any = msg match {
    case evt: AlarmEvent =>
      shardingAlarms ! evt

    case evt: ProductEvent =>
      shardingProducts ! evt

    case evt: OrderEvent =>
      shardingOrders ! evt
    case _ => None
  }
}
