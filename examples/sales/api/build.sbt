import Dependencies._

name         := "sales-api"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    sbRuntime,
    playEvents,
    scalapbJson4s,
    scalaTest      % Test
  )
}
