package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils._

object AppLoaderGenerator {
  def apply(fileName: String): AppLoaderGenerator = new AppLoaderGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): AppLoaderGenerator = new AppLoaderGenerator(modelLoader)
}

class AppLoaderGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  val serviceName = (s"${modelName}_${service}")
  val serviceImplName = (s"${serviceName}_${impl}")
  val appName = (s"${modelName}_${app}")
  val appLoaderName = (s"${appName}_${loader}")
  val cqAppName = (s"${modelName}_${cq}_${app}")
  val cqAppLoaderName = (s"${cqAppName}_${loader}")
  val crudAppName = (s"${modelName}_${crud}_${app}")
  val crudAppLoaderName = (s"${crudAppName}_${loader}")

  val appLoader =
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${implSrcPackage}
       |
       |import java.util.{Date, UUID}
       |
       |import akka.cluster.pubsub.DistributedPubSub
       |import akka.cluster.pubsub.DistributedPubSubMediator.Publish
       |import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
       |import akka.persistence.query.scaladsl.EventsByTagQuery
       |import akka.persistence.query._
       |import akka.stream.ActorMaterializer
       |import ${apiSrcPackage}._
       |import ${apiSrcPackage}.${dao}.${mysql}._
       |import ${crudImplSrcPackage}.${cToPascal(appLoaderName)}._
       |import ${apiSrcPackage}.${shard}._
       |import com.lightbend.lagom.scaladsl.client._
       |import com.lightbend.lagom.scaladsl.devmode._
       |import com.lightbend.lagom.scaladsl.server._
       |import com.softwaremill.macwire._
       |import play.api.Logger
       |import play.api.db._
       |import play.api.libs.ws.ahc._
       |import scalapb.GeneratedMessage
       |
       |import scala.concurrent.duration.{Duration, FiniteDuration}
       |
       |class ${cToPascal(appLoaderName)} extends LagomApplicationLoader {
       |
       |  override def load(context: LagomApplicationContext): LagomApplication =
       |    new ${cToPascal(appName)}(context) with ConfigurationServiceLocatorComponents
       |
       |  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
       |    new ${cToPascal(appName)}(context) with LagomDevModeComponents
       |
       |  override def describeService = Some(readDescriptor[${cToPascal(serviceName)}])
       |}
       |
       |object ${cToPascal(appLoaderName)} {
       |
       |  abstract class ${cToPascal(appName)}(context: LagomApplicationContext)
       |    extends LagomApplication(context)
       |      with AhcWSComponents
       |      with DBComponents
       |      with HikariCPComponents {
       |
       |    val logger = Logger(classOf[${cToPascal(appLoaderName)}])
       |
       |    // Bind the service that this server provides
       |    lazy val db = dbApi.database("${cToShell(modelDbSchema)}-db")
       |    lazy val publishQueue = config.getString("${cToShell(modelName)}.instant-event-publish-queue")
       |    lazy val duration = Duration(config.getString("db.${cToShell(modelDbSchema)}-db.event.reschedule-duration")).asInstanceOf[FiniteDuration]
       |    lazy val mediator = DistributedPubSub(actorSystem).mediator
       |    lazy val daoModule = wire[DaoModule]
       |    lazy val clusterModule = wire[${cToPascal(s"${cluster}_${shard}")}Module]
       |    lazy val domainEventApply = wire[${cToPascal(s"${modelName}_${domain}_${event}_${apply}")}]
       |    lazy val queryEventApply = wire[${cToPascal(s"${modelName}_${query}_${event}_${apply}")}]
       |    lazy val readJournal: EventsByTagQuery = PersistenceQuery(actorSystem)
       |      .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
       |    implicit val actorMaterializer = ActorMaterializer()(actorSystem)
       |
       |    override lazy val lagomServer: LagomServer = serverFor[${cToPascal(serviceName)}](wire[${cToPascal(serviceImplName)}])
       |
       |    subscribeJournalEvents()
       |
       |    private def subscribeJournalEvents(): Unit = {
       |      val offset: Option[String] = db.withTransaction { implicit c =>
       |        Some(daoModule.${cToCamel(journalTable)}Dao.selectCurrentOffset().offsetTime)
       |      }
       |
       |      if (logger.isInfoEnabled) {
       |        offset.map(x => logger.info(s"Starting from offset=$${x}"))
       |      }
       |
       |      readJournal
       |        .eventsByTag(
       |          "all",
       |          offset
       |            .map(x => {
       |              if (x.matches("^[\\\\+\\\\-]{0,1}[0-9]+$$")) Offset.sequence(x.toLong)
       |              else if(x != "") Offset.timeBasedUUID(UUID.fromString(x))
       |              else Offset.timeBasedUUID(UUID.fromString(x))
       |            })
       |            .getOrElse(Offset.noOffset)
       |        )
       |        .filter(ee => ee.event.isInstanceOf[GeneratedMessage])
       |        .runForeach(ee => {
       |          db.withTransaction { implicit c =>
       |            ee.event match {
       |              case evt: Event =>
       |                daoModule.eventJournalDao.createEventJournal(
       |                  ee.offset match {
       |                    case Sequence(x) =>
       |                      CreateEventJournalEvent(evt.userId, x, evt.entityId, x.toString, x.getClass.getName, x.asInstanceOf[GeneratedMessage].toByteString)
       |                    case TimeBasedUUID(x) =>
       |                      CreateEventJournalEvent(evt.userId, 0L, evt.entityId, x.toString, x.getClass.getName, x.asInstanceOf[GeneratedMessage].toByteString)
       |                  })
       |                queryEventApply.dispatch(evt)
       |              case x: ValueObject =>
       |                mediator ! Publish(publishQueue, x)
       |              case _ =>
       |            }
       |          }
       |        })(actorMaterializer)
       |        .recover({
       |          case t: Throwable =>
       |            logger.error("journal events by tag failed: {}", t)
       |            actorSystem.scheduler.scheduleOnce(duration)(subscribeJournalEvents)
       |        })
       |    }
       |  }
       |
       |}
     """.stripMargin.trim

  val cqAppLoader =
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${implSrcPackage}
       |
       |import java.util.{Date, UUID}
       |
       |import akka.cluster.pubsub.DistributedPubSub
       |import akka.cluster.pubsub.DistributedPubSubMediator.Publish
       |import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
       |import akka.persistence.query.scaladsl.EventsByTagQuery
       |import akka.persistence.query._
       |import akka.stream.ActorMaterializer
       |import ${apiSrcPackage}._
       |import ${apiSrcPackage}.${dao}.${mysql}._
       |import ${crudImplSrcPackage}.${cToPascal(cqAppLoaderName)}._
       |import ${apiSrcPackage}.${shard}._
       |import com.lightbend.lagom.scaladsl.client._
       |import com.lightbend.lagom.scaladsl.devmode._
       |import com.lightbend.lagom.scaladsl.server._
       |import com.softwaremill.macwire._
       |import play.api.Logger
       |import play.api.db._
       |import play.api.libs.ws.ahc._
       |import scalapb.GeneratedMessage
       |
       |import scala.concurrent.duration.{Duration, FiniteDuration}
       |
       |class ${cToPascal(cqAppLoaderName)} extends LagomApplicationLoader {
       |
       |  override def load(context: LagomApplicationContext): LagomApplication =
       |    new ${cToPascal(cqAppName)}(context) with ConfigurationServiceLocatorComponents
       |
       |  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
       |    new ${cToPascal(cqAppName)}(context) with LagomDevModeComponents
       |
       |  override def describeService = Some(readDescriptor[${cToPascal(serviceName)}])
       |}
       |
       |object ${cToPascal(cqAppLoaderName)} {
       |
       |  abstract class ${cToPascal(cqAppName)}(context: LagomApplicationContext)
       |    extends LagomApplication(context)
       |      with AhcWSComponents
       |      with DBComponents
       |      with HikariCPComponents {
       |
       |    val logger = Logger(classOf[${cToPascal(cqAppLoaderName)}])
       |
       |    // Bind the service that this server provides
       |    lazy val db = dbApi.database("${cToShell(modelDbSchema)}-db")
       |    lazy val publishQueue = config.getString("${cToShell(modelName)}.instant-event-publish-queue")
       |    lazy val duration = Duration(config.getString("db.${cToShell(modelDbSchema)}-db.event.reschedule-duration")).asInstanceOf[FiniteDuration]
       |    lazy val mediator = DistributedPubSub(actorSystem).mediator
       |    lazy val daoModule = wire[DaoModule]
       |    lazy val clusterModule = wire[${cToPascal(s"${cluster}_${shard}")}Module]
       |    lazy val domainEventApply = wire[${cToPascal(s"${modelName}_${domain}_${event}_${apply}")}]
       |    lazy val queryEventApply = wire[${cToPascal(s"${modelName}_${query}_${event}_${apply}")}]
       |    lazy val readJournal: EventsByTagQuery = PersistenceQuery(actorSystem)
       |      .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
       |    implicit val actorMaterializer = ActorMaterializer()(actorSystem)
       |
       |    override lazy val lagomServer: LagomServer = serverFor[${cToPascal(serviceName)}](wire[${cToPascal(serviceImplName)}])
       |
       |    subscribeJournalEvents()
       |
       |    private def subscribeJournalEvents(): Unit = {
       |      val offset: Option[String] = db.withTransaction { implicit c =>
       |        Some(daoModule.${cToCamel(journalTable)}Dao.selectCurrentOffset().offsetTime)
       |      }
       |
       |      if (logger.isInfoEnabled) {
       |        offset.map(x => logger.info(s"Starting from offset=$${x}"))
       |      }
       |
       |      readJournal
       |        .eventsByTag(
       |          "all",
       |          offset
       |            .map(x => {
       |              if (x.matches("^[\\\\+\\\\-]{0,1}[0-9]+$$")) Offset.sequence(x.toLong)
       |              else if(x != "") Offset.timeBasedUUID(UUID.fromString(x))
       |              else Offset.timeBasedUUID(UUID.fromString(x))
       |            })
       |            .getOrElse(Offset.noOffset)
       |        )
       |        .filter(ee => ee.event.isInstanceOf[GeneratedMessage])
       |        .runForeach(ee => {
       |          db.withTransaction { implicit c =>
       |            ee.event match {
       |              case evt: Event =>
       |                daoModule.eventJournalDao.createEventJournal(
       |                  ee.offset match {
       |                    case Sequence(x) =>
       |                      CreateEventJournalEvent(evt.userId, x, evt.entityId, x.toString, x.getClass.getName, x.asInstanceOf[GeneratedMessage].toByteString)
       |                    case TimeBasedUUID(x) =>
       |                      CreateEventJournalEvent(evt.userId, 0L, evt.entityId, x.toString, x.getClass.getName, x.asInstanceOf[GeneratedMessage].toByteString)
       |                  })
       |                queryEventApply.dispatch(evt)
       |              case x: ValueObject =>
       |                mediator ! Publish(publishQueue, x)
       |              case _ =>
       |            }
       |          }
       |        })(actorMaterializer)
       |        .recover({
       |          case t: Throwable =>
       |            logger.error("journal events by tag failed: {}", t)
       |            actorSystem.scheduler.scheduleOnce(duration)(subscribeJournalEvents)
       |        })
       |    }
       |  }
       |
       |}
     """.stripMargin.trim

  val crudAppLoader =
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${implSrcPackage}
       |
       |import akka.cluster.pubsub.DistributedPubSub
       |import ${apiSrcPackage}._
       |import ${apiSrcPackage}.${dao}.${mysql}._
       |import ${crudImplSrcPackage}.${cToPascal(crudAppLoaderName)}._
       |import com.lightbend.lagom.scaladsl.client._
       |import com.lightbend.lagom.scaladsl.devmode._
       |import com.lightbend.lagom.scaladsl.server._
       |import com.softwaremill.macwire._
       |import play.api.db._
       |import play.api.libs.ws.ahc._
       |
       |import scala.concurrent.duration.{Duration, FiniteDuration}
       |
       |class ${cToPascal(crudAppLoaderName)} extends LagomApplicationLoader {
       |
       |  override def load(context: LagomApplicationContext): LagomApplication =
       |    new ${cToPascal(crudAppName)}(context) with ConfigurationServiceLocatorComponents
       |
       |  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
       |    new ${cToPascal(crudAppName)}(context) with LagomDevModeComponents
       |
       |  override def describeService = Some(readDescriptor[${cToPascal(serviceName)}])
       |}
       |
       |object ${cToPascal(crudAppLoaderName)} {
       |
       |  abstract class ${cToPascal(crudAppName)}(context: LagomApplicationContext)
       |    extends LagomApplication(context)
       |      with AhcWSComponents
       |      with DBComponents
       |      with HikariCPComponents {
       |
       |    // Bind the service that this server provides
       |    lazy val db = dbApi.database("${cToShell(modelDbSchema)}-db")
       |    lazy val publishQueue = config.getString("${cToShell(modelName)}.instant-event-publish-queue")
       |    implicit val duration = Duration(config.getString("db.${cToShell(modelDbSchema)}-db.event.query-interval")).asInstanceOf[FiniteDuration]
       |    lazy val mediator = DistributedPubSub(actorSystem).mediator
       |    lazy val daoModule = wire[DaoModule]
       |    lazy val eventApply = wire[${cToPascal(s"${modelName}_${query}_${event}_${apply}")}]
       |    override lazy val lagomServer: LagomServer = serverFor[${cToPascal(serviceName)}](wire[${cToPascal(serviceImplName)}])
       |  }
       |
       |}
     """.stripMargin.trim

  def generate(): Unit = {
    save(
      s"${cToPascal(appLoaderName)}.scala",
      appLoader,
      implSrcDir
    )
    save(
      s"${cToPascal(cqAppLoaderName)}.scala",
      cqAppLoader,
      implSrcDir
    )
    save(
      s"${cToPascal(crudAppLoaderName)}.scala",
      crudAppLoader,
      crudImplSrcDir
    )
  }

  def offsetPattern(): String = {
    modelXml.child.filter(x => "entity" == x.label && journalTable == x.\@("name"))
      .flatMap(_.child.filter(x => x.label == "field" && "offset" == x.\@("name")))
      .map(x => x.\@("type") match {
        case "long" =>
          s"""
             |case Sequence(x) => x.toString
             |case TimeBasedUUID(x) => x.toString
           """.stripMargin.trim

        case "uuid" =>
          s"""
             |case Sequence(x) => x.toString
             |case TimeBasedUUID(x) => x.toString
           """.stripMargin.trim

      })
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }
}
