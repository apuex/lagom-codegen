package com.github.apuex.lagom.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

object ApplicationLoaderGenerator {
  def apply(fileName: String): ApplicationLoaderGenerator = new ApplicationLoaderGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): ApplicationLoaderGenerator = new ApplicationLoaderGenerator(modelLoader)
}

class ApplicationLoaderGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  val mappingName = (s"${modelName}")
  val serviceName = (s"${mappingName}_${service}")
  val serviceImplName = (s"${serviceName}_${impl}")
  val appName = (s"${mappingName}_${app}")
  val loaderName = (s"${appName}_${loader}")

  val appLoader =
    s"""
       |package ${implSrcPackage}
       |
       |import akka.actor.Props
       |import ${apiSrcPackage}._
       |import ${implSrcPackage}.${cToPascal(loaderName)}._
       |import ${apiSrcPackage}.ScalapbJson._
       |import com.lightbend.lagom.scaladsl.client._
       |import com.lightbend.lagom.scaladsl.devmode._
       |import com.lightbend.lagom.scaladsl.playjson._
       |import com.lightbend.lagom.scaladsl.server._
       |import com.softwaremill.macwire._
       |import play.api.libs.ws.ahc.AhcWSComponents
       |
       |import scala.collection.immutable.Seq
       |
       |class ${cToPascal(loaderName)} extends LagomApplicationLoader {
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
       |object ${cToPascal(loaderName)} {
       |  abstract class ${cToPascal(appName)}(context: LagomApplicationContext)
       |    extends LagomApplication(context)
       |      with AhcWSComponents {
       |
       |    // Bind the service that this server provides
       |    override lazy val lagomServer: LagomServer = serverFor[${cToPascal(serviceName)}](wire[${cToPascal(serviceImplName)}])
       |  }
       |}
     """.stripMargin

  def generate(): Unit = {
    new File(implSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${implSrcDir}/${cToPascal(loaderName)}.scala", "utf-8")
    printWriter.println(appLoader)
    printWriter.close()
  }
}
