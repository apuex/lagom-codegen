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
    xml.child
      .filter(x => x.label == "entity" && !(x.\@("transient") == "true"))
      .filter(x => "true" != x.\@("enum") && "" == x.\@("aggregatesTo") && journalTable != x.\@("name"))
      .sortWith((x, y) => depends(x, y))
      .map(x => toAggregate(x, xml))
      .map(generateClusterShardActor)
      .foreach(x => save(
        x._1,
        x._2,
        clusterSrcDir
      ))
    save(s"""${cToPascal(s"${cluster}_${shard}")}Module.scala""", wireModule(xml), clusterSrcDir)
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
         |  def props(config: Config) = Props(new ${facadeClassName}(config))
         |  def name: String = "${facadeClassName}"
         |}
         |
         |class ${facadeClassName} (config: Config) extends Actor with ActorLogging {
         |  val numberOfShards = config.getInt("${cToShell(modelName)}.entity.number-of-shards")
         |  val shardName: String = "${cToShell(s"${shard}_${name}")}"
         |
         |  val extractEntityId: ShardRegion.ExtractEntityId = {
         |    case cmd: Command =>
         |      (cmd.entityId.toString, cmd)
         |  }
         |
         |  val extractShardId: ShardRegion.ExtractShardId = {
         |    case cmd: Command =>
         |      (abs(cmd.entityId.hashCode) % numberOfShards).toString
         |    case ShardRegion.StartEntity(id) =>
         |      // StartEntity is used by remembering entities feature
         |      (abs(id.hashCode) % numberOfShards).toString
         |  }
         |
         |  ClusterSharding(context.system).start(
         |    shardName,
         |    ${className}.props(config),
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

  def wireModule(root: Node): String = {
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${clusterSrcPackage}
       |
       |import akka.actor._
       |import com.softwaremill.macwire._
       |import com.softwaremill.macwire.akkasupport._
       |import com.typesafe.config._
       |
       |@Module
       |class ${cToPascal(s"${cluster}_${shard}")}Module(system: ActorSystem, config: Config) {
       |  ${indent(wireClusterShards(root), 2)}
       |}
     """.stripMargin.trim
  }

  def wireClusterShards(root: Node): String = {
    root.child
      .filter(_.label == "entity")
      .filter(x => ("true" != x.\@("transient")))
      .filter(x => "true" != x.\@("enum") && "" == x.\@("aggregatesTo") && journalTable != x.\@("name"))
      .map(_.\@("name"))
      .map(x => s"${shard}_${x}s")
      .map(x => {
        s"""
           |lazy val ${cToCamel(x)} = wireActor[${cToPascal(x)}](${cToPascal(x)}.name)
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }
}
