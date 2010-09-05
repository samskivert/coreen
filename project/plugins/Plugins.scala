import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  // we need GWT user and dev for compiling the GWT bits and for devmode
  val gwtUser = "com.google.gwt" % "gwt-user" % "2.0.4"
  val gwtDev = "com.google.gwt" % "gwt-dev" % "2.0.4"

  // add a custom Ivy repository for gwt-asyncgen
  val ivyPattern = "[organization]/[module]/[revision]/[type]s/[artifact].[ext]"
  val gwtAsyncGenRepo = Resolver.url("gwt-asyncgen").artifacts(
    "http://gwt-asyncgen.googlecode.com/svn/releases/" + ivyPattern)
  val gwtAsyncGen = "com.samskivert" % "gwt-asyncgen" % "1.0"
}
