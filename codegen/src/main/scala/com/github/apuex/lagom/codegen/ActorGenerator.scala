package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

object ActorGenerator {
  def apply(fileName: String): ActorGenerator = new ActorGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): ActorGenerator = new ActorGenerator(modelLoader)
}

class ActorGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  val commandGen = ActorCommandPatternsGenerator(modelLoader)
  val eventsGen = ActorEventPatternsGenerator(modelLoader)

  def generate(): Unit = {
    xml.child.filter(_.label == "entity")
      .filter(x => "true" != x.\@("enum") && "" == x.\@("aggregatesTo") && journalTable != x.\@("name"))
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
         |import akka.actor._
         |import akka.event._
         |import akka.pattern._
         |import akka.persistence._
         |import akka.util.Timeout._
         |import akka.util._
         |import ${messageSrcPackage}._
         |import com.google.protobuf.timestamp.Timestamp
         |import com.typesafe.config._
         |
         |import scala.concurrent.ExecutionContext
         |import scala.concurrent.duration._
         |
         |
         |object ${className} {
         |  def props = Props[${className}]
         |  def name: String = "${className}"
         |}
         |
         |class ${className} (config: Config) extends PersistentActor with ActorLogging {
         |  override def persistenceId: String = s"$${self.path.name}"
         |  implicit def requestTimeout: Timeout = Duration(config.getString("db.${cToShell(modelDbSchema)}-db.event.query-interval")).asInstanceOf[FiniteDuration]
         |  implicit def executionContext: ExecutionContext = context.dispatcher
         |
         |  ${indent(defEntityFields(fields), 2)}
         |
         |  override def receiveRecover: Receive = {
         |    case evt: Event =>
         |      updateState(evt)
         |    case SnapshotOffer(_, x: ${cToPascal(name)}Vo) =>
         |      ${indent(updateFields(fields, "x"), 6)}
         |    case _: RecoveryCompleted =>
         |    case x => log.info("RECOVER: {} {}", this, x)
         |  }
         |
         |  override def receiveCommand: Receive = {
         |    ${indent(commandPatterns(aggregate), 4)}
         |
         |    case evt: ${cToPascal(name)}Event =>
         |      persist(evt)(updateState)
         |
         |    case x => log.info("UNHANDLED: {} {}", this, x)
         |  }
         |
         |  private def updateState: (Event => Unit) = {
         |    ${indent(eventPatterns(aggregate), 4)}
         |
         |    case x => log.info("UN-UPDATED: {} {}", this, x)
         |  }
         |
         |  private def isValid(): Boolean = {
         |    ${checkValid(primaryKey)}
         |  }
         |
         |  private def replyToSender(msg: Any) = {
         |    if ("deadLetters" != sender().path.name) sender() ! msg
         |  }
         |}
       """.stripMargin.trim

    (fileName, content)
  }

  def commandPatterns(aggregate: Aggregate): String = {
    commandGen.generateCallsForAggregate(aggregate)
  }

  def eventPatterns(aggregate: Aggregate): String = {
    eventsGen.generateCallsForAggregate(aggregate)
  }

  def checkValid(primaryKey: PrimaryKey): String = {
    primaryKey.fields
      .map(x => s"${cToCamel(x.name)} != ${defaultValue(x)}")
      .reduceOption((l, r) => s"${l} && ${r}")
      .getOrElse("")
  }
}
