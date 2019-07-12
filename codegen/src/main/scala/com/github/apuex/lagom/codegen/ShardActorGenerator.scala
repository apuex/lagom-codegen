package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.xml.Node

object ShardActorGenerator {
  def apply(fileName: String): ShardActorGenerator = new ShardActorGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): ShardActorGenerator = new ShardActorGenerator(modelLoader)
}

class ShardActorGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    xml.child.filter(_.label == "entity")
      .filter(x => "true" != x.\@("enum") && "" == x.\@("aggregatesTo") && journalTable != x.\@("name"))
      .sortWith((x, y) => depends(x, y))
      .map(x => toAggregate(x, xml))
      .map(generateShardActor)
      .foreach(x => save(
        x._1,
        x._2,
        clusterSrcDir
      ))
  }

  def generateShardActor(aggregate: Aggregate): (String, String) = {
    import aggregate._

    val className = s"${cToPascal(name)}Actor"
    val shardingClassName = s"${cToPascal(s"${shard}_${name}")}"
    val fileName = s"${shardingClassName}.scala"

    val content =
      s"""
         |/*****************************************************
         | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
         | *****************************************************/
         |package ${clusterSrcPackage}
         |
         |import akka.actor._
         |import akka.cluster.sharding._
         |import ${messageSrcPackage}._
         |import ${domainSrcPackage}._
         |import scala.collection.convert.ImplicitConversions._
         |
         |import scala.math.Numeric.IntIsIntegral._
         |
         |object ${shardingClassName} {
         |  def props = Props[${shardingClassName}]
         |  def name: String = "${shardingClassName}"
         |
         |  val shardName: String = "${shardingClassName}"
         |  var defaultNumberOfShards = 100
         |
         |  val extractEntityId: ShardRegion.ExtractEntityId = {
         |    case cmd: Command =>
         |      (cmd.entityId.toString, cmd)
         |  }
         |
         |  val extractShardId: ShardRegion.ExtractShardId = {
         |    case cmd: Command =>
         |      (abs(cmd.entityId.hashCode) % defaultNumberOfShards).toString
         |  }
         |}
         |
         |class ${shardingClassName} () extends ${className} () {
         |  override def unhandled(message: Any): Unit = message match {
         |    case x => log.info("unhandled COMMAND: {} {}", this, x)
         |  }
         |}
       """.stripMargin.trim
    (fileName, content)
  }
}
