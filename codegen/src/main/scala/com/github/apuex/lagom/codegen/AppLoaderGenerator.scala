package com.github.apuex.lagom.codegen

import java.io.{File, PrintWriter}

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
       |import ${apiSrcPackage}._
       |import ${crudImplSrcPackage}.${cToPascal(crudAppLoaderName)}._
       |import com.lightbend.lagom.scaladsl.client._
       |import com.lightbend.lagom.scaladsl.devmode._
       |import com.lightbend.lagom.scaladsl.server._
       |import com.softwaremill.macwire._
       |import play.api.db._
       |import play.api.libs.ws.ahc._
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
       |    lazy val db = dbApi.database("${cToShell(modelName)}-db")
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
       |import ${apiSrcPackage}._
       |import ${crudImplSrcPackage}.${cToPascal(crudAppLoaderName)}._
       |import com.lightbend.lagom.scaladsl.client._
       |import com.lightbend.lagom.scaladsl.devmode._
       |import com.lightbend.lagom.scaladsl.server._
       |import com.softwaremill.macwire._
       |import play.api.db._
       |import play.api.libs.ws.ahc._
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
       |    lazy val db = dbApi.database("${cToShell(modelName)}-db")
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
