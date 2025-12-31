import sbt.Keys.*
import sbt.{url, *}
import xerial.sbt.Sonatype.sonatypeCentralHost

val Version = "0.0.29"

resolvers += Resolver.mavenLocal

ThisBuild / sonatypeCredentialHost := sonatypeCentralHost

ThisBuild / publishTo := sonatypePublishToBundle.value

ThisBuild / organization := "com.fastscala"
ThisBuild / version := Version
ThisBuild / scalaVersion := "3.7.4"

ThisBuild / shellPrompt := { state => Project.extract(state).currentRef.project + "> " }

lazy val commonSettings = Seq(
  organization := "com.fastscala",

  sonatypeCentralDeploymentName := s"${organization.value}.${name.value}-${version.value}",

  sbtPluginPublishLegacyMavenStyle := false,

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

  publishTo := sonatypePublishToBundle.value,

  // crossScalaVersions := List("2.13.16"),
)

scalacOptions += "-Ypartial-unification"

val FSRoot = "./"

lazy val fs_core = (project in file(FSRoot + "fs-core"))
  .settings(
    commonSettings,
    name := "fs-core",
    version := Version,
    organization := "com.fastscala",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      // "net.logstash.logback" % "logstash-logback-encoder" % "8.0",
      "org.slf4j" % "slf4j-api" % "2.0.16",
      "com.github.loki4j" % "loki-logback-appender" % "2.0.0",
      "io.prometheus" % "prometheus-metrics-core" % "1.3.10",
      "io.prometheus" % "prometheus-metrics-instrumentation-jvm" % "1.3.10",
      "io.prometheus" % "prometheus-metrics-exporter-httpserver" % "1.3.10",
      "com.typesafe" % "config" % "1.4.3",
      "org.apache.commons" % "commons-text" % "1.12.0",
      "org.eclipse.jetty" % "jetty-server" % "12.0.12",
      "org.eclipse.jetty.websocket" % "jetty-websocket-jetty-server" % "12.0.12",
      "it.unimi.dsi" % "dsiutils" % "2.7.3",
      "commons-io" % "commons-io" % "2.21.0",
    )
  )

lazy val fs_circe = (project in file(FSRoot + "fs-circe"))
  .settings(
    commonSettings,
    name := "fs-circe",
    version := Version,
    organization := "com.fastscala",
    libraryDependencies ++= Seq("io.circe" %% "circe-core" % "0.14.10", "io.circe" %% "circe-generic" % "0.14.10", "io.circe" %% "circe-parser" % "0.14.10")
  )
  .dependsOn(fs_core)

lazy val fs_scala_xml = (project in file(FSRoot + "fs-scala-xml"))
  .settings(commonSettings, name := "fs-scala-xml", version := "0.0.1", organization := "com.fastscala", scalacOptions ++= Seq("-old-syntax", "-rewrite"), scalacOptions ++= Seq("-rewrite", "-source", "3.7-migration"), libraryDependencies ++= Seq("org.scala-lang.modules" %% "scala-xml" % "2.3.0"))
  .dependsOn(fs_core)

lazy val fs_db = (project in file(FSRoot + "fs-db"))
  .settings(
    commonSettings,
    name := "fs-db",
    version := Version,
    organization := "com.fastscala",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % "0.14.15",
      "io.circe" %% "circe-generic" % "0.14.15",
      "io.circe" %% "circe-parser" % "0.14.15",
      "org.postgresql" % "postgresql" % "42.7.8",
      "org.xerial" % "sqlite-jdbc" % "3.51.0.0",
      "org.scalikejdbc" %% "scalikejdbc" % "4.3.5",
      "com.google.guava" % "guava" % "33.5.0-jre",
      "commons-codec" % "commons-codec" % "1.20.0",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    Test / parallelExecution := false
  )
  .dependsOn(fs_core)

lazy val fs_components = (project in file(FSRoot + "fs-components"))
  .settings(
    commonSettings,
    name := "fs-components",
    version := Version,
    organization := "com.fastscala",
    scalacOptions ++= Seq("-Xmax-inlines", "50"),
    libraryDependencies ++= Seq("joda-time" % "joda-time" % "2.12.7", "com.lihaoyi" %% "upickle" % "4.1.0", "com.softwaremill.quicklens" %% "quicklens" % "1.9.11"),
    javaOptions += "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005",
    Compile / run / fork := true,
    Compile / run / connectInput := true
  )
  .dependsOn(fs_core)
  .dependsOn(fs_circe)
  .dependsOn(fs_scala_xml)
