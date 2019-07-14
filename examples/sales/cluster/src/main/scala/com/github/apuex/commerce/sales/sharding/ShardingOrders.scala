/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.sharding

import akka.actor._
import akka.cluster.sharding._
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.domain._
import com.typesafe.config._

import scala.math.Numeric.IntIsIntegral._

object ShardingOrders {
  def props(config: Config) = Props(new ShardingOrders(config))
  def name: String = "ShardingOrders"
}

class ShardingOrders (config: Config) extends Actor with ActorLogging {
  val numberOfShards = config.getInt("sales.entity.number-of-shards")
  val shardName: String = "sharding-order"

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case cmd: Command =>
      (cmd.entityId.toString, cmd)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: Command =>
      (abs(cmd.entityId.hashCode) % numberOfShards).toString
    case ShardRegion.StartEntity(id) =>
      // StartEntity is used by remembering entities feature
      (abs(id.hashCode) % numberOfShards).toString
  }

  ClusterSharding(context.system).start(
    shardName,
    OrderActor.props(config),
    ClusterShardingSettings(context.system),
    extractEntityId,
    extractShardId
  )

  def sharding(): ActorRef = {
    ClusterSharding(context.system).shardRegion(shardName)
  }

  override def receive: Receive = {
    case cmd: Command =>
      sharding() forward cmd
    case x => log.info("unhandled COMMAND: {} {}", this, x)
  }
}
