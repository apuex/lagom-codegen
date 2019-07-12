/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.shard

import akka.actor._
import akka.cluster.sharding._
import com.github.apuex.commerce.sales._
import com.typesafe.config._
import scala.collection.convert.ImplicitConversions._


object ShardingEventJournals {
  def props = Props[ShardingEventJournals]
  def name: String = "ShardingEventJournals"
}

class ShardingEventJournals (config: Config) extends Actor with ActorLogging {

  ShardingEventJournal.defaultNumberOfShards = config.getInt("sales.entity.number-of-shards")

  def shardingEventJournals(): ActorRef = {
    ClusterSharding(context.system).shardRegion(ShardingEventJournal.shardName)
  }

  ClusterSharding(context.system).start(
    ShardingEventJournal.shardName,
    ShardingEventJournal.props,
    ClusterShardingSettings(context.system),
    ShardingEventJournal.extractEntityId,
    ShardingEventJournal.extractShardId
  )

  override def receive: Receive = {
    case cmd: Command =>
      shardingEventJournals() forward cmd
    case x => log.info("unhandled COMMAND: {} {}", this, x)
  }
}
