package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.xml.Node

object ClusterShardActorGenerator {
  def apply(fileName: String): ClusterShardActorGenerator = new ClusterShardActorGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): ClusterShardActorGenerator = new ClusterShardActorGenerator(modelLoader)
}

class ClusterShardActorGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    xml.child.filter(_.label == "entity")
      .filter(x => "true" != x.\@("enum") && "true" != x.\@("aggregatesTo"))
      .sortWith((x, y) => depends(x, y))
      .map(x => toAggregate(x, xml))
      .map(generateClusterShardActor)
      .foreach(x => save(
        x._1,
        x._2,
        clusterSrcPackage
      ))
  }

  def generateClusterShardActor(aggregate: Aggregate): (String, String) = {
    import aggregate._

    val facadeClassName = s"Sharding${cToPascal(name)}s"
    val sharding = s"sharding${cToPascal(name)}s"
    val shardingClassName = s"Sharding${cToPascal(name)}"
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
         |package ${messageSrcPackage}._
         |import scala.collection.convert.ImplicitConversions._
         |
         |
         |object ${facadeClassName} {
         |  def props = Props[${facadeClassName}]
         |  def name: String = "${facadeClassName}"
         |}
         |
         |class ${facadeClassName} () extends Actor with ActorLogging {
         |
         |  val settings = Settings(context.system)
         |  ${shardingClassName}.defaultNumberOfShards = settings.entity.numberOfShards
         |
         |  def ${sharding}(): ActorRef = {
         |    ClusterSharding(context.system).shardRegion(${shardingClassName}.shardName)
         |  }
         |
         |  ClusterSharding(context.system).start(
         |    ${shardingClassName}.shardName,
         |    ${shardingClassName}.props,
         |    ClusterShardingSettings(context.system),
         |    ${shardingClassName}.extractEntityId,
         |    ${shardingClassName}.extractShardId
         |  )
         |
         |  override def receive: Receive = {
         |    case cmd: Command =>
         |      ${sharding}() forward cmd
         |    case x => log.info("unhandled COMMAND: {} {}", this, x)
         |  }
         |}
       """.stripMargin.trim
    (fileName, content)
  }
}
