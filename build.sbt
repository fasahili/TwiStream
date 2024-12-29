ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.14"

lazy val root = (project in file("."))
  .settings(
    name := "TwiStream"
  )
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.4",
  "org.apache.spark" %% "spark-sql" % "3.5.4",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.4",
  "org.apache.kafka" % "kafka-clients" % "3.9.0",
  "com.github.luben" % "zstd-jni" % "1.5.6-8",
  "com.typesafe.play" %% "play-json" % "2.10.6",
  "org.elasticsearch.client" % "elasticsearch-rest-client" % "8.17.0",
  "com.johnsnowlabs.nlp" %% "spark-nlp" % "5.5.0",
  "org.apache.spark" %% "spark-mllib" % "3.5.0"
)
resolvers ++= Seq(
  "Apache Repository" at "https://repo1.maven.org/maven2/",
  "Confluent" at "https://packages.confluent.io/maven/",
  "John Snow Labs Public" at "https://johnsnowlabs.jfrog.io/artifactory/spark-nlp-release"
)
libraryDependencySchemes += "com.github.luben" % "zstd-jni" % "always"
mainClass in Compile := Some("spark.Main")
ThisBuild / evictionErrorLevel := Level.Warn
