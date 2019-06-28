/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.impl

import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.impl.SalesAppLoader._
import com.lightbend.lagom.scaladsl.client._
import com.lightbend.lagom.scaladsl.devmode._
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.db._
import play.api.libs.ws.ahc._

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
    override lazy val lagomServer: LagomServer = serverFor[SalesService](wire[SalesServiceImpl])
  }

}
