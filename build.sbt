name := "kafka-it"
version := "0.1"
scalaVersion := "2.11.8"

val curator = "2.7.1"
libraryDependencies += "org.apache.curator" % "curator-test" % curator
libraryDependencies += "org.apache.curator" % "curator-framework" % curator
libraryDependencies += "org.apache.kafka" %% "kafka" % "1.1.0"

