package com.github.apuex.commerce.sales

import com.github.apuex.commerce.sales._
import akka._
import akka.stream.scaladsl._
import com.lightbend.lagom.scaladsl.api._
import play.api.libs.json.Json

trait SalesService extends Service {

  

  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override def descriptor: Descriptor = {
    import Service._
    import ScalapbJson._

    

    named("sales")
      .withCalls(
        
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
}
