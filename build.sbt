import sbt.*
import sbt.Keys.*

import scala.concurrent.duration.*

resolvers += Resolver.mavenLocal

ThisBuild / organization := "com.fastscala"
ThisBuild / scalaVersion := "2.13.14"

ThisBuild / shellPrompt := { state => Project.extract(state).currentRef.project + "> " }

scalacOptions += "-Ypartial-unification"

val FSRoot = "./"

lazy val root = (project in file(".")).aggregate(fs_demo)

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

      "org.eclipse.jetty.ee10" % "jetty-ee10-servlet" % "12.0.12",
      "org.eclipse.jetty.toolchain" % "jetty-jakarta-websocket-api" % "2.0.0",
      "org.eclipse.jetty.ee10.websocket" % "jetty-ee10-websocket-jakarta-server" % "12.0.12",

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

lazy val fs_scala_xml = (project in file(FSRoot + "fs-scala-xml"))
  .settings(
    name := "fs-scala-xml",

    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
    ),
  )
  .dependsOn(fastscala)

lazy val fs_scala_xml = (project in file(FSRoot + "fs-scala-xml"))
  .settings(
    name := "fs-scala-xml",

    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
    ),
  )
  .dependsOn(fastscala)

lazy val fs_db = (project in file(FSRoot + "fs_db"))
  .settings(
    name := "fs_db",
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "42.7.3",
      "org.xerial" % "sqlite-jdbc" % "3.46.0.1",
      "org.scalikejdbc" %% "scalikejdbc" % "4.3.1",
      "com.google.guava" % "guava" % "33.2.1-jre",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
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
  // .enablePlugins(JavaServerAppPackaging, SystemdPlugin)
  .settings(
    name := "fs_demo",

    Compile / packageBin / mainClass := Some("com.fastscala.demo.server.JettyServer"),
    Compile / mainClass := Some("com.fastscala.demo.server.JettyServer"),

    Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "scala",

    publishArtifact := true,

    libraryDependencies ++= Seq(
      "org.eclipse.jetty" % "jetty-server" % "12.0.12",
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
