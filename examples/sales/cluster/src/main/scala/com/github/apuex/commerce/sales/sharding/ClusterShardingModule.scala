/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.sharding

import akka.actor._
import com.softwaremill.macwire._
import com.softwaremill.macwire.akkasupport._
import com.typesafe.config._

@Module
class ClusterShardingModule(system: ActorSystem, config: Config) {
  lazy val shardingAlarms = wireActor[ShardingAlarms](ShardingAlarms.name)

  lazy val shardingProducts = wireActor[ShardingProducts](ShardingProducts.name)

  lazy val shardingOrders = wireActor[ShardingOrders](ShardingOrders.name)
}
