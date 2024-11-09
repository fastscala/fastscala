import sbt.*
import sbt.Keys.*

resolvers += Resolver.mavenLocal

ThisBuild / organization := "com.fastscala"
ThisBuild / scalaVersion := "2.13.15"

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

lazy val fs_circe = (project in file(FSRoot + "fs_circe"))
  .settings(
    name := "fs_circe",

    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % "0.14.10",
      "io.circe" %% "circe-generic" % "0.14.10",
      "io.circe" %% "circe-parser" % "0.14.10",
    ),
  )
  .dependsOn(fastscala)

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
      "org.postgresql" % "postgresql" % "42.7.3",
      "org.xerial" % "sqlite-jdbc" % "3.46.0.1",
      "org.scalikejdbc" %% "scalikejdbc" % "4.3.1",
      "com.google.guava" % "guava" % "33.2.1-jre",
      "commons-codec" % "commons-codec" % "1.17.1",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    ),
    Test / parallelExecution := false
  )
  .dependsOn(fastscala)

lazy val fs_templates = (project in file(FSRoot + "fs_templates"))
  .settings(
    name := "fs_templates",

    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.12.7"
    ),
  )
  .dependsOn(fastscala)
  .dependsOn(fs_scala_xml)

lazy val fs_templates_bootstrap = (project in file(FSRoot + "fs_templates_bootstrap"))
  .settings(name := "fs_templates_bootstrap")
  .dependsOn(fs_templates)
  .dependsOn(fs_circe)

lazy val fs_chartjs = (project in file(FSRoot + "fs_chartjs"))
  .settings(name := "fs_chartjs")
  .dependsOn(fastscala)
  .dependsOn(fs_scala_xml)
  .dependsOn(fs_circe)

lazy val fs_demo = (project in file(FSRoot + "fs_demo"))
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin)
  .settings(
    name := "fs_demo",

    Compile / packageBin / mainClass := Some("com.fastscala.demo.server.JettyServer"),
    Compile / mainClass := Some("com.fastscala.demo.server.JettyServer"),

    Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "scala",

    publishArtifact := true,

    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.5",
      "at.favre.lib" % "bcrypt" % "0.10.2",
      "com.lihaoyi" %% "scalatags" % "0.13.1",
    ),

    bashScriptEnvConfigLocation := Some("/etc/default/" + (Linux / packageName).value),
    rpmRelease := "1.0.0",
    rpmVendor := "kezlisolutions",
    rpmLicense := Some("none"),

    Linux / daemonUser := "fs_demo",
    Linux / daemonGroup := "fs_demo",

    javaOptions += "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005",
    Compile / run / fork := true,
    Compile / run / connectInput := true,
    javaOptions += "-Xmx2G",
    javaOptions += "-Xms400M",
  )
  .dependsOn(fs_templates_bootstrap)
  .dependsOn(fs_chartjs)

lazy val fs_taskmanager = (project in file(FSRoot + "fs_taskmanager"))
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin)
  .settings(
    name := "fs_taskmanager",

    Compile / packageBin / mainClass := Some("com.fastscala.taskmanager.server.JettyServer"),
    Compile / mainClass := Some("com.fastscala.taskmanager.server.JettyServer"),

    Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "scala",

    publishArtifact := true,

    libraryDependencies ++= Seq(
      ("org.typelevel" % "cats-effect" % "3.5.5") cross CrossVersion.for3Use2_13,
      "at.favre.lib" % "bcrypt" % "0.10.2",
      "com.lihaoyi" %% "scalatags" % "0.13.1",
    ),

    bashScriptEnvConfigLocation := Some("/etc/default/" + (Linux / packageName).value),
    rpmRelease := "1.0.0",
    rpmVendor := "kezlisolutions",
    rpmLicense := Some("none"),

    Linux / daemonUser := "fs_taskmanager",
    Linux / daemonGroup := "fs_taskmanager",

    javaOptions += "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005",
    Compile / run / fork := true,
    Compile / run / connectInput := true,
    javaOptions += "-Xmx2G",
    javaOptions += "-Xms400M",
  )
  .dependsOn(fs_demo)
