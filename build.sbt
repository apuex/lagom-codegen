import Dependencies._

name         := "lagom-codegen-root"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

lazy val root = (project in file("."))
  .aggregate(
    codegen,
    util,
    runtime
  )

lazy val codegen = (project in file("codegen"))
  .dependsOn(util)
  .enablePlugins(GraalVMNativeImagePlugin)

lazy val util = (project in file("util"))
lazy val runtime = (project in file("runtime"))

publishTo := sonatypePublishTo.value

