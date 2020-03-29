val finchVersion = "0.26.0"
val circeVersion = "0.10.1"
val scalatestVersion = "3.0.5"
val mongoDbDriverVersion = "2.4.2"

lazy val root = (project in file("."))
  .settings(
    organization := "com.neurone",
    name := "neurone-conector",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.7",
    mainClass in (Compile, run) := Some("com.neurone.neuroneconector.Main"),
    libraryDependencies ++= Seq(
      "com.github.finagle" %% "finchx-core" % finchVersion,
      "com.github.finagle" %% "finchx-circe" % finchVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion % "test",
      "org.mongodb.scala" %% "mongo-scala-driver" % mongoDbDriverVersion,
      "com.typesafe.akka" %% "akka-actor" % "2.5.22",
      "org.scalaz" %% "scalaz-core" % "7.2.27",
       "com.typesafe" % "config" % "1.4.0",
    )
    
    )
    enablePlugins(JavaAppPackaging)
