import Dependencies._

name         := "sales"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

lazy val root = (project in file("."))
  .aggregate(
    `model`,
    `message`,
    `api`,
    `dao`,
    `dao-mysql`,
    `impl`,
    `app`
  )

lazy val `model` = (project in file("model"))
lazy val `message` = (project in file("message"))
  .dependsOn(`model`)
lazy val `api` = (project in file("api"))
  .dependsOn(`message`)
  .enablePlugins(LagomScala)
lazy val `dao` = (project in file("dao"))
  .dependsOn(`message`)
lazy val `dao-mysql` = (project in file("dao-mysql"))
  .dependsOn(`dao`)
lazy val `impl` = (project in file("impl"))
  .dependsOn(`api`)
lazy val `crud-impl` = (project in file("crud-impl"))
  .dependsOn(`api`)
  .dependsOn(`dao-mysql`)
lazy val `app` = (project in file("app"))
  .dependsOn(`impl`)
  .enablePlugins(PlayScala)

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
publishTo := localRepo
