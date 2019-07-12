/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.shard

import akka.actor._
import akka.cluster.sharding._
import com.github.apuex.commerce.sales._
import com.typesafe.config._
import scala.collection.convert.ImplicitConversions._


object ShardingOrderItems {
  def props = Props[ShardingOrderItems]
  def name: String = "ShardingOrderItems"
}

class ShardingOrderItems (config: Config) extends Actor with ActorLogging {

  ShardingOrderItem.defaultNumberOfShards = config.getInt("sales.entity.number-of-shards")

  def shardingOrderItems(): ActorRef = {
    ClusterSharding(context.system).shardRegion(ShardingOrderItem.shardName)
  }

  ClusterSharding(context.system).start(
    ShardingOrderItem.shardName,
    ShardingOrderItem.props,
    ClusterShardingSettings(context.system),
    ShardingOrderItem.extractEntityId,
    ShardingOrderItem.extractShardId
  )

  override def receive: Receive = {
    case cmd: Command =>
      shardingOrderItems() forward cmd
    case x => log.info("unhandled COMMAND: {} {}", this, x)
  }
}
