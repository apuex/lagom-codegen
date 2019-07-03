/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
import Dependencies._

name         := "sales-crud-impl"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    akkaPersistenceCassandra,
    macwireMacros          % Provided,
    macrosakka             % Provided,
    macwireUtil,
    macwireProxy,
    scalaTest              % Test
  )
}
