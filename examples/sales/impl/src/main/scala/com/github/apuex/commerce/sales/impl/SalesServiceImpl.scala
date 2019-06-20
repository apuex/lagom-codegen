package com.github.apuex.commerce.sales.impl

import akka._
import akka.stream.scaladsl._
import com.github.apuex.commerce.sales._
import com.lightbend.lagom.scaladsl.api._
import play.api.libs.json.Json

import scala.concurrent.Future

class SalesServiceImpl extends SalesService {

  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] = {
    ServiceCall { is =>
      Future.successful(is.map(x => x))
    }
  }
}
