/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.shard

import akka.actor._
import akka.cluster.sharding._
import com.github.apuex.commerce.sales._
import com.typesafe.config._
import scala.collection.convert.ImplicitConversions._


object ShardingProducts {
  def props = Props[ShardingProducts]
  def name: String = "ShardingProducts"
}

class ShardingProducts (config: Config) extends Actor with ActorLogging {

  ShardingProduct.defaultNumberOfShards = config.getInt("sales.entity.number-of-shards")

  def shardingProducts(): ActorRef = {
    ClusterSharding(context.system).shardRegion(ShardingProduct.shardName)
  }

  ClusterSharding(context.system).start(
    ShardingProduct.shardName,
    ShardingProduct.props,
    ClusterShardingSettings(context.system),
    ShardingProduct.extractEntityId,
    ShardingProduct.extractShardId
  )

  override def receive: Receive = {
    case cmd: Command =>
      shardingProducts() forward cmd
    case x => log.info("unhandled COMMAND: {} {}", this, x)
  }
}
