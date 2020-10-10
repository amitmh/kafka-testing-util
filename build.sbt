name := "kafka-it"
version := "0.1"
scalaVersion := "2.11.8"

val curatorVersion = "2.7.1"
libraryDependencies += "org.apache.curator" % "curator-test" % curatorVersion
libraryDependencies += "org.apache.curator" % "curator-framework" % curatorVersion
libraryDependencies += "org.apache.kafka" %% "kafka" % "1.1.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % Test
libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.30" % Test

