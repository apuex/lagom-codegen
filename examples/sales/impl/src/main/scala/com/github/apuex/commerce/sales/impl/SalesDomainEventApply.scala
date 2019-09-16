/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.impl

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator._
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.sharding._
import scalapb.GeneratedMessage


class SalesDomainEventApply(clusterShardingModule: ClusterShardingModule,
  publishQueue: String,
  mediator: ActorRef) {

  import clusterShardingModule._

  def on(event: GeneratedMessage): Any = {
    event match {
      case x: Event =>
        dispatch(x)
        mediator ! Publish(publishQueue, x)
      case x: ValueObject =>
        mediator ! Publish(publishQueue, x)
      case _ => None
    }
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
