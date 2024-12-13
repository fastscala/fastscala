import sbt.{url, *}
import sbt.Keys.*

import xerial.sbt.Sonatype.sonatypeCentralHost
import scala.concurrent.duration.*

resolvers += Resolver.mavenLocal

ThisBuild / sonatypeCredentialHost := sonatypeCentralHost

ThisBuild / organization := "com.fastscala"
ThisBuild / version := "0.0.5"
ThisBuild / scalaVersion := "3.6.2"

ThisBuild / shellPrompt := { state => Project.extract(state).currentRef.project + "> " }

lazy val commonSettings = Seq(
  organization := "com.fastscala",

  sonatypeCentralDeploymentName := s"${organization.value}.${name.value}-${version.value}",

  scalacOptions ++= Seq("-old-syntax", "-rewrite"),

  sonatypeCredentialHost := Sonatype.sonatypeCentralHost,

  sonatypeProfileName := "com.fastscala",
  publishMavenStyle := true,
  licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("https://www.fastscala.com/")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/fastscala/fastscala"),
      "scm:git@github.com:fastscala/fastscala.git"
    )
  ),
  developers := List(
    Developer(id = "david", name = "David Miguel Antunes", email = "davidmiguel@antunes.net", url = url("https://www.linkedin.com/in/david-miguel-antunes/")),
  ),

  // isSnapshot := false,

  publishTo := sonatypePublishToBundle.value,
)

scalacOptions += "-Ypartial-unification"

val FSRoot = "./"

lazy val root = (project in file("."))
  .aggregate(fs_core)
  .aggregate(fs_circe)
  .aggregate(fs_scala_xml)
  .aggregate(fs_db)
  .aggregate(fs_components)

lazy val fs_core = (project in file(FSRoot + "fs-core"))
  .settings(
    commonSettings,
    name := "fs-core",

    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      "org.slf4j" % "slf4j-api" % "2.0.16",
      "com.github.loki4j" % "loki-logback-appender" % "1.5.2",
      "io.prometheus" % "prometheus-metrics-core" % "1.3.1",
      "io.prometheus" % "prometheus-metrics-instrumentation-jvm" % "1.3.1",
      "io.prometheus" % "prometheus-metrics-exporter-httpserver" % "1.3.1",
      "com.typesafe" % "config" % "1.4.3",

      "org.apache.commons" % "commons-text" % "1.12.0",

      "org.eclipse.jetty" % "jetty-server" % "12.0.12",
      "org.eclipse.jetty.websocket" % "jetty-websocket-jetty-server" % "12.0.12",
      "it.unimi.dsi" % "dsiutils" % "2.7.3",
    ),
  )

lazy val fs_circe = (project in file(FSRoot + "fs-circe"))
  .settings(
    commonSettings,
    name := "fs-circe",
    organization := "com.fastscala",

    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % "0.14.10",
      "io.circe" %% "circe-generic" % "0.14.10",
      "io.circe" %% "circe-parser" % "0.14.10",
    ),
  )
  .dependsOn(fs_core)

lazy val fs_scala_xml = (project in file(FSRoot + "fs-scala-xml"))
  .settings(
    commonSettings,
    name := "fs-scala-xml",
    organization := "com.fastscala",

    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
    ),
  )
  .dependsOn(fs_core)

lazy val fs_db = (project in file(FSRoot + "fs-db"))
  .settings(
    commonSettings,
    name := "fs-db",
    organization := "com.fastscala",

    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "42.7.4",
      "org.xerial" % "sqlite-jdbc" % "3.47.0.0",
      "org.scalikejdbc" %% "scalikejdbc" % "4.3.2",
      "com.google.guava" % "guava" % "33.3.1-jre",
      "commons-codec" % "commons-codec" % "1.17.1",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    ),
    Test / parallelExecution := false
  )
  .dependsOn(fs_core)
  .dependsOn(fs_scala_xml)
  .dependsOn(fs_circe)

lazy val fs_components = (project in file(FSRoot + "fs-components"))
  .settings(
    commonSettings,
    name := "fs-components",
    organization := "com.fastscala",

    scalacOptions ++= Seq("-Xmax-inlines", "50"),

    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.12.7"
    ),
  )
  .dependsOn(fs_core)
  .dependsOn(fs_circe)
  .dependsOn(fs_scala_xml)
