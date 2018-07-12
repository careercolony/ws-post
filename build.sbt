name := "ws-post-kafka-stream"

version := "0.1"

scalaVersion := "2.11.8"

val akkaExp = "1.0-RC4" // "2.0.5"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.14",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.14",
  "com.typesafe.akka" %% "akka-stream-experimental" % akkaExp,
  "com.typesafe.akka" %% "akka-http-core-experimental" % akkaExp,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaExp,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaExp,
  "org.java-websocket" % "Java-WebSocket" % "1.3.8"
)

// kafka
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.11-M2"
    exclude("com.typesafe.akka", "akka-stream")
    exclude("com.typesafe.akka", "akka-stream-experimental")
)

// neo4j
libraryDependencies += "org.neo4j.driver" % "neo4j-java-driver" % "1.0.4"

// mongo
libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.12.7"

// spary json
libraryDependencies += "io.spray" %% "spray-json" % "1.3.2"

