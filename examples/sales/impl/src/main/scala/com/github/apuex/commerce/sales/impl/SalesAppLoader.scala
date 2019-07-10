/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.impl

import java.util.concurrent.TimeUnit

import akka.cluster.pubsub.DistributedPubSub
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao.mysql._
import com.github.apuex.commerce.sales.impl.SalesAppLoader._
import com.lightbend.lagom.scaladsl.client._
import com.lightbend.lagom.scaladsl.devmode._
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.db._
import play.api.libs.ws.ahc._

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

    // Bind the service that this server provides
    lazy val db = dbApi.database("sales-db")
    lazy val publishQueue = "instant-event-publish-queue"
    implicit val duration = Duration(config.getString("db.sales-db.event.query-interval")).asInstanceOf[FiniteDuration]
    lazy val mediator = DistributedPubSub(actorSystem).mediator
    lazy val daoModule = wire[DaoModule]
    lazy val eventApply = wire[SalesEventApply]
    override lazy val lagomServer: LagomServer = serverFor[SalesService](wire[SalesServiceImpl])
  }

}
