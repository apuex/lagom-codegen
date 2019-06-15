import Dependencies._
sonatypeProfileName := "com.github.apuex"

publishMavenStyle := true

licenses := Seq("GPL3" -> url("https://www.gnu.org/licenses/gpl-3.0.txt"))

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("apuex", "lagom-codegen", artifactMaintainer))

homepage := Some(url("https://github.com/apuex/lagom-codegen"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/apuex/lagom-codegen.git"),
    "scm:git@github.com:apuex/lagom-codegen.git"
  )
)

developers := List(
  Developer(id="apuex", name="Wangxy", email=artifactMaintainer, url=url("https://github.com/apuex"))
)

/*
Command Line Usage

Publish a GPG-signed artifact to Sonatype:

$ sbt publishSigned

Do close and promote at once:

$ sbt sonatypeRelease

 */
