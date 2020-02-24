import scala.util.Properties
name := "WebSearch"
version := "0.1"
scalaVersion := "2.10.4"
val sparkVersion = "1.5.0"
val sparkDependencyScope = "provided"

libraryDependencies ++= Seq(
	"org.apache.spark" %% "spark-core" % sparkVersion % sparkDependencyScope,
	"org.apache.spark" %% "spark-streaming" % sparkVersion % sparkDependencyScope,
	"org.apache.spark" %% "spark-streaming-kafka" % "1.5.0",
	"joda-time" % "joda-time" % "2.8.2",
	"net.liftweb" %% "lift-json" % "2.5.1"
)

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".types" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  case "log4j.properties" => MergeStrategy.discard
  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
