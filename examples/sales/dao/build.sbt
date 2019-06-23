import Dependencies._

name         := "sales-dao"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    jdbc,
    scalaTest      % Test
  )
}
