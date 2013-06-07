
import AssemblyKeys._

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("javax", "xml", xs @ _*) => MergeStrategy.last
    case PathList("org", "apache", "commons", "collections", xs @ _*) => MergeStrategy.last
    case PathList("org", "w3c", "dom", xs @ _*) => MergeStrategy.last
    case "jasperreports_extension.properties" => MergeStrategy.last    
    case ".project" => MergeStrategy.discard
    case ".classpath" => MergeStrategy.discard
    case "defaultconfig.properties" => MergeStrategy.discard
    case x => old(x)
  }
}

test in assembly := {}

jarName in assembly := "smtp4n-server.jar"

name := "smtp4n-server"

version := "0.1"

scalaVersion := "2.10.0"

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature" )

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "spray repo" at "http://repo.spray.io",
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.1.0",
	"com.typesafe.akka" %% "akka-slf4j" % "2.1.0",
	"io.spray" % "spray-http" % "1.1-M7",
	"io.spray" % "spray-httpx" % "1.1-M7",
	"io.spray" % "spray-util" % "1.1-M7",
	"io.spray" % "spray-io" % "1.1-M7",
	"io.spray" % "spray-can" % "1.1-M7",
	"io.spray" % "spray-routing" % "1.1-M7",
	"io.spray" %% "spray-json" % "1.2.3",
	"junit" % "junit" % "4.10" % "test",
	"org.specs2" %% "specs2" % "1.13" % "test",
	"org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
	"javax.mail" % "mail" % "1.4.7",
	"dnsjava" % "dnsjava" % "2.1.1",
	"com.stackmob" %% "scaliak" % "0.7.0",
	"org.scalaz" %% "scalaz-core" % "7.0.0",
	"com.twitter" % "chill_2.10" % "0.2.3"
)
