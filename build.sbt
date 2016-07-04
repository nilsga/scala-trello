organization := "com.github.nilsga"

name := "scala-trello"

version := "0.6-SNAPSHOT"

val akkaVersion = "2.4.7"

scalaVersion := "2.11.7"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "de.heikoseeberger" %% "akka-http-json4s" % "1.7.0",
  "org.json4s" %% "json4s-jackson" % "3.3.0.RC3",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

credentials += Credentials(
  "Artifactory Realm",
  "oss.jfrog.org",
  sys.env.getOrElse("OSS_JFROG_USER", ""),
  sys.env.getOrElse("OSS_JFROG_PASS", "")
)

publishTo := {
  val jfrog = "https://oss.jfrog.org/artifactory/"
  if (isSnapshot.value)
    Some("OJO Snapshots" at jfrog + "oss-snapshot-local")
  else
    Some("OJO Releases" at jfrog + "oss-release-local")
}

    
