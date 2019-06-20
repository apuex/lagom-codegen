import Dependencies._

name         := "sales"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

lazy val root = (project in file("."))
  .aggregate(
    `message`,
    `api`,
    `dao`,
    `impl`,
    `app`
  )

lazy val `message` = (project in file("message"))
lazy val `api` = (project in file("api"))
  .dependsOn(`message`)
  .enablePlugins(LagomScala)
lazy val `dao` = (project in file("dao"))
  .dependsOn(`message`)
lazy val `impl` = (project in file("impl"))
  .dependsOn(`api`)
  .dependsOn(`dao`)
lazy val `app` = (project in file("app"))
  .dependsOn(`impl`)
  .enablePlugins(PlayScala)

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
publishTo := localRepo
