package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

object CrudServiceGenerator {
  def apply(fileName: String): CrudServiceGenerator = new CrudServiceGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): CrudServiceGenerator = new CrudServiceGenerator(modelLoader)
}

class CrudServiceGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    save(
      s"${cToPascal(s"${modelName}_${service}")}.scala",
      generateService(),
      apiSrcDir
    )
    save(
      s"${cToPascal(s"${modelName}_${service}_${impl}")}.scala",
      generateServiceImpl(),
      crudImplSrcDir
    )
  }

  def generateService(): String = {
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${apiSrcPackage}
       |
       |import ${messageSrcPackage}._
       |import akka._
       |import akka.stream.scaladsl._
       |import com.lightbend.lagom.scaladsl.api._
       |import play.api.libs.json.Json
       |
       |trait ${cToPascal(modelName)}Service extends Service {
       |
       |  ${indent(calls(), 2)}
       |
       |  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
       |
       |  override def descriptor: Descriptor = {
       |    import Service._
       |    import ScalapbJson._
       |
       |    ${indent(callJsonFormats(), 4)}
       |
       |    named("${cToShell(modelName)}")
       |      .withCalls(
       |        ${indent(callDescs(), 8)}
       |        pathCall("/api/events?offset", events _)
       |      ).withAutoAcl(true)
       |  }
       |}
     """.stripMargin.trim
  }

  def generateServiceImpl(): String = {
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${crudImplSrcPackage}
       |
       |import akka._
       |import akka.stream.scaladsl._
       |import ${messageSrcPackage}._
       |import com.lightbend.lagom.scaladsl.api._
       |import play.api.db.Database
       |import play.api.libs.json.Json
       |
       |import scala.concurrent.Future
       |
       |class ${cToPascal(modelName)}ServiceImpl (db: Database) extends ${cToPascal(modelName)}Service {
       |
       |  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] = {
       |    ServiceCall { is =>
       |      Future.successful(is.map(x => x))
       |    }
       |  }
       |}
     """.stripMargin.trim
  }
  def calls(): String = ""
  def callJsonFormats(): String = ""
  def callDescs(): String = ""
}


