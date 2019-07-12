/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.shard

import akka.actor._
import akka.cluster.sharding._
import com.github.apuex.commerce.sales._
import com.typesafe.config._
import scala.collection.convert.ImplicitConversions._


object ShardingOrders {
  def props = Props[ShardingOrders]
  def name: String = "ShardingOrders"
}

class ShardingOrders (config: Config) extends Actor with ActorLogging {

  ShardingOrder.defaultNumberOfShards = config.getInt("sales.entity.number-of-shards")

  def shardingOrders(): ActorRef = {
    ClusterSharding(context.system).shardRegion(ShardingOrder.shardName)
  }

  ClusterSharding(context.system).start(
    ShardingOrder.shardName,
    ShardingOrder.props,
    ClusterShardingSettings(context.system),
    ShardingOrder.extractEntityId,
    ShardingOrder.extractShardId
  )

  override def receive: Receive = {
    case cmd: Command =>
      shardingOrders() forward cmd
    case x => log.info("unhandled COMMAND: {} {}", this, x)
  }
}
