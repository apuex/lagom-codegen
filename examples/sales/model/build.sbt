import Dependencies._

name         := "sales-model"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    codegenUtil            % Test,
    scalaTest              % Test
  )
}
