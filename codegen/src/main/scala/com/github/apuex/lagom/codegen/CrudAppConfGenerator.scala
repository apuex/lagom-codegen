package com.github.apuex.lagom.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

object CrudAppConfGenerator {
  def apply(fileName: String): CrudAppConfGenerator = new CrudAppConfGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): CrudAppConfGenerator = new CrudAppConfGenerator(modelLoader)
}

class CrudAppConfGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    generateAppConf()
    generateLogConf()
    generateMessageConf()
    generateRoutesConf()
  }

  def generateAppConf(): Unit = {
    new File(crudAppProjectConfDir).mkdirs()
    val printWriter = new PrintWriter(s"${crudAppProjectConfDir}/application.conf", "utf-8")
    printWriter.println(
      s"""
         |#######################################################
         |##  This file is 100% ***GENERATED***, DO NOT EDIT!  ##
         |#######################################################
         |
         |# https://www.playframework.com/documentation/latest/Configuration
         |
         |${cToShell(modelName)} {
         |  instant-event-publish-queue = instant-event-publish-queue
         |  request-timeout = 30 seconds
         |}
         |
         |play {
         |  application {
         |    loader = "${implSrcPackage}.${cToPascal(s"${modelName}_${app}_${loader}")}"
         |  }
         |  http {
         |    secret {
         |      // TODO: replace it with your own key!
         |      key="cfd16c3a-f0f2-4fa9-8e58-ff9a2ad2a422"
         |      key=$${? APPLICATION_SECRET}
         |    }
         |  }
         |  filters {
         |    hosts {
         |      // TODO: replace it hosts allowed!
         |      allowed=["localhost", "${cToShell(modelName)}"]
         |    }
         |    headers {
         |      // TODO: replace it your own security options!
         |      frameOptions=null
         |      xssProtection=null
         |      contentTypeOptions=null
         |      permittedCrossDomainPolicies=null
         |      contentSecurityPolicy=null
         |    }
         |  }
         |  server {
         |    http {
         |      port = 9000
         |    }
         |  }
         |  akka {
         |    actor-system = "${cToShell(modelName)}"
         |  }
         |}
         |
         |akka {
         |  loggers = ["akka.event.slf4j.Slf4jLogger"]
         |  loglevel = "INFO"
         |  log-config-on-start = off
         |  log-dead-letters = 0
         |  log-dead-letters-during-shutdown = off
         |
         |  actor {
         |    provider = "akka.cluster.ClusterActorRefProvider"
         |
         |    serializers {
         |      ${cToShell(modelName)}-protobuf = "akka.remote.serialization.ProtobufSerializer"
         |    }
         |
         |    serialization-bindings {
         |      "java.io.Serializable" = none
         |      // scalapb 0.8.4
         |      "scalapb.GeneratedMessage" = ${cToShell(modelName)}-protobuf
         |      // google protobuf-java 3.6.1
         |      "com.google.protobuf.GeneratedMessageV3" = ${cToShell(modelName)}-protobuf
         |    }
         |  }
         |
         |//  remote {
         |//    startup-timeout = 60 s
         |//
         |//    netty.tcp {
         |//      hostname = "localhost"      // default to the first seed node
         |//      port = 2553                 // default port
         |//      hostname = $${? HOSTNAME}   // override with -DHOSTNAME
         |//      port = $${? PORT}           // override with -DPORT
         |//    }
         |//  }
         |//
         |//  cluster {
         |//    seed-nodes = [
         |//      "akka.tcp://${cToShell(modelName)}@localhost:2553",
         |//    ]
         |//  }
         |}
         |
         |db {
         |  ${cToShell(modelDbSchema)}-db {
         |    driver = com.mysql.cj.jdbc.Driver
         |    dbhost = "mysql"
         |    dbhost = $${? DBHOST}
         |    url = "jdbc:mysql://"$${db.${cToShell(modelDbSchema)}-db.dbhost}"/${modelDbSchema}?characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&verifyServerCertificate=false"
         |    username = ${modelDbSchema}
         |    password = password
         |    event {
         |      query-interval = 3 seconds
         |    }
         |  }
         |}
         |
       """.stripMargin
        .trim)
    printWriter.close()
  }

  def generateLogConf(): Unit = {
    new File(crudAppProjectConfDir).mkdirs()
    val printWriter = new PrintWriter(s"${crudAppProjectConfDir}/logback.xml", "utf-8")
    printWriter.println(
      s"""
         |<!--
         |/*****************************************************
         | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
         | *****************************************************/
         | -->
         |<!-- https://www.playframework.com/documentation/latest/SettingsLogger -->
         |<configuration>
         |
         |  <conversionRule conversionWord="coloredLevel" converterClass="play.api.libs.logback.ColoredLevel" />
         |
         |  <!--
         |  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
         |    <file>$${application.home :-.}/logs/application.log</file>
         |    <encoder>
         |      <pattern>%date [%level] from %logger in %thread - %message%n%xException</pattern>
         |    </encoder>
         |  </appender>
         |  -->
         |
         |  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
         |    <encoder>
         |      <!--
         |      <pattern>%coloredLevel %logger{15} - %message%n%xException{10}</pattern>
         |      -->
         |      <pattern>%date [%level] from %logger in %thread - %message%n%xException</pattern>
         |    </encoder>
         |  </appender>
         |
         |  <!--
         |  <appender name="ASYNCFILE" class="ch.qos.logback.classic.AsyncAppender">
         |    <appender-ref ref="FILE" />
         |  </appender>
         |  -->
         |
         |  <appender name="ASYNCSTDOUT" class="ch.qos.logback.classic.AsyncAppender">
         |    <appender-ref ref="STDOUT" />
         |  </appender>
         |
         |  <logger name="play" level="INFO" />
         |  <logger name="akka" level="INFO" />
         |  <logger name="application" level="INFO" />
         |  <logger name="${modelPackage}" level="INFO" />
         |
         |  <root level="INFO">
         |    <appender-ref ref="ASYNCSTDOUT" />
         |    <!--
         |    <appender-ref ref="ASYNCFILE" />
         |    -->
         |  </root>
         |
         |</configuration>
       """.stripMargin
        .trim)
    printWriter.close()
  }

  def generateMessageConf(): Unit = {
    new File(crudAppProjectConfDir).mkdirs()
    val printWriter = new PrintWriter(s"${crudAppProjectConfDir}/messages", "utf-8")
    printWriter.println(
      s"""
         |#######################################################
         |##  This file is 100% ***GENERATED***, DO NOT EDIT!  ##
         |#######################################################
         |
         |# https://www.playframework.com/documentation/latest/ScalaI18N
       """.stripMargin
        .trim)
    printWriter.close()
  }

  def generateRoutesConf(): Unit = {
    new File(crudAppProjectConfDir).mkdirs()
    val printWriter = new PrintWriter(s"${crudAppProjectConfDir}/routes", "utf-8")
    printWriter.println(
      s"""
         |#######################################################
         |##  This file is 100% ***GENERATED***, DO NOT EDIT!  ##
         |#######################################################
         |
         |# Routes
         |# This file defines all application routes (Higher priority routes first)
         |# https://www.playframework.com/documentation/latest/ScalaRouting
         |# ~~~~
         |
         |# Map static resources from the /public folder to the /assets URL path
         |GET     /assets/*file               controllers.Assets.versioned(path="/public", file: Asset)
       """.stripMargin
        .trim)
    printWriter.close()
  }
}
