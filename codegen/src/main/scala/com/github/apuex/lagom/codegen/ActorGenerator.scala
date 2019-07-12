package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.xml.Node

object ActorGenerator {
  def apply(fileName: String): ActorGenerator = new ActorGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): ActorGenerator = new ActorGenerator(modelLoader)
}

class ActorGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    xml.child.filter(_.label == "entity")
      .filter(x => "true" != x.\@("enum") && "" == x.\@("aggregatesTo"))
      .sortWith((x, y) => depends(x, y))
      .map(x => toAggregate(x, xml))
      .map(generateActor)
      .foreach(x => save(
        x._1,
        x._2,
        domainSrcDir
      ))
  }

  def generateActor(aggregate: Aggregate): (String, String) = {
    import aggregate._

    val className = s"${cToPascal(name)}Actor"
    val fileName = s"${className}.scala"

    val content =
      s"""
         |/*****************************************************
         | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
         | *****************************************************/
         |package ${domainSrcPackage}
         |
         |import ${messageSrcPackage}._
         |import com.google.protobuf.timestamp.Timestamp
         |import akka.actor._
         |import akka.event._
         |import akka.pattern._
         |import akka.persistence._
         |import akka.util._
         |import akka.util.Timeout._
         |
         |import scala.collection.convert.ImplicitConversions._
         |import scala.concurrent.ExecutionContext
         |import scala.concurrent.duration._
         |import scala.util._
         |
         |
         |object ${className} {
         |  def props = Props[${className}]
         |  def name: String = "${className}"
         |}
         |
         |class ${className} () extends PersistentActor with ActorLogging {
         |  override def persistenceId: String = s"$${self.path.name}"
         |  implicit def requestTimeout: Timeout = FiniteDuration(20, SECONDS)
         |  implicit def executionContext: ExecutionContext = context.dispatcher
         |
         |
         |  override def receiveRecover: Receive = {
         |    case evt: Event =>
         |      updateState(evt)
         |    case SnapshotOffer(_, x: ${cToPascal(name)}Vo) =>
         |    case x: RecoveryCompleted =>
         |    case x => log.info("RECOVER: {} {}", this, x)
         |  }
         |
         |  override def receiveCommand: Receive = {
         |    case x => log.info("UNHANDLED: {} {}", this, x)
         |  }
         |
         |  private def updateState: (Event => Unit) = {
         |    case x => log.info("UN-UPDATED: {} {}", this, x)
         |  }
         |
         |  private def isValid(): Boolean = {
         |    true
         |  }
         |
         |  private def replyToSender(msg: Any) = {
         |    if ("deadLetters" != sender().path.name) sender() ! msg
         |  }
         |}
       """.stripMargin.trim

    (fileName, content)
  }
}
