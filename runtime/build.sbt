import Dependencies._

name := "lagom-codegen-runtime"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

libraryDependencies ++= Seq(
  slf4jSimple % Test,
  scalaTest % Test
)

publishTo := sonatypePublishTo.value
