package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.xml.Node


object CqrsEventsAppGenerator {
  def apply(fileName: String): CqrsEventsAppGenerator = CqrsEventsAppGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): CqrsEventsAppGenerator = new CqrsEventsAppGenerator(modelLoader)
}

class CqrsEventsAppGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    val content = generateServiceImpl()
    save(
      s"${cToPascal(s"${modelName}_${event}_${apply}")}.scala",
      content,
      implSrcDir
    )
  }

  def generateServiceImpl(): String = {
    val constructorParams = Seq(
      "clusterShardingModule: ClusterShardingModule",
      "publishQueue: String",
      "mediator: ActorRef"
    )
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")

    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${crudImplSrcPackage}
       |
       |import akka.actor._
       |import akka.cluster.pubsub.DistributedPubSubMediator._
       |import ${messageSrcPackage}.ScalapbJson._
       |import ${messageSrcPackage}._
       |import ${messageSrcPackage}.${shard}._
       |import com.github.apuex.events.play.EventEnvelope
       |
       |
       |class ${cToPascal(s"${modelName}_${event}_${apply}")}(${indent(constructorParams, 2)}) {
       |
       |  import clusterShardingModule._
       |
       |  def on(ee: EventEnvelope): Any = {
       |    ee.event
       |      .map(unpack)
       |      .map({
       |        case x: Event =>
       |          dispatch(x)
       |        case x: ValueObject =>
       |          mediator ! Publish(publishQueue, x)
       |      })
       |  }
       |
       |  def dispatch(msg: Any): Any = msg match {
       |    ${indent(calls(), 4)}
       |    case _ => None
       |  }
       |}
     """.stripMargin.trim
  }

  def calls(): String = calls(xml)
    .filter(_ != "")
    .reduceOption((l, r) => s"${l}\n\n${r}")
    .getOrElse("")

  def calls(root: Node): Seq[String] = {
    root.child
      .filter(_.label == "entity")
      .filter(x => "true" != x.\@("enum") && "" == x.\@("aggregatesTo") && journalTable != x.\@("name"))
      .map(_.\@("name"))
      .map(x => {
        s"""
           |case evt: ${cToPascal(x)}Event =>
           |  ${cToCamel(shard)}${cToPascal(x)}s ! evt
     """.stripMargin.trim
      })
  }
}


