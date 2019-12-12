/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
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
    `domain`,
    `cluster`,
    `impl`,
    `app`,
    `dao`,
    `dao-mysql`,
    `crud-impl`,
    `crud-app`
  )

lazy val `model` = (project in file("model"))
lazy val `message` = (project in file("message"))
  .dependsOn(`model`)
  .enablePlugins(LagomScala)
lazy val `api` = (project in file("api"))
  .dependsOn(`message`)
lazy val `domain` = (project in file("domain"))
  .dependsOn(`message`)
lazy val `cluster` = (project in file("cluster"))
  .dependsOn(`domain`)
lazy val `dao` = (project in file("dao"))
  .dependsOn(`message`)
lazy val `dao-mysql` = (project in file("dao-mysql"))
  .dependsOn(`dao`)
lazy val `impl` = (project in file("impl"))
  .dependsOn(`api`)
  .dependsOn(`cluster`)
  .dependsOn(`dao-mysql`)
lazy val `crud-impl` = (project in file("crud-impl"))
  .dependsOn(`api`)
  .dependsOn(`dao-mysql`)
lazy val `app` = (project in file("app"))
  .dependsOn(`impl`)
  .enablePlugins(PlayScala)
lazy val `crud-app` = (project in file("crud-app"))
  .dependsOn(`crud-impl`)
  .enablePlugins(PlayScala)

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
publishTo := localRepo
