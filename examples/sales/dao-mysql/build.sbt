import Dependencies._

name         := "sales-dao-mysql"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    jdbc,
    playAnorm,
    mysqlDriver,
    macwireMacros        % Provided,
    macrosakka           % Provided,
    macwireUtil,
    macwireProxy,
    scalaTest      % Test
  )
}
