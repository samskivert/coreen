import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val proguard = "org.scala-tools.sbt" % "sbt-proguard-plugin" % "0.0.5"

  val siasiaRepo = "Pyrostream Repository" at "http://siasia.insomnia247.nl/repo-snapshots"
  val githubUploader = "net.ps" %% "github-uploader" % "1.0"
}
