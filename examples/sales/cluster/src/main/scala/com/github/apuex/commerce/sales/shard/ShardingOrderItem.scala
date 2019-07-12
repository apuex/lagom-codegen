/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.shard

import akka.actor._
import akka.cluster.sharding._
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.domain._
import scala.collection.convert.ImplicitConversions._

import scala.math.Numeric.IntIsIntegral._

object ShardingOrderItem {
  def props = Props[ShardingOrderItem]
  def name: String = "ShardingOrderItem"

  val shardName: String = "ShardingOrderItem"
  var defaultNumberOfShards = 100

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case cmd: Command =>
      (cmd.entityId.toString, cmd)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: Command =>
      (abs(cmd.entityId.hashCode) % defaultNumberOfShards).toString
  }
}

class ShardingOrderItem () extends OrderItemActor () {
  override def unhandled(message: Any): Unit = message match {
    case x => log.info("unhandled COMMAND: {} {}", this, x)
  }
}
