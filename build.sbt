import sbt.*
import sbt.Keys.*

import scala.concurrent.duration.*

resolvers += Resolver.mavenLocal

ThisBuild / organization := "com.fastscala"
ThisBuild / scalaVersion := "2.13.14"

ThisBuild / shellPrompt := { state => Project.extract(state).currentRef.project + "> " }

val AkkaVersion = "2.7.0"
val AkkaHttpVersion = "10.2.10"

executableScriptName := "router-prod"

scalacOptions += "-Ypartial-unification"

val LiftVersion = "3.5.0"

val FSRoot = "./"

lazy val fastscala = (project in file(FSRoot + "fastscala"))
  .settings(
    name := "fastscala",

    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      // "net.logstash.logback" % "logstash-logback-encoder" % "8.0",
      "org.slf4j" % "slf4j-api" % "2.0.16",
      "com.github.loki4j" % "loki-logback-appender" % "1.5.2",
      "io.prometheus" % "simpleclient_servlet_jakarta" % "0.16.0",
      "com.typesafe" % "config" % "1.4.3",

      "org.apache.commons" % "commons-text" % "1.12.0",
      "commons-io" % "commons-io" % "2.16.1",

      "org.eclipse.jetty" % "jetty-servlet" % "11.0.22",
      "org.eclipse.jetty.toolchain" % "jetty-jakarta-websocket-api" % "2.0.0",
      "org.eclipse.jetty.websocket" % "websocket-jakarta-server" % "11.0.22",

      "io.circe" %% "circe-core" % "0.14.9",
      "io.circe" %% "circe-generic" % "0.14.9",
      "io.circe" %% "circe-parser" % "0.14.9",
    ),
  )

lazy val fs_scala_xml = (project in file(FSRoot + "fs_scala_xml_support"))
  .settings(
    name := "fs_scala_xml",

    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
    ),
  )
  .dependsOn(fastscala)

lazy val fs_db = (project in file(FSRoot + "fs_db"))
  .settings(
    name := "fs_db",
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "42.6.0",
      "org.xerial" % "sqlite-jdbc" % "3.42.0.0",
      "org.scalikejdbc" %% "scalikejdbc" % "4.0.0",
      "com.google.guava" % "guava" % "32.1.1-jre",
      "org.scalatest" %% "scalatest" % "3.2.16" % Test,
    ),
    Test / parallelExecution := false
  )
  .dependsOn(fastscala)
  .dependsOn(fs_scala_xml)

lazy val fs_templates = (project in file(FSRoot + "fs_templates"))
  .settings(
    name := "fs_templates",

    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.12.7"
    ),
  )
  .dependsOn(fastscala)
  .dependsOn(fs_db)

lazy val fs_templates_bootstrap = (project in file(FSRoot + "fs_templates_bootstrap"))
  .settings(name := "fs_templates_bootstrap")
  .dependsOn(fs_templates)
  .dependsOn(fastscala)
  .dependsOn(fs_db)

lazy val fs_chartjs = (project in file(FSRoot + "fs_chartjs"))
  .settings(name := "fs_chartjs")
  .dependsOn(fastscala)
  .dependsOn(fs_scala_xml)

lazy val fs_demo = (project in file(FSRoot + "fs_demo"))
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin)
  .settings(
    name := "fs_demo",

    Compile / packageBin / mainClass := Some("com.fastscala.demo.server.JettyServer"),
    Compile / mainClass := Some("com.fastscala.demo.server.JettyServer"),

    Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "scala",

    publishArtifact := true,
    bashScriptEnvConfigLocation := Some("/etc/default/" + (Linux / packageName).value),
    rpmRelease := "1.0.0",
    rpmVendor := "kezlisolutions",
    rpmLicense := Some("none"),

    Linux / daemonUser := "fs_demo",
    Linux / daemonGroup := "fs_demo",

    libraryDependencies ++= Seq(
      "org.eclipse.jetty" % "jetty-server" % "11.0.22",
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "at.favre.lib" % "bcrypt" % "0.10.2",
      "com.lihaoyi" %% "scalatags" % "0.13.1",
    ),

    Compile / run / fork := true,
    Compile / run / connectInput := true,
    javaOptions += "-Xmx2G",
    javaOptions += "-Xms400M",
  )
  .dependsOn(fs_templates_bootstrap)
  .dependsOn(fs_chartjs)
