/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
import Dependencies._

name         := "sales-domain"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    akkaPersistenceCassandra,
    akkaPersistence,
    akkaPersistenceQuery,
    macwireMacros          % Provided,
    macrosakka             % Provided,
    macwireUtil,
    macwireProxy,
    scalaTest              % Test
  )
}
