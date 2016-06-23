name := "kafka-connect-slack"
version := "1.0"
scalaVersion := "2.11.8"

resolvers += "scalac repo" at "https://raw.githubusercontent.com/ScalaConsultants/mvn-repo/master/"

libraryDependencies ++= {
  val kafkaVersion = "0.10.0.0"

  Seq(
    "org.apache.kafka" % "connect-api" % kafkaVersion,
    "org.apache.kafka" % "connect-runtime" % kafkaVersion,
    "io.scalac" %% "slack-scala-bot-core" % "0.2.2"
  )
}