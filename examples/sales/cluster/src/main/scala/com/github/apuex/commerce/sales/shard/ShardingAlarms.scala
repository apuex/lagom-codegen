/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.shard

import akka.actor._
import akka.cluster.sharding._
import com.github.apuex.commerce.sales._
import com.typesafe.config._
import scala.collection.convert.ImplicitConversions._


object ShardingAlarms {
  def props = Props[ShardingAlarms]
  def name: String = "ShardingAlarms"
}

class ShardingAlarms (config: Config) extends Actor with ActorLogging {

  ShardingAlarm.defaultNumberOfShards = config.getInt("sales.entity.number-of-shards")

  def shardingAlarms(): ActorRef = {
    ClusterSharding(context.system).shardRegion(ShardingAlarm.shardName)
  }

  ClusterSharding(context.system).start(
    ShardingAlarm.shardName,
    ShardingAlarm.props,
    ClusterShardingSettings(context.system),
    ShardingAlarm.extractEntityId,
    ShardingAlarm.extractShardId
  )

  override def receive: Receive = {
    case cmd: Command =>
      shardingAlarms() forward cmd
    case x => log.info("unhandled COMMAND: {} {}", this, x)
  }
}
