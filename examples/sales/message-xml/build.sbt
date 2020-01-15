/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
import Dependencies._

name         := "sales-message-xml"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

fork := true

libraryDependencies ++= {
  Seq(
    scalaXml,
    scalaTest              % Test
  )
}
