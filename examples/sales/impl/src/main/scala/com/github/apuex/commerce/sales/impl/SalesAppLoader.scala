/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.impl

import java.util.{Date, UUID}

import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.persistence.query.scaladsl.EventsByTagQuery
import akka.persistence.query._
import akka.stream.ActorMaterializer
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao.mysql._
import com.github.apuex.commerce.sales.impl.SalesAppLoader._
import com.github.apuex.commerce.sales.sharding._
import com.github.apuex.springbootsolution.runtime.DateFormat.toScalapbTimestamp
import com.lightbend.lagom.scaladsl.client._
import com.lightbend.lagom.scaladsl.devmode._
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.Logger
import play.api.db._
import play.api.libs.ws.ahc._
import scalapb.GeneratedMessage

import scala.concurrent.duration.{Duration, FiniteDuration}

class SalesAppLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SalesApp(context) with ConfigurationServiceLocatorComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SalesApp(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[SalesService])
}

object SalesAppLoader {

  abstract class SalesApp(context: LagomApplicationContext)
    extends LagomApplication(context)
      with AhcWSComponents
      with DBComponents
      with HikariCPComponents {

    val logger = Logger(classOf[SalesAppLoader])

    // Bind the service that this server provides
    lazy val db = dbApi.database("sales-db")
    lazy val publishQueue = "instant-event-publish-queue"
    implicit val duration = Duration(config.getString("db.sales-db.event.query-interval")).asInstanceOf[FiniteDuration]
    lazy val mediator = DistributedPubSub(actorSystem).mediator
    lazy val daoModule = wire[DaoModule]
    lazy val clusterModule = wire[ClusterShardingModule]
    lazy val domainEventApply = wire[SalesDomainEventApply]
    lazy val queryEventApply = wire[SalesQueryEventApply]
    lazy val readJournal: EventsByTagQuery = PersistenceQuery(actorSystem)
      .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
    implicit val actorMaterializer = ActorMaterializer()(actorSystem)

    override lazy val lagomServer: LagomServer = serverFor[SalesService](wire[SalesServiceImpl])

    val offset: Option[String] = db.withTransaction { implicit c =>
      Some(daoModule.eventJournalDao.selectCurrentOffset().toString)
    }

    if(logger.isInfoEnabled) {
      offset.map(x => logger.info(s"Starting from offset=${x}"))
    }

    readJournal
      .eventsByTag(
        "all",
        offset
          .map(x => {
            if (x.matches("^[\\+\\-]{0,1}[0-9]+$")) Offset.sequence(x.toLong)
            else Offset.timeBasedUUID(UUID.fromString(x))
          })
          .getOrElse(Offset.noOffset)
      )
      .filter(ee => ee.event.isInstanceOf[GeneratedMessage])
      .runForeach(ee => {
          db.withTransaction { implicit c =>
            ee.event match {
              case x: Event =>
                daoModule.eventJournalDao.createEventJournal(
                  CreateEventJournalEvent(x.userId, ee.offset match {
                    case Sequence(x) => x
                    // case TimeBasedUUID(x) => x
                  }, x.entityId, Some(toScalapbTimestamp(new Date())), x.getClass.getName, x.asInstanceOf[GeneratedMessage].toByteString)
                )
                queryEventApply.dispatch(x)
              case x: ValueObject =>
                mediator ! Publish(publishQueue, x)
              case _ =>
            }
          }
      })(actorMaterializer)
  }

}
