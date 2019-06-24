import Dependencies._

name         := "sales-impl"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    playEvents,
    akkaPersistence,
    akkaPersistenceQuery,
    akkaClusterSharding,
    macwireMacros        % Provided,
    scalaTest      % Test
  )
}
