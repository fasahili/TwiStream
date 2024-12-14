ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.14"

lazy val root = (project in file("."))
  .settings(
    name := "TwiStream"
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.3",
  "org.apache.spark" %% "spark-sql" % "3.5.3",
  "org.apache.kafka" % "kafka-clients" % "3.8.1",
  "com.github.luben" % "zstd-jni" % "1.5.6-4",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.1",
)


resolvers ++= Seq(
  "Apache Repository" at "https://repo1.maven.org/maven2/",
  "Confluent" at "https://packages.confluent.io/maven/"
)

libraryDependencySchemes += "com.github.luben" % "zstd-jni" % "always"
mainClass in Compile := Some("package.name.Main")

ThisBuild / evictionErrorLevel := Level.Warn
