package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._

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
  val crudAppName = (s"${modelName}_${app}")
  val crudAppLoaderName = (s"${crudAppName}_${loader}")

  val appLoader =
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${implSrcPackage}
       |
       |import akka.cluster.pubsub.DistributedPubSub
       |import akka.persistence.query.PersistenceQuery
       |import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
       |import akka.persistence.query.scaladsl.EventsByTagQuery
       |import ${apiSrcPackage}._
       |import ${apiSrcPackage}.${dao}.${mysql}._
       |import ${crudImplSrcPackage}.${cToPascal(crudAppLoaderName)}._
       |import ${apiSrcPackage}.${shard}._
       |import com.lightbend.lagom.scaladsl.client._
       |import com.lightbend.lagom.scaladsl.devmode._
       |import com.lightbend.lagom.scaladsl.server._
       |import com.softwaremill.macwire._
       |import play.api.db._
       |import play.api.libs.ws.ahc._
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
       |    // Bind the service that this server provides
       |    lazy val db = dbApi.database("${cToShell(modelDbSchema)}-db")
       |    lazy val publishQueue = "instant-event-publish-queue"
       |    implicit val duration = Duration(config.getString("db.${cToShell(modelDbSchema)}-db.event.query-interval")).asInstanceOf[FiniteDuration]
       |    lazy val mediator = DistributedPubSub(actorSystem).mediator
       |    lazy val daoModule = wire[DaoModule]
       |    lazy val clusterModule = wire[${cToPascal(s"${cluster}_${shard}")}Module]
       |    lazy val domainEventApply = wire[${cToPascal(s"${modelName}_${domain}_${event}_${apply}")}]
       |    lazy val queryEventApply = wire[${cToPascal(s"${modelName}_${query}_${event}_${apply}")}]
       |    lazy val readJournal: EventsByTagQuery = PersistenceQuery(actorSystem)
       |      .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
       |    override lazy val lagomServer: LagomServer = serverFor[${cToPascal(serviceName)}](wire[${cToPascal(serviceImplName)}])
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
       |    lazy val publishQueue = "instant-event-publish-queue"
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
      s"${cToPascal(crudAppLoaderName)}.scala",
      crudAppLoader,
      crudImplSrcDir
    )
  }
}
