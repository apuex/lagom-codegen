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
import com.github.apuex.commerce.sales.impl.SalesCqAppLoader._
import com.github.apuex.commerce.sales.sharding._
import com.lightbend.lagom.scaladsl.client._
import com.lightbend.lagom.scaladsl.devmode._
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.Logger
import play.api.db._
import play.api.libs.ws.ahc._
import scalapb.GeneratedMessage

import scala.concurrent.duration.{Duration, FiniteDuration}

class SalesCqAppLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SalesCqApp(context) with ConfigurationServiceLocatorComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SalesCqApp(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[SalesService])
}

object SalesCqAppLoader {

  abstract class SalesCqApp(context: LagomApplicationContext)
    extends LagomApplication(context)
      with AhcWSComponents
      with DBComponents
      with HikariCPComponents {

    val logger = Logger(classOf[SalesCqAppLoader])

    // Bind the service that this server provides
    lazy val db = dbApi.database("sales-db")
    lazy val publishQueue = config.getString("sales.instant-event-publish-queue")
    lazy val duration = Duration(config.getString("db.sales-db.event.reschedule-duration")).asInstanceOf[FiniteDuration]
    lazy val mediator = DistributedPubSub(actorSystem).mediator
    lazy val daoModule = wire[DaoModule]
    lazy val clusterModule = wire[ClusterShardingModule]
    lazy val domainEventApply = wire[SalesDomainEventApply]
    lazy val queryEventApply = wire[SalesQueryEventApply]
    lazy val readJournal: EventsByTagQuery = PersistenceQuery(actorSystem)
      .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
    implicit val actorMaterializer = ActorMaterializer()(actorSystem)

    override lazy val lagomServer: LagomServer = serverFor[SalesService](wire[SalesServiceImpl])

    subscribeJournalEvents()

    private def subscribeJournalEvents(): Unit = {
      val offset: Option[String] = db.withTransaction { implicit c =>
        Some(daoModule.eventJournalDao.selectCurrentOffset().offsetTime)
      }

      if (logger.isInfoEnabled) {
        offset.map(x => logger.info(s"Starting from offset=${x}"))
      }

      readJournal
        .eventsByTag(
          "all",
          offset
            .map(x => {
              if (x.matches("^[\\+\\-]{0,1}[0-9]+$")) Offset.sequence(x.toLong)
              else if(x != "") Offset.timeBasedUUID(UUID.fromString(x))
              else Offset.timeBasedUUID(UUID.fromString(x))
            })
            .getOrElse(Offset.noOffset)
        )
        .filter(ee => ee.event.isInstanceOf[GeneratedMessage])
        .runForeach(ee => {
          db.withTransaction { implicit c =>
            ee.event match {
              case evt: Event =>
                daoModule.eventJournalDao.createEventJournal(
                  ee.offset match {
                    case Sequence(x) =>
                      CreateEventJournalEvent(evt.userId, x, evt.entityId, x.toString, x.getClass.getName, x.asInstanceOf[GeneratedMessage].toByteString)
                    case TimeBasedUUID(x) =>
                      CreateEventJournalEvent(evt.userId, 0L, evt.entityId, x.toString, x.getClass.getName, x.asInstanceOf[GeneratedMessage].toByteString)
                  })
                queryEventApply.dispatch(evt)
              case x: ValueObject =>
                mediator ! Publish(publishQueue, x)
              case _ =>
            }
          }
        })(actorMaterializer)
        .recover({
          case t: Throwable =>
            logger.error("journal events by tag failed: {}", t)
            actorSystem.scheduler.scheduleOnce(duration)(subscribeJournalEvents)
        })
    }
  }

}
