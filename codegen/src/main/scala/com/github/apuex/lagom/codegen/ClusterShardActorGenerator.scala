package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._

object ClusterShardActorGenerator {
  def apply(fileName: String): ClusterShardActorGenerator = new ClusterShardActorGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): ClusterShardActorGenerator = new ClusterShardActorGenerator(modelLoader)
}

class ClusterShardActorGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    xml.child.filter(_.label == "entity")
      .filter(x => "true" != x.\@("enum") && "" == x.\@("aggregatesTo"))
      .sortWith((x, y) => depends(x, y))
      .map(x => toAggregate(x, xml))
      .map(generateClusterShardActor)
      .foreach(x => save(
        x._1,
        x._2,
        clusterSrcDir
      ))
  }

  def generateClusterShardActor(aggregate: Aggregate): (String, String) = {
    import aggregate._

    val facadeClassName = s"${cToPascal(s"${shard}_${name}s")}"
    val className = s"${cToPascal(name)}Actor"
    val fileName = s"${facadeClassName}.scala"

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
         |import com.typesafe.config._
         |
         |import scala.math.Numeric.IntIsIntegral._
         |
         |object ${facadeClassName} {
         |  def props = Props[${facadeClassName}]
         |  def name: String = "${facadeClassName}"
         |}
         |
         |class ${facadeClassName} (config: Config) extends Actor with ActorLogging {
         |  val defaultNumberOfShards = config.getInt("sales.entity.number-of-shards")
         |  val shardName: String = "${cToShell(s"${shard}_${name}")}"
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
         |
         |  ClusterSharding(context.system).start(
         |    shardName,
         |    ${className}.props,
         |    ClusterShardingSettings(context.system),
         |    extractEntityId,
         |    extractShardId
         |  )
         |
         |  def ${shard}(): ActorRef = {
         |    ClusterSharding(context.system).shardRegion(shardName)
         |  }
         |
         |  override def receive: Receive = {
         |    case cmd: Command =>
         |      ${shard}() forward cmd
         |    case x => log.info("unhandled COMMAND: {} {}", this, x)
         |  }
         |}
       """.stripMargin.trim
    (fileName, content)
  }
}
