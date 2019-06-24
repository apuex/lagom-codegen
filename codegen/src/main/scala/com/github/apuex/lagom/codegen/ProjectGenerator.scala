package com.github.apuex.lagom.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.runtime.SymbolConverters.cToShell

object ProjectGenerator {
  def apply(fileName: String): ProjectGenerator = new ProjectGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): ProjectGenerator = new ProjectGenerator(modelLoader)
}

class ProjectGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    rootProjectSettings()
    modelProjectSettings()
    messageProjectSettings()
    apiProjectSettings()
    daoProjectSettings()
    daoMysqlProjectSettings()
    implProjectSettings()
    crudImplProjectSettings()
    appProjectSettings()
    crudAppProjectSettings()
  }

  def modelProjectSettings(): Unit = {
    new File(modelProjectDir).mkdirs()
    val printWriter = new PrintWriter(s"${modelProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |
         |name         := "${modelProjectName}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |libraryDependencies ++= {
         |  Seq(
         |    codegenUtil    % Test,
         |    scalaTest      % Test
         |  )
         |}
       """.stripMargin.trim
    )
    printWriter.close()
  }

  def messageProjectSettings(): Unit = {
    new File(messageProjectDir).mkdirs()
    val printWriter = new PrintWriter(s"${messageProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |
         |name         := "${messageProjectName}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |libraryDependencies ++= {
         |  Seq(
         |    sbRuntime,
         |    scalapbRuntime % "protobuf",
         |    scalapbJson4s,
         |    scalaTest      % Test
         |  )
         |}
         |
         |PB.targets in Compile := Seq(
         |  scalapb.gen() -> (sourceManaged in Compile).value
         |)
       """.stripMargin.trim
    )
    printWriter.close()
  }

  def apiProjectSettings(): Unit = {
    new File(apiProjectDir).mkdirs()
    val printWriter = new PrintWriter(s"${apiProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |
         |name         := "${apiProjectName}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |libraryDependencies ++= {
         |  Seq(
         |    sbRuntime,
         |    playEvents,
         |    scalapbJson4s,
         |    scalaTest      % Test
         |  )
         |}
       """.stripMargin.trim
    )
    printWriter.close()
  }


  def daoProjectSettings(): Unit = {
    new File(daoProjectDir).mkdirs()
    val printWriter = new PrintWriter(s"${daoProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |
         |name         := "${daoProjectName}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |libraryDependencies ++= {
         |  Seq(
         |    jdbc,
         |    scalaTest      % Test
         |  )
         |}
       """.stripMargin.trim
    )
    printWriter.close()
  }

  def daoMysqlProjectSettings(): Unit = {
    new File(daoMysqlProjectDir).mkdirs()
    val printWriter = new PrintWriter(s"${daoMysqlProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |
         |name         := "${daoMysqlProjectName}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |libraryDependencies ++= {
         |  Seq(
         |    jdbc,
         |    playAnorm,
         |    mysqlDriver,
         |    scalaTest      % Test
         |  )
         |}
       """.stripMargin.trim
    )
    printWriter.close()
  }

  def implProjectSettings(): Unit = {
    new File(implSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${implProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |
         |name         := "${implProjectName}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |libraryDependencies ++= {
         |  Seq(
         |    playEvents,
         |    akkaPersistence,
         |    akkaPersistenceQuery,
         |    akkaClusterSharding,
         |    macwire        % Provided,
         |    scalaTest      % Test
         |  )
         |}
       """.stripMargin.trim
    )
    printWriter.close()
  }

  def crudImplProjectSettings(): Unit = {
    new File(crudImplProjectDir).mkdirs()
    val printWriter = new PrintWriter(s"${crudImplProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |
         |name         := "${crudImplProjectName}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |libraryDependencies ++= {
         |  Seq(
         |    playEvents,
         |    akkaPersistence,
         |    akkaPersistenceQuery,
         |    akkaClusterSharding,
         |    macwire        % Provided,
         |    scalaTest      % Test
         |  )
         |}
       """.stripMargin.trim
    )
    printWriter.close()
  }

  def appProjectSettings(): Unit = {
    new File(appProjectDir).mkdirs()
    val printWriter = new PrintWriter(s"${appProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |import sbtassembly.MergeStrategy
         |
         |name         := "${appProjectName}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |libraryDependencies ++= {
         |  Seq(
         |    logback,
         |    leveldbjni,
         |    scalaTest      % Test
         |  )
         |}
         |
         |assemblyJarName in assembly := s"$${name.value}-assembly-$${version.value}.jar"
         |mainClass in assembly := Some("play.core.server.ProdServerStart")
         |fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)
         |
         |assemblyExcludedJars in assembly := {
         |  val cp = (fullClasspath in assembly).value
         |  cp.filter( x =>
         |    x.data.getName.contains("javax.activation-api")
         |      || x.data.getName.contains("lagom-logback")
         |  )
         |}
         |
         |assemblyMergeStrategy in assembly := {
         |  case manifest if manifest.contains("MANIFEST.MF") =>
         |    // We don't need manifest files since sbt-assembly will create
         |    // one with the given settings
         |    MergeStrategy.discard
         |  case PathList("META-INF", "io.netty.versions.properties") =>
         |    MergeStrategy.discard
         |  case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
         |    // Keep the content for all reference-overrides.conf files
         |    MergeStrategy.concat
         |  case x =>
         |    // For all the other files, use the default sbt-assembly merge strategy
         |    val oldStrategy = (assemblyMergeStrategy in assembly).value
         |    oldStrategy(x)
         |}
       """.stripMargin.trim
    )
    printWriter.close()
  }

  def crudAppProjectSettings(): Unit = {
    new File(crudAppProjectDir).mkdirs()
    val printWriter = new PrintWriter(s"${crudAppProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |import sbtassembly.MergeStrategy
         |
         |name         := "${crudAppProjectName}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |libraryDependencies ++= {
         |  Seq(
         |    logback,
         |    leveldbjni,
         |    scalaTest      % Test
         |  )
         |}
         |
         |assemblyJarName in assembly := s"$${name.value}-assembly-$${version.value}.jar"
         |mainClass in assembly := Some("play.core.server.ProdServerStart")
         |fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)
         |
         |assemblyExcludedJars in assembly := {
         |  val cp = (fullClasspath in assembly).value
         |  cp.filter( x =>
         |    x.data.getName.contains("javax.activation-api")
         |      || x.data.getName.contains("lagom-logback")
         |  )
         |}
         |
         |assemblyMergeStrategy in assembly := {
         |  case manifest if manifest.contains("MANIFEST.MF") =>
         |    // We don't need manifest files since sbt-assembly will create
         |    // one with the given settings
         |    MergeStrategy.discard
         |  case PathList("META-INF", "io.netty.versions.properties") =>
         |    MergeStrategy.discard
         |  case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
         |    // Keep the content for all reference-overrides.conf files
         |    MergeStrategy.concat
         |  case x =>
         |    // For all the other files, use the default sbt-assembly merge strategy
         |    val oldStrategy = (assemblyMergeStrategy in assembly).value
         |    oldStrategy(x)
         |}
       """.stripMargin.trim
    )
    printWriter.close()
  }

  def rootProjectSettings(): Unit = {
    // build.sbt
    rootProjectBuildSbt()
    rootProjectBuildProperties()
    rootProjectPluginSbt()
    rootProjectDependencies()
  }

  def makeRootProjectDir(): Boolean = new File(s"${rootProjectDir}/project/").mkdirs()

  def rootProjectDependencies(): Unit = {
    makeRootProjectDir()
    val printWriter = new PrintWriter(s"${rootProjectDir}/project/Dependencies.scala", "utf-8")
    printWriter.println(
      s"""
         |import sbt._
         |import scalapb.compiler.Version.scalapbVersion
         |
         |object Dependencies {
         |  lazy val scalaVersionNumber    = "2.12.8"
         |  lazy val akkaVersion           = "2.5.22"
         |  lazy val artifactGroupName     = "${modelPackage}"
         |  lazy val artifactVersionNumber = "${modelVersion}"
         |  lazy val artifactMaintainer    = "xtwxy@hotmail"
         |  lazy val sprayVersion          = "1.3.5"
         |  lazy val playVersion           = "2.7.2"
         |  lazy val lagomVersion          = "1.5.0"
         |
         |  lazy val scalaXml        = "org.scala-lang.modules"    %%  "scala-xml"                           % "1.0.6"
         |  lazy val akkaActor       = "com.typesafe.akka"         %%  "akka-actor"                          % akkaVersion
         |  lazy val akkaRemote      = "com.typesafe.akka"         %%  "akka-remote"                         % akkaVersion
         |  lazy val akkaStream      = "com.typesafe.akka"         %%  "akka-stream"                         % akkaVersion
         |  lazy val akkaPersistence = "com.typesafe.akka"         %%  "akka-persistence"                    % akkaVersion
         |  lazy val leveldbjni      = "org.fusesource.leveldbjni" %   "leveldbjni-all"                      % "1.8"
         |  lazy val akkaPersistenceQuery = "com.typesafe.akka"    %%  "akka-persistence-query"              % akkaVersion
         |  lazy val akkaPersistenceCassandra = "com.typesafe.akka"%%  "akka-persistence-cassandra"          % "0.93"
         |  lazy val akkaCluster     = "com.typesafe.akka"         %%  "akka-cluster"                        % akkaVersion
         |  lazy val akkaClusterTools= "com.typesafe.akka"         %%  "akka-cluster-tools"                  % akkaVersion
         |  lazy val akkaClusterMetrics = "com.typesafe.akka"      %%  "akka-cluster-metrics"                % akkaVersion
         |  lazy val akkaClusterSharding = "com.typesafe.akka"     %%  "akka-cluster-sharding"               % akkaVersion
         |  lazy val akkaSlf4j       = "com.typesafe.akka"         %%  "akka-slf4j"                          % akkaVersion
         |  lazy val akkaTestkit     = "com.typesafe.akka"         %%  "akka-testkit"                        % akkaVersion
         |  lazy val play            = "com.typesafe.play"         %%  "play"                                % playVersion
         |  lazy val playTest        = "com.typesafe.play"         %%  "play-test"                           % playVersion
         |  lazy val jodaTime        = "joda-time"                 %   "joda-time"                           % "2.10.1"
         |  lazy val scalapbRuntime  = "com.thesamet.scalapb"      %%  "scalapb-runtime"                     % scalapbVersion
         |  lazy val scalapbJson4s   = "com.thesamet.scalapb"      %%  "scalapb-json4s"                      % "0.9.0-M1"
         |  lazy val playAnorm       = "org.playframework.anorm"   %%  "anorm"                               % "2.6.2"
         |  lazy val mysqlDriver     = "mysql"                     %   "mysql-connector-java"                % "8.0.16"
         |  lazy val playJson        = "com.typesafe.play"         %%  "play-json"                           % playVersion
         |  lazy val lagomApi        = "com.lightbend.lagom"       %%  "lagom-scaladsl-api"                  % lagomVersion
         |  lazy val macwire         = "com.softwaremill.macwire"  %%  "macros"                              % "2.3.0"
         |
         |  lazy val sbRuntime       = "com.github.apuex.springbootsolution" %% "scala-runtime"              % "1.0.9"
         |  lazy val playEvents      = "com.github.apuex"          %%  "play-events"                         % "1.0.2"
         |  lazy val codegenUtil     = "com.github.apuex"          %%  "lagom-codegen-util"                  % "1.0.0"
         |  lazy val serializer      = "com.github.apuex.protobuf" %   "protobuf-serializer"                 % "1.0.1"
         |  lazy val playSocketIO    = "com.lightbend.play"        %%  "play-socket-io"                      % "1.0.0-beta-2"
         |  lazy val macwireMicros   = "com.softwaremill.macwire"  %%  "macros"                              % "2.3.0"
         |  lazy val guava           = "com.google.guava"          %   "guava"                               % "22.0"
         |  lazy val slf4jApi        = "org.slf4j"                 %   "slf4j-api"                           % "1.7.25"
         |  lazy val slf4jSimple     = "org.slf4j"                 %   "slf4j-simple"                        % "1.7.25"
         |  lazy val logbackClassic  = "ch.qos.logback"            %   "logback-classic"                     % "1.2.3"
         |  lazy val scalaTest       = "org.scalatest"             %%  "scalatest"                           % "3.0.4"
         |  lazy val scalaTesplusPlay= "org.scalatestplus.play"    %%  "scalatestplus-play"                  % "3.1.2"
         |  lazy val scalacheck      = "org.scalacheck"            %%  "scalacheck"                          % "1.13.4"
         |  lazy val scalaTestPlusPlay = "org.scalatestplus.play"  %%  "scalatestplus-play"                  % "3.1.2"
         |
         |  lazy val dependedRepos = Seq(
         |      "Atlassian Releases" at "https://maven.atlassian.com/public/",
         |      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
         |      Resolver.sonatypeRepo("snapshots")
         |  )
         |
         |  lazy val confPath = "../conf"
         |  lazy val localRepo = Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
         |}
       """.stripMargin
        .trim
    )
    printWriter.close()
  }

  def rootProjectPluginSbt(): Unit = {
    makeRootProjectDir()
    val printWriter = new PrintWriter(s"${rootProjectDir}/project/plugin.sbt", "utf-8")
    printWriter.println(
      s"""
         |addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.5.0")
         |addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.2")
         |addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
         |addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.20")
         |
         |addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.20")
         |libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.9.0-M5"
       """.stripMargin
        .trim
    )
    printWriter.close()
  }

  def rootProjectBuildProperties(): Unit = {
    makeRootProjectDir()
    val printWriter = new PrintWriter(s"${rootProjectDir}/project/build.properties", "utf-8")
    printWriter.println("sbt.version=1.2.8")
    printWriter.close()
  }

  def rootProjectBuildSbt(): Unit = {
    makeRootProjectDir()
    val printWriter = new PrintWriter(s"${rootProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |
         |name         := "${cToShell(modelName)}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |lazy val root = (project in file("."))
         |  .aggregate(
         |    `${model}`,
         |    `${message}`,
         |    `${api}`,
         |    `${impl}`,
         |    `${app}`,
         |    `${dao}`,
         |    `${dao}-${mysql}`,
         |    `${crud}-${impl}`,
         |    `${crud}-${app}`
         |  )
         |
         |lazy val `${model}` = (project in file("${model}"))
         |lazy val `${message}` = (project in file("${message}"))
         |  .dependsOn(`${model}`)
         |lazy val `${api}` = (project in file("${api}"))
         |  .dependsOn(`${message}`)
         |  .enablePlugins(LagomScala)
         |lazy val `${dao}` = (project in file("${dao}"))
         |  .dependsOn(`message`)
         |lazy val `${dao}-${mysql}` = (project in file("${dao}-${mysql}"))
         |  .dependsOn(`${dao}`)
         |lazy val `${impl}` = (project in file("${impl}"))
         |  .dependsOn(`${api}`)
         |lazy val `${crud}-${impl}` = (project in file("${crud}-${impl}"))
         |  .dependsOn(`${api}`)
         |  .dependsOn(`${dao}-${mysql}`)
         |lazy val `${app}` = (project in file("${app}"))
         |  .dependsOn(`${impl}`)
         |  .enablePlugins(PlayScala)
         |lazy val `${crud}-${app}` = (project in file("${crud}-${app}"))
         |  .dependsOn(`${crud}-${impl}`)
         |  .enablePlugins(PlayScala)
         |
         |resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
         |publishTo := localRepo
       """.stripMargin
        .trim
    )
    printWriter.close()
  }
}
