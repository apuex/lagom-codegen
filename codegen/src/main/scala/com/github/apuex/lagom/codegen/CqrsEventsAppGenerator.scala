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
  val crudGen = CrudEventsAppGenerator(modelLoader)

  def generate(): Unit = {
    save(
      s"${cToPascal(s"${modelName}_${domain}_${event}_${apply}")}.scala",
      generateServiceImpl(implSrcPackage),
      implSrcDir
    )
    save(
      s"${cToPascal(s"${modelName}_${query}_${event}_${apply}")}.scala",
      crudGen.generateServiceImpl(implSrcPackage),
      implSrcDir
    )
  }

  def generateServiceImpl(srcPackage: String): String = {
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
       |package ${srcPackage}
       |
       |import akka.actor._
       |import akka.cluster.pubsub.DistributedPubSubMediator._
       |import ${messageSrcPackage}._
       |import ${messageSrcPackage}.${shard}._
       |import scalapb.GeneratedMessage
       |
       |
       |class ${cToPascal(s"${modelName}_${domain}_${event}_${apply}")}(${indent(constructorParams, 2)}) {
       |
       |  import clusterShardingModule._
       |
       |  def on(event: GeneratedMessage): Any = {
       |    event match {
       |      case x: Event =>
       |        dispatch(x)
       |        mediator ! Publish(publishQueue, x)
       |      case x: ValueObject =>
       |        mediator ! Publish(publishQueue, x)
       |      case _ => None
       |    }
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
      .filter(x => ("true" != x.\@("transient")))
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


